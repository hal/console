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
package org.jboss.hal.ballroom.table;

import org.jboss.hal.ballroom.table.Selector.Order;
import org.jboss.hal.ballroom.table.Selector.Page;
import org.jboss.hal.ballroom.table.Selector.Search;

/**
 * Builder for a {@link Selector}.
 *
 * @author Harald Pehl
 */
public class SelectorBuilder {

    private Order order;
    private Page page;
    private Search search;
    private Boolean selected;

    public SelectorBuilder() {
        this.order = Order.current;
        this.page = Page.all;
        this.search = Search.none;
        this.selected = null;
    }

    public SelectorBuilder order(Order order) {
        this.order = order;
        return this;
    }

    public SelectorBuilder page(Page page) {
        this.page = page;
        return this;
    }

    public SelectorBuilder search(Search search) {
        this.search = search;
        return this;
    }

    public SelectorBuilder selected() {
        this.selected = true;
        return this;
    }

    public SelectorBuilder unselected() {
        this.selected = false;
        return this;
    }

    public Selector build() {
        Selector selector = new Selector();
        selector.order = order.name();
        selector.page = page.name();
        selector.search = search.name();
        if (selected != null) {
            selector.selected = selected;
        }
        return selector;
    }
}
