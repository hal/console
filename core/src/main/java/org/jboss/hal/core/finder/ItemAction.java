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

import java.util.HashMap;
import java.util.Map;

import elemental.dom.Element;

/**
 * @author Harald Pehl
 */
public class ItemAction<T> {

    final String title;
    final Element element;
    final ItemActionHandler<T> handler;
    final String href;
    final Map<String, String> parameter;

    public ItemAction(final String title, final ItemActionHandler<T> handler) {
        this(title, null, handler, null, (String[]) null);
    }

    public ItemAction(final Element element, final ItemActionHandler<T> handler) {
        this(null, element, handler, null, (String[]) null);
    }

    public ItemAction(final String title, final String href, String... parameter) {
        this(title, null, null, href, parameter);
    }

    public ItemAction(final Element element, final String href, String... parameter) {
        this(null, element, null, href, parameter);
    }

    private ItemAction(final String title, final Element element, final ItemActionHandler<T> handler,
            final String href, String... parameter) {
        this.title = title;
        this.element = element;
        this.handler = handler;
        this.href = href;
        this.parameter = new HashMap<>();
        if (parameter != null && parameter.length > 1) {
            if (parameter.length % 2 != 0) {
                throw new IllegalArgumentException("Parameter in new ItemAction() must be key/value pairs");
            }
            for (int i = 0; i < parameter.length; i += 2) {
                this.parameter.put(parameter[i], parameter[i + 1]);
            }
        }
    }
}
