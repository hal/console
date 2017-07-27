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
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;

import static elemental2.dom.DomGlobal.document;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.div;

public class Tree<T> implements IsElement, Attachable {

    @JsType(isNative = true)
    public static class Bridge<T> {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static <T> Bridge<T> select(String selector);

        public native void jstree(Options options);

        /**
         * Adds a selection change callback.
         */
        public native void on(String event, SelectionChangeHandler<T> handler);

        public native Api<T> jstree(boolean _true);
    }


    private static final String ROOT_NODE = "#";
    private static final String CHANGED_EVENT = "changed.jstree";

    private final String id;
    private final HTMLElement div;
    private final Options options;
    private Bridge<T> bridge;
    private Api<T> api;


    /**
     * Creates a tree with the specified root node. All other nodes are loaded on demand using the provided callback.
     */
    @SuppressWarnings("unchecked")
    public Tree(final String id, final Node<T> root, final DataFunction<T> data) {
        this.id = id;
        this.div = div().id(id).asElement();
        this.options = initOptions();
        this.options.core.data = (DataFunction<T>) (node, callback) -> {
            if (ROOT_NODE.equals(node.id)) {
                Node<T>[] rootNodes = new Node[]{root};
                callback.result(rootNodes);
            } else {
                data.load(node, callback);
            }
        };
    }

    /**
     * Creates a tree and populates the tree with the specified nodes. This expects all nodes at construction time and
     * does not load nodes on demand.
     * <p>
     * If you use this constructor you must ensure that {@code T} can be turned into JSON.
     */
    public Tree(final String id, final Array<Node<T>> nodes) {
        this.id = id;
        this.div = div().id(id).asElement();
        this.options = initOptions();
        this.options.core.data = nodes;
    }

    private Options<T> initOptions() {
        Options<T> options = new Options<>();
        options.core = new Options.Core<>();
        options.core.animation = false;
        options.core.multiple = false;
        options.core.themes = new Options.Themes();
        options.core.themes.name = "hal"; //NON-NLS
        options.core.themes.dots = false;
        options.core.themes.icons = true;
        options.core.themes.responsive = true;
        options.core.themes.striped = false;
        options.core.themes.url = false;
        options.plugins = new String[]{"search", "wholerow"}; //NON-NLS
        return options;
    }

    @Override
    public HTMLElement asElement() {
        return div;
    }

    /**
     * Initialized the {@link org.jboss.hal.ballroom.tree.Api} instance using the {@link
     * org.jboss.hal.ballroom.tree.Options} given at constructor argument. Make sure to call
     * this method before using any of the API methods. It's safe to call the methods multiple times (the
     * initialization will happen only once).
     */
    @Override
    public void attach() {
        if (api == null) {
            // TODO check security context and adjust options if necessary
            bridge = Bridge.select("#" + id);
            bridge.jstree(options);
            api = bridge.jstree(true);
        }
    }


    // ------------------------------------------------------ API access

    /**
     * Getter for the {@link org.jboss.hal.ballroom.tree.Api} instance.
     *
     * @throws IllegalStateException if the API wasn't initialized using {@link #attach()}
     */
    public Api<T> api() {
        if (api == null) {
            throw new IllegalStateException(
                    "Tree('" + id + "') is not attached. Call Tree.attach() before using any of the API methods!");
        }
        return api;
    }

    public final void onSelectionChange(SelectionChangeHandler<T> handler) {
        if (bridge == null) {
            throw new IllegalStateException(
                    "Tree('" + id + "') is not attached. Call Tree.attach() before you register callbacks!");
        }
        bridge.on(CHANGED_EVENT, handler);
    }

    public void select(final String id, final boolean closeSelected) {
        api().deselectAll(true);
        api().selectNode(id, false, false);
        if (closeSelected) {
            api().closeNode(id);
        }
        asElement().focus();
        document.getElementById(id).scrollIntoView(false);
    }
}
