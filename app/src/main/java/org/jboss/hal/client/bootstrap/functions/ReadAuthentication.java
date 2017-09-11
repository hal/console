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

import java.util.Set;
import javax.inject.Inject;

import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.Roles;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Control;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;

import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Reads attributes from {@code /core-service=management/access=authorization}. This is implemented in an extra
 * bootstrap function, because the operation might fail in some corner cases (e.g. when the current user is a host
 * scoped role scoped to a slave host).
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ReadAuthentication implements BootstrapStep {

    private static final AddressTemplate AUTHENTICATION_TEMPLATE = AddressTemplate.of(
            "/core-service=management/access=authorization");

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
    public void execute(FlowContext context, Control control) {
        logStart();

        ResourceAddress address = AUTHENTICATION_TEMPLATE.resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(RECURSIVE_DEPTH, 1)
                .build();
        dispatcher.executeInFlow(control, operation, result -> {
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

                    logDone();
                    control.proceed();
                },
                (op, failure) -> {
                    logger.error(
                            "{}: Unable to read {} (insufficient rights?). Use default values as fallback.",
                            name(), AUTHENTICATION_TEMPLATE);

                    applyDefaults();
                    logDone();
                    control.proceed();
                });
    }

    private Role scopedRole(Property property, Role.Type type, String scopeAttribute) {
        Role baseRole = environment.getRoles().get(Ids.role(property.getValue().get(BASE_ROLE).asString()));
        Set<String> scope = property.getValue().get(scopeAttribute).asList().stream()
                .map(ModelNode::asString).collect(toSet());
        return new Role(property.getName(), baseRole, type, scope);
    }

    private void applyDefaults() {
        environment.setAccessControlProvider(SIMPLE);
        environment.getRoles().clear();
        environment.getRoles().addAll(Roles.DEFAULT_ROLES);
    }

    @Override
    public String name() {
        return "Bootstrap[ReadAuthentication]";
    }
}
