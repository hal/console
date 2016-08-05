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
 * Shows the assigned roles of the selected principal (the reverse of the {@link MembershipColumn}.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.ASSIGNMENT)
public class AssignmentColumn extends FinderColumn<Assignment> {

    @Inject
    public AssignmentColumn(final Finder finder,
            final ItemActionFactory itemActionFactory,
            final AccessControl accessControl,
            final AccessControlTokens tokens,
            final Resources resources) {
        super(new Builder<Assignment>(finder, Ids.ASSIGNMENT, resources.constants().assignment())

                .itemRenderer(item -> new ItemDisplay<Assignment>() {
                    @Override
                    public String getId() {
                        return item.getRole().getId();
                    }

                    @Override
                    public String getTitle() {
                        return item.getRole().getName();
                    }

                    @Override
                    public Element asElement() {
                        if (item.getRole().isScoped()) {
                            return ItemDisplay.withSubtitle(item.getRole().getName(),
                                    item.getRole().getBaseRole().getName() + " / " +
                                            String.join(", ", item.getRole().getScope()));
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
                        List<String> data = new ArrayList<>(RoleColumn.filterData(item.getRole()));
                        data.add(item.isInclude() ? "includes" : "excludes"); //NON-NLS
                        return String.join(" ", data);
                    }

                    @Override
                    public List<ItemAction<Assignment>> actions() {
                        return singletonList(itemActionFactory.remove(resources.constants().assignment(),
                                item.getRole().getName(), itm -> Browser.getWindow().alert(Names.NYI)));
                    }
                })

                .withFilter()
                .onPreview(item -> new AssignmentPreview(tokens, item.getRole(), resources))
        );

        setItemsProvider((context, callback) -> {
            List<Assignment> assignments = new ArrayList<>();
            Optional<String> optional = StreamSupport
                    .stream(finder.getContext().getPath().spliterator(), false)
                    .filter(segment -> Ids.USER.equals(segment.getColumnId()) ||
                            Ids.GROUP.equals(segment.getColumnId()))
                    .findFirst()
                    .map(FinderSegment::getItemId);

            optional.ifPresent(resourceName -> {
                Principal principal = accessControl.principals().get(resourceName);
                if (principal != null) {
                    accessControl.assignments().byPrincipal(principal)
                            .sorted(Assignments.EXCLUDES_FIRST
                                    .thenComparing(Assignments.STANDARD_FIRST)
                                    .thenComparing(Assignments.BY_ROLE_NAME))
                            .forEach(assignments::add);
                }
            });

            // Show / hide the assigned roles from the include / exclude drop-downs
            // The id is on the <a> element, but we need to show / hide the parent <li> element
            Set<String> includeIds = assignments.stream()
                    .map(assignment -> includeId(assignment.getRole()))
                    .collect(Collectors.toSet());
            Set<String> excludeIds = assignments.stream()
                    .map(assignment -> excludeId(assignment.getRole()))
                    .collect(Collectors.toSet());
            Elements.stream(Browser.getDocument().querySelectorAll(
                    "[aria-" + UIConstants.LABELLED_BY + "=" + Ids.ASSIGNMENT_INCLUDE + "] > li > a")) //NON-NLS
                    .forEach(element -> Elements.setVisible(element.getParentElement(),
                            !includeIds.contains(element.getId())));
            Elements.stream(Browser.getDocument().querySelectorAll(
                    "[aria-" + UIConstants.LABELLED_BY + "=" + Ids.ASSIGNMENT_EXCLUDE + "] > li > a")) //NON-NLS
                    .forEach(element -> Elements.setVisible(element.getParentElement(),
                            !excludeIds.contains(element.getId())));

            callback.onSuccess(assignments);
        });

        // Setup column actions to include *all* roles.
        // Already included roles will be filtered out later in the ItemsProvider
        Element include = new Elements.Builder().span()
                .css(fontAwesome("plus"))
                .title(resources.constants().includeRole())
                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                .data(UIConstants.PLACEMENT, "bottom")
                .end().build();
        List<ColumnAction<Assignment>> includeActions = new ArrayList<>();
        StreamSupport.stream(accessControl.roles().spliterator(), false)
                .sorted(comparing(Role::getType)
                        .thenComparing(comparing(Role::getName)))
                .forEach(role -> includeActions.add(new ColumnAction<Assignment>(includeId(role), role.getName(),
                        column -> Browser.getWindow().alert(Names.NYI))));
        addColumnActions(Ids.ASSIGNMENT_INCLUDE, include, includeActions);

        // Setup column actions to exclude *all* roles.
        // Already excluded roles will be filtered out later in the ItemsProvider
        Element exclude = new Elements.Builder().span()
                .css(fontAwesome("minus"))
                .title(resources.constants().excludeRole())
                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                .data(UIConstants.PLACEMENT, "bottom")
                .end().build();
        List<ColumnAction<Assignment>> excludeActions = new ArrayList<>();
        StreamSupport.stream(accessControl.roles().spliterator(), false)
                .sorted(comparing(Role::getType)
                        .thenComparing(comparing(Role::getName)))
                .forEach(role -> excludeActions.add(new ColumnAction<Assignment>(excludeId(role), role.getName(),
                        column -> Browser.getWindow().alert(Names.NYI))));
        addColumnActions(Ids.ASSIGNMENT_EXCLUDE, exclude, excludeActions);
    }

    private String includeId(Role role) {
        return Ids.build(Ids.ASSIGNMENT_INCLUDE, role.getName());
    }

    private String excludeId(Role role) {
        return Ids.build(Ids.ASSIGNMENT_EXCLUDE, role.getName());
    }

}
