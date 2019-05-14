/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.window;

public class ExtensionStorage {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExtensionStorage.class);

    private final Storage storage;
    private final Map<String, InstalledExtension> extensions;

    public ExtensionStorage() {
        this.storage = WebStorageWindow.of(window).localStorage;
        this.extensions = load();
    }

    private Map<String, InstalledExtension> load() {
        Map<String, InstalledExtension> extensions = new LinkedHashMap<>();
        if (storage != null) {
            String payload = storage.getItem(Ids.EXTENSION_STORAGE);
            if (payload != null) {
                try {
                    List<Property> properties = ModelNode.fromBase64(payload).asPropertyList();
                    properties.forEach(property -> extensions.put(property.getName(),
                            InstalledExtension.fromModelNode(property.getValue())));
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
        for (NamedNode extension : extensions.values()) {
            nodes.add(extension.getName(), extension.asModelNode());
        }
        return nodes.toBase64String();
    }


    // ------------------------------------------------------ crud

    public void add(InstalledExtension extension) {
        extensions.put(extension.getName(), extension);
        save();
    }

    public List<InstalledExtension> list() {
        return new ArrayList<>(extensions.values());
    }

    public InstalledExtension get(String name) {
        return extensions.get(name);
    }

    public void remove(InstalledExtension extension) {
        extensions.remove(extension.getName());
        save();
    }
}
