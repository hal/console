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
package org.jboss.hal.client.bootstrap.endpoint;

import java.util.ArrayList;
import java.util.List;

import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.window;

/** Registry for the management endpoints which uses the local storage of the browser. */
public class EndpointStorage {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(EndpointStorage.class);

    private final Storage storage;
    private final List<Endpoint> endpoints;

    public EndpointStorage() {
        storage = WebStorageWindow.of(window).localStorage;
        endpoints = load();
    }

    private List<Endpoint> load() {
        List<Endpoint> endpoints = new ArrayList<>();
        if (storage != null) {
            String payload = storage.getItem(Ids.ENDPOINT_STORAGE);
            if (payload != null) {
                try {
                    List<ModelNode> nodes = ModelNode.fromBase64(payload).asList();
                    for (ModelNode node : nodes) {
                        endpoints.add(new Endpoint(node));
                    }
                } catch (IllegalArgumentException e) {
                    logger.error("Unable to read endpoints from local storage using key '{}': {}",
                            Ids.ENDPOINT_STORAGE, e.getMessage());
                }
            }
        }
        return endpoints;
    }

    private void save() {
        if (storage != null) {
            storage.setItem(Ids.ENDPOINT_STORAGE, toBase64());
        }
    }

    private String toBase64() {
        ModelNode nodes = new ModelNode();
        for (Endpoint endpoint : endpoints) {
            nodes.add(endpoint);
        }
        return nodes.toBase64String();
    }

    public void add(Endpoint endpoint) {
        endpoints.add(endpoint);
        save();
    }

    public void remove(Endpoint endpoint) {
        endpoints.remove(endpoint);
        save();
    }

    void saveSelection(Endpoint selected) {
        for (Endpoint endpoint : endpoints) {
            if (selected.getName().equals(endpoint.getName())) {
                endpoint.setSelected(true);
            } else {
                endpoint.setSelected(false);
            }
        }
        save();
    }

    public Endpoint get(String name) {
        for (Endpoint endpoint : endpoints) {
            if (name.equals(endpoint.getName())) {
                return endpoint;
            }
        }
        return null;
    }

    public List<Endpoint> list() {
        return endpoints;
    }
}
