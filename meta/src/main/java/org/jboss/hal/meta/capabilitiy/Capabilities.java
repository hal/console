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
package org.jboss.hal.meta.capabilitiy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.StatementContext;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Harald Pehl
 */
public class Capabilities {

    @FunctionalInterface
    public interface CapabilitiesCallback extends AsyncCallback<Iterable<AddressTemplate>> {

        @Override
        default void onFailure(Throwable caught) {
            logger.error(caught.getMessage(), caught);
        }
    }


    @NonNls private static final Logger logger = LoggerFactory.getLogger(Capabilities.class);

    private final Environment environment;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Map<String, Capability> registry;

    @Inject
    public Capabilities(final Environment environment,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.registry = new HashMap<>();
    }

    /**
     * Looks up a capability from the local cache. Returns an empty collection if no such capability was found.
     */
    public Iterable<AddressTemplate> lookup(final String name) {
        if (contains(name)) {
            return registry.get(name).getTemplates();
        }
        return Collections.emptyList();
    }

    /**
     * Looks up a capability from the remote capability registry.
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public void lookup(final String name, final CapabilitiesCallback callback) {
        if (!ManagementModel.supportsCapabilitiesRegistry(environment.getManagementVersion())) {
            callback.onFailure(new UnsupportedOperationException("Unable to lookup capabilities for " + name +
                    ", capabilities registry is not supported for management model version " +
                    environment.getManagementVersion()));

        } else {
            ResourceAddress address = AddressTemplate.of("{domain.controller}/core-service=capability-registry")
                    .resolve(statementContext);
            Operation operation = new Operation.Builder("get-provider-points", address) //NON-NLS
                    .param(NAME, name)
                    .build();
            dispatcher.execute(operation,
                    result -> {
                        List<AddressTemplate> templates = result.asList().stream()
                                .map(ModelNode::asString)
                                .map(AddressTemplate::of)
                                .collect(toList());
                        register(name, templates);
                        callback.onSuccess(lookup(name));
                    },
                    (op, failure) -> callback.onFailure(new RuntimeException(
                            "Error reading capabilities for " + name + " using " + op + ": " + failure)),
                    (op, exception) -> callback.onFailure(new RuntimeException(
                            "Error reading capabilities for " + name + " using " + op + ": " + exception.getMessage(),
                            exception)));
        }
    }

    public boolean contains(final String name) {return registry.containsKey(name);}

    public void register(final String name, final AddressTemplate first, final AddressTemplate... rest) {
        safeGet(name).addTemplate(first);
        if (rest != null) {
            for (AddressTemplate template : rest) {
                safeGet(name).addTemplate(template);
            }
        }
    }

    public void register(final String name, final Iterable<AddressTemplate> templates) {
        for (AddressTemplate template : templates) {
            safeGet(name).addTemplate(template);
        }
    }

    public void register(final Capability capability) {
        if (contains(capability.getName())) {
            Capability existing = registry.get(capability.getName());
            for (AddressTemplate template : capability.getTemplates()) {
                existing.addTemplate(template);
            }
        } else {
            registry.put(capability.getName(), capability);
        }
    }

    private Capability safeGet(final String name) {
        if (registry.containsKey(name)) {
            return registry.get(name);
        } else {
            Capability capability = new Capability(name);
            registry.put(name, capability);
            return capability;
        }
    }
}
