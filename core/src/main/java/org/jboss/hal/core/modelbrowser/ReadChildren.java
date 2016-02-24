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
package org.jboss.hal.core.modelbrowser;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import elemental.js.util.JsArrayOf;
import org.jboss.hal.ballroom.tree.DataFunction;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * Function which gets invoked when the user opens a node in the model browser tree.
 * TODO Error handling
 *
 * @author Harald Pehl
 */
class ReadChildren implements DataFunction<Context> {

    private static final String ID_SEPARATOR = "___";
    private static final String NO_SINGLETON = "no_singleton";

    static String uniqueId(Node<Context> parent, String name) {
        String parentId = parent.id;
        int index = parent.id.indexOf(ID_SEPARATOR);
        if (index != -1) {
            parentId = parent.id.substring(index + ID_SEPARATOR.length(), parent.id.length());
        }
        return parentId + ID_SEPARATOR + name;
    }

    private final Dispatcher dispatcher;

    ReadChildren(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void load(final Node<Context> node, final ResultCallback<Context> callback) {
        if (node.data.isFullyQualified()) {
            Operation operation = new Operation.Builder(READ_CHILDREN_TYPES_OPERATION, node.data.getAddress())
                    .param(INCLUDE_SINGLETONS, true)
                    .build();
            dispatcher.execute(operation, result -> {
                List<ModelNode> modelNodes = result.asList();
                Multimap<String, String> resources = HashMultimap.create();
                for (ModelNode modelNode : modelNodes) {
                    String name = modelNode.asString();
                    if (name.contains("=")) {
                        List<String> parts = Splitter.on('=').limit(2).splitToList(name);
                        resources.put(parts.get(0), parts.get(1));
                    } else {
                        resources.put(name, NO_SINGLETON);
                    }
                }

                JsArrayOf<Node<Context>> children = JsArrayOf.create();
                for (Map.Entry<String, Collection<String>> entry : resources.asMap().entrySet()) {
                    String name = entry.getKey();
                    Set<String> singletons = new HashSet<>(entry.getValue());
                    if (singletons.size() == 1 && singletons.contains(NO_SINGLETON)) {
                        singletons = Collections.emptySet();
                    }
                    ResourceAddress address = new ResourceAddress(node.data.getAddress()).add(name, "*");
                    Context context = new Context(address, singletons);
                    // ids need to be unique!
                    Node.Builder<Context> builder = new Node.Builder<>(uniqueId(node, name), name, context)
                            .folder();
                    if (!singletons.isEmpty()) {
                        builder.icon(fontAwesome("list-ul"));
                    }
                    children.push(builder.build());
                }
                callback.result(children);
            });

        } else {
            ResourceAddress parentAddress = node.data.getAddress().getParent();
            Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, parentAddress)
                    .param(CHILD_TYPE, node.text)
                    .build();
            dispatcher.execute(operation, result -> {
                List<ModelNode> modelNodes = result.asList();
                JsArrayOf<Node<Context>> children = JsArrayOf.create();
                SortedSet<String> singletons = new TreeSet<>(node.data.getSingletons());

                // Add existing children
                for (ModelNode modelNode : modelNodes) {
                    String name = modelNode.asString();
                    singletons.remove(name);
                    ResourceAddress address = new ResourceAddress(parentAddress).add(node.text, name);
                    Context context = new Context(address, Collections.emptySet());
                    Node<Context> child = new Node.Builder<>(uniqueId(node, name), name, context)
                            .folder()
                            .icon(fontAwesome("file-text-o"))
                            .build();
                    children.push(child);
                }

                // Add non-existing singletons
                for (String singleton : singletons) {
                    ResourceAddress address = new ResourceAddress(parentAddress).add(node.text, singleton);
                    Context context = new Context(address, Collections.emptySet());
                    Node<Context> child = new Node.Builder<>(uniqueId(node, singleton), singleton, context)
                            .icon(fontAwesome("file-o"))
                            .disabled()
                            .build();
                    children.push(child);
                }

                callback.result(children);
            });
        }
    }
}
