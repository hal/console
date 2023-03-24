/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta;

import java.util.List;
import java.util.function.Supplier;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.gwt.resources.client.TextResource;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_LABEL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NILLABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUIRED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.meta.AddressTemplate.ROOT;
import static org.jboss.hal.meta.security.SecurityContext.RWX;

/** Simple data struct for common metadata. Used to keep the method signatures small and tidy. */
@JsType
public class Metadata {

    private static final Logger logger = LoggerFactory.getLogger(Metadata.class);

    @JsIgnore
    public static Metadata empty() {
        return new Metadata(ROOT, () -> RWX, new ResourceDescription(new ModelNode()),
                new Capabilities(null));
    }

    @JsIgnore
    public static Metadata staticDescription(TextResource description) {
        return Metadata.staticDescription(StaticResourceDescription.from(description));
    }

    /**
     * Constructs a Metadata with read-write-execution permissions and a non-working Capabilities object.
     */
    @JsIgnore
    public static Metadata staticDescription(ResourceDescription description) {
        return new Metadata(ROOT, () -> RWX, new ResourceDescription(description), new Capabilities(null));
    }

    /**
     * Constructs a Metadata with read-write-execution permissions and a working Capabilities object based on the
     * environment object.
     */
    @JsIgnore
    public static Metadata staticDescription(ResourceDescription description, Environment environment) {
        return new Metadata(ROOT, () -> RWX, new ResourceDescription(description), new Capabilities(environment));
    }

    private final AddressTemplate template;
    private final Supplier<SecurityContext> securityContext;
    private final ResourceDescription description;
    private final Capabilities capabilities;

    @JsIgnore
    public Metadata(AddressTemplate template, Supplier<SecurityContext> securityContext,
            ResourceDescription description, Capabilities capabilities) {
        this.template = template;
        this.securityContext = securityContext;
        this.description = description;
        this.capabilities = capabilities;
    }

    /** Copies attributes from this description to the specified metadata */
    @JsIgnore
    public void copyAttribute(String attribute, Metadata destination) {
        Property p = getDescription().findAttribute(ATTRIBUTES, attribute);
        if (p != null) {
            destination.getDescription().get(ATTRIBUTES).get(attribute).set(p.getValue());
        }
    }

    /**
     * Makes the specified attribute writable. This is necessary if you copy attributes from a complex attribute to
     * another metadata. Without adjustment the copied attributes are read-only in the destination metadata.
     */
    @JsIgnore
    public void makeWritable(String attribute) {
        getSecurityContext().get(ATTRIBUTES).get(attribute).get(READ).set(true);
        getSecurityContext().get(ATTRIBUTES).get(attribute).get(WRITE).set(true);
    }

    /** Shortcut for {@link #copyAttribute(String, Metadata)} and {@link #makeWritable(String)} */
    @JsIgnore
    public void copyComplexAttributeAttributes(Iterable<String> attributes, Metadata destination) {
        for (String attribute : attributes) {
            copyAttribute(attribute, destination);
            destination.makeWritable(attribute);
        }
    }

    /**
     * Creates a new metadata instance based on this metadata with the attributes taken from the specified complex
     * attribute. The resource description will only include the attributes but no operations!
     */
    @JsIgnore
    public Metadata forComplexAttribute(String name) {
        return forComplexAttribute(name, false);
    }

    /**
     * Creates a new metadata instance based on this metadata with the attributes taken from the specified complex
     * attribute. The resource description will only include the attributes but no operations!
     *
     * @param prefixLabel if {@code true} the labels of the attributes of the complex attribute are prefixed with name
     *                    of the complex attribute.
     */
    @JsIgnore
    public Metadata forComplexAttribute(String name, boolean prefixLabel) {
        if (name.indexOf('.') != -1) {
            Metadata metadata = this;
            List<String> parts = Splitter.on('.').trimResults().omitEmptyStrings().splitToList(name);
            for (String part : parts) {
                metadata = metadata.nested(metadata, part, prefixLabel);
            }
            return metadata;
        } else {
            return nested(this, name, prefixLabel);
        }
    }

