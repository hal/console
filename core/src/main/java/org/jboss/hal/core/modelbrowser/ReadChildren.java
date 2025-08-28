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
package org.jboss.hal.core.modelbrowser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.hal.ballroom.tree.DataFunction;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATA_SOURCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WORKMANAGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XA_DATA_SOURCE;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.Ids.MODEL_BROWSER_ROOT;

/**
 * Function which gets invoked when the user opens a node in the model browser tree. TODO Error handling
 */
final class ReadChildren implements DataFunction<Context> {

    private static final String ID_SEPARATOR = "___";
    private static final String NO_SINGLETON = "no_singleton";

    // some resources are implemented as singletons but there is no reason to display them that way
    // since they do not represent pre-defined values
    List<String> falseSingletons = Arrays.asList(HOST, SERVER, DATA_SOURCE, XA_DATA_SOURCE, WORKMANAGER);

    static String uniqueId(Node<Context> parent, String name) {
        String parentId = parent.id;
        int index = parent.id.lastIndexOf(ID_SEPARATOR);
        if (index != -1 && parent.data.isFullyQualified()) {
            parentId = parent.id.substring(index + ID_SEPARATOR.length());

            String[] grandparentParts = parent.parents[0].split(ID_SEPARATOR);
            String resourceName = grandparentParts[0];
            if (grandparentParts.length == 2 && !resourceName.equals(MODEL_BROWSER_ROOT)) {
                parentId = resourceName + "__" + parentId;
            }
        }
        return parentId + ID_SEPARATOR + name;
    }

    private final Dispatcher dispatcher;

    ReadChildren(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(final Node<Context> node, final ResultCallback<Context> callback) {
        if (node.data.isFullyQualified()) {
            Operation operation = new Operation.Builder(node.data.getAddress(), READ_CHILDREN_TYPES_OPERATION)
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

                List<Node<Context>> children = new ArrayList<>();
                for (Map.Entry<String, Collection<String>> entry : resources.asMap().entrySet()) {
                    String name = entry.getKey();
                    Set<String> singletons = new HashSet<>(entry.getValue());
                    if ((singletons.size() == 1 && singletons.contains(NO_SINGLETON)) || falseSingletons.contains(name)) {
                        singletons = Collections.emptySet();
                    }
                    ResourceAddress address = new ResourceAddress(node.data.getAddress()).add(name, "*");
                    Context context = new Context(address, singletons);
                    // ids need to be unique!
                    Node.Builder<Context> builder = new Node.Builder<>(uniqueId(node, name), name, context)
                            .asyncFolder();
                    if (!singletons.isEmpty()) {
                        builder.icon(fontAwesome("list-ul"));
                    }
                    children.add(builder.build());
                }
                callback.result(children.toArray(new Node[children.size()]));
            });

        } else {
            ResourceAddress parentAddress = node.data.getAddress().getParent();
            Operation operation = new Operation.Builder(parentAddress, READ_CHILDREN_NAMES_OPERATION)
                    .param(CHILD_TYPE, node.text)
                    .build();
            dispatcher.execute(operation, result -> {
                List<ModelNode> modelNodes = result.asList();
                List<Node<Context>> children = new ArrayList<>();
                SortedSet<String> singletons = new TreeSet<>(node.data.getSingletons());

                // Add existing children
                for (ModelNode modelNode : modelNodes) {
                    String name = SafeHtmlUtils.fromString(modelNode.asString()).asString();
                    singletons.remove(name);
                    ResourceAddress address = new ResourceAddress(parentAddress).add(node.text, name);
                    Context context = new Context(address, Collections.emptySet());
                    Node<Context> child = new Node.Builder<>(uniqueId(node, name), name, context)
                            .asyncFolder()
                            .icon(fontAwesome("file-text-o"))
                            .build();
                    children.add(child);
                }

                // Add non-existing singletons
                for (String singleton : singletons) {
                    ResourceAddress address = new ResourceAddress(parentAddress).add(node.text, singleton);
                    Context context = new Context(address, Collections.emptySet());
                    Node<Context> child = new Node.Builder<>(uniqueId(node, singleton), singleton, context)
                            .icon(fontAwesome("file-o"))
                            .disabled()
                            .build();
                    children.add(child);
                }

                callback.result(children.toArray(new Node[children.size()]));
            });
        }
    }
}
