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

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Collections.singletonList;

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

    PrincipalColumn(final Finder finder, final String id, final String title,
            final ColumnActionFactory columnActionFactory,
            final AccessControl accessControl, final AccessControlTokens tokens,
            final Resources resources,  final List<Principal> principals) {
        super(new Builder<Principal>(finder, id, title)

                .itemsProvider((context, callback) -> callback.onSuccess(principals))

                .itemRenderer(item -> new ItemDisplay<Principal>() {
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
                        return singletonList(new ItemAction<>(resources.constants().remove(),
                                itm -> DialogFactory.confirmation(
                                        resources.messages().removeResourceConfirmationTitle(itm.getName()),
                                        resources.messages().removeResourceConfirmationQuestion(itm.getName()),
                                        () -> {
                                            Browser.getWindow().alert(Names.NYI);
                                            return true;
                                        }).show()
                        ));
                    }

                    @Override
                    public String nextColumn() {
                        return Ids.ASSIGNMENT;
                    }
                })

                .onPreview(item -> new PrincipalPreview(accessControl, tokens, item, resources))
                .showCount()
                .withFilter()
        );

        addColumnAction(columnActionFactory.refresh(Ids.build(id, "refresh"),
                column -> accessControl.reload(() -> refresh(RefreshMode.RESTORE_SELECTION))));
    }
}
