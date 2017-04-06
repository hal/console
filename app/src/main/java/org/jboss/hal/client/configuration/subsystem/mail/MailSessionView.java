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

import java.util.List;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.autocomplete.ReadChildrenAutoComplete;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.MAIL_SESSION_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.SERVER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.mail.AddressTemplates.SOCKET_BINDING_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * Implementation note: Not based on MBUI XML due to special handling of mail servers. Each mail session can define
 * three different servers: SMTP, IMAP and POP3.
 *
 * @author Claudio Miranda
 */
public class MailSessionView extends HalViewImpl implements MailSessionPresenter.MyView {

    private final Form<MailSession> mailSessionForm;
    private final NamedNodeTable<NamedNode> serverTable;
    private final Form<NamedNode> serverForm;

    private MailSessionPresenter presenter;

    @Inject
    public MailSessionView(final MetadataRegistry metadataRegistry,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        // ============================================
        // mail-session attributes
        Metadata mailSessionMetadata = metadataRegistry.lookup(MAIL_SESSION_TEMPLATE);
        mailSessionForm = new ModelNodeForm.Builder<MailSession>(
                Ids.MAIL_SESSION_FORM, mailSessionMetadata)
                .onSave((form, changedValues) -> presenter.saveMailSession(changedValues))
                .prepareReset(form -> presenter.resetMailSession(form))
                .build();
        registerAttachable(mailSessionForm);

        Element mailSessionElement = new Elements.Builder()
                .div()
                .p().textContent(mailSessionMetadata.getDescription().getDescription()).end()
                .add(mailSessionForm.asElement())
                .end()
                .build();
        navigation.addPrimary(Ids.MAIL_SESSION_ENTRY, resources.constants().attributes(),
                fontAwesome("envelope"), mailSessionElement);

        // ============================================
        // mail-server: smtp, pop, imap
        Metadata serverMetadata = metadataRegistry.lookup(SERVER_TEMPLATE);

        //noinspection ConstantConditions
        Options<NamedNode> tableOptions = new ModelNodeTable.Builder<NamedNode>(serverMetadata)
                .button(tableButtonFactory.add(SERVER_TEMPLATE, (event, api) -> presenter.launchAddServer()))
                .button(tableButtonFactory.remove(SERVER_TEMPLATE,
                        (event, api) -> presenter.removeServer(api.selectedRow())))
                .column(new ColumnBuilder<NamedNode>(TYPE, resources.constants().type(),
                        (cell, type, row, meta) -> row.getName().toUpperCase()).build())
                .column(new ColumnBuilder<NamedNode>(OUTBOUND_SOCKET_BINDING_REF, "Outbound Socket Binding", //NON-NLS
                        (cell, type, row, meta) -> row.get(OUTBOUND_SOCKET_BINDING_REF)
                                .asString()).build())
                .build();
        serverTable = new NamedNodeTable<>(Ids.MAIL_SERVER_TABLE, serverMetadata, tableOptions);
        registerAttachable(serverTable);

        serverForm = new ModelNodeForm.Builder<NamedNode>(Ids.MAIL_SERVER_FORM, serverMetadata)
                .include(OUTBOUND_SOCKET_BINDING_REF, USERNAME, PASSWORD, "ssl", "tls")
                .unsorted()
                .onSave((f, changedValues) -> presenter.saveServer(f.getModel().getName(), changedValues))
                .prepareReset(f -> presenter.resetServer(f.getModel().getName(), f))
                .build();
        serverForm.getFormItem(OUTBOUND_SOCKET_BINDING_REF).registerSuggestHandler(
                new ReadChildrenAutoComplete(dispatcher, statementContext, SOCKET_BINDING_TEMPLATE));
        registerAttachable(serverForm);

        mailSessionElement = new Elements.Builder()
                .div()
                .p().textContent(serverMetadata.getDescription().getDescription()).end()
                .add(serverTable.asElement())
                .add(serverForm.asElement())
                .end()
                .build();
        navigation.addPrimary(Ids.MAIL_SERVER_ENTRY, Names.SERVER, pfIcon("server"), mailSessionElement);

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
        serverTable.bindForm(serverForm);
    }

    @Override
    public void setPresenter(final MailSessionPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void update(final MailSession mailSession) {
        mailSessionForm.view(mailSession);

        List<NamedNode> servers = asNamedNodes(failSafePropertyList(mailSession, SERVER));
        serverForm.clear();
        serverTable.update(servers);
        serverTable.api().button(0).enable(servers.size() != 3);
    }

    @Override
    public void select(final NamedNode mailServer) {
        serverTable.select(mailServer);
    }
}
