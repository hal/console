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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.config.AccessControlProvider.RBAC;
import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACCESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTHORIZATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BASE_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOSTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_SCOPED_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAPPED_ROLES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROLES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP_SCOPED_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STANDARD_ROLE_NAMES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VERBOSE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHOAMI;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Reads attributes from {@code /core-service=management/access=authorization}. This is implemented in an extra bootstrap
 * function, because the operation might fail in some corner cases (e.g. when the current user is a host scoped role scoped to a
 * slave host).
 */
public final class ReadAuthentication implements Task<FlowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ReadAuthentication.class);
    private static final AddressTemplate CORE_SERVICE_TEMPLATE = AddressTemplate.of("/core-service=management");

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final StatementContext statementContext;

    @Inject
    public ReadAuthentication(Dispatcher dispatcher, Environment environment, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.statementContext = statementContext;
    }

    @Override
    public Promise<FlowContext> apply(final FlowContext context) {
        logger.debug("Read authentication");
        ResourceAddress address = CORE_SERVICE_TEMPLATE.resolve(statementContext);
        Operation opAuthorization = new Operation.Builder(address, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, ACCESS)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE_DEPTH, 1)
                .build();
        Operation opWhoami = new Operation.Builder(ResourceAddress.root(), WHOAMI)
                .param(VERBOSE, true)
                .build();
        return dispatcher.execute(new Composite(opAuthorization, opWhoami))
                .then((CompositeResult compositeResult) -> {

                    ModelNode result = compositeResult.step(0).get(RESULT);
                    if (result.hasDefined(AUTHORIZATION)) {
                        result = result.get(AUTHORIZATION);
                        // provider
                        AccessControlProvider accessControlProvider = asEnumValue(result, PROVIDER,
                                AccessControlProvider::valueOf, SIMPLE);
                        environment.setAccessControlProvider(accessControlProvider);

                        // standard roles
                        if (result.hasDefined(STANDARD_ROLE_NAMES)) {
                            result.get(STANDARD_ROLE_NAMES).asList().stream()
                                    .map(node -> new Role(node.asString()))
                                    .forEach(role -> environment.getRoles().add(role));
                        }

                        // scoped roles
                        if (!environment.isStandalone()) {
                            if (result.hasDefined(HOST_SCOPED_ROLE)) {
                                result.get(HOST_SCOPED_ROLE).asPropertyList().stream()
                                        .map(property -> scopedRole(property, Role.Type.HOST, HOSTS))
                                        .forEach(role -> environment.getRoles().add(role));
                            }
                            if (result.hasDefined(SERVER_GROUP_SCOPED_ROLE)) {
                                result.get(SERVER_GROUP_SCOPED_ROLE).asPropertyList().stream()
                                        .map(property -> scopedRole(property, Role.Type.SERVER_GROUP, SERVER_GROUPS))
                                        .forEach(role -> environment.getRoles().add(role));
                            }
                        }
                    } else {
                        logger.warn("Unable to read {} (insufficient rights?). Use :whoami values as fallback.",
                                CORE_SERVICE_TEMPLATE.append("access=authorization"));
                        ModelNode resultWhoami = compositeResult.step(1).get(RESULT);
                        environment.setAccessControlProvider(RBAC);
                        environment.getRoles().clear();
                        if (resultWhoami.hasDefined(ROLES)) {
                            resultWhoami.get(ROLES).asList().stream()
                                    .map(node -> new Role(node.asString()))
                                    .forEach(role -> environment.getRoles().add(role));
                        } else if (resultWhoami.hasDefined(MAPPED_ROLES)) {
                            resultWhoami.get(MAPPED_ROLES).asList().stream()
                                    .map(node -> new Role(node.asString()))
                                    .forEach(role -> environment.getRoles().add(role));
                        }
                    }
                    return Promise.resolve(context);
                });
    }

    private Role scopedRole(Property property, Role.Type type, String scopeAttribute) {
        Role baseRole = environment.getRoles().get(Ids.role(property.getValue().get(BASE_ROLE).asString()));
        Set<String> scope = property.getValue().hasDefined(scopeAttribute)
                ? property.getValue().get(scopeAttribute).asList().stream().map(ModelNode::asString).collect(toSet())
                : Collections.emptySet();
        return new Role(property.getName(), baseRole, type, scope);
    }
}
