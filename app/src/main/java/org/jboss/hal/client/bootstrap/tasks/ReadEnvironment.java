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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.OperationMode;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.User;
import org.jboss.hal.config.Version;
import org.jboss.hal.config.keycloak.Keycloak;
import org.jboss.hal.config.keycloak.KeycloakHolder;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.meta.ManagementModel;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Reads important information from the root resource like product name and version, operation mode and management
 * version. Executes the {@code :whoami} operation to get the current user / roles.
 */
public class ReadEnvironment implements BootstrapTask {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ReadEnvironment.class);

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final User user;
    private KeycloakHolder keycloakHolder;

    @Inject
    public ReadEnvironment(Dispatcher dispatcher, Environment environment, User user, KeycloakHolder keycloakHolder) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.user = user;
        this.keycloakHolder = keycloakHolder;
    }

    @Override
    public Completable call(FlowContext context) {
        logger.debug("Read environment");

        Keycloak keycloak = keycloakHolder.getKeycloak();
        environment.setSingleSignOn(keycloak != null);
        if (keycloak != null) {
            logger.debug("Keycloak token: {}", keycloak.token);
        }

        List<Operation> ops = new ArrayList<>();
        ops.add(new Operation.Builder(ResourceAddress.root(), READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .param(INCLUDE_RUNTIME, true)
                .build());
        ops.add(new Operation.Builder(ResourceAddress.root(), WHOAMI).param(VERBOSE, true).build());

        return dispatcher.execute(new Composite(ops))
                .doOnSuccess((CompositeResult result) -> {
                    ModelNode node = result.step(0).get(RESULT);

                    // operation mode
                    OperationMode operationMode = asEnumValue(node, LAUNCH_TYPE, (name) -> OperationMode.valueOf(name),
                            OperationMode.UNDEFINED);
                    environment.setOperationMode(operationMode);
                    logger.debug("Operation mode: {}", operationMode);

                    // name and org
                    if (node.get(NAME).isDefined()) {
                        String name = node.get(NAME).asString();
                        environment.setName(name);
                    }
                    String orgAttribute = environment.isStandalone() ? ORGANIZATION : DOMAIN_ORGANIZATION;
                    if (node.get(orgAttribute).isDefined()) {
                        String org = node.get(orgAttribute).asString();
                        environment.setOrganization(org);
                    }

                    // server info
                    environment.setInstanceInfo(node.get(PRODUCT_NAME).asString(), node.get(PRODUCT_VERSION).asString(),
                            node.get(RELEASE_CODENAME).asString(), node.get(RELEASE_VERSION).asString());

                    // management version
                    Version version = ManagementModel.parseVersion(node);
                    environment.setManagementVersion(version);
                    logger.debug("Management model version: {}", version);

                    if (environment.isStandalone()) {
                        Server.STANDALONE.addServerAttributes(node);
                    }

                    // user info
                    if (environment.isSingleSignOn()) {
                        user.setName(keycloak.userProfile.username);
                        // as Keycloak is a native js object, the Java 8 collection methods as: stream, foreach, iterator
                        // are not supported on the javascript side when run in the browser.
                        if (keycloak.realmAccess != null && keycloak.realmAccess.roles != null) {
                            for (int i = 0; i < keycloak.realmAccess.roles.length; i++) {
                                String role = keycloak.realmAccess.roles[i];
                                user.addRole(new Role(role));
                            }
                        }
                    } else {
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
                    }
                    user.setAuthenticated(true);
                    logger.debug("User info: {} {}", user.getName(), user.getRoles());
                })
                .toCompletable();
    }
}
