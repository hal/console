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

import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * A node in a tree - used for both nodes and leafs.
 *
 * @author Harald Pehl
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Node<T> {

    public static class Builder<T> {

        private final Node<T> node;

        public Builder(final String id, final String text, T data) {
            node = new Node<>();
            node.id = id;
            node.text = text;
            node.data = data;
            node.icon = fontAwesome("file-o");
        }

        public Builder<T> folder() {
            node.children = true;
            node.icon = fontAwesome("folder");
            return this;
        }

        public Builder<T> icon(String icon) {
            node.icon = icon;
            return this;
        }

        public Builder<T> disabled() {
            if (node.state == null) {
                node.state = new State();
            }
            node.state.disabled = true;
            return this;
        }

        public Node<T> build() {
            return node;
        }
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class State {

        public boolean opened;
        public boolean disabled;
        public boolean checked;
    }


    public String id;
    public String text;
    public String icon;
    public State state;
    public String parent;
    public JsArrayOf<String> parents;
    public boolean children;
    public T data;
}
