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
package org.jboss.hal.core.analytics;

import jsinterop.annotations.JsMethod;

import static jsinterop.annotations.JsPackage.GLOBAL;

public class GoogleAnalytics {

    public void trackPageView(String page) {
        ga("set", "page", page);
        ga("send", "pageview");
    }

    public void customDimension(int index, Object value) {
        ga("set", "dimension" + index, value);
    }

    @JsMethod(namespace = GLOBAL, name = "ga")
    static native void ga(String command, String type);

    @JsMethod(namespace = GLOBAL, name = "ga")
    static native void ga(String command, String type, Object value);
}
