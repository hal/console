/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.tree;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.meta.security.SecurityContext;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.tree;

/**
 * @author Harald Pehl
 */
public class Tree<T> implements IsElement, Attachable {

    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void jstree(Options options);

        public native <T> Api<T> jstree(boolean _true);
    }


    public static final String ROOT_NODE = "#";

    private final String id;
    private final SecurityContext securityContext;
    private final Options options;
    private final Element div;
    private Api<T> api;


    public Tree(final String id, final SecurityContext securityContext,
            final Node<T> root, final DataFunction<T> data) {
        this.id = id;
        this.securityContext = securityContext;
        this.options = initOptions(root, data);
        this.div = Browser.getDocument().createDivElement();
        this.div.setId(id);
        this.div.getClassList().add(tree);
    }

    private Options<T> initOptions(final Node<T> root, final DataFunction<T> data) {
        Options<T> options = new Options<>();
        options.core = new Options.Core<>();
        options.core.animation = false;
        options.core.multiple = false;
        options.core.data = (node, callback) -> {
            if (ROOT_NODE.equals(node.id)) {
                JsArrayOf<Node<T>> rootNodes = JsArrayOf.create();
                rootNodes.push(root);
                callback.result(rootNodes);
            } else {
                data.load(node, callback);
            }
        };
        options.core.themes = new Options.Themes();
        options.core.themes.name = "hal"; //NON-NLS
        options.core.themes.dots= false;
        options.core.themes.icons = true;
        options.core.themes.responsive = true;
        options.core.themes.striped = false;
        options.core.themes.url = false;
        options.plugins = JsArrayOf.create();
        options.plugins.push("wholerow"); //NON-NLS
        return options;
    }

    @Override
    public Element asElement() {
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
            Bridge bridge = Bridge.select("#" + id);
            bridge.jstree(options);
            api = bridge.jstree(true);
        }
    }


    // ------------------------------------------------------ API access

    /**
     * Getter for the {@link org.jboss.hal.ballroom.tree.Api} instance.
     * @throws IllegalStateException if the API wasn't initialized using {@link #attach()}
     */
    public Api api() {
        if (api == null) {
            throw new IllegalStateException(
                    "Tree('" + id + "') is not attached. Call Tree.attach() before using any of the API methods!");
        }
        return api;
    }
}
