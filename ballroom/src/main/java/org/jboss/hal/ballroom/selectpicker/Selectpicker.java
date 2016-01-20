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
package org.jboss.hal.ballroom.selectpicker;

import elemental.dom.Element;
import elemental.events.Event;
import elemental.util.ArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;
import static org.jboss.hal.resources.Names.UNDEFINED;

/**
 * @author Harald Pehl
 */
public class Selectpicker {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public String noneSelectedText;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        private static final Options DEFAULT_OPTIONS = new Options();

        static {
            DEFAULT_OPTIONS.noneSelectedText = UNDEFINED;
        }

        public static Options get() {
            return DEFAULT_OPTIONS;
        }
    }


    private final static String VAL = "val";
    private final static String DESELECT_ALL = "deselectAll";
    private final static String CHANGE_EVENT = "changed.bs.select";


    @JsFunction
    @FunctionalInterface
    public interface SingleChangeListener {

        void onChange(Event event, int index, String newValue, String oldValue);
    }


    @JsType(isNative = true)
    public static class Single {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Single select(Element element);

        public native String val();

        public native void selectpicker(String method, String param);

        public native void on(@NonNls String event, SingleChangeListener listener);

        @JsOverlay
        public final String getValue() {
            return val();
        }

        @JsOverlay
        public final void setValue(String value) {
            selectpicker(VAL, value);
        }

        @JsOverlay
        public final void onChange(SingleChangeListener listener) {
            on(CHANGE_EVENT, listener);
        }
    }


    @JsFunction
    @FunctionalInterface
    public interface MultiChangeListener {

        void onChange(Event event, int index, ArrayOf<String> newValue, ArrayOf<String> oldValue);
    }


    @JsType(isNative = true)
    public static class Multi {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Multi select(Element element);

        public native ArrayOf<String> val();

        public native void selectpicker(String method);

        public native void selectpicker(String method, ArrayOf<String> param);

        public native void on(@NonNls String event, MultiChangeListener listener);

        @JsOverlay
        public final ArrayOf<String> getValue() {
            return val();
        }

        @JsOverlay
        public final void clear() {
            selectpicker(DESELECT_ALL);
        }

        @JsOverlay
        public final void setValue(ArrayOf<String> value) {
            selectpicker(VAL, value);
        }

        @JsOverlay
        public final void onChange(MultiChangeListener listener) {
            on(CHANGE_EVENT, listener);
        }
    }
}
