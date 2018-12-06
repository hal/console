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

import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.UniqueNameValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.client.accesscontrol.AccessControlTasks.AddAssignment;
import org.jboss.hal.client.accesscontrol.AccessControlTasks.AddRoleMapping;
import org.jboss.hal.client.accesscontrol.AccessControlTasks.CheckRoleMapping;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.User;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.accesscontrol.AddressTemplates.INCLUDE_TEMPLATE;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

class PrincipalColumn extends FinderColumn<Principal> {

    static List<String> filterData(Principal principal) {
        List<String> data = new ArrayList<>();
        data.add(principal.getName());
        if (principal.getRealm() != null) {
            data.add(principal.getRealm());
        }
        return data;
    }

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Provider<Progress> progress;
    private final AccessControl accessControl;
    private final Resources resources;

    PrincipalColumn(Finder finder,
            String id,
            String title,
            Principal.Type type,
            ColumnActionFactory columnActionFactory,
            Dispatcher dispatcher,
            EventBus eventBus,
            Provider<Progress> progress,
            User currentUser,
            AccessControl accessControl,
            AccessControlTokens tokens,
            AccessControlResources accessControlResources,
            Resources resources) {

        super(new Builder<Principal>(finder, id, title)
                .onPreview(item -> new PrincipalPreview(accessControl, tokens, item, resources))
                .showCount()
                .withFilter());
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.accessControl = accessControl;
        this.resources = resources;

        // we assume that the add operations for INCLUDE_TEMPLATE and EXCLUDE_TEMPLATE have the same rights
        addColumnAction(columnActionFactory.add(Ids.ROLE_ADD, title, INCLUDE_TEMPLATE, column -> {
            Metadata metadata = Metadata.staticDescription(accessControlResources.principal());
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(id, Ids.FORM), metadata)
                    .addOnly()
                    .include(NAME, REALM, INCLUDE, EXCLUDE)
                    .unsorted()
                    .build();

            List<String> roleNames = new ArrayList<>();
            accessControl.roles().standardRoles().stream().sorted(comparing(Role::getName)).map(Role::getName)
                    .forEach(roleNames::add);
            accessControl.roles().scopedRoles().stream().sorted(comparing(Role::getName)).map(Role::getName)
                    .forEach(roleNames::add);
            form.getFormItem(INCLUDE).registerSuggestHandler(new StaticAutoComplete(roleNames));
            form.getFormItem(EXCLUDE).registerSuggestHandler(new StaticAutoComplete(roleNames));

            form.addFormValidation(frm -> {
                FormItem<String> name = frm.getFormItem(NAME);
                FormItem<String> realm = frm.getFormItem(REALM);
                if (!name.isUndefined()) {
                    String[] names = getCurrentItems().stream()
                            .map(item -> item.getName() + "@" + item.getRealm()).toArray(String[]::new);
                    UniqueNameValidation<String> validator = new UniqueNameValidation<>(names);

                    ValidationResult nameValidationResult = validator
                            .validate(name.getValue() + "@" + realm.getValue());

                    if (!nameValidationResult.isValid()) {
                        return nameValidationResult;
                    }
                }

                FormItem<List<String>> includeItem = frm.getFormItem(INCLUDE);
                FormItem<List<String>> excludeItem = frm.getFormItem(EXCLUDE);
                boolean noIncludes = includeItem.isUndefined() || includeItem.getValue() == null || includeItem
                        .getValue().isEmpty();
                boolean noExcludes = excludeItem.isUndefined() || excludeItem.getValue() == null || excludeItem
                        .getValue().isEmpty();
                return noIncludes && noExcludes
                        ? ValidationResult.invalid(resources.constants().noRolesIncludedOrExcluded())
                        : ValidationResult.OK;
            });

            new AddResourceDialog(resources.messages().addResourceTitle(title), form,
                    (name, model) -> addPrincipal(type, name, model)).show();
        }));

        addColumnAction(columnActionFactory.refresh(Ids.build(id, "refresh"),
                column -> accessControl.reload(() -> refresh(RESTORE_SELECTION))));

