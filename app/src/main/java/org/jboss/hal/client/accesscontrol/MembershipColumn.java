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
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * Shows the members (principals) of the selected role (the reverse of the {@link AssignmentColumn}.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.MEMBERSHIP)
public class MembershipColumn extends FinderColumn<Assignment> {

    @Inject
    public MembershipColumn(final Finder finder, final AccessControl accessControl, final AccessControlTokens tokens,
            final Resources resources) {
        super(new Builder<Assignment>(finder, Ids.MEMBERSHIP, resources.constants().membership())

                .itemsProvider((context, callback) -> {
                    List<Assignment> assignments = new ArrayList<>();
                    Optional<String> optional = StreamSupport
                            .stream(finder.getContext().getPath().spliterator(), false)
                            .filter(segment -> Ids.ROLE.equals(segment.getColumnId()))
                            .findFirst()
                            .map(FinderSegment::getItemId);

                    optional.ifPresent(roleId -> {
                        Role role = accessControl.roles().get(roleId);
                        if (role != null) {
                            accessControl.assignments().byRole(role)
                                    .sorted(Assignments.USERS_FIRST.thenComparing(Assignments.BY_PRINCIPAL_NAME))
                                    .forEach(assignments::add);
                        }
                    });

                    callback.onSuccess(assignments);
                })

                .itemRenderer(item -> new ItemDisplay<Assignment>() {
                    @Override
                    public String getId() {
                        return item.getPrincipal().getId();
                    }

                    @Override
                    public String getTitle() {
                        return item.getPrincipal().getName();
                    }

                    @Override
                    public Element asElement() {
                        if (item.getPrincipal().getRealm() != null) {
                            return ItemDisplay
                                    .withSubtitle(item.getPrincipal().getName(), item.getPrincipal().getRealm());
                        }
                        return null;
                    }

                    @Override
                    public String getTooltip() {
                        return item.isInclude() ? resources.constants().includes() : resources.constants().excludes();
                    }

                    @Override
                    public Element getIcon() {
                        Element icon = Browser.getDocument().createSpanElement();
                        if (item.isInclude()) {
                            icon.setClassName(fontAwesome("plus"));
                        } else {
                            icon.setClassName(fontAwesome("minus"));
                        }
                        return icon;
                    }

                    @Override
                    public String getFilterData() {
                        List<String> data = new ArrayList<>(PrincipalColumn.filterData(item.getPrincipal()));
                        data.add(item.isInclude() ? "includes" : "excludes"); //NON-NLS
                        return String.join(" ", data);
                    }
                })

                .withFilter()
                .onPreview(item -> new MembershipPreview(tokens, item.getPrincipal(), resources))
        );
    }
}
