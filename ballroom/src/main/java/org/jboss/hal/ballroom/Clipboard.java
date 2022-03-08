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
package org.jboss.hal.ballroom;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Clipboard implementation based on <a href="https://clipboardjs.com/">clipboardjs</a>.
 *
 * @see <a href="https://clipboardjs.com/">https://clipboardjs.com/</a>
 */
@JsType(name = "ClipboardJS", namespace = GLOBAL, isNative = true)
public class Clipboard {

    @JsConstructor
    @SuppressWarnings("UnusedParameters")
    public Clipboard(HTMLElement element, Options options) {
    }

    public native void on(String type, Handler handler);

    public native void destroy();

    @JsFunction
    @FunctionalInterface
    public interface Handler {

        void handle(Event event);
    }

    @JsFunction
    @FunctionalInterface
    public interface TextProvider {

        String provideText(HTMLElement element);
    }

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public TextProvider text;
    }

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Event {

        public String action;
        public String text;
        public HTMLElement trigger;
    }
}
