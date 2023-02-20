/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.update;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Format;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.update.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KIND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_REVERT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;
import static org.jboss.hal.resources.CSS.pfIcon;

@Column(Ids.UPDATE_HISTORY)
public class HistoryColumn extends FinderColumn<NamedNode> {

    private static final RegExp HISTORY_REGEX = RegExp.compile("^\\[([a-z0-9]+)\\]\\s+([0-9-T:Z]+)\\s+-\\s+(install|update)$"); // NON-NLS

    private final EventBus eventBus;

    @Inject
    public HistoryColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final Resources resources) {
        super(new Builder<NamedNode>(finder, Ids.UPDATE_HISTORY, Names.HISTORY)
                .columnAction(columnActionFactory.refresh(Ids.UPDATE_HISTORY_REFRESH))
                .onPreview(item -> new HistoryPreview(item, dispatcher, statementContext, resources))
                .showCount()
                .withFilter());
        this.eventBus = eventBus;

        setItemsProvider(context -> {
            ResourceAddress address = INSTALLER_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, HISTORY).build();
            return dispatcher.execute(operation).then(result -> {
                List<NamedNode> nodes = result.asList().stream()
                        .map(ModelNode::asString)
                        .map(HISTORY_REGEX::exec)
                        .filter(matchResult -> matchResult.getGroupCount() == 4)
                        .map(matchResult -> {
                            String revision = matchResult.getGroup(1);
                            String date = matchResult.getGroup(2);
                            String kind = matchResult.getGroup(3);
                            ModelNode payload = new ModelNode();
                            payload.get(REVISION).set(revision);
                            payload.get(DATE).set(date);
                            payload.get(KIND).set(kind);
                            return new NamedNode(revision, payload);
                        })
                        .sorted(Comparator.comparing(node -> {
                            Date date = ModelNodeHelper.failSafeDate(node, DATE);
                            return date == null ? new Date() : date;
                        }))
                        .collect(toList());
                return Promise.resolve(nodes);
            });
        });

        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getFilterData() {
                return getTitle() + " " + item.get(KIND).asString();
            }

            @Override
            public String getTooltip() {
                return item.get(KIND).asString();
            }

            @Override
            public HTMLElement getIcon() {
                switch (item.get(KIND).asString()) {
                    case "install":
                        return Icons.custom(pfIcon("bundle"));
                    case "update":
                        return Icons.custom(pfIcon("build"));
                    default:
                        return Icons.unknown();
                }
            }

            @Override
            public HTMLElement element() {
                Date date = ModelNodeHelper.failSafeDate(item, DATE);
                return ItemDisplay.withSubtitle(item.getName(), Format.mediumDateTime(date));
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                if ("update".equals(item.get(KIND).asString())) {
                    actions.add(new ItemAction.Builder<NamedNode>()
                            .title(resources.constants().revert())
                            .handler(item1 -> revert(item1.getName()))
                            .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_REVERT))
                            .build());
                }
                return actions;
            }
        });
    }

    private void revert(String revision) {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }
}