        setItemsProvider((context, callback) -> {
            Set<Principal> principals = type == Principal.Type.USER ? accessControl.principals()
                    .users() : accessControl.principals().groups();
            callback.onSuccess(principals.stream().sorted(comparing(Principal::getName)).collect(toList()));
        });

        setItemRenderer(item -> new ItemDisplay<Principal>() {
            @Override
            public String getId() {
                return item.getId();
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement element() {
                if (item.getRealm() != null) {
                    return ItemDisplay.withSubtitle(item.getName(), item.getRealm());
                }
                return null;
            }

            @Override
            public String getFilterData() {
                return String.join(" ", filterData(item));
            }

            @Override
            public List<ItemAction<Principal>> actions() {
                SafeHtml question = item.getType() == Principal.Type.USER ? resources.messages()
                        .removeUserQuestion(item.getName()) : resources.messages().removeGroupQuestion(item.getName());
                SafeHtml success = item.getType() == Principal.Type.USER ? resources.messages()
                        .removeUserSuccess(item.getName()) : resources.messages().removeGroupSuccess(item.getName());
                return singletonList(new ItemAction.Builder<Principal>()
                        .title(resources.constants().remove())
                        .handler(itm -> {
                            if (type == Principal.Type.USER && itm.getName().equals(currentUser.getName())) {
                                MessageEvent.fire(eventBus,
                                        Message.error(resources.messages().removeCurrentUserError()));
                            } else {
                                DialogFactory.showConfirmation(
                                        resources.messages().removeConfirmationTitle(title), question,
                                        () -> {
                                            List<Operation> operations = accessControl.assignments().byPrincipal(item)
                                                    .map(assignment -> new Operation.Builder(
                                                            AddressTemplates.assignment(assignment), REMOVE
                                                    ).build())
                                                    .collect(toList());
                                            if (!operations.isEmpty()) {
                                                dispatcher.execute(new Composite(operations),
                                                        (CompositeResult result) -> {
                                                            MessageEvent.fire(eventBus, Message.success(success));
                                                            accessControl.reload(() -> refresh(CLEAR_SELECTION));
                                                        });
                                            }
                                        });
                            }
                        })
                        .build());
            }

            @Override
            public String nextColumn() {
                return Ids.ASSIGNMENT;
            }
        });
    }

    private void addPrincipal(Principal.Type type, String name, ModelNode model) {
        List<Task<FlowContext>> tasks = new ArrayList<>();
        collectTasks(tasks, type, name, true, model, INCLUDE);
        collectTasks(tasks, type, name, false, model, EXCLUDE);
        if (!tasks.isEmpty()) {
            series(new FlowContext(progress.get()), tasks)
                    .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                        @Override
                        public void onSuccess(FlowContext context) {
                            String typeName = type == Principal.Type.USER
                                    ? resources.constants().user()
                                    : resources.constants().group();
                            MessageEvent.fire(eventBus, Message.success(resources.messages()
                                    .addResourceSuccess(typeName, name)));
                            accessControl.reload(() -> refresh(Ids.principal(type.name().toLowerCase(), name)));
                        }
                    });
        }
    }

    private void collectTasks(List<Task<FlowContext>> tasks, Principal.Type type, String name,
            boolean include, ModelNode modelNode, String attribute) {
        String realm = modelNode.hasDefined(REALM) ? modelNode.get(REALM).asString() : null;
        String resourceName = Principal.buildResourceName(type, name, realm);
        Principal principal = new Principal(type, resourceName, name, realm);

        if (modelNode.hasDefined(attribute)) {
            modelNode.get(attribute).asList().stream()
                    .map(nameNode -> accessControl.roles().get(Ids.role(nameNode.asString())))
                    .forEach(role -> {
                        if (role != null) {
                            tasks.add(new CheckRoleMapping(dispatcher, role));
                            tasks.add(new AddRoleMapping(dispatcher, role, status -> status == 404));
                            tasks.add(new AddAssignment(dispatcher, role, principal, include));
                        }
                    });
        }
    }
}
