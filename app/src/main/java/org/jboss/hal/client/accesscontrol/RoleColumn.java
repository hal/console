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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Comparator.comparing;

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
    public RoleColumn(final Finder finder, final ColumnActionFactory columnActionFactory,
            final AccessControl accessControl, final AccessControlTokens tokens,
            final Environment environment, final Resources resources) {
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
                            actions.add(new ItemAction<>(resources.constants().remove(),
                                    itm -> DialogFactory.confirmation(
                                            resources.messages().removeResourceConfirmationTitle(itm.getName()),
                                            resources.messages().removeResourceConfirmationQuestion(itm.getName()),
                                            () -> {
                                                Browser.getWindow().alert(Names.NYI);
                                                return true;
                                            }).show()));
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

        addColumnAction(columnActionFactory.refresh(Ids.build(Ids.ROLE, "refresh"),
                column -> accessControl.reload(() -> refresh(RefreshMode.RESTORE_SELECTION))));
    }
}
