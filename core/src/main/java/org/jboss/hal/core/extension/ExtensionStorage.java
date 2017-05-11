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
package org.jboss.hal.core.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import elemental.client.Browser;
import elemental.html.Storage;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SCRIPT;

/**
 * @author Harald Pehl
 */
public class ExtensionStorage {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExtensionStorage.class);

    private final Storage storage;
    private final Map<String, Extension> extensions;

    public ExtensionStorage() {
        this.storage = Browser.getWindow().getLocalStorage();
        this.extensions = load();
    }

    private Map<String, Extension> load() {
        Map<String, Extension> extensions = new LinkedHashMap<>();
        if (storage != null) {
            String payload = storage.getItem(Ids.EXTENSION_STORAGE);
            if (payload != null) {
                try {
                    List<ModelNode> nodes = ModelNode.fromBase64(payload).asList();
                    nodes.forEach(node -> extensions.put(node.get(SCRIPT).asString(), new Extension(node)));
                } catch (IllegalArgumentException e) {
                    logger.error("Unable to read extensions from local storage using key '{}': {}",
                            Ids.EXTENSION_STORAGE, e.getMessage());
                }
            }
        }
        return extensions;
    }

    private void save() {
        if (storage != null) {
            storage.setItem(Ids.EXTENSION_STORAGE, toBase64());
        }
    }

    private String toBase64() {
        ModelNode nodes = new ModelNode();
        for (ModelNode extension : extensions.values()) {
            nodes.add(extension);
        }
        return nodes.toBase64String();
    }

    public void save(Extension extension) {
        extensions.put(extension.getScript(), extension);
        save();
    }

    public Extension get(String name) {
        return extensions.get(name);
    }

    public void remove(Extension extension) {
        extensions.remove(extension.getScript());
        save();
    }

    public List<Extension> list() {
        return new ArrayList<>(extensions.values());
    }
}
