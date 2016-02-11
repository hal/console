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
package org.jboss.hal.ballroom.form;

import elemental.dom.Element;
import elemental.js.events.JsEvent;
import elemental.js.util.JsArrayOf;
import elemental.util.ArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jetbrains.annotations.NonNls;

import java.util.List;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.js.JsHelper.asJsArray;
import static org.jboss.hal.ballroom.js.JsHelper.asList;
import static org.jboss.hal.resources.Names.UNDEFINED;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * @author Harald Pehl
 */
public class SelectBoxBridge {

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


    @JsFunction
    @FunctionalInterface
    public interface ChangeListener {

        void onChange(JsEvent event, int index);
    }


    @JsType(isNative = true)
    public static class Single {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Single element(Element element);

        public native String val();

        public native void selectpicker(String method);

        public native void selectpicker(String method, String param);

        public native void on(@NonNls String event, ChangeListener listener);

        @JsOverlay
        public final String getValue() {
            return val();
        }

        @JsOverlay
        public final void setValue(String value) {
            selectpicker(VAL, value);
        }

        @JsOverlay
        public final void onChange(ChangeListener listener) {
            on(CHANGE_EVENT, listener);
        }

        @JsOverlay
        public final void refresh() {
            selectpicker(REFRESH);
        }
    }


    @JsType(isNative = true)
    public static class Multi {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Multi element(Element element);

        public native JsArrayOf<String> val();

        public native void selectpicker(String method);

        public native void selectpicker(String method, ArrayOf<String> param);

        public native void on(@NonNls String event, ChangeListener listener);

        @JsOverlay
        public final List<String> getValue() {
            return asList(val());
        }

        @JsOverlay
        public final void clear() {
            selectpicker(DESELECT_ALL);
        }

        @JsOverlay
        public final void refresh() {
            selectpicker(REFRESH);
        }

        @JsOverlay
        public final void setValue(List<String> value) {
            selectpicker(VAL, asJsArray(value));
        }

        @JsOverlay
        public final void onChange(ChangeListener listener) {
            on(CHANGE_EVENT, listener);
        }
    }


    private final static String VAL = "val";
    private final static String DESELECT_ALL = "deselectAll";
    private final static String REFRESH = "refresh";
    private final static String CHANGE_EVENT = "changed.bs.select";
}
