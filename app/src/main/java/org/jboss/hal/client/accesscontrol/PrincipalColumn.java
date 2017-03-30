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
import elemental.dom.Element;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.autocomplete.StaticAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.AddAssignment;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.AddRoleMapping;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.CheckRoleMapping;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.User;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.SuccessfulOutcome;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
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

    PrincipalColumn(final Finder finder,
            final String id,
            final String title,
            final Principal.Type type,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final Provider<Progress> progress,
            final User currentUser,
            final AccessControl accessControl,
            final AccessControlTokens tokens,
            final AccessControlResources accessControlResources,
            final Resources resources) {

        super(new Builder<Principal>(finder, id, title)
                .onPreview(item -> new PrincipalPreview(accessControl, tokens, item, resources))
                .showCount()
                .withFilter());
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.accessControl = accessControl;
        this.resources = resources;

        addColumnAction(columnActionFactory.add(Ids.ROLE_ADD, title, column -> {
            Metadata metadata = Metadata.staticDescription(accessControlResources.principal());
            Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(id, Ids.FORM_SUFFIX), metadata)
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
            public Element asElement() {
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
                return singletonList(new ItemAction<Principal>(resources.constants().remove(), itm -> {
                    if (type == Principal.Type.USER && itm.getName().equals(currentUser.getName())) {
                        MessageEvent.fire(eventBus, Message.error(resources.messages().removeCurrentUserError()));
                    } else {
                        DialogFactory.showConfirmation(
                                resources.messages().removeConfirmationTitle(title), question,
                                () -> {
                                    List<Operation> operations = accessControl.assignments().byPrincipal(item)
                                            .map(assignment -> new Operation.Builder(REMOVE,
                                                    AddressTemplates.assignment(assignment)).build())
                                            .collect(toList());
                                    dispatcher.execute(new Composite(operations), (CompositeResult result) -> {
                                        MessageEvent.fire(eventBus, Message.success(success));
                                        accessControl.reload(() -> refresh(CLEAR_SELECTION));
                                    });
                                });
                    }
                }));
            }

            @Override
            public String nextColumn() {
                return Ids.ASSIGNMENT;
            }
        });
    }

    private void addPrincipal(final Principal.Type type, final String name, final ModelNode model) {
        List<Function<FunctionContext>> functions = new ArrayList<>();
        collectFunctions(functions, type, name, true, model, INCLUDE);
        collectFunctions(functions, type, name, false, model, EXCLUDE);
        if (!functions.isEmpty()) {
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new SuccessfulOutcome(eventBus, resources) {
                        @Override
                        public void onSuccess(final FunctionContext context) {
                            String typeName = type == Principal.Type.USER
                                    ? resources.constants().user()
                                    : resources.constants().group();
                            MessageEvent.fire(eventBus, Message.success(resources.messages()
                                    .addResourceSuccess(typeName, name)));
                            accessControl.reload(() -> refresh(Ids.principal(type.name().toLowerCase(), name)));
                        }
                    },
                    functions.toArray(new Function[functions.size()]));
        }
    }

    private void collectFunctions(List<Function<FunctionContext>> functions, Principal.Type type, String name,
            boolean include, ModelNode modelNode, String attribute) {
        String realm = modelNode.hasDefined(REALM) ? modelNode.get(REALM).asString() : null;
        String resourceName = Principal.buildResourceName(type, name, realm);
        Principal principal = new Principal(type, resourceName, name, realm);

        if (modelNode.hasDefined(attribute)) {
            modelNode.get(attribute).asList().stream()
                    .map(nameNode -> accessControl.roles().get(Ids.role(nameNode.asString())))
                    .forEach(role -> {
                        if (role != null) {
                            functions.add(new CheckRoleMapping(dispatcher, role));
                            functions.add(new AddRoleMapping(dispatcher, role, status -> status == 404));
                            functions.add(new AddAssignment(dispatcher, role, principal, include));
                        }
                    });
        }
    }
}
