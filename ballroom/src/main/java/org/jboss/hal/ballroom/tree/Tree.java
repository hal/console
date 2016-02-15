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
public class Tree implements IsElement, Attachable {


    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void jstree(Options options);

        public native Api jstree(boolean _true);
    }


    private final String id;
    private final SecurityContext securityContext;
    private final Options options;
    private final Element div;
    private Api api;


    public Tree(final String id, final SecurityContext securityContext, final Options options) {
        this.id = id;
        this.securityContext = securityContext;
        this.options = options;

        this.div = Browser.getDocument().createDivElement();
        this.div.getClassList().add(tree);
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
