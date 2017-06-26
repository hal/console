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
package org.jboss.hal.ballroom;

import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

/**
 * @author Harald Pehl
 */
@JsType(isNative = true)
public class Tooltip {

    @JsMethod(namespace = GLOBAL, name = "$")
    public native static Tooltip element(HTMLElement element);

    @JsMethod(namespace = GLOBAL, name = "$")
    public native static Tooltip select(String selector);

    @JsMethod(name = "tooltip")
    public native void init();

    native void tooltip(String method);

    native void attr(String name, String value);

    native void on(String event, JsCallback callback);

    @JsOverlay
    public final Tooltip show() {
        tooltip("show"); //NON-NLS
        return this;
    }

    @JsOverlay
    public final Tooltip hide() {
        tooltip("hide"); //NON-NLS
        return this;
    }

    @JsOverlay
    public final Tooltip setTitle(String title) {
        attr("data-original-title", title); //NON-NLS
        return this;
    }

    @JsOverlay
    public final void onHide(JsCallback callback) {
        on("hidden.bs.tooltip", callback); //NON-NLS
    }
}
