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

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;

/**
 * Custom data tables button.
 *
 * @param <T> the row type
 *
 * @author Harald Pehl
 * @see <a href="https://datatables.net/extensions/buttons/custom">https://datatables.net/extensions/buttons/custom</a>
 */
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
         * @param api   the data tables API
         */
        void action(Object event, Api<T> api);
    }


    public String text;
    public ActionHandler<T> action;
    public String extend;
}
