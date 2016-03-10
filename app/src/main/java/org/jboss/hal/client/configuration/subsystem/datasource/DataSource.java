package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * @author Harald Pehl
 */
public class DataSource extends NamedNode {

    private final boolean xa;

    public DataSource(final String name, final boolean xa) {
        super(name, new ModelNode());
        this.xa = xa;
    }

    public DataSource(final Property property, final boolean xa) {
        super(property);
        this.xa = xa;
    }

    public boolean isXa() {
        return xa;
    }
}
