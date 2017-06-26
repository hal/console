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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.small;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.resources.CSS.itemText;

/**
 * Controls the layout of a finder item. For simple items you only need to implement the {@link #getTitle()} method.
 * Override the default implementations if you need more control over the display and behaviour of the item.
 *
 * @author Harald Pehl
 */
@FunctionalInterface
public interface ItemDisplay<T> extends IsElement, HasTitle {

    /**
     * An unique id for this item.
     * <p>
     * Please make sure the id returned by this method matches the id which is part of the {@link FinderPath} returned
     * by {@link ApplicationFinderPresenter#finderPath()}
     *
     * @return an id based on {@link #getTitle()}: {@code Ids.asId(getTitle())}
     */
    default String getId() {
        return Ids.asId(getTitle());
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
     * Whether a tooltip should be shown for the item.
     *
     * @return {@code null} by default (no tooltip)
     */
    default String getTooltip() {
        return null;
    }

    /**
     * Whether to show an icon left to the item text. The icon should be a PatternFly or FontAwesome icon in normal
     * size. See {@link org.jboss.hal.resources.Icons} for a list of common icons.
     *
     * @return {@code null} by default
     */
    default HTMLElement getIcon() {
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
    default HTMLElement asElement() {
        return null;
    }

    /**
     * Factory methods which can be used when overriding {@link #asElement()}. Creates a {@code <div>} element with two
     * nested {@code <span>} elements. One for the title and a smaller one for the subtitle.
     *
     * @param title
     * @param subtitle
     *
     * @return
     */
    static HTMLElement withSubtitle(String title, String subtitle) {
        return span().css(itemText)
                .add(span().textContent(title))
                .add(small().css(CSS.subtitle).title(subtitle).textContent(subtitle))
                .asElement();
    }
}
