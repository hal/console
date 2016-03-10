package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.NamedNode;

/**
 * @author Harald Pehl
 */
public class XADataSource extends NamedNode {

    public XADataSource(final String name) {
        super(name, new ModelNode());
    }

    public XADataSource(final Property property) {
        super(property);
    }
}
