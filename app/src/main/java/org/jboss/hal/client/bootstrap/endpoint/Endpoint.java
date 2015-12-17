/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.bootstrap.endpoint;

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
public class Endpoint extends ModelNode {

    public Endpoint(ModelNode endpoint) {
        set(endpoint);
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Endpoint && getName().equals(((Endpoint) other).getName());
    }

    public String getName() {
        return get(NAME_KEY).asString();
    }

    public boolean isSelected() {
        return get(SELECTED).asBoolean();
    }

    public void setSelected(boolean selected) {
        get(SELECTED).set(selected);
    }

    public String getUrl() {
        StringBuilder url = new StringBuilder();
        url.append(get(SCHEME).asString()).append("://").append(get(HOST).asString());
        ModelNode port = get(PORT);
        if (port.isDefined() && port.asInt() != 0 && port.asInt() != 80) {
            url.append(":").append(port);
        }
        return url.toString();
    }
}
