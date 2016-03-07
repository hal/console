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
public class ColumnAction<T> {

    final String id;
    final String title;
    final Element element;
    ColumnActionHandler<T> handler;

    public ColumnAction(final String id, final String title) {
        this(id, title, null, null);
    }

    public ColumnAction(final String id, final String title, final ColumnActionHandler<T> handler) {
        this(id, title, null, handler);
    }

    public ColumnAction(final String id, final Element element) {
        this(id, null, element, null);
    }

    public ColumnAction(final String id, final Element element, final ColumnActionHandler<T> handler) {
        this(id, null, element, handler);
    }

    private ColumnAction(final String id, final String title, final Element element,
            final ColumnActionHandler<T> handler) {
        this.id = id;
        this.title = title;
        this.element = element;
        this.handler = handler;
    }
}
