/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.tree;

import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.UIConstants.HASH;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/** A node in a tree - used for both nodes and leafs. */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Node<T> {

    public String id;
    public String text;
    public String icon;
    public State state;
    public String parent;
    public String[] parents;
    public boolean children;
    public T data;

    public static class Builder<T> {

        private final Node<T> node;

        public Builder(final String id, final String text, T data) {
            node = new Node<>();
            node.id = id;
            node.text = text;
            node.data = data;
            node.icon = fontAwesome("file-o");
        }

        public Builder<T> root() {
            node.parent = HASH;
            return this;
        }

        public Builder<T> parent(String id) {
            node.parent = id;
            return this;
        }

        public Builder<T> folder() {
            node.icon = fontAwesome("folder");
            return this;
        }

        public Builder<T> open() {
            if (node.state == null) {
                node.state = new State();
            }
            node.state.opened = true;
            return this;
        }

        public Builder<T> asyncFolder() {
            node.children = true;
            return folder();
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
}
