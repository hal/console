/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.base.Splitter;
import com.google.gwt.resources.client.TextResource;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.meta.AddressTemplate.ROOT;
import static org.jboss.hal.meta.security.SecurityContext.RWX;

/**
 * Simple data struct for common metadata. Used to keep the method signatures small and tidy.
 *
 * @author Harald Pehl
 */
@JsType
public class Metadata {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(Metadata.class);

    @JsIgnore
    public static Metadata empty() {
        return new Metadata(ROOT, () -> RWX, new ResourceDescription(new ModelNode()),
                new Capabilities(null));
    }

    @JsIgnore
    public static Metadata staticDescription(TextResource description) {
        return Metadata.staticDescription(StaticResourceDescription.from(description));
    }

    @JsIgnore
    public static Metadata staticDescription(ResourceDescription description) {
        return new Metadata(ROOT, () -> RWX, new ResourceDescription(description), new Capabilities(null));
    }

    private final AddressTemplate template;
    private final Supplier<SecurityContext> securityContext;
    private final ResourceDescription description;
    private final Capabilities capabilities;

    @JsIgnore
    public Metadata(final AddressTemplate template, final Supplier<SecurityContext> securityContext,
            final ResourceDescription description, final Capabilities capabilities) {
        this.template = template;
        this.securityContext = securityContext;
        this.description = description;
        this.capabilities = capabilities;
    }

