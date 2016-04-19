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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.spi.Message;

/**
 * Controls the layout of a finder item. For simple items you only need to implement the {@link #getTitle()} method.
 * Override the default implementations if you need more control over the display and behaviour of the item.
 *
 * @author Harald Pehl
 */
@FunctionalInterface
public interface ItemDisplay<T> extends IsElement, HasTitle {

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
     * Whether a tooltip should be shown for the item.
     *
     * @return {@code null} by default
     */
    default String getTooltip() {
        return null;
    }

    /**
     * Whether to show an icon left to the item text. The icon should be an PatternFly or FontAwesome icon in normal
     * size.
     *
     * @return {@code null} by default
     */
    default Element getIcon() {
        return null;
    }

    /**
     * Whether this item triggers a next column (hence is a folder not a leaf).
     *
     * @return {@code null} by default
     */
    default String nextColumn() {
        return null;
    }

    /**
     * Defines the action(s) available for the item.
     *
     * @return an empty map by default.
     */
    default List<ItemAction<T>> actions() {
        return new ArrayList<>();
    }

    /**
     * If this method returns an element != {@code null} this element is used to display the item.
     *
     * @return {@code null} by default
     */
    default Element asElement() {
        return null;
    }
}
