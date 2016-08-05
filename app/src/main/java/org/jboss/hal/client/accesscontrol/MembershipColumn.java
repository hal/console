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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * Shows the members (principals) of the selected role (the reverse of the {@link AssignmentColumn}.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.MEMBERSHIP)
public class MembershipColumn extends FinderColumn<Assignment> {

    private final Resources resources;

    @Inject
    public MembershipColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final AccessControl accessControl,
            final AccessControlTokens tokens,
            final Resources resources) {
        super(new Builder<Assignment>(finder, Ids.MEMBERSHIP, resources.constants().membership())

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

                    @Override
                    public List<ItemAction<Assignment>> actions() {
                        return singletonList(itemActionFactory.remove(resources.constants().membership(),
                                item.getPrincipal().getName(), itm -> Browser.getWindow().alert(Names.NYI)));
                    }
                })

                .withFilter()
                .onPreview(item -> new MembershipPreview(tokens, item.getPrincipal(), resources))
        );
        this.resources = resources;

        setItemsProvider((context, callback) -> {
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

            // Show / hide the assigned roles from the include / exclude drop-downs
            // The id is on the <a> element, but we need to show / hide the parent <li> element
            Set<String> includeIds = assignments.stream()
                    .map(assignment -> includeId(assignment.getPrincipal()))
                    .collect(Collectors.toSet());
            Set<String> excludeIds = assignments.stream()
                    .map(assignment -> excludeId(assignment.getPrincipal()))
                    .collect(Collectors.toSet());
            Elements.stream(Browser.getDocument().querySelectorAll(
                    "[aria-" + UIConstants.LABELLED_BY + "=" + Ids.MEMBERSHIP_INCLUDE + "] > li > a")) //NON-NLS
                    .forEach(element -> Elements.setVisible(element.getParentElement(),
                            !includeIds.contains(element.getId())));
            Elements.stream(Browser.getDocument().querySelectorAll(
                    "[aria-" + UIConstants.LABELLED_BY + "=" + Ids.MEMBERSHIP_EXCLUDE + "] > li > a")) //NON-NLS
                    .forEach(element -> Elements.setVisible(element.getParentElement(),
                            !excludeIds.contains(element.getId())));

            callback.onSuccess(assignments);
        });

        // Setup column actions to include *all* principals.
        // Already included principals will be filtered out later in the ItemsProvider
        Element include = new Elements.Builder().span()
                .css(fontAwesome("plus"))
                .title(resources.constants().includeUserGroup())
                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                .data(UIConstants.PLACEMENT, "bottom")
                .end().build();
        List<ColumnAction<Assignment>> includeActions = new ArrayList<>();
        StreamSupport.stream(accessControl.principals().spliterator(), false)
                .sorted(comparing(Principal::getType)
                        .thenComparing(comparing(Principal::getName)))
                .forEach(principal -> includeActions.add(new ColumnAction<>(
                        includeId(principal), typeAndName(principal),
                        column -> Browser.getWindow().alert(Names.NYI))));
        addColumnActions(Ids.MEMBERSHIP_INCLUDE, include, includeActions);

        // Setup column actions to exclude *all* principals.
        // Already excluded principals will be filtered out later in the ItemsProvider
        Element exclude = new Elements.Builder().span()
                .css(fontAwesome("minus"))
                .title(resources.constants().excludeUserGroup())
                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                .data(UIConstants.PLACEMENT, "bottom")
                .end().build();
        List<ColumnAction<Assignment>> excludeActions = new ArrayList<>();
        StreamSupport.stream(accessControl.principals().spliterator(), false)
                .sorted(comparing(Principal::getType)
                        .thenComparing(comparing(Principal::getName)))
                .forEach(principal -> excludeActions.add(new ColumnAction<>(
                        excludeId(principal), typeAndName(principal),
                        column -> Browser.getWindow().alert(Names.NYI))));
        addColumnActions(Ids.MEMBERSHIP_EXCLUDE, exclude, excludeActions);
    }

    private String includeId(Principal principal) {
        return Ids.build(Ids.MEMBERSHIP_INCLUDE, principal.getType().name().toLowerCase(), principal.getName());
    }

    private String excludeId(Principal principal) {
        return Ids.build(Ids.MEMBERSHIP_EXCLUDE, principal.getType().name().toLowerCase(), principal.getName());
    }

    private String typeAndName(Principal principal) {
        if (principal.getType() == Principal.Type.USER) {
            return resources.constants().user() + " " + principal.getName();
        } else {
            return resources.constants().group() + " " + principal.getName();
        }
    }
}
