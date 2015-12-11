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
package org.jboss.hal.meta.description;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;

import java.util.Collections;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Represents the result of a read-resource-description operation for one specific resource.
 *
 * @author Harald Pehl
 */
public class ResourceDescription extends ModelNode {

    public final static ResourceDescription EMPTY = new ResourceDescription();

    public ResourceDescription() {
        super();
    }

    public ResourceDescription(ModelNode payload) {
        set(payload);
    }

    public String getDescription() {
        return get(DESCRIPTION).asString();
    }

    public boolean hasAttributes() {
        return hasDefined(ATTRIBUTES);
    }

    public List<Property> getAttributes() {
        return get(ATTRIBUTES).asPropertyList();
    }

    public List<Property> getRequiredRequestProperties() {
        String path = OPERATIONS + "." + ADD + "." + REQUEST_PROPERTIES;
        ModelNode requestProperties = ModelNodeHelper.failSafeGet(this, path);

        if (requestProperties.isDefined()) {
            Iterable<Property> required = Iterables.filter(requestProperties.asPropertyList(),
                    requestProperty -> requestProperty.getValue().hasDefined(REQUIRED) &&
                            requestProperty.getValue().get(REQUIRED).asBoolean());
            return Lists.newArrayList(required);

        } else {
            return Collections.emptyList();
        }
    }

    public List<Property> getRequiredAttributes() {
        if (hasAttributes()) {
            Iterable<Property> required = Iterables.filter(get(ATTRIBUTES).asPropertyList(),
                    requestProperty -> requestProperty.getValue().hasDefined(NILLABLE) &&
                            !requestProperty.getValue().get(NILLABLE).asBoolean());
            return Lists.newArrayList(required);

        } else {
            return Collections.emptyList();
        }
    }
}
