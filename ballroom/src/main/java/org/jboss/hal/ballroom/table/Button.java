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

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Custom data tables button.
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/extensions/buttons/custom">https://datatables.net/extensions/buttons/custom</a>
 */
@SuppressWarnings("WeakerAccess")
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Button<T> {

    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    public enum Scope {
        SELECTED("selected"), SELECTED_SINGLE("selectedSingle");

        private final String selector;

        Scope(final String selector) {
            this.selector = selector;
        }

        public String selector() {
            return selector;
        }
    }


    /**
     * Action handler for a custom button.
     *
     * @param <T> the row type
     *
     * @see <a href="https://datatables.net/reference/option/buttons.buttons.action">https://datatables.net/reference/option/buttons.buttons.action</a>
     */
    @JsFunction
    @FunctionalInterface
    public interface ActionHandler<T> {

        /**
         * Action handler callback
         *
         * @param event the object which triggered the action
         * @param table the data table
         */
        void action(Object event, Table<T> table);
    }


    public String text;
    public ActionHandler<T> action;
    public String extend;
    public String constraint;
}
