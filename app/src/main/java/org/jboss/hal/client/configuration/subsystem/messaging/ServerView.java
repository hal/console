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
package org.jboss.hal.client.configuration.subsystem.messaging;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.configuration.PathsAutoComplete;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.form.GroupedForm;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.elytron.CredentialReference;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.messaging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELATIVE_TO;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

/**
 * @author Harald Pehl
 */
public class ServerView extends HalViewImpl implements ServerPresenter.MyView {

    private VerticalNavigation verticalNavigation;
    private Form<NamedNode> form;
    private Form<ModelNode> pagingDirectoryForm;
    private Form<ModelNode> bindingsDirectoryForm;
    private Form<ModelNode> largeMessagesDirectoryForm;
    private Form<ModelNode> journalDirectoryForm;
    private Form<ModelNode> crForm;
    private ServerPresenter presenter;

    @Inject
    ServerView(final MbuiContext mbuiContext, CredentialReference cr) {

        verticalNavigation = new VerticalNavigation();
        Metadata metadata = mbuiContext.metadataRegistry().lookup(SERVER_TEMPLATE);

        // there are several attributes with no attribute-group
        // wee add them under "attributes" tab
        List<String> attrs = new ArrayList<>();
        metadata.getDescription().getAttributes(ATTRIBUTES).forEach(p -> {
            if (!p.getValue().hasDefined("attribute-group")) {
                attrs.add(p.getName());
            }
        });

        String clusterCR = "cluster-credential-reference";
        crForm = cr.form(Ids.MESSAGING_SERVER, metadata, clusterCR, "cluster-password",
                () -> form.<String>getFormItem("cluster-password").getValue(),
                () -> presenter.resourceAddress(),
                () -> presenter.reload());

        LabelBuilder lb = new LabelBuilder();
        form = new GroupedForm.Builder<NamedNode>("messaging-server-form", metadata)
                .customGroup(Ids.build(Ids.MESSAGING_SERVER, ATTRIBUTES, Ids.FORM_SUFFIX),
                        mbuiContext.resources().constants().attributes())
                .include(attrs)
                .end()
                .attributeGroup("management").end()
                .attributeGroup("security").end()
                .attributeGroup("journal").end()
                .attributeGroup("cluster").end()
                .customGroup(Ids.build(Ids.MESSAGING_SERVER, clusterCR, Ids.TAB_SUFFIX), lb.label(clusterCR))
                .add(crForm)
                .end()
                .attributeGroup("message-expiry").end()
                .attributeGroup("transaction").end()
                .attributeGroup("statistics").end()
                .attributeGroup("debug").end()
                .onSave((form, changedValues) -> presenter.saveServer(changedValues))
                .prepareReset(form -> presenter.resetServer(form))
                .build();


        registerAttachable(form, crForm);

        HTMLElement htmlSection = section()
                .add(h(1).textContent(Names.CONFIGURATION))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(form)
                .asElement();


        verticalNavigation.addPrimary(Ids.build(Ids.MESSAGING_SERVER, "item", Ids.TAB_SUFFIX), Names.CONFIGURATION,
                "pficon pficon-settings", htmlSection);

        Metadata bindingMetadata = mbuiContext.metadataRegistry().lookup(BINDING_DIRECTORY_TEMPLATE);
        Metadata journalMetadata = mbuiContext.metadataRegistry().lookup(JOURNAL_DIRECTORY_TEMPLATE);
        Metadata largeMetadata = mbuiContext.metadataRegistry().lookup(LARGE_MESSAGES_DIRECTORY_TEMPLATE);
        Metadata pagingMetadata = mbuiContext.metadataRegistry().lookup(PAGING_DIRECTORY_TEMPLATE);


        bindingsDirectoryForm = new ModelNodeForm.Builder<>(Ids.BINDING_DIRECTORY_FORM, bindingMetadata)
                .singleton(
                        () -> new Operation.Builder(BINDING_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()),
                                READ_RESOURCE_OPERATION).build(),
                        () -> mbuiContext.crud().addSingleton(Ids.BINDING_DIRECTORY_FORM, Names.BINDINGS_DIRECTORY,
                                BINDING_DIRECTORY_TEMPLATE, address -> presenter.reload()))
                .prepareRemove(form -> mbuiContext.crud().removeSingleton(Names.BINDINGS_DIRECTORY,
                        BINDING_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()), () -> presenter.reload()))
                .onSave((form, changedValues) -> mbuiContext.crud().saveSingleton(Names.BINDINGS_DIRECTORY,
                        BINDING_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()), changedValues,
                        bindingMetadata,
                        () -> presenter.reload()))
                .build();

        journalDirectoryForm = new ModelNodeForm.Builder<>(Ids.JOURNAL_DIRECTORY_FORM, journalMetadata)
                .singleton(
                        () -> new Operation.Builder(JOURNAL_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()),
                                READ_RESOURCE_OPERATION).build(),
                        () -> mbuiContext.crud().addSingleton(Ids.JOURNAL_DIRECTORY_FORM, Names.JOURNAL_DIRECTORY,
                                JOURNAL_DIRECTORY_TEMPLATE, address -> presenter.reload()))
                .prepareRemove(form -> mbuiContext.crud().removeSingleton(Names.JOURNAL_DIRECTORY,
                        JOURNAL_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()), () -> presenter.reload()))
                .onSave((form, changedValues) -> mbuiContext.crud().saveSingleton(Names.JOURNAL_DIRECTORY,
                        JOURNAL_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()), changedValues,
                        journalMetadata,
                        () -> presenter.reload()))
                .build();

        largeMessagesDirectoryForm = new ModelNodeForm.Builder<>(Ids.LARGE_MESSAGES_DIRECTORY_FORM, largeMetadata)
                .singleton(
                        () -> new Operation.Builder(
                                LARGE_MESSAGES_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()),
                                READ_RESOURCE_OPERATION).build(),
                        () -> mbuiContext.crud()
                                .addSingleton(Ids.LARGE_MESSAGES_DIRECTORY_FORM, Names.LARGE_MESSAGES_DIRECTORY,
                                        LARGE_MESSAGES_DIRECTORY_TEMPLATE, address -> presenter.reload()))
                .prepareRemove(form -> mbuiContext.crud()
                        .removeSingleton(Names.LARGE_MESSAGES_DIRECTORY, LARGE_MESSAGES_DIRECTORY_TEMPLATE
                                .resolve(mbuiContext.statementContext()), () -> presenter.reload()))
                .onSave((form, changedValues) -> mbuiContext.crud()
                        .saveSingleton(Names.LARGE_MESSAGES_DIRECTORY, LARGE_MESSAGES_DIRECTORY_TEMPLATE
                                        .resolve(mbuiContext.statementContext()), changedValues, largeMetadata,
                                () -> presenter.reload()))
                .build();

        pagingDirectoryForm = new ModelNodeForm.Builder<>(Ids.PAGING_DIRECTORY_FORM, pagingMetadata)
                .singleton(
                        () -> new Operation.Builder(PAGING_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()),
                                READ_RESOURCE_OPERATION).build(),
                        () -> mbuiContext.crud()
                                .addSingleton(Ids.PAGING_DIRECTORY_FORM, Names.PAGING_DIRECTORY,
                                        PAGING_DIRECTORY_TEMPLATE,
                                        address -> presenter.reload()))
                .prepareRemove(form -> mbuiContext.crud().removeSingleton(Names.PAGING_DIRECTORY,
                        PAGING_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()), () -> presenter.reload()))
                .onSave((form, changedValues) -> mbuiContext.crud().saveSingleton(Names.PAGING_DIRECTORY,
                        PAGING_DIRECTORY_TEMPLATE.resolve(mbuiContext.statementContext()), changedValues,
                        pagingMetadata,
                        () -> presenter.reload()))
                .build();


        String primaryIdDirectory = "messaging-server-directory-entry";

        HTMLElement pagingDirectoryElement = section()
                .add(h(1).textContent(Names.PAGING_DIRECTORY))
                .add(p().textContent(pagingMetadata.getDescription().getDescription()))
                .add(pagingDirectoryForm)
                .asElement();

        HTMLElement bindingsDirectoryElement = section()
                .add(h(1).textContent(Names.BINDINGS_DIRECTORY))
                .add(p().textContent(bindingMetadata.getDescription().getDescription()))
                .add(bindingsDirectoryForm)
                .asElement();

        HTMLElement largeMessagesElement = section()
                .add(h(1).textContent(Names.LARGE_MESSAGES_DIRECTORY))
                .add(p().textContent(largeMetadata.getDescription().getDescription()))
                .add(largeMessagesDirectoryForm)
                .asElement();

        HTMLElement journalElement = section()
                .add(h(1).textContent(Names.JOURNAL_DIRECTORY))
                .add(p().textContent(journalMetadata.getDescription().getDescription()))
                .add(journalDirectoryForm)
                .asElement();


        verticalNavigation.addPrimary(primaryIdDirectory, "Directories", "pficon pficon-repository");
        verticalNavigation.addSecondary(primaryIdDirectory, Ids.build(Ids.PAGING_DIRECTORY, Ids.ENTRY_SUFFIX), "Paging",
                pagingDirectoryElement);
        verticalNavigation
                .addSecondary(primaryIdDirectory, Ids.build(Ids.BINDING_DIRECTORY, Ids.ENTRY_SUFFIX), "Bindings",
                        bindingsDirectoryElement);
        verticalNavigation
                .addSecondary(primaryIdDirectory, Ids.build(Ids.LARGE_MESSAGES_DIRECTORY, Ids.ENTRY_SUFFIX),
                        "Large Messages",
                        largeMessagesElement);
        verticalNavigation
                .addSecondary(primaryIdDirectory, Ids.build(Ids.JOURNAL_DIRECTORY, Ids.ENTRY_SUFFIX), "Journal",
                        journalElement);

        registerAttachable(verticalNavigation);
        registerAttachable(pagingDirectoryForm);
        registerAttachable(bindingsDirectoryForm);
        registerAttachable(largeMessagesDirectoryForm);
        registerAttachable(journalDirectoryForm);

        HTMLElement root = row()
                .add(column()
                        .addAll(verticalNavigation.panes()))
                .asElement();

        initElement(root);


        form.getFormItem("journal-datasource").registerSuggestHandler(
                new ReadChildrenAutoComplete(mbuiContext.dispatcher(), mbuiContext.statementContext(),
                        AddressTemplate.of("/{selected.profile}/subsystem=datasources/data-source=*")));
        pagingDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        bindingsDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        largeMessagesDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
        journalDirectoryForm.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());
    }

    @Override
    public void update(final NamedNode server) {
        form.view(server);
        pagingDirectoryForm.view(failSafeGet(server, "path/paging-directory"));
        bindingsDirectoryForm.view(failSafeGet(server, "path/bindings-directory"));
        largeMessagesDirectoryForm.view(failSafeGet(server, "path/large-messages-directory"));
        journalDirectoryForm.view(failSafeGet(server, "path/journal-directory"));
        crForm.view(failSafeGet(server, "cluster-credential-reference"));
    }

    @Override
    public void setPresenter(final ServerPresenter presenter) {
        this.presenter = presenter;
    }
}
