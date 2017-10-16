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
package org.jboss.hal.ballroom.listview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.HasTitle;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.resources.Ids;

/** Controls the layout of a list view item. */
public interface ItemDisplay<T> extends IsElement, HasTitle {

    /**
     * An unique id for this item. If you use a {@link DataProvider} make sure to use the same IDs.
     *
     * @return an id based on {@link #getTitle()}
     */
    default String getId() {
        return Ids.asId(getTitle());
    }

    default String getStatusIcon() {
        return null;
    }

    default HTMLElement getStatusElement() {
        return null;
    }

    default SafeHtml getTitleHtml() {
        return null;
    }

    default HasElements getTitleElements() {
        return null;
    }

    default String getDescription() {
        return null;
    }

    default SafeHtml getDescriptionHtml() {
        return null;
    }

    default HasElements getDescriptionElements() {
        return null;
    }

    default String getAdditionalInfo() {
        return null;
    }

    default SafeHtml getAdditionalInfoHtml() {
        return null;
    }

    default HasElements getAdditionalInfoElements() {
        return null;
    }

    /** The length of the description content. Override this method to control when the display content */
    default int getDescriptionLength() {
        return -1;
    }

    /** The maximum allowed length of the description content to be displayed by default. */
    default int getMaxDescriptionLength() {
        return 600;
    }

    default boolean hideDescriptionWhenLarge() {
        return getDescriptionLength() > getMaxDescriptionLength();
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
}
