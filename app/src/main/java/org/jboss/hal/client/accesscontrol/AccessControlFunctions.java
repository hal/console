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

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.dmr.model.ResourceCheck;
import org.jboss.hal.meta.Metadata;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Functions related to principals, roles and assignments.
 *
 * @author Harald Pehl
 */
final class AccessControlFunctions {

    /**
     * Checks whether a role mapping for a given role exists and pushes {@code 200} to the context stack if it exists,
     * {@code 404} otherwise.
     */
    static class CheckRoleMapping extends ResourceCheck {

        CheckRoleMapping(final Dispatcher dispatcher, final Role role) {
            super(dispatcher, AddressTemplates.roleMapping(role));
        }
    }


    /**
     * Adds a role mapping for a given role if the predicate returns {@code true}, proceeds otherwise.
     * Expects an integer status code at the top of the context stack which is used to call the predicate.
     */
    static class AddRoleMapping implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role role;
        private final Predicate<Integer> predicate;

        AddRoleMapping(final Dispatcher dispatcher, final Role role, final Predicate<Integer> predicate) {
            this.dispatcher = dispatcher;
            this.role = role;
            this.predicate = predicate;
        }

        @Override
        @SuppressWarnings("Duplicates")
        public void execute(final Control<FunctionContext> control) {
            if (control.getContext().emptyStack()) {
                control.proceed();
            } else {
                Integer status = control.getContext().pop();
                if (predicate.test(status)) {
                    Operation operation = new Operation.Builder(ADD, AddressTemplates.roleMapping(role)).build();
                    dispatcher.executeInFunction(control, operation, result -> control.proceed());
                } else {
                    control.proceed();
                }
            }
        }
    }


    /**
     * Modifies the include-all flag of a role-mapping. Please make sure that the role-mapping exists before using this
     * function. Use a combination of {@link CheckRoleMapping} and {@link AddRoleMapping} to do so.
     */
    static class ModifyIncludeAll implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role role;
        private final boolean includeAll;

        ModifyIncludeAll(final Dispatcher dispatcher, final Role role, final boolean includeAll) {
            this.dispatcher = dispatcher;
            this.role = role;
            this.includeAll = includeAll;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            Operation operation = new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, AddressTemplates.roleMapping(role))
                    .param(NAME, INCLUDE_ALL)
                    .param(VALUE, includeAll)
                    .build();
            dispatcher.executeInFunction(control, operation, result -> control.proceed());
        }
    }


    /**
     * Removes a role mapping for a given role if the predicate returns {@code true}, proceeds otherwise.
     * Expects an integer status code at the top of the context stack which is used to call the predicate.
     */
    static class RemoveRoleMapping implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role role;
        private final Predicate<Integer> predicate;

        RemoveRoleMapping(final Dispatcher dispatcher, final Role role, final Predicate<Integer> predicate) {
            this.dispatcher = dispatcher;
            this.role = role;
            this.predicate = predicate;
        }

        @Override
        @SuppressWarnings("Duplicates")
        public void execute(final Control<FunctionContext> control) {
            if (control.getContext().emptyStack()) {
                control.proceed();
            } else {
                Integer status = control.getContext().pop();
                if (predicate.test(status)) {
                    Operation operation = new Operation.Builder(REMOVE, AddressTemplates.roleMapping(role)).build();
                    dispatcher.executeInFunction(control, operation, result -> control.proceed());
                } else {
                    control.proceed();
                }
            }
        }
    }


    /**
     * Adds an assignment to a role-mapping. Please make sure that the role-mapping exists before using this function.
     * Use a combination of {@link CheckRoleMapping} and {@link AddRoleMapping} to do so.
     */
    static class AddAssignment implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role role;
        private final Principal principal;
        private final boolean include;

        AddAssignment(final Dispatcher dispatcher, final Role role, final Principal principal, final boolean include) {
            this.dispatcher = dispatcher;
            this.role = role;
            this.principal = principal;
            this.include = include;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress address = AddressTemplates.roleMapping(role)
                    .add(include ? INCLUDE : EXCLUDE, principal.getResourceName());
            Operation.Builder builder = new Operation.Builder(ADD, address)
                    .param(NAME, principal.getName())
                    .param(TYPE, principal.getType().name());
            if (principal.getRealm() != null) {
                builder.param(REALM, principal.getRealm());
            }
            dispatcher.executeInFunction(control, builder.build(), result -> control.proceed());
        }
    }


    /**
     * Removes assignments from a role-mapping. Please make sure that the role-mapping exists before using this
     * function. Use a combination of {@link CheckRoleMapping} and {@link AddRoleMapping} to do so.
     */
    static class RemoveAssignments implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final List<Assignment> assignments;

        RemoveAssignments(final Dispatcher dispatcher, final List<Assignment> assignments) {
            this.dispatcher = dispatcher;
            this.assignments = assignments;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            if (assignments.isEmpty()) {
                control.proceed();
            } else if (assignments.size() == 1) {
                Assignment assignment = assignments.get(0);
                ResourceAddress address = AddressTemplates.assignment(assignment);
                Operation operation = new Operation.Builder(REMOVE, address).build();
                dispatcher.execute(operation, result -> control.proceed());
            } else {
                List<Operation> operations = assignments.stream()
                        .map(assignment -> {
                            ResourceAddress address = AddressTemplates.assignment(assignment);
                            return new Operation.Builder(REMOVE, address).build();
                        })
                        .collect(toList());
                dispatcher.execute(new Composite(operations), (CompositeResult result) -> control.proceed());
            }
        }
    }


    /**
     * Adds a scoped role.
     */
    static class AddScopedRole implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role.Type type;
        private final String name;
        private final ModelNode payload;

        AddScopedRole(final Dispatcher dispatcher, final Role.Type type, final String name, final ModelNode payload) {
            this.dispatcher = dispatcher;
            this.type = type;
            this.name = name;
            this.payload = payload;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress address = AddressTemplates.scopedRole(new Role(name, null, type, null));
            Operation operation = new Operation.Builder(ADD, address)
                    .payload(payload)
                    .build();
            dispatcher.executeInFunction(control, operation, result -> control.proceed());
        }
    }


    /**
     * Modifies a scoped role.
     */
    static class ModifyScopedRole implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role role;
        private final Map<String, Object> changedValues;
        private final Metadata metadata;

        ModifyScopedRole(final Dispatcher dispatcher, final Role role, final Map<String, Object> changedValues,
                final Metadata metadata) {
            this.dispatcher = dispatcher;
            this.role = role;
            this.changedValues = changedValues;
            this.metadata = metadata;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress address = AddressTemplates.scopedRole(role);
            Operation operation = new OperationFactory().fromChangeSet(address, changedValues, metadata);
            dispatcher.executeInFunction(control, operation, result -> control.proceed());
        }
    }


    /**
     * Removes a scoped role.
     */
    static class RemoveScopedRole implements Function<FunctionContext> {

        private final Dispatcher dispatcher;
        private final Role role;

        RemoveScopedRole(final Dispatcher dispatcher, final Role role) {
            this.dispatcher = dispatcher;
            this.role = role;
        }

        @Override
        public void execute(final Control<FunctionContext> control) {
            ResourceAddress address = AddressTemplates.scopedRole(role);
            Operation operation = new Operation.Builder(REMOVE, address).build();
            dispatcher.executeInFunction(control, operation, result -> control.proceed());
        }
    }


    static Operation addAssignmentOperation(final Role role, final Principal principal, boolean include) {
        ResourceAddress address = AddressTemplates.roleMapping(role)
                .add(include ? INCLUDE : EXCLUDE, principal.getResourceName());
        Operation.Builder builder = new Operation.Builder(ADD, address)
                .param(NAME, principal.getName())
                .param(TYPE, principal.getType().name());
        if (principal.getRealm() != null) {
            builder.param(REALM, principal.getRealm());
        }
        return builder.build();
    }


    private AccessControlFunctions() {
    }
}
