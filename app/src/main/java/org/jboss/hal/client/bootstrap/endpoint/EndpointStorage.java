package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.storage.client.Storage;
import org.jboss.hal.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for the management endpoints which uses the local storage of the browser.
 *
 * @author Harald Pehl
 */
public class EndpointStorage {
    private static final String KEY = "org.jboss.hal.bootstrap.endpoints";

    private final Storage storage;

    public EndpointStorage() {
        storage = Storage.getLocalStorageIfSupported();
    }

    public List<Endpoint> load() {
        List<Endpoint> endpoints = new ArrayList<>();
        if (storage != null) {
            String payload = storage.getItem(KEY);
            List<ModelNode> nodes = ModelNode.fromBase64(payload).asList();
            for (ModelNode node : nodes) {
                endpoints.add(new Endpoint(node));
            }
        }
        return endpoints;
    }

    public void save(List<Endpoint> endpoints) {
        if (storage != null) {
            storage.setItem(KEY, toBase64(endpoints));
        }
    }

    public void saveSelection(Endpoint selected) {
        List<Endpoint> endpoints = load();
        for (Endpoint endpoint : endpoints) {
            if (selected.getName().equals(endpoint.getName())) {
                endpoint.setSelected(true);
            } else {
                endpoint.setSelected(false);
            }
        }
        save(endpoints);
    }

    public Endpoint get(String name) {
        List<Endpoint> endpoints = load();
        for (Endpoint endpoint : endpoints) {
            if (name.equals(endpoint.getName())) {
                return endpoint;
            }
        }
        return null;
    }

    private String toBase64(List<Endpoint> endpoints) {
        ModelNode nodes = new ModelNode();
        for (Endpoint endpoint : endpoints) {
            nodes.add(endpoint);
        }
        return nodes.toBase64String();
    }
}
