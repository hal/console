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
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.AddRoleMapping;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.AddScopedRole;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.CheckRoleMapping;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.ModifyIncludeAll;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.ModifyScopedRole;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.RemoveAssignments;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.RemoveRoleMapping;
import org.jboss.hal.client.accesscontrol.AccessControlFunctions.RemoveScopedRole;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Role;
import org.jboss.hal.config.RolesChangedEvent;
import org.jboss.hal.config.Settings;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.ModifyResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.SuccessfulOutcome;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.accesscontrol.AddressTemplates.*;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.ROLE)
@Requires({ROLE_MAPPING_ADDRESS, HOST_SCOPED_ROLE_ADDRESS, SERVER_GROUP_SCOPED_ROLE_ADDRESS})
public class RoleColumn extends FinderColumn<Role> {

    static List<String> filterData(Role role) {
        List<String> data = new ArrayList<>();
        data.add(role.getName());
        if (role.isScoped()) {
            data.add("scoped"); //NON-NLS
            data.add(role.getType().name().toLowerCase());
            data.add(String.join(" ", role.getScope()));
        } else {
            data.add("standard"); //NON-NLS
        }
        return data;
    }

    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Provider<Progress> progress;
    private final AccessControl accessControl;
    private final Resources resources;
    private final List<String> standardRoleNames;

