package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.storage.client.Storage;
import org.jboss.hal.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for the management endpoints which uses the local storage of the browser.
 *
 * @author Harald Pehl
 */
public class EndpointStorage {

    private static final String KEY = "org.jboss.hal.bootstrap.endpoints";
    private static final Logger logger = LoggerFactory.getLogger(EndpointStorage.class);

    private final Storage storage;
    private final List<Endpoint> endpoints;

    public EndpointStorage() {
        storage = Storage.getLocalStorageIfSupported();
        endpoints = load();
    }

    private List<Endpoint> load() {
        List<Endpoint> endpoints = new ArrayList<>();
        if (storage != null) {
            String payload = storage.getItem(KEY);
            if (payload != null) {
                try {
                    List<ModelNode> nodes = ModelNode.fromBase64(payload).asList();
                    for (ModelNode node : nodes) {
                        endpoints.add(new Endpoint(node));
                    }
                } catch (IllegalArgumentException e) {
                    //noinspection HardCodedStringLiteral
                    logger.error("Unable to read endpoints from local storage using key '{}': {}", KEY, e.getMessage());
                }
            }
        }
        return endpoints;
    }

    private void save() {
        if (storage != null) {
            storage.setItem(KEY, toBase64());
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
    }

    public void saveSelection(Endpoint selected) {
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
