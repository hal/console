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
package org.jboss.hal.ballroom.table;

import org.jboss.hal.ballroom.table.Api.SelectorModifier.Order;
import org.jboss.hal.ballroom.table.Api.SelectorModifier.Page;
import org.jboss.hal.ballroom.table.Api.SelectorModifier.Search;

/**
 * Builder for a {@link Api.SelectorModifier}.
 *
 * @author Harald Pehl
 */
class SelectorModifierBuilder {

    private Order order;
    private Page page;
    private Search search;
    private Boolean selected;

    SelectorModifierBuilder() {
        this.order = Order.current;
        this.page = Page.all;
        this.search = Search.none;
        this.selected = null;
    }

    SelectorModifierBuilder order(Order order) {
        this.order = order;
        return this;
    }

    SelectorModifierBuilder page(Page page) {
        this.page = page;
        return this;
    }

    SelectorModifierBuilder search(Search search) {
        this.search = search;
        return this;
    }

    SelectorModifierBuilder selected() {
        this.selected = true;
        return this;
    }

    SelectorModifierBuilder unselected() {
        this.selected = false;
        return this;
    }

    Api.SelectorModifier build() {
        Api.SelectorModifier selectorModifier = new Api.SelectorModifier();
        selectorModifier.order = order.name();
        selectorModifier.page = page.name();
        selectorModifier.search = search.name();
        if (selected != null) {
            selectorModifier.selected = selected;
        }
        return selectorModifier;
    }
}
