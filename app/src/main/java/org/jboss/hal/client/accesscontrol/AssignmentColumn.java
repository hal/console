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
 * Shows the assigned roles of the selected principal (the reverse of the {@link MembershipColumn}.
 *
 * @author Harald Pehl
 */
@AsyncColumn(Ids.ASSIGNMENT)
public class AssignmentColumn extends FinderColumn<Assignment> {

    @Inject
    public AssignmentColumn(final Finder finder, final AccessControl accessControl, final AccessControlTokens tokens,
            final Resources resources) {
        super(new Builder<Assignment>(finder, Ids.ASSIGNMENT, resources.constants().assignment())

                .itemsProvider((context, callback) -> {
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

                    callback.onSuccess(assignments);
                })

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
                })

                .withFilter()
                .onPreview(item -> new AssignmentPreview(tokens, item.getRole(), resources))
        );
    }
}
