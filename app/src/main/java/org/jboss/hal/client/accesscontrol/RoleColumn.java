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

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Comparator.comparing;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
@AsyncColumn(Ids.ROLE)
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

    @Inject
    public RoleColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final AccessControl accessControl,
            final AccessControlTokens tokens,
            final Environment environment,
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

                .itemRenderer(item -> new ItemDisplay<Role>() {
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
                        if (item.isScoped()) {
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
                        actions.add(new ItemAction<>(resources.constants().edit(),
                                itm -> Browser.getWindow().alert(Names.NYI)));
                        if (item.isScoped()) {
                            actions.add(itemActionFactory.remove(resources.constants().role(), item.getName(),
                                    itm -> Browser.getWindow().alert(Names.NYI)));
                        }
                        return actions;
                    }

                    @Override
                    public String nextColumn() {
                        return Ids.MEMBERSHIP;
                    }
                })

                .onPreview(item -> new RolePreview(accessControl, tokens, item, resources))
                .showCount()
                .withFilter()
        );

        if (!environment.isStandalone()) {
            // add host scoped role (hsr)
            AddressTemplate hsrTemplate = AddressTemplate
                    .of("/core-service=management/access=authorization/host-scoped-role=*");
            Metadata hsrMeta = metadataRegistry.lookup(hsrTemplate);
            AddResourceDialog hsrDialog = new AddResourceDialog(Ids.ROLE_HOST_SCOPED_FORM,
                    resources.messages().addResourceTitle(resources.constants().hostScopedRole()),
                    hsrMeta, (name, model) -> {
                ResourceAddress address = hsrTemplate.resolve(statementContext, name);
                Operation operation = new Operation.Builder(ADD, address)
                        .payload(model)
                        .build();
                dispatcher.execute(operation, result -> {
                    MessageEvent.fire(eventBus, Message.success(
                            resources.messages().addResourceSuccess(resources.constants().hostScopedRole(), name)));
                    refresh(name);
                });
            });
            ColumnAction<Role> hsrAction = new ColumnAction<>(Ids.ROLE_HOST_SCOPED_ADD,
                    resources.constants().hostScopedRole(), column -> hsrDialog.show());

            // add server group scoped role (sgsr)
            AddressTemplate sgsrTemplate = AddressTemplate
                    .of("/core-service=management/access=authorization/server-group-scoped-role=*");
            Metadata sgsrMeta = metadataRegistry.lookup(sgsrTemplate);
            AddResourceDialog sgsrDialog = new AddResourceDialog(Ids.ROLE_SERVER_GROUP_SCOPED_FORM,
                    resources.messages().addResourceTitle(resources.constants().hostScopedRole()),
                    sgsrMeta, (name, model) -> {
                ResourceAddress address = sgsrTemplate.resolve(statementContext, name);
                Operation operation = new Operation.Builder(ADD, address)
                        .payload(model)
                        .build();
                dispatcher.execute(operation, result -> {
                    MessageEvent.fire(eventBus, Message.success(resources.messages()
                            .addResourceSuccess(resources.constants().serverGroupScopedRole(), name)));
                    refresh(name);
                });
            });
            ColumnAction<Role> sgsrAction = new ColumnAction<>(Ids.ROLE_SERVER_GROUP_SCOPED_ADD,
                    resources.constants().serverGroupScopedRole(), column -> sgsrDialog.show());

            List<ColumnAction<Role>> actions = new ArrayList<>();
            actions.add(hsrAction);
            actions.add(sgsrAction);
            addColumnActions(Ids.ROLE_ADD, pfIcon("add-circle-o"), resources.constants().add(), actions);
        }
        addColumnAction(columnActionFactory.refresh(Ids.ROLE_REFRESH,
                column -> accessControl.reload(() -> refresh(RefreshMode.RESTORE_SELECTION))));
    }
}
