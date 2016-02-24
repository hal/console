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

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * @author Harald Pehl
 */
@JsType(isNative = true)
public class Api<T> {

    @JsFunction
    @FunctionalInterface
    public interface OpenCallback {

        void opened();
    }

    @JsMethod(name = "get_node")
    public native Node<T> getNode(String id);

    @JsMethod(name = "open_node")
    public native void openNode(String id);

    @JsMethod(name = "open_node")
    public native void openNode(String id, OpenCallback callback);

    @JsMethod(name = "close_node")
    public native void closeNode(String id);

    @JsMethod(name = "select_node")
    public native void selectNode(String id, boolean suppressEvent, boolean preventOpen);

    @JsMethod(name = "deselect_all")
    public native void deselectAll(boolean suppressEvent);
}
