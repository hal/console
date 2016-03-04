package org.jboss.hal.meta;

import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

/**
 * Simple data struct for the various metadata. Only used to keep the method signatures small and tidy.
 * @author Harald Pehl
 */
public class Metadata {

    private final SecurityContext securityContext;
    private final ResourceDescription description;
    private final Capabilities capabilities;

    public Metadata(final SecurityContext securityContext, final ResourceDescription description,
            final Capabilities capabilities) {
        this.securityContext = securityContext;
        this.description = description;
        this.capabilities = capabilities;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public ResourceDescription getDescription() {
        return description;
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }
}
