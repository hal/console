/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import javax.annotation.Nullable;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;

import com.google.gwt.user.client.ui.Focusable;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.i;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.helpBlock;
import static org.jboss.hal.resources.CSS.inputGroup;
import static org.jboss.hal.resources.CSS.inputGroupAddon;

/**
 * Encapsulates the L&F of a {@linkplain FormItem form item} for a given {@linkplain Form.State form state}. The appearance must
 * include the label and the input element. Appearances should not hold state (except UI state). State should be kept in the
 * form item only not in its appearance(s).
 * <p>
 * An appearance can apply / unapply {@linkplain Decoration decorations}.
 */
interface Appearance<T> extends IsElement, Attachable, Focusable {

    /** Used as a {@code data-} attribute in the root element of the appearances. */
    String FORM_ITEM_GROUP = "formItemGroup";

    // ------------------------------------------------------ static builder methods

    static HTMLElement inputGroup() {
        return div().css(inputGroup).element();
    }

    static HTMLElement helpBlock() {
        return span().css(helpBlock).element();
    }

    static HTMLElement restrictedMarker() {
        return span().css(inputGroupAddon)
                .add(i().css(fontAwesome("lock"))).element();
    }

    static HTMLElement hintMarker() {
        return span().css(inputGroupAddon).element();
    }

    // ------------------------------------------------------ API

    void showValue(T value);

    default void showExpression(String expression) {
        // noop
    }

    default String asString(T value) {
        return String.valueOf(value);
    }

    void clearValue();

    String getId();

    void setId(String id);

    void setName(String name);

    void setLabel(String label);

    default void apply(Decoration decoration) {
        apply(decoration, null);
    }

    <C> void apply(Decoration decoration, @Nullable C context);

    void unapply(Decoration decoration);
}
