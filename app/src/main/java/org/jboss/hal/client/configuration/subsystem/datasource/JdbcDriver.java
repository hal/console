package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * @author Harald Pehl
 */
public class JdbcDriver extends NamedNode {

    public JdbcDriver(final String name) {
        super(name, new ModelNode());
    }

    public JdbcDriver(final Property property) {
        super(property);
    }
}
