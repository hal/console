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
package org.jboss.hal.client.deployment;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import elemental2.core.JsArray;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.resources.CSS.fontAwesome;

class ContentParser {

    private static final Comparator<ContentEntry> BY_NAME = Comparator.comparing(c -> c.name);
    private static final Comparator<ContentEntry> BY_DEPTH = Comparator.comparingInt(c -> c.depth);

    private static final String DIRECTORY = "directory";
    private static final String FILE_SIZE = "file-size";

    static final Function<String, String> NODE_ID = path -> Ids.build("bct", path, "node");

    @SuppressWarnings("unchecked")
    void parse(Node<ContentEntry> root, JsArray<Node<ContentEntry>> nodes, List<ModelNode> content) {
        nodes.push(root);

        Map<String, Node<ContentEntry>> nodesByPath = new HashMap<>();
        content.stream()
                .map(this::contentEntry)
                .filter(contentEntry -> contentEntry.directory)
                .sorted(BY_DEPTH.thenComparing(BY_NAME))
                .forEach(directory -> {
                    String parentPath = parentPath(directory);
                    Node<ContentEntry> parent = parentPath == null ? root : nodesByPath.get(parentPath);

                    if (parent != null) {
                        Node<ContentEntry> node = pushFolder(nodes, parent, directory);
                        nodesByPath.put(directory.path, node);
                    }
                });

        content.stream()
                .map(this::contentEntry)
                .filter(contentEntry -> !contentEntry.directory)
                .sorted(BY_NAME)
                .forEach(file -> {
                    String parentPath = parentPath(file);
                    Node<ContentEntry> parent = parentPath == null ? root : nodesByPath.get(parentPath);
                    if (parent != null) {
                        pushEntry(nodes, parent, file);
                    }
                });
    }

    private ContentEntry contentEntry(ModelNode node) {
        String path = node.get(PATH).asString();
        String safePath = SafeHtmlUtils.htmlEscape(path);
        Iterable<String> segments = Splitter.on('/').omitEmptyStrings().split(safePath);

        ContentEntry contentEntry = new ContentEntry();
        contentEntry.name = Iterables.getLast(segments);
        contentEntry.path = safePath;
        contentEntry.depth = Iterables.size(segments);
        contentEntry.directory = node.hasDefined(DIRECTORY) && node.get(DIRECTORY).asBoolean();
        contentEntry.fileSize = node.hasDefined(FILE_SIZE) ? node.get(FILE_SIZE).asLong() : 0;
        return contentEntry;
    }

    @SuppressWarnings("unchecked")
    private Node<ContentEntry> pushFolder(JsArray<Node<ContentEntry>> nodes, Node<ContentEntry> parent,
            ContentEntry contentEntry) {
        Node<ContentEntry> node = new Node.Builder<>(NODE_ID.apply(contentEntry.path), contentEntry.name, contentEntry)
                .parent(parent.id)
                .folder()
                .build();
        nodes.push(node);
        return node;
    }

    @SuppressWarnings("unchecked")
    private Node<ContentEntry> pushEntry(JsArray<Node<ContentEntry>> nodes, Node<ContentEntry> parent,
            ContentEntry contentEntry) {
        Node<ContentEntry> node = new Node.Builder<>(NODE_ID.apply(contentEntry.path), contentEntry.name, contentEntry)
                .parent(parent.id)
                .icon(fontAwesome("file-text-o"))
                .build();
        nodes.push(node);
        return node;
    }

    private String parentPath(ContentEntry contentEntry) {
        String path = contentEntry.path.endsWith("/")
                ? contentEntry.path.substring(0, contentEntry.path.length() - 1)
                : contentEntry.path;
        int index = path.lastIndexOf('/');
        if (index != -1) {
            return path.substring(0, index + 1);
        }
        return null;
    }
}
