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
package org.jboss.hal.core.subsystem;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.ItemsProvider;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;

/**
 * @author Harald Pehl
 */
public class SubsystemProvider implements ItemsProvider<SubsystemMetadata> {

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final AddressTemplate template;
    private final Subsystems subsystems;

    public SubsystemProvider(final Dispatcher dispatcher, final StatementContext statementContext,
            final AddressTemplate template, final Subsystems subsystems) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.template = template;
        this.subsystems = subsystems;
    }

    @Override
    public void get(final FinderContext context, final AsyncCallback<List<SubsystemMetadata>> callback) {
        ResourceAddress address = template.resolve(statementContext);
        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                .param(CHILD_TYPE, SUBSYSTEM).build();
        dispatcher.execute(operation, result -> {

            List<SubsystemMetadata> combined = new ArrayList<>();
            for (ModelNode modelNode : result.asList()) {
                String name = modelNode.asString();
                if (subsystems.containsConfiguration(name)) {
                    combined.add(subsystems.getConfigurationSubsystem(name));

                } else {
                    String title = new LabelBuilder().label(name);
                    SubsystemMetadata subsystem = new SubsystemMetadata(name, title, null, null, null,
                            false);
                    combined.add(subsystem);
                }
            }
            callback.onSuccess(combined);
        });
    }
}
