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
package org.jboss.hal.core.mbui;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;

import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class ResourceDescriptionBuilder {

    public ResourceDescription empty() {
        return new ResourceDescription(new ModelNode());
    }

    public ResourceDescription attributes(String... attributes) {
        ModelNode list = new ModelNode();
        if (attributes != null) {
            for (String attribute : attributes) {
                list.get(attribute).set(new ModelNode());
            }
        }
        return new ResourceDescription(new ModelNode().set(ATTRIBUTES, list));
    }

    public ResourceDescription requestProperties(Map<String, Boolean> requestProperties) {
        ModelNode list = new ModelNode();
        if (requestProperties != null) {
            for (Map.Entry<String, Boolean> entry : requestProperties.entrySet()) {
                list.get(entry.getKey()).set(new ModelNode().set(REQUIRED, entry.getValue()));
            }
        }
        return new ResourceDescription(
                new ModelNode().set(OPERATIONS,
                        new ModelNode().set(ADD,
                                new ModelNode().set(REQUEST_PROPERTIES, list))));
    }
}
