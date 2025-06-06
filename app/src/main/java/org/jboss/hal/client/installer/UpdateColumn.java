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

import org.jboss.elemento.HtmlContentBuilder;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.dialog.Dialog;
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
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.HTMLElement;
import elemental2.promise.Promise;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.small;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_ADDRESS;
import static org.jboss.hal.client.installer.AddressTemplates.INSTALLER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ARTIFACT_CHANGES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_INFO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HISTORY_FROM_REVISION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LIST_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_REVERT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREPARE_UPDATES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REVISION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TIMESTAMP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNACCEPTED_CERTIFICATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATES;
import static org.jboss.hal.flow.Flow.sequential;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.pfIcon;

@AsyncColumn(Ids.UPDATE_MANAGER_UPDATE)
@Requires(INSTALLER_ADDRESS)
public class UpdateColumn extends FinderColumn<UpdateItem> {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final Progress progress;

    @Inject
    public UpdateColumn(final Finder finder,
            final EventBus eventBus,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final ColumnActionFactory columnActionFactory,
            final Resources resources,
            @Footer final Progress progress) {

        super(new Builder<UpdateItem>(finder, Ids.UPDATE_MANAGER_UPDATE, Names.UPDATES)
                .onPreview(item -> new UpdatePreview(item, dispatcher, statementContext, resources))
                .showCount()
                .withFilter()
                .filterDescription(resources.messages().updatesFilterDescription()));

        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
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
                    case CONFIG_CHANGE:
                        return Icons.custom(fontAwesome("cogs"));
                    case UNDEFINED:
                    default:
                        return Icons.unknown();
                }
            }

            @Override
            public HTMLElement element() {
                final HtmlContentBuilder<HTMLElement> builder = span().css(itemText)
                        .add(span().textContent(item.getName()));

                for (String s : item.getVersions()) {
                    builder.add(br()).add(small().css(CSS.marginLeft5).add(s));
                }

                return builder
                        .add(small().css(CSS.subtitle, CSS.marginLeft5).title(Format.mediumDateTime(item.getTimestamp()))
                                .textContent(Format.mediumDateTime(item.getTimestamp())))
                        .element();
            }

            @Override
            public List<ItemAction<UpdateItem>> actions() {
                List<ItemAction<UpdateItem>> actions = new ArrayList<>();
                if (item.getUpdateKind() == UpdateItem.UpdateType.UPDATE) {
                    actions.add(new ItemAction.Builder<UpdateItem>()
                            .title(resources.constants().revert())
                            .handler(itm -> revert(itm))
                            .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_REVERT))
                            .build());
                }
                return actions;
            }
        });

        List<ColumnAction<UpdateItem>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction.Builder<UpdateItem>(Ids.UPDATE_MANAGER_UPDATE_ONLINE)
                .title(resources.constants().onlineUpdates())
                .handler(column -> updateOnline())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addActions.add(new ColumnAction.Builder<UpdateItem>(Ids.UPDATE_MANAGER_UPDATE_OFFLINE)
                .title(resources.constants().offlineUsingArchives())
                .handler(column -> updateOffline())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addActions.add(new ColumnAction.Builder<UpdateItem>(Ids.UPDATE_MANAGER_UPDATE_PATCH)
                .title(resources.constants().customPatches())
                .handler(column -> updatePatch())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, PREPARE_UPDATES))
                .build());
        addColumnActions(Ids.UPDATE_MANAGER_UPDATE_ADD_ACTIONS, fontAwesome("download"),
                resources.constants().installationUpdateMethods(),
                addActions);
        addColumnAction(columnActionFactory.refresh(Ids.UPDATE_MANAGER_UPDATE_REFRESH));
        addColumnAction(new ColumnAction.Builder<UpdateItem>(Ids.UPDATE_MANAGER_CLEAN)
                .element(columnActionFactory.addButton(resources.constants().clean(), "fa fa-eraser"))
                .handler(column -> clean())
                .constraint(Constraint.executable(INSTALLER_TEMPLATE, "clean"))
                .build());
    }

    private void updateOnline() {
        progress.reset();

        // if there are unaccepted licences - open import Wizard (with additional explanation step and no import)
        // afterwards go on to check if there are any updates and either show the dialog or update wizard

        Operation listUpdateOp = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), LIST_UPDATES)
                .build();

        prepareUpdateWizard(listUpdateOp, (missingCerts, missingCertInfos, updates) -> {
            if (!missingCerts.isEmpty()) {
                new UpdateOnlineWizard(eventBus, dispatcher, statementContext, resources, listUpdateOp, missingCerts,
                        missingCertInfos).show(this);
            } else {
                new UpdateOnlineWizard(eventBus, dispatcher, statementContext, resources, updates).show(this);
            }
        }, UPDATES);

        progress.finish();

    }

    private void prepareUpdateWizard(Operation listUpdateOp, WizardBuilder wizardBuilder, String updateType) {
        Operation licenseCheckOp = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext),
                UNACCEPTED_CERTIFICATE)
                .build();

        List<ModelNode> updates = new ArrayList<>();
        List<String> missingCerts = new ArrayList<>();
        List<CertificateInfo> missingCertInfos = new ArrayList<>();
        List<Task<FlowContext>> tasks = List.of(
                (flowContext) -> {
                    return dispatcher.execute(licenseCheckOp).then(
                            result -> {
                                for (ModelNode node : result.asList()) {
                                    missingCerts.add(node.get(CERTIFICATE_CONTENT).asString());
                                    missingCertInfos.add(new CertificateInfo(node.get(CERTIFICATE_INFO)));
                                }
                                return Promise.resolve(flowContext);
                            }).catch_((error) -> {
                                progress.finish();
                                MessageEvent.fire(eventBus,
                                        Message.error(resources.messages().lastOperationFailed(), error.toString()));
                                return Promise.resolve(flowContext);
                            });
                },
                (flowContext) -> {
                    // get the list of updates if no certificates are missing
                    if (missingCerts.isEmpty()) {
                        return dispatcher.execute(listUpdateOp).then(
                                result -> {
                                    updates.addAll(result.get(updateType).asList());
                                    return Promise.resolve(flowContext);
                                }).catch_((error) -> {
                                    progress.finish();
                                    MessageEvent.fire(eventBus,
                                            Message.error(resources.messages().lastOperationFailed(), error.toString()));
                                    return Promise.resolve(flowContext);
                                });
                    } else {
                        return Promise.resolve(flowContext);
                    }
                },
                (flowContext) -> {
                    // build and show the wizard
                    if (missingCerts.isEmpty() && updates.isEmpty()) {
                        Dialog dialog = new Dialog.Builder(resources.constants().noUpdates())
                                .add(p().innerHtml(resources.messages().noUpdatesFound()).element())
                                .closeOnEsc(true)
                                .closeOnly()
                                .size(Dialog.Size.SMALL)
                                .build();
                        dialog.show();
                    } else {
                        wizardBuilder.build(missingCerts, missingCertInfos, updates);
                    }
                    progress.finish();
                    return Promise.resolve(flowContext);
                });

        sequential(new FlowContext(Progress.NOOP), tasks)
                .catch_(Promise::reject);
    }

    private void updateOffline() {
        new UpdateOfflineWizard(eventBus, dispatcher, statementContext, resources).show(this);
    }

    private void updatePatch() {
        new UpdatePatchWizard(eventBus, dispatcher, statementContext, metadataRegistry, resources).show(this);
    }

    private void clean() {
        Operation operation = new Operation.Builder(INSTALLER_TEMPLATE.resolve(statementContext), CLEAN).build();
        dispatcher.execute(operation,
                result -> MessageEvent.fire(eventBus, Message.success(resources.messages().serverCandidateCleanupSuccess())));
    }

    private void revert(UpdateItem updateItem) {
        Operation operation = new Operation.Builder(AddressTemplates.INSTALLER_TEMPLATE.resolve(statementContext),
                HISTORY_FROM_REVISION)
                .param(REVISION, updateItem.getName())
                .build();

        prepareUpdateWizard(operation, (missingCerts, missingCertInfos, updates) -> {
            if (!missingCerts.isEmpty()) {
                new RevertWizard(eventBus, dispatcher, statementContext, resources, updateItem, operation, missingCerts,
                        missingCertInfos).show(this);
            } else {
                new RevertWizard(eventBus, dispatcher, statementContext, resources, updateItem, updates).show(this);
            }
        }, ARTIFACT_CHANGES);
    }

    interface WizardBuilder {
        void build(List<String> missingCerts, List<CertificateInfo> missingCertInfos, List<ModelNode> updates);
    }
}
