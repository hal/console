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
import org.jboss.hal.ballroom.JsCallback;

@JsType(isNative = true)
class Api<T> {

    @JsMethod
    native void close_node(String id);

    @JsMethod
    native void deselect_all(boolean suppressEvent);

    @JsMethod
    native Node<T>[] get_selected(boolean full);

    @JsMethod
    native void select_node(String id, boolean suppressEvent, boolean preventOpen);

    @JsMethod
    native Node<T> get_node(String id);

    @JsMethod
    native void open_node(String id, JsCallback callback);

    @JsMethod
    native void refresh_node(String id);

    @JsMethod
    native void search(String query);

    @JsMethod
    native void clear_search();

    @JsMethod
    native void destroy(boolean keepHtml);
}
