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

import elemental.js.events.JsEvent;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Handler when the selection changes.
 */
@JsFunction
@FunctionalInterface
public interface SelectionChangeHandler<T> {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    class SelectionContext<T> {
        public String action;
        public Api<T> api;
        public Node<T> node;
        public JsArrayOf<String> selected;
    }

    /**
     * Called when a selection changed. That is when an item is selected <em>or</em> deselected.
     */
    void onSelectionChanged(JsEvent event, SelectionContext<T> context);
}
