/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.hal.ballroom;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;
import org.jetbrains.annotations.NonNls;

import java.util.List;

/**
 * Helper to generate unique IDs.
 *
 * @author Harald Pehl
 */
public final class IdBuilder {

    public static String build(@NonNls String id, @NonNls String... additionalIds) {
        if (Strings.emptyToNull(id) == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        List<String> ids = Lists.newArrayList(id);
        if (additionalIds != null) {
            for (String additionalId : additionalIds) {
                ids.add(Strings.emptyToNull(additionalId));
            }
        }
        return Joiner.on('-').skipNulls().join(ids);
    }

    public static void set(Widget widget, @NonNls String id) {
        set(widget.getElement(), id);
    }

    public static void set(com.google.gwt.dom.client.Element element, @NonNls String id) {
        element.setId(id);
    }

    public static void set(elemental.dom.Element element, @NonNls String id) {
        element.setId(id);
    }

    public static String uniqueId() {
        return Document.get().createUniqueId();
    }

    private IdBuilder() {}
}
