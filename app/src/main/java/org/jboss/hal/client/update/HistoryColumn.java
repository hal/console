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

import org.jboss.hal.ballroom.Format;
import org.jboss.hal.client.update.wizard.UpdateWizard;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.update.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KIND;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_REVERT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RETURN_CODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;
import static org.jboss.hal.resources.CSS.pfIcon;

@Column(Ids.UPDATE_HISTORY)
public class HistoryColumn extends FinderColumn<HistoryItem> {

    private static final RegExp HISTORY_REGEX = RegExp
            .compile("^\\[([a-z0-9]+)\\]\\s+([0-9-T:Z]+)\\s+-\\s+(install|update|rollback)$"); // NON-NLS

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Progress progress;

    @Inject
    public HistoryColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final Resources resources,
            @Footer final Progress progress) {

        super(new Builder<HistoryItem>(finder, Ids.UPDATE_HISTORY, Names.HISTORY)
                .onPreview(item -> new HistoryPreview(item, dispatcher, statementContext, resources))
                .showCount()
                .withFilter());

        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.progress = progress;

        setItemsProvider(context -> {
            ResourceAddress address = INSTALLER_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, HISTORY).build();
            return dispatcher.execute(operation).then(result -> {
                List<HistoryItem> nodes = result.asList().stream()
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
                            return new HistoryItem(revision, payload);
                        })
                        .sorted(Comparator.comparing((HistoryItem node) -> {
                            Date date = ModelNodeHelper.failSafeDate(node, DATE);
                            return date == null ? new Date() : date;
                        }).reversed())
                        .collect(toList());
                return Promise.resolve(nodes);
            });
        });

        setItemRenderer(item -> new ItemDisplay<HistoryItem>() {
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
                switch (item.getKind()) {
                    case INSTALL:
                        return Icons.custom(pfIcon("bundle"));
                    case UPDATE:
                        return Icons.custom(pfIcon("build"));
                    case ROLLBACK:
                        return Icons.custom(pfIcon("restart"));
                    case UNDEFINED:
                    default:
                        return Icons.unknown();
                }
            }

            @Override
            public HTMLElement element() {
                return ItemDisplay.withSubtitle(item.getName(), Format.mediumDateTime(item.getDate()));
            }

            @Override
            public List<ItemAction<HistoryItem>> actions() {
                List<ItemAction<HistoryItem>> actions = new ArrayList<>();
                if (item.getKind() == HistoryItem.Kind.UPDATE || item.getKind() == HistoryItem.Kind.ROLLBACK) {
                    actions.add(new ItemAction.Builder<HistoryItem>()
                            .title(resources.constants().revert())
                            .handler(item1 -> revert(item1))
                            .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_REVERT))
                            .build());
                }
                return actions;
            }
        });

        List<ColumnAction<HistoryItem>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<HistoryItem>(Ids.UPDATE_HISTORY_ADD_ONLINE)
                .title(resources.constants().updateServer())
                .handler(column -> update())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addActions.add(new ColumnAction.Builder<HistoryItem>(Ids.UPDATE_HISTORY_ADD_OFFLINE)
                .title(resources.constants().uploadZip())
                .handler(column -> upload())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addColumnActions(Ids.UPDATE_HISTORY_ADD_ACTIONS, pfIcon("maintenance"), resources.constants().updateServer(),
                addActions);
        addColumnAction(columnActionFactory.refresh(Ids.UPDATE_HISTORY_REFRESH));
        addColumnAction(new ColumnAction.Builder<HistoryItem>(Ids.UPDATE_HISTORY_CLEAN)
                .element(columnActionFactory.addButton("Clean", "fa fa-eraser"))
                .handler(column -> clean())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, "clean"))
                .build());
    }

    private void update() {
        progress.reset();
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_UPDATES).build();
        dispatcher.execute(operation,
                result -> {
                    progress.finish();
                    int returnCode = result.get(RETURN_CODE).asInt();
                    if (returnCode == ReturnCodes.LIST_UPDATES_NO_UPDATES) {
                        MessageEvent.fire(eventBus, Message.info(resources.messages().noUpdates()));
                    } else if (returnCode == ReturnCodes.LIST_UPDATES_UPDATES) {
                        List<String> updates = result.get(UPDATES).asList().stream().map(ModelNode::asString).collect(toList());
                        startUpdate(updates);
                    } else {
                        MessageEvent.fire(eventBus, Message.error(resources.messages().unknownReturnCode(returnCode)));
                    }
                }, (op, error) -> progress.finish());
    }

    private void startUpdate(final List<String> updates) {
        new UpdateWizard(eventBus, dispatcher, statementContext, resources, updates).show();
    }

    private void upload() {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }

    private void clean() {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }

    private void revert(HistoryItem history) {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }
}
