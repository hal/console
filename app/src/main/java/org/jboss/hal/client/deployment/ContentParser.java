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
package org.jboss.hal.client.deployment;

import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import elemental.js.util.JsArrayOf;
import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @author Harald Pehl
 */
class ContentParser {

    private static final String DIRECTORY = "directory";
    private static final String FILE_SIZE = "file-size";

    void parse(JsArrayOf<Node<ContentEntry>> nodes, Node<ContentEntry> root, List<ModelNode> content) {
        nodes.push(root);
        List<ContentEntry> contentEntries = content.stream().map(this::contentEntry).collect(toList());
        Map<String, ContentEntry> byPath = contentEntries.stream()
                .collect(toMap(contentEntry -> contentEntry.path, identity()));
        readChildren(nodes, root, contentEntries, byPath);
    }

    private void readChildren(JsArrayOf<Node<ContentEntry>> nodes, Node<ContentEntry> parent,
            List<ContentEntry> contentEntries, Map<String, ContentEntry> checklist) {

        contentEntries.stream()
                .filter(contentEntry -> checklist.containsKey(contentEntry.path))
                .forEach(contentEntry -> {
                    checklist.remove(contentEntry.path);

                    if (contentEntry.directory) {
                        Node<ContentEntry> node = pushFolder(nodes, parent, contentEntry);
                        List<ContentEntry> children = checklist.values().stream()
                                .filter(ce -> ce.path.startsWith(contentEntry.path) &&
                                        ce.depth == contentEntry.depth + 1)
                                .collect(toList());
                        if (!children.isEmpty()) {
                            readChildren(nodes, node, children, checklist);
                        }

                    } else {
                        pushEntry(nodes, parent, contentEntry);
                    }
                });
    }

    private ContentEntry contentEntry(ModelNode node) {
        String path = node.get(PATH).asString();
        Iterable<String> segments = Splitter.on('/').omitEmptyStrings().split(path);

        ContentEntry contentEntry = new ContentEntry();
        contentEntry.name = Iterables.getLast(segments);
        contentEntry.path = path;
        contentEntry.depth = Iterables.size(segments);
        contentEntry.directory = node.hasDefined(DIRECTORY) && node.get(DIRECTORY).asBoolean();
        contentEntry.fileSize = node.hasDefined(FILE_SIZE) ? node.get(FILE_SIZE).asLong() : 0;
        return contentEntry;
    }

    private Node<ContentEntry> pushFolder(JsArrayOf<Node<ContentEntry>> nodes, Node<ContentEntry> parent,
            ContentEntry contentEntry) {
        Node<ContentEntry> node = new Node.Builder<>(Ids.build(parent.id, Ids.uniqueId()), contentEntry.name,
                contentEntry)
                .parent(parent.id)
                .folder()
                .build();
        nodes.push(node);
        return node;
    }

    private Node<ContentEntry> pushEntry(JsArrayOf<Node<ContentEntry>> nodes, Node<ContentEntry> parent,
            ContentEntry contentEntry) {
        Node<ContentEntry> node = new Node.Builder<>(Ids.build(parent.id, Ids.uniqueId()), contentEntry.name,
                contentEntry)
                .parent(parent.id)
                .icon(fontAwesome("file-text-o"))
                .build();
        nodes.push(node);
        return node;
    }
}
