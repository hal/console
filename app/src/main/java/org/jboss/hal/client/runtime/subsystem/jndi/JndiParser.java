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
package org.jboss.hal.client.runtime.subsystem.jndi;

import java.util.List;

import elemental.js.util.JsArrayOf;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILDREN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Harald Pehl
 */
class JndiParser {

    void parse(JsArrayOf<Node<JndiContext>> nodes, Node<JndiContext> root, List<Property> children) {
        nodes.push(root);
        readChildren(nodes, root, children);
    }

    private void readChildren(JsArrayOf<Node<JndiContext>> nodes, Node<JndiContext> parent, List<Property> children) {

        children.stream()
                .filter(child -> child.getValue().isDefined())
                .forEach(child -> {

                    ModelNode modelNode = child.getValue();
                    if (modelNode.hasDefined(VALUE)) {
                        pushEntry(nodes, parent, child.getName(), new JndiContext());

                    } else {
                        Node<JndiContext> node = pushFolder(nodes, parent, child.getName(), new JndiContext());
                        if (modelNode.hasDefined(CHILDREN)) {
                            readChildren(nodes, node, modelNode.get(CHILDREN).asPropertyList());

                        } else if (child.getValue().getType() == ModelType.OBJECT) {
                            readChildren(nodes, node, child.getValue().asPropertyList());
                        }
                    }
                });
    }

    private Node<JndiContext>  pushFolder(JsArrayOf<Node<JndiContext>> nodes, Node<JndiContext> parent, String name,
            JndiContext jndiContext) {
        Node<JndiContext> node = new Node.Builder<>(Ids.build(parent.id, Ids.uniqueId()), name, jndiContext)
                .parent(parent.id)
                .folder()
                .build();
        nodes.push(node);
        return node;
    }

    private Node<JndiContext>  pushEntry(JsArrayOf<Node<JndiContext>> nodes, Node<JndiContext> parent, String name,
            JndiContext jndiContext) {
        Node<JndiContext> node = new Node.Builder<>(Ids.build(parent.id, Ids.uniqueId()), name, jndiContext)
                .parent(parent.id)
                .icon(fontAwesome("file-text-o"))
                .build();
        nodes.push(node);
        return node;
    }
}
