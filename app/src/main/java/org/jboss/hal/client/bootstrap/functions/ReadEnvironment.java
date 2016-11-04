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

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.hal.client.bootstrap.functions;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.ManagementModel;

import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Reads important information from the root resource like product name and version, operation mode and management
 * version. Executes the {@code :whoami} operation to get the current user / roles.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ReadEnvironment implements BootstrapFunction {

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final User user;

    @Inject
    public ReadEnvironment(
            Dispatcher dispatcher,
            Environment environment,
            User user) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.user = user;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        logStart();

        List<Operation> ops = new ArrayList<>();
        ops.add(new Operation.Builder(READ_RESOURCE_OPERATION, ResourceAddress.root())
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build());
        ops.add(new Operation.Builder(WHOAMI, ResourceAddress.root()).param(VERBOSE, true).build());
        ResourceAddress address = new ResourceAddress().add("core-service", "management")
                .add("access", "authorization");
        ops.add(new Operation.Builder(READ_ATTRIBUTE_OPERATION, address).param(NAME, PROVIDER).build());

        dispatcher.executeInFunction(control, new Composite(ops),
                (CompositeResult result) -> {
                    // server info
                    logger.debug("{}: Parse root resource", name());
                    ModelNode node = result.step(0).get(RESULT);
                    environment.setInstanceInfo(node.get(PRODUCT_NAME).asString(),
                            node.get(PRODUCT_VERSION).asString(),
                            node.get(RELEASE_CODENAME).asString(), node.get(RELEASE_VERSION).asString(),
                            node.get(NAME).asString());
                    if (environment.isStandalone()) {
                        Server.STANDALONE.addServerAttributes(node);
                    }

                    // operation mode
                    environment.setOperationMode(node.get(LAUNCH_TYPE).asString());

                    // management version
                    Version version = ManagementModel.parseVersion(node);
                    environment.setManagementVersion(version);

                    // user info
                    logger.debug("{}: Parse whoami data", name());
                    ModelNode whoami = result.step(1).get(RESULT);
                    String username = whoami.get("identity").get("username").asString();
                    user.setName(username);
                    if (whoami.hasDefined("mapped-roles")) {
                        List<ModelNode> roles = whoami.get("mapped-roles").asList();
                        for (ModelNode role : roles) {
                            String roleName = role.asString();
                            user.addRole(roleName);
                        }
                    }

                    // access control provider
                    AccessControlProvider accessControlProvider = ModelNodeHelper
                            .asEnumValue(result.step(2).get(RESULT), AccessControlProvider::valueOf, SIMPLE);
                    environment.setAccessControlProvider(accessControlProvider);

                    logDone();
                    control.proceed();
                });
    }

    @Override
    public String name() {
        return "Bootstrap[ReadEnvironment]";
    }
}
