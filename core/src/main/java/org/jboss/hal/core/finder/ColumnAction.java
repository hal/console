/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    final ColumnActionHandler<T> handler;

    public ColumnAction(final String id, final String title, final ColumnActionHandler<T> handler) {
        this(id, title, null, handler);
    }

    ColumnAction(final String id, final Element element) {
        this(id, null, element, null);
    }

    ColumnAction(final String id, final Element element, final ColumnActionHandler<T> handler) {
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
