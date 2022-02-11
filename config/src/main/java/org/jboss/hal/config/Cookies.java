/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.config;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

@JsType(isNative = true, namespace = GLOBAL, name = "Cookies")
class Cookies {

    static native String get(String name);

    static native void set(String name, String value);

    @JsOverlay
    static void set(String name, String value, int expires) {
        CookieOptions options = new CookieOptions();
        options.expires = expires;
        set(name, value, options);
    }

    static native void set(String name, String value, CookieOptions options);

    static native void remove(String name);

    private Cookies() {
    }

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class CookieOptions {

        int expires;
    }
}
