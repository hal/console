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
package org.jboss.hal.ballroom.tree;

import elemental2.core.Array;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.JsCallback;

/**
 * @author Harald Pehl
 */
@JsType(isNative = true)
public class Api<T> {

    @JsMethod(name = "close_node")
    public native void closeNode(String id);

    @JsMethod(name = "open_node")
    public native void openNode(String id);

    @JsMethod(name = "open_node")
    public native void openNode(String id, JsCallback callback);

    @JsMethod(name = "refresh_node")
    public native void refreshNode(String id);

    @JsMethod(name = "get_node")
    public native Node<T> getNode(String id);

    @JsMethod
    public native Array<Node<T>> get_selected(boolean full);

    @JsOverlay
    public final Node<T> getSelected() {
        Array<Node<T>> selected = get_selected(true);
        return selected.getLength() == 0 ? null : selected.getAt(0);
    }

    @JsMethod(name = "select_node")
    public native void selectNode(String id, boolean suppressEvent, boolean preventOpen);

    @JsMethod(name = "deselect_all")
    public native void deselectAll(boolean suppressEvent);

    @JsMethod
    public native void search(String query);

    @JsMethod(name = "clear_search")
    public native void clearSearch();

    @JsMethod
    public native void destroy(boolean keepHtml);
}