    @Inject
    public RoleColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final @Footer Provider<Progress> progress,
            final AccessControl accessControl,
            final AccessControlTokens tokens,
            final Environment environment,
            final Settings settings,
            final Resources resources) {

        super(new Builder<Role>(finder, Ids.ROLE, resources.constants().role())
                .itemsProvider((context, callback) -> {
                    List<Role> roles = new ArrayList<>();
                    accessControl.roles().standardRoles().stream()
                            .sorted(comparing(Role::getName))
                            .forEach(roles::add);
                    if (!environment.isStandalone()) {
                        accessControl.roles().scopedRoles().stream()
                                .sorted(comparing(Role::getName))
                                .forEach(roles::add);
                    }
                    callback.onSuccess(roles);
                })
                .onPreview(item -> new RolePreview(accessControl, tokens, item, resources))
                .showCount()
                .withFilter()
        );

        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.accessControl = accessControl;
        this.resources = resources;
        this.standardRoleNames = accessControl.roles().standardRoles().stream()
                .sorted(comparing(Role::getName))
                .map(Role::getName)
                .collect(toList());

        if (!environment.isStandalone()) {
            List<ColumnAction<Role>> actions = new ArrayList<>();
            actions.add(new ColumnAction.Builder<Role>(Ids.ROLE_HOST_SCOPED_ADD)
                    .title(resources.constants().hostScopedRole())
                    .handler(column -> addScopedRole(Role.Type.HOST, resources.constants().hostScopedRole(),
                            HOST_SCOPED_ROLE_TEMPLATE, AddressTemplate.of("/host=*"),
                            Ids.ROLE_HOST_SCOPED_FORM, HOSTS))
                    .build());
            actions.add(new ColumnAction.Builder<Role>(Ids.ROLE_SERVER_GROUP_SCOPED_ADD)
                    .title(resources.constants().serverGroupScopedRole())
                    .handler(column -> addScopedRole(Role.Type.SERVER_GROUP,
                            resources.constants().serverGroupScopedRole(),
                            SERVER_GROUP_SCOPED_ROLE_TEMPLATE, AddressTemplate.of("/server-group=*"),
                            Ids.ROLE_SERVER_GROUP_SCOPED_FORM, SERVER_GROUPS))
                    .build());
            addColumnActions(Ids.ROLE_ADD, pfIcon("add-circle-o"), resources.constants().add(), actions);
        }
        addColumnAction(columnActionFactory.refresh(Ids.ROLE_REFRESH,
                column -> accessControl.reload(() -> refresh(RefreshMode.RESTORE_SELECTION))));

        setItemRenderer(item -> new ItemDisplay<Role>() {
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
                if (item.isIncludeAll() && item.isScoped()) {
                    String scopedInfo = item.getBaseRole().getName() + " / " + String.join(", ", item.getScope());
                    return new Elements.Builder()
                            .span().css(itemText)
                            .span().textContent(item.getName()).end()
                            .start("small").css(subtitle).title(scopedInfo)
                            .innerHtml(new SafeHtmlBuilder()
                                    .appendEscaped(resources.constants().includesAll())
                                    .appendHtmlConstant("<br/>") //NON-NLS
                                    .appendEscaped(scopedInfo)
                                    .toSafeHtml())
                            .end()
                            .end().build();

                } else if (item.isIncludeAll()) {
                    return ItemDisplay.withSubtitle(item.getName(), resources.constants().includesAll());

                } else if (item.isScoped()) {
                    return ItemDisplay.withSubtitle(item.getName(),
                            item.getBaseRole().getName() + " / " + String.join(", ", item.getScope()));
                }
                return null;
            }

            @Override
            public String getFilterData() {
                return String.join(" ", filterData(item));
            }

            @Override
            public List<ItemAction<Role>> actions() {
                List<ItemAction<Role>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<Role>()
                        .title(resources.constants().edit())
                        .handler(itm -> {
                            switch (itm.getType()) {
                                case STANDARD:
                                    editStandardRole(itm);
                                    break;
                                case HOST:
                                    editScopedRole(itm, resources.constants().hostScopedRole(),
                                            HOST_SCOPED_ROLE_TEMPLATE, AddressTemplate.of("/host=*"),
                                            Ids.ROLE_HOST_SCOPED_FORM, HOSTS);
                                    break;
                                case SERVER_GROUP:
                                    editScopedRole(itm, resources.constants().serverGroupScopedRole(),
                                            SERVER_GROUP_SCOPED_ROLE_TEMPLATE, AddressTemplate.of("/server-group=*"),
                                            Ids.ROLE_SERVER_GROUP_SCOPED_FORM, SERVER_GROUPS);
                                    break;
                            }
                        })
                        .build());
                if (item.isScoped()) {
                    String type = item.getType() == Role.Type.HOST
                            ? resources.constants().hostScopedRole()
                            : resources.constants().serverGroupScopedRole();
                    actions.add(new ItemAction.Builder<Role>()
                            .title(resources.constants().remove())
                            .handler(itm -> {
                                if (settings.get(RUN_AS).asSet().contains(itm.getName())) {
                                    MessageEvent.fire(eventBus,
                                            Message.error(resources.messages().removeRunAsRoleError(item.getName())));
                                } else {
                                    DialogFactory.showConfirmation(
                                            resources.messages().removeConfirmationTitle(type),
                                            resources.messages().removeRoleQuestion(itm.getName()),
                                            () -> removeScopedRole(itm, type));
                                }
                            })
                            .build());
                }
                return actions;
            }

            @Override
            public String nextColumn() {
                return Ids.MEMBERSHIP;
            }
        });
    }


    // ------------------------------------------------------ add roles

    @SuppressWarnings("ConstantConditions")
    private void addScopedRole(Role.Type type, String typeName, AddressTemplate template,
            AddressTemplate typeaheadTemplate, String formId, String scopeAttribute) {
        Metadata metadata = metadataRegistry.lookup(template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(formId, metadata)
                .addOnly()
                .unboundFormItem(new NameItem(), 0)
                .unboundFormItem(new SwitchItem(INCLUDE_ALL, new LabelBuilder().label(INCLUDE_ALL)), 3,
                        resources.messages().includeAllHelpText())
                .include(BASE_ROLE, scopeAttribute)
                .customFormItem(BASE_ROLE, attributeDescription -> {
                    SingleSelectBoxItem item = new SingleSelectBoxItem(BASE_ROLE,
                            new LabelBuilder().label(BASE_ROLE), standardRoleNames, false);
                    item.setRequired(true);
                    return item;
                })
                .build();
        form.getFormItem(scopeAttribute).setRequired(true);
        form.getFormItem(scopeAttribute)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, typeaheadTemplate));

        new AddResourceDialog(resources.messages().addResourceTitle(typeName), form, (name, model) -> {
            List<Function<FunctionContext>> functions = new ArrayList<>();
            functions.add(new AddScopedRole(dispatcher, type, name, model));
            if (model.hasDefined(INCLUDE_ALL) && model.get(INCLUDE_ALL).asBoolean()) {
                // We only need the role name in the functions,
                // so it's ok to setup a transient role w/o the other attributes.
                Role transientRole = new Role(name, null, type, null);
                functions.add(new CheckRoleMapping(dispatcher, transientRole));
                functions.add(new AddRoleMapping(dispatcher, transientRole, status -> status == 404));
                functions.add(new ModifyIncludeAll(dispatcher, transientRole, true));
            }
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new SuccessfulOutcome(eventBus, resources) {
                        @Override
                        public void onSuccess(final FunctionContext context) {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().addResourceSuccess(typeName, name)));
                            accessControl.reload(() -> {
                                refresh(Ids.role(name));
                                eventBus.fireEvent(new RolesChangedEvent());
                            });
                        }
                    },
                    functions.toArray(new Function[functions.size()]));
        }).show();
    }


    // ------------------------------------------------------ modify roles

    private void editStandardRole(final Role role) {
        Metadata metadata = metadataRegistry.lookup(ROLE_MAPPING_TEMPLATE);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.ROLE_MAPPING_FORM, metadata)
                .unboundFormItem(new NameItem(), 0)
                .include(INCLUDE_ALL)
                .build();
        form.getFormItem(NAME).setEnabled(false);
        form.getFormItem(NAME).setRequired(false);

        ModelNode modelNode = new ModelNode();
        modelNode.get(INCLUDE_ALL).set(role.isIncludeAll());
        new ModifyResourceDialog(resources.messages().modifyResourceTitle(resources.constants().role()),
                form, (frm, changedValues) ->
                new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                        new SuccessfulOutcome(eventBus, resources) {
                            @Override
                            public void onSuccess(final FunctionContext context) {
                                MessageEvent.fire(eventBus, Message.success(resources.messages()
                                        .modifyResourceSuccess(resources.constants().role(), role.getName())));
                                accessControl.reload(() -> {
                                    refresh(role.getId());
                                    eventBus.fireEvent(new RolesChangedEvent());
                                });
                            }
                        },
                        new CheckRoleMapping(dispatcher, role),
                        new AddRoleMapping(dispatcher, role, status -> status == 404),
                        new ModifyIncludeAll(dispatcher, role, frm.getModel().get(INCLUDE_ALL).asBoolean())))
                .show(modelNode);
        form.getFormItem(NAME).setValue(role.getName());
    }

    private void editScopedRole(Role role, String type, AddressTemplate template, AddressTemplate typeaheadTemplate,
            String formId, String scopeAttribute) {
        Metadata metadata = metadataRegistry.lookup(template);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(formId, metadata)
                .include(BASE_ROLE, scopeAttribute)
                .customFormItem(BASE_ROLE, attributeDescription -> {
                    SingleSelectBoxItem item = new SingleSelectBoxItem(BASE_ROLE,
                            new LabelBuilder().label(BASE_ROLE), standardRoleNames, false);
                    item.setRequired(true);
                    return item;
                })
                .unboundFormItem(new SwitchItem(INCLUDE_ALL, new LabelBuilder().label(INCLUDE_ALL)), 2,
                        resources.messages().includeAllHelpText())
                .build();
        form.getFormItem(scopeAttribute).setRequired(true);
        form.getFormItem(scopeAttribute)
                .registerSuggestHandler(new ReadChildrenAutoComplete(dispatcher, statementContext, typeaheadTemplate));
        form.getFormItem(INCLUDE_ALL).setValue(role.isIncludeAll());

        ModelNode modelNode = new ModelNode();
        modelNode.get(BASE_ROLE).set(role.getBaseRole().getName());
        role.getScope().forEach(scope -> modelNode.get(scopeAttribute).add(scope));
        new ModifyResourceDialog(resources.messages().modifyResourceTitle(type), form, (frm, changedValues) -> {
            boolean hasIncludesAll = changedValues.containsKey(INCLUDE_ALL);
            boolean includesAll = (boolean) changedValues.getOrDefault(INCLUDE_ALL, false);
            changedValues.remove(INCLUDE_ALL); // must not be in changedValues when calling ModifyScopedRole

            List<Function<FunctionContext>> functions = new ArrayList<>();
            functions.add(new ModifyScopedRole(dispatcher, role, changedValues, metadata));
            if (hasIncludesAll) {
                functions.add(new CheckRoleMapping(dispatcher, role));
                functions.add(new AddRoleMapping(dispatcher, role, status -> status == 404));
                functions.add(new ModifyIncludeAll(dispatcher, role, includesAll));
            }
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                    new SuccessfulOutcome(eventBus, resources) {
                        @Override
                        public void onSuccess(final FunctionContext context) {
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().modifyResourceSuccess(type, role.getName())));
                            accessControl.reload(() -> {
                                refresh(role.getId());
                                eventBus.fireEvent(new RolesChangedEvent());
                            });
                        }
                    },
                    functions.toArray(new Function[functions.size()]));
        }).show(modelNode);
    }


    // ------------------------------------------------------ remove roles

    private void removeScopedRole(Role role, final String type) {
        List<Function<FunctionContext>> functions = new ArrayList<>();
        List<Assignment> assignments = accessControl.assignments().byRole(role).collect(toList());
        if (!assignments.isEmpty()) {
            functions.add(new RemoveAssignments(dispatcher, assignments));
        }
        functions.add(new CheckRoleMapping(dispatcher, role));
        functions.add(new RemoveRoleMapping(dispatcher, role, status -> status == 200));
        functions.add(new RemoveScopedRole(dispatcher, role));

        new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(),
                new SuccessfulOutcome(eventBus, resources) {
                    @Override
                    public void onSuccess(final FunctionContext context) {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().removeResourceSuccess(type, role.getName())));
                        accessControl.reload(() -> {
                            refresh(RefreshMode.CLEAR_SELECTION);
                            eventBus.fireEvent(new RolesChangedEvent());
                        });
                    }
                },
                functions.toArray(new Function[functions.size()]));
    }
}
