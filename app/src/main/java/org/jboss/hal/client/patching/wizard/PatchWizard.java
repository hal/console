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
package org.jboss.hal.client.patching.wizard;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class PatchWizard {

    final Resources resources;
    final Environment environment;
    final Metadata metadata;
    final StatementContext statementContext;
    final Dispatcher dispatcher;
    final Provider<Progress> progress;
    final ServerActions serverActions;
    final Callback callback;

    PatchWizard(Resources resources, Environment environment, Metadata metadata,
            StatementContext statementContext, Dispatcher dispatcher, Provider<Progress> progress,
            ServerActions serverActions, Callback callback) {
        this.resources = resources;
        this.environment = environment;
        this.metadata = metadata;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.serverActions = serverActions;
        this.callback = callback;
    }

    /**
     * Checks if each servers of a host is stopped, if the server is started, asks the user to stop them. It is a good practice
     * to apply/rollback a patch to a stopped server to prevent application and internal services from failing.
     */
    void checkServersState(Consumer<List<Property>> callback) {
        if (environment.isStandalone()) {
            callback.accept(null);
        } else {

            String host = statementContext.selectedHost();
            ResourceAddress address = new ResourceAddress().add(HOST, host);
            Operation operation = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .param(CHILD_TYPE, SERVER_CONFIG)
                    .build();

            dispatcher.execute(operation, result -> {
                List<Property> servers = result.asPropertyList();
                boolean anyServerStarted = false;
                for (Iterator<Property> iter = servers.iterator(); iter.hasNext();) {
                    Property serverProp = iter.next();
                    Server server = new Server(host, serverProp);
                    if (!server.isStopped()) {
                        anyServerStarted = true;
                    } else {
                        iter.remove();
                    }
                }
                if (anyServerStarted) {
                    callback.accept(servers);
                } else {
                    callback.accept(null);
                }
            });
        }
    }
}
