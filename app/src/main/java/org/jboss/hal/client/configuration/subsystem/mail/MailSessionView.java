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
package org.jboss.hal.client.configuration.subsystem.mail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.MAIL_SESSION_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.SELECTED_MAIL_SESSION_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.SOCKET_BINDING_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OUTBOUND_SOCKET_BINDING_REF;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Implementation note: Not based on MBUI XML due to special handling of mail servers. Each mail session can define
 * three different servers: smtp, imap and pop3.
 *
 * @author Claudio Miranda
 */
public class MailSessionView extends HalViewImpl implements MailSessionPresenter.MyView {

    private final Map<String, ModelNodeForm> forms;
    private final DataTable<NamedNode> serversTable;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;

    private MailSessionPresenter presenter;

    @Inject
    public MailSessionView(MetadataRegistry metadataRegistry,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final StatementContext statementContext,
            final Resources resources) {
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.forms = new HashMap<>();

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        TableButtonFactory tableButtonFactory = new TableButtonFactory(metadataProcessor, progress, dispatcher,
                eventBus, new SelectionAwareStatementContext(statementContext, () -> presenter.getMailSessionName()),
                resources);

        // ============================================
        // mail-session attributes
        Metadata mailSessionMetadata = metadataRegistry.lookup(MAIL_SESSION_TEMPLATE);

        ModelNodeForm<ModelNode> mailSessionAttributesForm = new ModelNodeForm.Builder<>(
                Ids.MAIL_SESSION_ATTRIBUTES_FORM, mailSessionMetadata)
                .onSave((form, changedValues) -> presenter.save(changedValues))
                .build();

        Element navigationElement = new Elements.Builder()
                .div()
                .p().textContent(mailSessionMetadata.getDescription().getDescription()).end()
                .add(mailSessionAttributesForm.asElement())
                .end()
                .build();

        forms.put(Ids.MAIL_SESSION_ATTRIBUTES_FORM, mailSessionAttributesForm);
        navigation.addPrimary(Ids.MAIL_SESSION_ATTRIBUTES_ENTRY, resources.constants().attributes(),
                fontAwesome("envelope"), navigationElement);
        registerAttachable(mailSessionAttributesForm);

        // ============================================
        // server: smtp, pop, imap
        Metadata serverMetadata = metadataRegistry.lookup(SERVER_TEMPLATE);

        AddressTemplate serverTemplate = SELECTED_MAIL_SESSION_TEMPLATE.append("server=*");
        //noinspection ConstantConditions
        Options<NamedNode> tableOptions = new ModelNodeTable.Builder<NamedNode>(serverMetadata)
                .column(new ColumnBuilder<NamedNode>(ModelDescriptionConstants.TYPE, resources.constants().type(),
                        (cell, type, row, meta) -> row.getName().toUpperCase()).build())
                .column(new ColumnBuilder<NamedNode>(OUTBOUND_SOCKET_BINDING_REF, "Outbound Socket Binding", //NON-NLS
                        (cell, type, row, meta) -> row.get(OUTBOUND_SOCKET_BINDING_REF)
                                .asString()).build())
                .button(resources.constants().add(), (event, api) -> presenter.launchAddNewServer())
                .button(tableButtonFactory.remove(
                        ModelDescriptionConstants.SERVER,
                        serverTemplate, (api) -> api.selectedRow().getName(),
                        () -> presenter.loadMailSession()))
                .build();
        serversTable = new ModelNodeTable<>(Ids.MAIL_SERVER_TABLE, tableOptions);
        registerAttachable(serversTable);

        ModelNodeForm<NamedNode> formServer = new ModelNodeForm.Builder<NamedNode>(Ids.MAIL_SERVER_FORM, serverMetadata)
                .include(OUTBOUND_SOCKET_BINDING_REF, "username", "password", "ssl", "tls")
                .unsorted()
                .onSave((f, changedValues) -> presenter.save(changedValues))
                .build();

        registerAttachable(formServer);
        forms.put(Ids.MAIL_SERVER_FORM, formServer);

        navigationElement = new Elements.Builder()
                .div()
                .p().textContent(serverMetadata.getDescription().getDescription()).end()
                .add(serversTable.asElement())
                .add(formServer.asElement())
                .end()
                .build();
        navigation.addPrimary(Ids.MAIL_SERVER_ENTRY, Names.SERVER, pfIcon("server"), navigationElement);

        // ============================================
        // main layout
        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.MAIL_SESSION).end()
                    .addAll(navigation.panes())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        initElement(root);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void attach() {
        super.attach();
        ModelNodeForm form = forms.get(Ids.MAIL_SERVER_FORM);
        serversTable.api().bindForm(form);
    }

    @Override
    public void setPresenter(final MailSessionPresenter presenter) {
        this.presenter = presenter;

        ModelNodeForm form = forms.get(Ids.MAIL_SERVER_FORM);
        form.getFormItem(OUTBOUND_SOCKET_BINDING_REF).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, SOCKET_BINDING_TEMPLATE));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(final MailSession mailSession) {
        ModelNodeForm<ModelNode> formAttributes = forms.get(Ids.MAIL_SESSION_ATTRIBUTES_FORM);
        formAttributes.view(mailSession.asModelNode());

        // clean the table model and refresh the UI state
        serversTable.api().clear().refresh(RESET);
        serversTable.api().button(0).enable(mailSession.getServers().size() != 3);
        if (mailSession.hasDefined(ModelDescriptionConstants.SERVER)) {
            // convert the list result from ModelNode to NamedNode
            List<NamedNode> serverTypeModels = asNamedNodes(
                    mailSession.asModelNode().get(ModelDescriptionConstants.SERVER).asPropertyList());
            // update the table model and refresh the UI state
            serversTable.api().clear().add(serverTypeModels).refresh(RESET);
        }
        // always clean the form under the table (the form associated to the table item)
        ModelNodeForm<ModelNode> form = forms.get(Ids.MAIL_SERVER_FORM);
        form.clear();
    }
}
