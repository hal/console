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
package org.jboss.hal.core.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Provides methods to read subsystem resources from (sub)deployments. */
public class DeploymentResources {

    public static final String DEPLOYMENT_ADDRESS = "{selected.host}/{selected.server}/deployment=*";
    public static final String SUBDEPLOYMENT_ADDRESS = "{selected.host}/{selected.server}/deployment=*/subdeployment=*";
    private static final AddressTemplate DEPLOYMENT_TEMPLATE = AddressTemplate.of(DEPLOYMENT_ADDRESS);
    private static final AddressTemplate SUBDEPLOYMENT_TEMPLATE = AddressTemplate.of(SUBDEPLOYMENT_ADDRESS);

    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    @Inject
    public DeploymentResources(Dispatcher dispatcher, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
    }

    public <T extends DeploymentResource> void readChildren(String subsystem, String resource,
            DeploymentResourceSupplier<T> supplier, Consumer<List<T>> callback) {
        // /deployment=*/subsystem=<subsystem>:read-children-resources(child-type=<resource>)
        // fails with "WFLYCTL0217: Child resource '\"deployment\" => \"*\"' not found"
        // That's why we use: /deployment=*/subsystem=<subsystem>/<resource>=*:read-resource()

        ResourceAddress deploymentAddress = DEPLOYMENT_TEMPLATE
                .append(SUBSYSTEM + "=" + subsystem)
                .append(resource + "=*")
                .resolve(statementContext);
        Operation deploymentJobOperation = new Operation.Builder(deploymentAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();

        ResourceAddress subdeploymentAddress = SUBDEPLOYMENT_TEMPLATE
                .append(SUBSYSTEM + "=" + subsystem)
                .append(resource + "=*")
                .resolve(statementContext);
        Operation subdeploymentJobOperation = new Operation.Builder(subdeploymentAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE, true)
                .build();

        dispatcher.execute(new Composite(deploymentJobOperation, subdeploymentJobOperation),
                (CompositeResult result) -> {
                    List<T> nodes = new ArrayList<>();
                    Consumer<ModelNode> nodeConsumer = node -> {
                        ResourceAddress address = new ResourceAddress(node.get(ADDRESS));
                        nodes.add(supplier.get(address, node.get(RESULT)));
                    };
                    result.step(0).get(RESULT).asList().forEach(nodeConsumer);
                    result.step(1).get(RESULT).asList().forEach(nodeConsumer);
                    callback.accept(nodes);
                });
    }
}
