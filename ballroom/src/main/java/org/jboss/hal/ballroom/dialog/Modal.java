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
package org.jboss.hal.ballroom.dialog;

import org.jboss.hal.ballroom.JsCallback;

import elemental2.core.JsObject;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

@JsType(isNative = true)
public abstract class Modal {

    @JsMethod(namespace = GLOBAL)
    public static native Modal $(String selector);

    public native void modal(ModalOptions modalOptions);

    public native void modal(String action);

    public native void on(String event, JsCallback callback);

    public native JsObject data(String selector);

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class ModalOptions {

        public String backdrop;
        public boolean keyboard;

        @JsOverlay
        public static ModalOptions create(final boolean closeOnEsc) {
            ModalOptions options = new ModalOptions();
            options.backdrop = "static"; // NON-NLS
            options.keyboard = closeOnEsc;
            return options;
        }
    }
}
