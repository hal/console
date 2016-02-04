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
package org.jboss.hal.core.finder;

import elemental.dom.Element;

/**
 * @author Harald Pehl
 */
class ActionStruct<T extends FinderItem> {

    final String title;
    final Element content;
    final ColumnAction<T> columnAction;
    final ItemAction<T> itemAction;

    ActionStruct(final String title, final ColumnAction<T> columnAction) {
        this(title, null, columnAction, null);
    }

    ActionStruct(final Element content, final ColumnAction<T> columnAction) {
        this(null, content, columnAction, null);
    }

    ActionStruct(final String title, final ItemAction<T> itemAction) {
        this(title, null, null, itemAction);
    }

    ActionStruct(final Element content, final ItemAction<T> itemAction) {
        this(null, content, null, itemAction);
    }

    private ActionStruct(final String title, final Element content, final ColumnAction<T> columnAction,
            final ItemAction<T> itemAction) {
        this.columnAction = columnAction;
        this.title = title;
        this.content = content;
        this.itemAction = itemAction;
    }
}
