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

import java.util.function.Supplier;

import com.google.gwt.resources.client.TextResource;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public Metadata customResourceDescription(ResourceDescription resourceDescription) {
        return new Metadata(template, securityContext, resourceDescription, capabilities);
    }

    @JsProperty
    public AddressTemplate getTemplate() {
        return template;
    }

    @JsProperty
    public SecurityContext getSecurityContext() {
        if (securityContext != null && securityContext.get() != null) {
            return securityContext.get();
        } else {
            logger.error("No security context found for {}. Return SecurityContext.READ_ONLY", template);
            return SecurityContext.READ_ONLY;
        }
    }

    @JsProperty
    public ResourceDescription getDescription() {
        return description;
    }

    @JsIgnore
    public Capabilities getCapabilities() {
        return capabilities;
    }
}
