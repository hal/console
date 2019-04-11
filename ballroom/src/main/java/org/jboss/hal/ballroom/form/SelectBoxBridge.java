/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.List;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLSelectElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jetbrains.annotations.NonNls;

import static java.util.Arrays.asList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.resources.UIConstants.OBJECT;

public class SelectBoxBridge {

    private static final String VAL = "val";
    private static final String DESELECT_ALL = "deselectAll";
    private static final String REFRESH = "refresh";
    private static final String CHANGE_EVENT = "changed.bs.select";


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public String noneSelectedText;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        public static Options get() {
            Options options = new Options();
            options.noneSelectedText = UNDEFINED;
            return options;
        }
    }


    @JsFunction
    @FunctionalInterface
    public interface ChangeListener {

        void onChange(Event event, int index);
    }


    @JsType(isNative = true)
    public static class Single {

        @JsMethod(namespace = GLOBAL, name = "$")
        public static native Single element(HTMLSelectElement element);

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
        public static native Multi element(HTMLElement element);

        public native String[] val();

        public native void selectpicker(String method);

        public native void selectpicker(String method, String[] param);

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
            selectpicker(VAL, value.toArray(new String[0]));
        }

        @JsOverlay
        public final void onChange(ChangeListener listener) {
            on(CHANGE_EVENT, listener);
        }
    }
}
