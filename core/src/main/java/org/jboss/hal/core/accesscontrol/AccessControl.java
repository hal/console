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
package org.jboss.hal.core.accesscontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.Roles;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.User;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;

import static java.util.stream.Collectors.toSet;

import static org.jboss.hal.config.AccessControlProvider.RBAC;
import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.config.Role.ADMINISTRATOR;
import static org.jboss.hal.config.Role.SUPER_USER;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES_ONLY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.BASE_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXCLUDE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOSTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_SCOPED_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_ALL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REALM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROLE_MAPPING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP_SCOPED_ROLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STANDARD_ROLE_NAMES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USE_IDENTITY_ROLES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/** Small helper class to check if the current user has access to restricted features. */
public class AccessControl {

    private static final String LOCAL_USERNAME = "$local";
    private static final ResourceAddress ADDRESS = AddressTemplate.of("/core-service=management/access=authorization")
            .resolve(StatementContext.NOOP);

    private static final Logger logger = LoggerFactory.getLogger(AccessControl.class);

    private final Environment environment;
    private final EventBus eventBus;
    private final Settings settings;
    private final User currentUser;
    private final Dispatcher dispatcher;
    private final Resources resources;

    private final Roles roles;
    private final Principals principals;
    private final Assignments assignments;

    @Inject
    public AccessControl(Environment environment,
            EventBus eventBus,
            Dispatcher dispatcher,
            User currentUser,
            Settings settings,
            Resources resources) {
        this.environment = environment;
        this.eventBus = eventBus;
        this.settings = settings;
        this.currentUser = currentUser;
        this.dispatcher = dispatcher;
        this.resources = resources;

        this.roles = environment.getRoles();
        this.principals = new Principals();
        this.assignments = new Assignments();
    }

    public boolean isSuperUserOrAdministrator() {
        if (environment.getAccessControlProvider() == AccessControlProvider.RBAC) {
            Set<String> runAs = settings.get(RUN_AS).asSet();
            if (runAs.isEmpty()) {
                return currentUser.isSuperuser() || currentUser.isAdministrator();
            } else {
                return runAs.contains(SUPER_USER.getName()) || runAs.contains(ADMINISTRATOR.getName());
            }
        }
        return true;
    }

    public boolean isSingleSignOn() {
        return environment.isSingleSignOn();
    }

    public void switchProvider() {
        Operation.Builder providerBuilder = new Operation.Builder(ADDRESS, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, PROVIDER);
        Operation.Builder useIdentityBuilder = new Operation.Builder(ADDRESS, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, USE_IDENTITY_ROLES);
        if (environment.getAccessControlProvider() == SIMPLE) {
            Composite composite = new Composite(
                    providerBuilder.param(VALUE, RBAC.name().toLowerCase()).build(),
                    useIdentityBuilder.param(VALUE, true).build());
            DialogFactory.showConfirmation(resources.constants().switchProvider(),
                    resources.messages().switchToRbacProvider(),
                    () -> dispatcher.execute(composite, (Consumer<CompositeResult>) result -> {
                        environment.setAccessControlProvider(RBAC);
                        MessageEvent
                                .fire(eventBus, Message.success(
                                        resources.messages().switchProviderSuccess(SIMPLE.name(), RBAC.name())));
                    }));
        } else {
            Composite composite = new Composite(
                    providerBuilder.param(VALUE, SIMPLE.name().toLowerCase()).build(),
                    useIdentityBuilder.param(VALUE, false).build());
            DialogFactory.showConfirmation(resources.constants().switchProvider(),
                    resources.messages().switchToSimpleProvider(),
                    () -> dispatcher.execute(composite, (Consumer<CompositeResult>) result -> {
                        environment.setAccessControlProvider(SIMPLE);
                        MessageEvent.fire(eventBus, Message.success(
                                resources.messages().switchProviderSuccess(RBAC.name(), SIMPLE.name())));
                    }));
        }
    }

    private void reset() {
        roles.clear();
        principals.clear();
        assignments.clear();
    }

    public void reload(Callback callback) {
        reset();

        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation.Builder(ADDRESS, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .param(ATTRIBUTES_ONLY, true)
                .build());
        if (!environment.isStandalone()) {
            operations.add(new Operation.Builder(ADDRESS, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, HOST_SCOPED_ROLE)
                    .param(RECURSIVE, true)
                    .build());
            operations.add(new Operation.Builder(ADDRESS, READ_CHILDREN_RESOURCES_OPERATION)
                    .param(CHILD_TYPE, SERVER_GROUP_SCOPED_ROLE)
                    .param(RECURSIVE, true)
                    .build());
        }
        operations.add(new Operation.Builder(ADDRESS, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, ROLE_MAPPING)
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

            // noinspection UnusedAssignment
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

            // sync with current user
            String currentUserId = Ids.principal(Principal.Type.USER.name().toLowerCase(), currentUser.getName());
            Principal currentPrincipal = principals.get(currentUserId);
            if (currentPrincipal != null) {
                Set<Role> currentRoles = assignments.byPrincipal(currentPrincipal)
                        .map(Assignment::getRole)
                        .collect(toSet());
                currentUser.refreshRoles(currentRoles);
            }

            callback.execute();
        });
    }

    private Role scopedRole(Property property, Role.Type type, String scopeAttribute) {
        Role baseRole = roles.get(Ids.role(property.getValue().get(BASE_ROLE).asString()));
        Set<String> scope = property.getValue().get(scopeAttribute).asList().stream()
                .map(ModelNode::asString).collect(toSet());
        return new Role(property.getName(), baseRole, type, scope);
    }

    private void addAssignment(Property property, Role role, boolean include) {
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

    public Roles roles() {
        return roles;
    }

    public Principals principals() {
        return principals;
    }

    public Assignments assignments() {
        return assignments;
    }
}
