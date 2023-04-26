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
package org.jboss.hal.client.installer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Format;
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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ARTIFACT_CHANGES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_REVERT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMESTAMP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;
import static org.jboss.hal.resources.CSS.pfIcon;

@Column(Ids.INSTALLER_UPDATE)
public class UpdateColumn extends FinderColumn<UpdateItem> {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private final Progress progress;

    @Inject
    public UpdateColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final ColumnActionFactory columnActionFactory,
            final Resources resources,
            @Footer final Progress progress) {

        super(new Builder<UpdateItem>(finder, Ids.INSTALLER_UPDATE, Names.UPDATES)
                .onPreview(item -> new UpdatePreview(item, dispatcher, statementContext, resources))
                .showCount()
                .withFilter()
                .filterDescription(resources.messages().updatesFilterDescription()));

        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;
        this.progress = progress;

        setItemsProvider(context -> {
            ResourceAddress address = INSTALLER_TEMPLATE.resolve(statementContext);
            Operation operation = new Operation.Builder(address, HISTORY).build();
            return dispatcher.execute(operation).then(result -> {
                List<UpdateItem> nodes = result.asList().stream()
                        .map(UpdateItem::new)
                        .sorted(comparing((UpdateItem node) -> {
                            Date date = ModelNodeHelper.failSafeDate(node, TIMESTAMP);
                            return date == null ? new Date() : date;
                        }).reversed())
                        .collect(toList());
                return Promise.resolve(nodes);
            });
        });

        setItemRenderer(item -> new ItemDisplay<UpdateItem>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getFilterData() {
                return getTitle() + " " + item.get(TYPE).asString();
            }

            @Override
            public String getTooltip() {
                return item.get(TYPE).asString();
            }

            @Override
            public HTMLElement getIcon() {
                switch (item.getUpdateKind()) {
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
                return ItemDisplay.withSubtitle(item.getName(), Format.mediumDateTime(item.getTimestamp()));
            }

            @Override
            public List<ItemAction<UpdateItem>> actions() {
                List<ItemAction<UpdateItem>> actions = new ArrayList<>();
                if (item.getUpdateKind() == UpdateItem.UpdateType.UPDATE) {
                    actions.add(new ItemAction.Builder<UpdateItem>()
                            .title(resources.constants().revert())
                            .handler(item1 -> revert(item1))
                            .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_REVERT))
                            .build());
                }
                return actions;
            }
        });

        List<ColumnAction<UpdateItem>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<UpdateItem>(Ids.INSTALLER_UPDATE_SERVER)
                .title(resources.constants().updateServer())
                .handler(column -> update())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addActions.add(new ColumnAction.Builder<UpdateItem>(Ids.INSTALLER_UPDATE_ZIP)
                .title(resources.constants().uploadZip())
                .handler(column -> upload())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addColumnActions(Ids.INSTALLER_UPDATE_ADD_ACTIONS, pfIcon("maintenance"), resources.constants().updateServer(),
                addActions);
        addColumnAction(columnActionFactory.refresh(Ids.INSTALLER_UPDATE_REFRESH));
        addColumnAction(new ColumnAction.Builder<UpdateItem>(Ids.INSTALLER_CLEAN)
                .element(columnActionFactory.addButton(resources.constants().clean(), "fa fa-eraser"))
                .handler(column -> clean())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, "clean"))
                .build());
    }

    private void update() {
        progress.reset();
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_UPDATES).build();
        dispatcher.execute(operation,
                result -> {
                    List<ModelNode> updates = result.get(UPDATES).asList();
                    if (updates.isEmpty()) {
                        MessageEvent.fire(eventBus, Message.info(resources.messages().noUpdates()));
                    } else {
                        startUpdate(updates);
                    }
                    progress.finish();
                }, (op, error) -> {
                    progress.finish();
                    MessageEvent.fire(eventBus, Message.error(resources.messages().lastOperationFailed()));
                });
    }

    private void startUpdate(final List<ModelNode> updates) {
        new UpdateWizard(eventBus, dispatcher, statementContext, resources, updates).show(this);
    }

    private void upload() {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }

    private void clean() {
        MessageEvent.fire(eventBus, Message.error(SafeHtmlUtils.fromSafeConstant(Names.NYI)));
    }

    private void revert(UpdateItem updateItem) {
        Operation operation = new Operation.Builder(AddressTemplates.INSTALLER_TEMPLATE.resolve(statementContext), HISTORY)
                .param(REVISION, updateItem.getName())
                .build();
        dispatcher.execute(operation,
                result -> {
                    List<ModelNode> updates = result.get(ARTIFACT_CHANGES).asList();
                    if (updates.isEmpty()) {
                        MessageEvent.fire(eventBus, Message.warning(resources.messages().noUpdates()));
                    } else {
                        startRevert(updateItem, updates);
                    }
                }, (op, error) -> MessageEvent.fire(eventBus, Message.error(resources.messages().lastOperationFailed())));
    }

    private void startRevert(UpdateItem updateItem, List<ModelNode> updates) {
        new RevertWizard(eventBus, dispatcher, statementContext, resources, updateItem, updates).show(this);
    }
}