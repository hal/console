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

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.spi.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Controls the layout of a finder item. For simple items you only need to implement the {@link #getTitle()} method.
 * Override the default implementations if you need more control over the display and behaviour of the item.
 *
 * @author Harald Pehl
 */
public interface ItemDisplay<T> extends IsElement {

    String getTitle();

    /**
     * An unique id for this item
     *
     * @return an id based on {@link #getTitle()}
     */
    default String getId() {
        Iterable<String> parts = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults().split(getTitle());
        return FluentIterable.from(parts).transform(String::toLowerCase).join(Joiner.on('-'));
    }

    /**
     * If this method returns an element != {@code null} this element is used to display the item.
     *
     * @return {@code null} by default
     */
    default Element asElement() {
        return null;
    }

    /**
     * The data which is used to filter items.
     *
     * @return {@link #getTitle()} by default
     */
    default String getFilterData() {
        return getTitle();
    }

    /**
     * Whether the item should be decorated with a colorful marker as left border.
     *
     * @return {@code null} by default
     */
    default Message.Level getMarker() {
        return null;
    }

    /**
     * Whether a tooltip shoudl be shown for the item.
     *
     * @return {@code null} by default
     */
    default String getTooltip() {
        return null;
    }

    /**
     * Whether this item is a folder or not.
     *
     * @return {@code false} by default
     */
    default boolean isFolder() {
        return false;
    }

    /**
     * Defines the action(s) available for the item.
     *
     * @return an empty map by default.
     */
    default List<ItemAction<T>> actions() {
        return new ArrayList<>();
    }
}