    @JsIgnore
    public Metadata forComplexAttribute(String name, boolean prefixComplexAttribute) {
        ModelNode payload = new ModelNode();
        payload.get(DESCRIPTION).set(failSafeGet(description, ATTRIBUTES + "/" + name + "/" + DESCRIPTION));

        Property complexAttribute = description.findAttribute(ATTRIBUTES, name);
        if (complexAttribute != null && complexAttribute.getValue().hasDefined(VALUE_TYPE)) {
            complexAttribute.getValue().get(VALUE_TYPE).asPropertyList().forEach(nestedProperty -> {
                // The nested name must *always* use the dot notation,
                // since it's used when building the DMR operations
                String nestedName = name + "." + nestedProperty.getName();
                // The name which is used for the label can be prefixed with the complex attribute name
                // (if prefixComplexAttribute == true), is stored as an artificial attribute and
                // picked up by LabelBuilder.label(Property)
                String label = prefixComplexAttribute
                        ? name + "-" + nestedProperty.getName()
                        : nestedProperty.getName();
                ModelNode nestedDescription = nestedProperty.getValue();
                nestedDescription.get(HAL_LABEL).set(label);
                payload.get(ATTRIBUTES).get(nestedName).set(nestedDescription);
            });
        }

        SecurityContext parentContext = this.securityContext.get();
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
            public boolean isReadable(final String attribute) {
                return isReadable(); // if the complex attribute is readable all nested attributes are readable as well
            }

            @Override
            public boolean isWritable(final String attribute) {
                return isWritable(); // if the complex attribute is writable all nested attributes are writable as well
            }

            @Override
            public boolean isExecutable(final String operation) {
                return parentContext.isExecutable(operation);
            }
        };
        return new Metadata(template, () -> attributeContext, new ResourceDescription(payload), capabilities);
    }

    // @formatter:off
    /**
     * Adds all attributes under a complex attribute to the main list of attributes, it also prefix those nested
     * attributes
     * with the complex attribute name. It serves the purpose to shows the attributes in the onAdd modal dialog to add
     * a
     * resource.
     * <p>
     * For example, the resource-description for subsystem=elytron/properties-realm is as below (they are cut for
     * simplicity)
     * <pre>
     *
     * "attributes" => {
     *      "case-sensitive" => {
     *          "type" => BOOLEAN,
     *          "description" => "Case sensitivity of the properties realm. If case insensitive only lower usernames are
     *              allowed.",
     *      },
     *      "groups-attribute" => {
     *          "type" => STRING,
     *          "description" => "The name of the attribute in the returned AuthorizationIdentity that should contain the group
     *              membership information for the identity.",
     *      },
     *      "groups-properties" => {
     *          "type" => OBJECT,
     *          "description" => "The properties file containing the users and their groups.",
     *          "value-type" => {
     *              "path" => {
     *                  "type" => STRING,
     *                  "description" => "The path to the file containing the users and their groups.",
     *                  "required" => true,
     *                  "nillable" => false,
     *              },
     *              "relative-to" => {
     *                  "type" => STRING,
     *                  "description" => "The pre-defined path the path is relative to.",
     *                  "required" => false,
     *                  "nillable" => true,
     *              }
     *          }
     *      }
     * }
     * </pre>
     * <p>
     * After this method is called the resource description contains
     * <pre>
     *     The resource-description for subsystem=elytron/properties-realm
     * "attributes" => {
     *      "case-sensitive" => {
     *          "type" => BOOLEAN,
     *          "description" => "Case sensitivity of the properties realm. If case insensitive only lower usernames are
     *              allowed.",
     *      },
     *      "groups-attribute" => {
     *          "type" => STRING,
     *          "description" => "The name of the attribute in the returned AuthorizationIdentity that should contain the group
     *              membership information for the identity.",
     *      },
     *      "groups-properties-path" => {
     *          "type" => STRING,
     *          "description" => "The path to the file containing the users and their groups.",
     *          "required" => true,
     *          "nillable" => false,
     *      },
     *      "groups-properties-relative-to" => {
     *          "type" => STRING,
     *          "description" => "The pre-defined path the path is relative to.",
     *          "required" => false,
     *          "nillable" => true,
     *      }
     * },
     * </pre>
     *
     * @param complexAttributeName    The complex attribute name
     * @param fromRequestProperties   If the attributes should be loaded from operations/add/request-properties path or
     *                                attributes path. If is true, the attributes from request-properties path will be
     *                                appended, if false, the list of attributes will replace the main attributes path.
     * @param prefixComplexAttribute  If the repackaged attribute names should be prefixed with the complex attribute
     *                                name, as in the above example.
     * @param appendRequestProperties when appendRequestProperties and fromRequestProperties is true, the attributes
     *                                are appended instead of replaced.
     *
     * @return A new Metadata with the repackaged attributes.
     */
    @JsIgnore
    public Metadata repackageComplexAttribute(String complexAttributeName, boolean fromRequestProperties,
            boolean prefixComplexAttribute, boolean appendRequestProperties) {

        ModelNode nestedDescription = new ModelNode();
        ModelNode nestedAttributes;

        boolean enhancedSyntax = complexAttributeName.indexOf(".") > -1;
        List<String> enhancedAttribute = Splitter.on(".").splitToList(complexAttributeName);
        String attributeFirst = enhancedSyntax ? enhancedAttribute.get(0) : null;
        String attributeSecond = enhancedSyntax ? enhancedAttribute.get(1) : null;


        if (fromRequestProperties) {
            if (enhancedSyntax)
                nestedAttributes = this.description.get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES).get(attributeFirst).get(VALUE_TYPE).get(attributeSecond).get(VALUE_TYPE);
            else
                nestedAttributes = this.description.get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES).get(complexAttributeName).get(VALUE_TYPE);


            if (appendRequestProperties) {
                // the attributes are appended, as the request-properties attribute are used to add a new resource, the
                // attributes should be appended instead of replaced.
                nestedDescription.get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES).set(this.description.get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES));
            }
        } else {
            if (enhancedSyntax)
                nestedAttributes = this.description.get(ATTRIBUTES).get(attributeFirst).get(VALUE_TYPE).get(attributeSecond).get(VALUE_TYPE);
            else
                nestedAttributes = this.description.get(ATTRIBUTES).get(complexAttributeName).get(VALUE_TYPE);
        }

        if (enhancedSyntax)
            nestedDescription.get(DESCRIPTION).set(this.description.get(ATTRIBUTES).get(attributeFirst).get(VALUE_TYPE).get(attributeSecond).get(DESCRIPTION));
        else
            nestedDescription.get(DESCRIPTION).set(this.description.get(ATTRIBUTES).get(complexAttributeName).get(DESCRIPTION));

        for (Property prop : nestedAttributes.asPropertyList()) {
            // rename the nested attributes to prefix them with the complex attribute name
            // as the child nested attribute may exist in other child nested attributes
            // an example is users-properties and groups-properties in /subsystem=elytron/properties-realm=*
            // both contains "path" and "relative-to" nested attributes.
            String newName = prop.getName();
            if (prefixComplexAttribute) {
                if (enhancedSyntax)
                    newName = attributeSecond + "-" + prop.getName();
                else
                    newName = complexAttributeName + "-" + prop.getName();
            }

            if (fromRequestProperties) {
                nestedDescription.get(OPERATIONS).get(ADD).get(REQUEST_PROPERTIES).get(newName).set(prop.getValue());
            } else {
                nestedDescription.get(ATTRIBUTES).get(newName).set(prop.getValue());
            }
        }

        // delegates the security-context calls to check against the complex attribute, because the nested attributes
        // lacks the access-control constraint,
        SecurityContext sc = new SecurityContext(new ModelNode()) {
            @Override
            public boolean isReadable() {
                return securityContext.get().isReadable();
            }

            @Override
            public boolean isWritable() {
                return securityContext.get().isWritable();
            }

            @Override
            public boolean isReadable(final String attribute) {
                String _name = enhancedSyntax ? attributeFirst : complexAttributeName;
                return securityContext.get().isReadable(_name);
            }

            @Override
            public boolean isWritable(final String attribute) {
                String _name = enhancedSyntax ? attributeFirst : complexAttributeName;
                return securityContext.get().isWritable(_name);
            }

            @Override
            public boolean isExecutable(final String operation) {
                String _name = enhancedSyntax ? attributeFirst : complexAttributeName;
                return securityContext.get().isExecutable(_name);
            }
        };
        return new Metadata(template, () -> sc, new ResourceDescription(nestedDescription), capabilities);
    }
    // @formatter:on

    @JsIgnore
    public Metadata customResourceDescription(ResourceDescription resourceDescription) {
        return new Metadata(template, securityContext, resourceDescription, capabilities);
    }

    /**
     * @return the address template
     */
    @JsProperty
    public AddressTemplate getTemplate() {
        return template;
    }

    /**
     * @return the security context
     */
    @JsProperty
    public SecurityContext getSecurityContext() {
        if (securityContext != null && securityContext.get() != null) {
            return securityContext.get();
        } else {
            logger.error("No security context found for {}. Return SecurityContext.READ_ONLY", template);
            return SecurityContext.READ_ONLY;
        }
    }

    /**
     * @return the resource description
     */
    @JsProperty
    public ResourceDescription getDescription() {
        return description;
    }

    @JsIgnore
    public Capabilities getCapabilities() {
        return capabilities;
    }
}
