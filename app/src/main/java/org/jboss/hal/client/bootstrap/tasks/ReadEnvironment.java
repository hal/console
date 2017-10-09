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

package org.jboss.hal.client.bootstrap.tasks;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.OperationMode;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.User;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.ManagementModel;
import rx.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Reads important information from the root resource like product name and version, operation mode and management
 * version. Executes the {@code :whoami} operation to get the current user / roles.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ReadEnvironment implements BootstrapTask {

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final User user;

    @Inject
    public ReadEnvironment(Dispatcher dispatcher, Environment environment, User user) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.user = user;
    }

    @Override
    public Completable call() {
        List<Operation> ops = new ArrayList<>();
        ops.add(new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build());
        ops.add(new Operation.Builder(ResourceAddress.root(), WHOAMI).param(VERBOSE, true).build());

        return dispatcher.execute(new Composite(ops))
                .doOnSuccess((CompositeResult result) -> {
                    // server info
                    ModelNode node = result.step(0).get(RESULT);
                    environment.setInstanceInfo(node.get(PRODUCT_NAME).asString(),
                            node.get(PRODUCT_VERSION).asString(),
                            node.get(RELEASE_CODENAME).asString(), node.get(RELEASE_VERSION).asString(),
                            node.get(NAME).asString());

                    // operation mode
                    //noinspection Convert2MethodRef (conflicts with second method reference below)
                    OperationMode operationMode = asEnumValue(node, LAUNCH_TYPE, (name) -> OperationMode.valueOf(name),
                            OperationMode.UNDEFINED);
                    environment.setOperationMode(operationMode);

                    // management version
                    Version version = ManagementModel.parseVersion(node);
                    environment.setManagementVersion(version);

                    if (environment.isStandalone()) {
                        Server.STANDALONE.addServerAttributes(node);
                    }

                    // user info
                    ModelNode whoami = result.step(1).get(RESULT);
                    String username = whoami.get("identity").get("username").asString();
                    user.setName(username);
                    if (whoami.hasDefined("mapped-roles")) {
                        List<ModelNode> roles = whoami.get("mapped-roles").asList();
                        for (ModelNode role : roles) {
                            String roleName = role.asString();
                            user.addRole(new Role(roleName));
                        }
                    }
                })
                .toCompletable();
    }
}
