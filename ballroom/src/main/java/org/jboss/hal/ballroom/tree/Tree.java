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
package org.jboss.hal.ballroom.tree;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.JsCallback;

import elemental2.core.JsArray;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.UIConstants.HASH;

public class Tree<T> implements IsElement, Attachable {

    private static final String ROOT_NODE = HASH;

    private final String id;
    private final HTMLElement div;
    private final Options options;
    private Bridge<T> bridge;
    private Api<T> api;

    /**
     * Creates a tree with the specified root node. All other nodes are loaded on demand using the provided callback.
     */
    @SuppressWarnings("unchecked")
    public Tree(String id, Node<T> root, DataFunction<T> data) {
        this.id = id;
        this.div = div().id(id).element();
        this.options = initOptions();
        this.options.core.data = (DataFunction<T>) (node, callback) -> {
            if (ROOT_NODE.equals(node.id)) {
                Node<T>[] rootNodes = new Node[] { root };
                callback.result(rootNodes);
            } else {
                data.load(node, callback);
            }
        };
    }

    /**
     * Creates a tree and populates the tree with the specified nodes. This expects all nodes at construction time and does not
     * load nodes on demand.
     * <p>
     * If you use this constructor you must ensure that {@code T} can be turned into JSON.
     */
    public Tree(String id, JsArray<Node<T>> nodes) {
        this.id = id;
        this.div = div().id(id).element();
        this.options = initOptions();
        this.options.core.data = nodes;
    }

    private Options<T> initOptions() {
        Options<T> options = new Options<>();
        options.core = new Options.Core<>();
        options.core.animation = false;
        options.core.multiple = false;
        options.core.themes = new Options.Themes();
        options.core.themes.name = "hal"; // NON-NLS
        options.core.themes.dots = false;
        options.core.themes.icons = true;
        options.core.themes.responsive = true;
        options.core.themes.striped = false;
        options.core.themes.url = false;
        options.plugins = new String[] { "search", "wholerow" }; // NON-NLS
        return options;
    }

    @Override
    public HTMLElement element() {
        return div;
    }

    /**
     * Initialized the {@link org.jboss.hal.ballroom.tree.Api} instance using the {@link org.jboss.hal.ballroom.tree.Options}
     * given at constructor argument. Make sure to call this method before using any of the API methods. It's safe to call the
     * methods multiple times (the initialization will happen only once).
     */
    @Override
    public void attach() {
        if (bridge == null || api == null) {
            // TODO check security context and adjust options if necessary
            bridge = Bridge.select(HASH + id);
            bridge.jstree(options);
            api = bridge.jstree(true);
        }
    }

    private Bridge<T> bridge() {
        if (bridge == null || api == null) {
            throw unattached();
        }
        return bridge;
    }

    private Api<T> api() {
        if (bridge == null || api == null) {
            throw unattached();
        }
        return api;
    }

    private IllegalStateException unattached() {
        return new IllegalStateException(
                "Tree('" + id + "') is not attached. Call Tree.attach() before using any of the API methods!");
    }

    // ------------------------------------------------------ methods

    public void destroy() {
        api().destroy(false);
    }

    public Node<T> getNode(String id) {
        return api().get_node(id);
    }

    public Node<T> getSelected() {
        Node<T>[] selected = api().get_selected(true);
        return selected.length == 0 ? null : selected[0];
    }

    public void openNode(String id, JsCallback callback) {
        api().open_node(id, callback);
    }

    public void refreshNode(String id) {
        api().refresh_node(id);
    }

    public void selectNode(String id) {
        selectNode(id, false);
    }

    public void selectNode(String id, boolean closeSelected) {
        api().deselect_all(true);
        api().select_node(id, false, false);
        if (closeSelected) {
            api().close_node(id);
        }
        Element element = document.getElementById(id);
        if (element != null) {
            element.scrollIntoView(false);
        }
    }

    public void search(String query) {
        api().search(query);
    }

    public void clearSearch() {
        api().clear_search();
    }

    // ------------------------------------------------------ events

    public void onReady(EventHandler<Void> handler) {
        bridge().on("ready.jstree", handler);
    }

    public void onSelectionChange(EventHandler<SelectionContext<T>> handler) {
        bridge().on("changed.jstree", handler);
    }
}