    private Metadata nested(Metadata metadata, String name, boolean prefixLabel) {
        ModelNode payload = new ModelNode();
        payload.get(DESCRIPTION).set(failSafeGet(metadata.description, ATTRIBUTES + "/" + name + "/" + DESCRIPTION));
        payload.get(REQUIRED).set(failSafeGet(metadata.description, ATTRIBUTES + "/" + name + "/" + REQUIRED));
        payload.get(NILLABLE).set(failSafeGet(metadata.description, ATTRIBUTES + "/" + name + "/" + NILLABLE));

        Property complexAttribute = metadata.description.findAttribute(ATTRIBUTES, name);
        if (complexAttribute != null && complexAttribute.getValue().hasDefined(VALUE_TYPE)) {
            complexAttribute.getValue().get(VALUE_TYPE).asPropertyList().forEach(nestedProperty -> {
                // The nested name is *always* just the nested property name,
                // since it's used when building the DMR operations
                String nestedName = nestedProperty.getName();
                ModelNode nestedDescription = nestedProperty.getValue();
                // The name which is used for the label can be prefixed with the complex attribute name.
                // If prefixComplexAttribute == true), it is stored as an artificial attribute and picked
                // up by LabelBuilder.label(Property)
                if (prefixLabel) {
                    nestedDescription.get(HAL_LABEL).set(name + "-" + nestedProperty.getName());
                }
                payload.get(ATTRIBUTES).get(nestedName).set(nestedDescription);
            });
        }

        SecurityContext parentContext = metadata.securityContext.get();
        SecurityContext attributeContext = new SecurityContext(new ModelNode()) {
            @Override
            public boolean isReadable() {
                return parentContext.isReadable(name);
            }

            @Override
            public boolean isWritable() {
                return parentContext.isWritable(name);
            }

            @Override
            public boolean isReadable(String attribute) {
                return isReadable(); // if the complex attribute is readable all nested attributes are readable as well
            }

            @Override
            public boolean isWritable(String attribute) {
                return isWritable(); // if the complex attribute is writable all nested attributes are writable as well
            }

            @Override
            public boolean isExecutable(String operation) {
                return parentContext.isExecutable(operation);
            }
        };
        return new Metadata(metadata.template, () -> attributeContext, new ResourceDescription(payload), metadata.capabilities);
    }

    @JsIgnore
    public Metadata forOperation(String name) {
        ModelNode payload = new ModelNode();
        payload.get(DESCRIPTION).set(failSafeGet(description, OPERATIONS + "/" + name + "/" + DESCRIPTION));
        payload.get(ATTRIBUTES).set(failSafeGet(description, OPERATIONS + "/" + name + "/" + REQUEST_PROPERTIES));

        SecurityContext parentContext = this.securityContext.get();
        SecurityContext operationContext = new SecurityContext(new ModelNode()) {
            @Override
            public boolean isReadable() {
                return parentContext.isExecutable(name);
            }

            @Override
            public boolean isWritable() {
                return parentContext.isExecutable(name);
            }

            @Override
            public boolean isReadable(String attribute) {
                return isReadable(); // if the operation is executable all of its request properties are readable as well
            }

            @Override
            public boolean isWritable(String attribute) {
                return isWritable(); // if the operation is executable all of its request properties are writable as well
            }

            @Override
            public boolean isExecutable(String operation) {
                return parentContext.isExecutable(operation);
            }
        };
        return new Metadata(template, () -> operationContext, new ResourceDescription(payload), capabilities);
    }

    /** @return the address template */
    @JsProperty
    public AddressTemplate getTemplate() {
        return template;
    }

    /** @return the security context */
    @JsProperty
    public SecurityContext getSecurityContext() {
        if (securityContext != null && securityContext.get() != null) {
            return securityContext.get();
        } else {
            logger.error("No security context found for {}. Return SecurityContext.READ_ONLY", template);
            return SecurityContext.READ_ONLY;
        }
    }

    /** @return the resource description */
    @JsProperty
    public ResourceDescription getDescription() {
        return description;
    }

    @JsIgnore
    public Capabilities getCapabilities() {
        return capabilities;
    }
}
