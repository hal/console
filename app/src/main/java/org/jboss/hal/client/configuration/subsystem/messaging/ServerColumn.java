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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.promise.Promise;

import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.BINDING_DIRECTORY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.JOURNAL_DIRECTORY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.LARGE_MESSAGES_DIRECTORY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.MESSAGING_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.PAGING_DIRECTORY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ADD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ELYTRON_DOMAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.MESSAGING_SERVER_ADD;
import static org.jboss.hal.resources.Ids.build;

@AsyncColumn(Ids.MESSAGING_SERVER_CONFIGURATION)
@Requires(value = SERVER_ADDRESS, recursive = false)
public class ServerColumn extends FinderColumn<NamedNode> {

    @Inject
    public ServerColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            CrudOperations crud,
            MetadataRegistry metadataRegistry,
            PlaceManager placeManager,
            StatementContext statementContext,
            Resources resources,
            EventBus eventBus,
            Dispatcher dispatcher,
            Places places) {

        super(new FinderColumn.Builder<NamedNode>(finder, Ids.MESSAGING_SERVER_CONFIGURATION, Names.SERVER)

                .itemsProvider(context -> crud.readChildren(MESSAGING_SUBSYSTEM_TEMPLATE, SERVER)
                        .then(children -> Promise.resolve(asNamedNodes(children))))

                .onBreadcrumbItem((item, context) -> {
                    // replace 'server' request parameter
                    PlaceRequest current = placeManager.getCurrentPlaceRequest();
                    PlaceRequest place = places.replaceParameter(current, SERVER, item.getName()).build();
                    placeManager.revealPlace(place);
                })

                .onPreview(ServerPreview::new)
                .useFirstActionAsBreadcrumbHandler()
                .pinnable()
                .withFilter());

        addColumnAction(columnActionFactory.add(MESSAGING_SERVER_ADD, Names.SERVER, SERVER_TEMPLATE,
                column -> {
                    // read server resources, if there is no server
                    // the path parameters are optional
                    crud.readChildren(MESSAGING_SUBSYSTEM_TEMPLATE, SERVER,
                            children -> {
                                Metadata metadata = metadataRegistry.lookup(SERVER_TEMPLATE);
                                TextBoxItem pathBindingDir = new TextBoxItem("path-bindings-directory");
                                TextBoxItem pathJournalDir = new TextBoxItem("path-journal-directory");
                                TextBoxItem pathLargeMessagesDir = new TextBoxItem(
                                        "path-large-messages-directory");
                                TextBoxItem pathPagingDir = new TextBoxItem("path-paging-directory");
                                TextBoxItem elytronDomain = new TextBoxItem("elytron-domain");
                                elytronDomain.registerSuggestHandler(
                                        new ReadChildrenAutoComplete(dispatcher, statementContext,
                                                SECURITY_DOMAIN_TEMPLATE));

                                boolean hasServers = !children.isEmpty();
                                pathBindingDir.setRequired(hasServers);
                                pathJournalDir.setRequired(hasServers);
                                pathLargeMessagesDir.setRequired(hasServers);
                                pathPagingDir.setRequired(hasServers);
                                elytronDomain.setRequired(true);

                                Form<ModelNode> form = new ModelNodeForm.Builder<>(
                                        build(MESSAGING_SERVER_ADD, FORM), metadata)
                                                .requiredOnly()
                                                .unboundFormItem(new NameItem())
                                                .unboundFormItem(elytronDomain)
                                                .unboundFormItem(pathBindingDir)
                                                .unboundFormItem(pathJournalDir)
                                                .unboundFormItem(pathLargeMessagesDir)
                                                .unboundFormItem(pathPagingDir)
                                                .build();

                                AddResourceDialog dialog = new AddResourceDialog(
                                        resources.messages().addResourceTitle(Names.SERVER), form,
                                        (name, modelNode) -> {
                                            if (modelNode != null) {
                                                ResourceAddress address = SERVER_TEMPLATE.resolve(
                                                        statementContext, name);
                                                Composite composite = new Composite();
                                                Operation addOp = new Operation.Builder(address, ADD)
                                                        .param(ELYTRON_DOMAIN, elytronDomain.getValue())
                                                        .build();
                                                composite.add(addOp);

                                                if (!pathBindingDir.isEmpty()) {
                                                    ResourceAddress bindDirAddress = BINDING_DIRECTORY_TEMPLATE.resolve(
                                                            statementContext, name);
                                                    Operation pathBindDirOp = new Operation.Builder(bindDirAddress,
                                                            ADD)
                                                                    .param(PATH, pathBindingDir.getValue())
                                                                    .build();
                                                    composite.add(pathBindDirOp);
                                                }

                                                if (!pathJournalDir.isEmpty()) {
                                                    ResourceAddress journalDirAddress = JOURNAL_DIRECTORY_TEMPLATE
                                                            .resolve(
                                                                    statementContext, name);
                                                    Operation pathJournalDirOp = new Operation.Builder(
                                                            journalDirAddress, ADD)
                                                                    .param(PATH, pathJournalDir.getValue())
                                                                    .build();
                                                    composite.add(pathJournalDirOp);
                                                }

                                                if (!pathLargeMessagesDir.isEmpty()) {
                                                    ResourceAddress largMsgDirAddress = LARGE_MESSAGES_DIRECTORY_TEMPLATE
                                                            .resolve(statementContext, name);
                                                    Operation pathLargMsgDirOp = new Operation.Builder(
                                                            largMsgDirAddress, ADD)
                                                                    .param(PATH, pathLargeMessagesDir.getValue())
                                                                    .build();
                                                    composite.add(pathLargMsgDirOp);
                                                }

                                                if (!pathPagingDir.isEmpty()) {
                                                    ResourceAddress pagingDirAddress = PAGING_DIRECTORY_TEMPLATE.resolve(
                                                            statementContext, name);
                                                    Operation pathPagingDirOp = new Operation.Builder(
                                                            pagingDirAddress, ADD)
                                                                    .param(PATH, pathPagingDir.getValue())
                                                                    .build();
                                                    composite.add(pathPagingDirOp);
                                                }

                                                dispatcher.execute(composite,
                                                        (CompositeResult compositeResult) -> {
                                                            MessageEvent.fire(eventBus,
                                                                    Message.success(resources.messages()
                                                                            .addResourceSuccess(Names.SERVER,
                                                                                    name)));
                                                            column.refresh(Ids.messagingServer(name));

                                                        }, (operation, failure) -> MessageEvent.fire(eventBus,
                                                                Message.error(resources.messages()
                                                                        .addResourceError(name, failure))));
                                            }
                                        });
                                dialog.getForm().<String> getFormItem(NAME).addValidationHandler(
                                        createUniqueValidation());
                                dialog.show();
                            });

                }));
        addColumnAction(columnActionFactory.refresh(Ids.MESSAGING_SERVER_CONFIGURATION_REFRESH));

        // noinspection Convert2Diamond
        setItemRenderer(item -> new ItemDisplay<NamedNode>() {
            @Override
            public String getId() {
                return Ids.messagingServer(item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String nextColumn() {
                return Ids.MESSAGING_SERVER_SETTINGS;
            }

            @Override
            public List<ItemAction<NamedNode>> actions() {
                List<ItemAction<NamedNode>> actions = new ArrayList<>();
                actions.add(itemActionFactory.view(
                        places.selectedProfile(NameTokens.MESSAGING_SERVER).with(SERVER, item.getName()).build()));
                actions.add(itemActionFactory.remove(Names.SERVER, item.getName(), SERVER_TEMPLATE, ServerColumn.this));
                return actions;
            }
        });
    }
}
