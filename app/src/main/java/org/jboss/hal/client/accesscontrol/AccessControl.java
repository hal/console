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
package org.jboss.hal.client.accesscontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.config.AccessControlProvider.RBAC;
import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Kind of presenter which holds code to read and parse the RBAC related management model.
 *
 * @author Harald Pehl
 */
public class AccessControl {

    private final static String LOCAL_USERNAME = "$local";

    @SuppressWarnings("HardCodedStringLiteral")
    private static final ResourceAddress RBAC_ROOT_ADDRESS = new ResourceAddress()
            .add("core-service", "management")
            .add("access", "authorization");

    @NonNls private static final Logger logger = LoggerFactory.getLogger(AccessControl.class);

    private final Environment environment;
    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final Resources resources;

    private final Roles roles;
    private final Principals principals;
    private final Assignments assignments;

    @Inject
    public AccessControl(final Environment environment, final EventBus eventBus, final Dispatcher dispatcher,
            final Resources resources) {
        this.environment = environment;
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.resources = resources;

        this.roles = new Roles();
        this.principals = new Principals();
        this.assignments = new Assignments();
    }

    void switchProvider() {
        Operation.Builder builder = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, RBAC_ROOT_ADDRESS)
                .param(NAME, PROVIDER);
        if (environment.getAccessControlProvider() == SIMPLE) {
            DialogFactory.confirmation(resources.constants().switchProvider(),
                    resources.messages().switchToRbacProvider(), () -> {
                        dispatcher.execute(builder.param(VALUE, RBAC.name().toLowerCase()).build(), result -> {
                            environment.setAccessControlProvider(RBAC);
                            MessageEvent.fire(eventBus, Message.success(resources.messages().switchProviderSuccess()));
                        });
                        return true;
                    }).show();
        } else {
            DialogFactory.confirmation(resources.constants().switchProvider(),
                    resources.messages().switchToSimpleProvider(), () -> {
                        dispatcher.execute(builder.param(VALUE, SIMPLE.name().toLowerCase()).build(), result -> {
                            environment.setAccessControlProvider(SIMPLE);
                            MessageEvent.fire(eventBus, Message.success(resources.messages().switchProviderSuccess()));
                        });
                        return true;
                    }).show();
        }
    }

    private void reset() {
        roles.clear();
        principals.clear();
        assignments.clear();
    }

    void reload(Scheduler.ScheduledCommand andThen) {
        reset();

        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation.Builder(READ_RESOURCE_OPERATION, RBAC_ROOT_ADDRESS)
                .param(INCLUDE_RUNTIME, true)
                .param(ATTRIBUTES_ONLY, true)
                .build());
        if (!environment.isStandalone()) {
            operations.add(new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, RBAC_ROOT_ADDRESS)
                    .param(CHILD_TYPE, "host-scoped-role")
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, RBAC_ROOT_ADDRESS)
                    .param(CHILD_TYPE, "server-group-scoped-role")
                    .param(RECURSIVE, true)
                    .build());
        }
        operations.add(new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, RBAC_ROOT_ADDRESS)
                .param(CHILD_TYPE, "role-mapping")
                .param(RECURSIVE, true)
                .build());
        dispatcher.execute(new Composite(operations), (CompositeResult result) -> {

            int step = 0;
            ModelNode attributes = result.step(step++).get(RESULT);
            AccessControlProvider accessControlProvider = ModelNodeHelper
                    .asEnumValue(attributes, PROVIDER, AccessControlProvider::valueOf, SIMPLE);
            environment.setAccessControlProvider(accessControlProvider);

            attributes.get(STANDARD_ROLE_NAMES).asList().stream()
                    .map(node -> new Role(node.asString()))
                    .forEach(roles::add);

            if (!environment.isStandalone()) {
                result.step(step++).get(RESULT).asPropertyList().stream()
                        .map(property -> scopedRole(property, Role.Type.HOST, HOSTS))
                        .forEach(roles::add);

                result.step(step++).get(RESULT).asPropertyList().stream()
                        .map(property -> scopedRole(property, Role.Type.SERVER_GROUP, SERVER_GROUPS))
                        .forEach(roles::add);
            }

            result.step(step++).get(RESULT).asPropertyList().forEach(p1 -> {
                Role role = roles.get(Ids.role(p1.getName()));
                if (role != null) {
                    ModelNode assignmentNode = p1.getValue();
                    if (assignmentNode.hasDefined(INCLUDE_ALL)) {
                        role.setIncludeAll(assignmentNode.get(INCLUDE_ALL).asBoolean());
                    }
                    if (assignmentNode.hasDefined(INCLUDE)) {
                        assignmentNode.get(INCLUDE).asPropertyList().forEach(p2 -> addAssignment(p2, role, true));
                    }
                    if (assignmentNode.hasDefined(EXCLUDE)) {
                        assignmentNode.get(EXCLUDE).asPropertyList().forEach(p2 -> addAssignment(p2, role, false));
                    }

                } else {
                    logger.error("Cannot add assignment for role {}: No matching role found!", p1.getName());
                }
            });

            andThen.execute();
        });
    }

    private Role scopedRole(Property property, Role.Type type, String scopeAttribute) {
        Role baseRole = roles.get(Ids.role(property.getValue().get(BASE_ROLE).asString()));
        Set<String> scope = property.getValue().get(scopeAttribute).asList().stream()
                .map(ModelNode::asString).collect(toSet());
        return new Role(property.getName(), baseRole, type, scope);
    }

    private void addAssignment(final Property property, final Role role, final boolean include) {
        String resourceName = property.getName();
        ModelNode node = property.getValue();

        String name = node.get(NAME).asString();
        if (LOCAL_USERNAME.equals(name)) {
            return; // skip '$local' assignment
        }
        Principal.Type type = ModelNodeHelper.asEnumValue(node, TYPE, Principal.Type::valueOf, null);
        String realm = node.hasDefined(REALM) ? node.get(REALM).asString() : null;
        Principal principal = new Principal(type, resourceName, name, realm);
        principals.add(principal);

        Assignment assignment = new Assignment(principal, role, include);
        assignments.add(assignment);
    }

    Roles roles() {
        return roles;
    }

    Principals principals() {
        return principals;
    }

    Assignments assignments() {
        return assignments;
    }
}
