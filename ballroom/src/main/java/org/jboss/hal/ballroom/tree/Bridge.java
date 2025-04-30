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
package org.jboss.hal.ballroom.tree;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true)
class Bridge<T> {

    @JsMethod(namespace = GLOBAL, name = "$")
    static native <T> Bridge<T> select(String selector);

    native void jstree(Options<T> options);

    @SuppressWarnings("SameParameterValue")
    native Api<T> jstree(boolean _true);

    native void on(String event, SimpleEventHandler handler);

    native <E> void on(String event, EventHandler<E> handler);
}
