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
package org.jboss.hal.client.runtime.subsystem.transaction;

import java.util.List;

import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.PARTICIPANTS_LOGSTORE_RUNTIME_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PARTICIPANTS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSACTIONS;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.PAGE;
import static org.jboss.hal.resources.Ids.TABLE;

public class TransactionsView extends HalViewImpl implements TransactionsPresenter.MyView {

    private final Table<NamedNode> transactionsTable;
    private final Form<NamedNode> transactionsForm;
    private final Table<NamedNode> participantsTable;
    private final Form<NamedNode> participantsForm;
    private final Pages pages;
    private TransactionsPresenter presenter;
    private String selectedTx;

    @Inject
    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    public TransactionsView(final MetadataRegistry metadataRegistry, final Resources resources) {


        // ==================================== transactions

        Metadata metadataTx = metadataRegistry.lookup(TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE);

        transactionsTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(TRANSACTIONS, TABLE), metadataTx)
                .button(resources.constants().probe(), table -> presenter.probe())
                .button(resources.constants().reload(), table -> presenter.reload())
                .column(Names.TRANSACTION, (cell, type, row, meta) -> row.getName())
                .column(Names.PARTICIPANTS, this::showParticipants, "20em") //NON-NLS
                .build();

        transactionsForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(TRANSACTIONS, FORM), metadataTx)
                .includeRuntime()
                .readOnly()
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(Names.TRANSACTIONS))
                .add(p().textContent(metadataTx.getDescription().getDescription()))
                .add(transactionsTable)
                .add(transactionsForm)
                .asElement();

        // ==================================== participants

        Metadata metadataPart = metadataRegistry.lookup(PARTICIPANTS_LOGSTORE_RUNTIME_TEMPLATE);

        participantsTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(PARTICIPANTS, TABLE), metadataPart)
                .column(Names.PARTICIPANT, (cell, type, row, meta) -> row.getName())
                .build();

        participantsForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(PARTICIPANTS, FORM), metadataPart)
                .includeRuntime()
                .readOnly()
                .build();

        HTMLElement sectionParticipants = section()
                .add(h(1).textContent(Names.PARTICIPANT))
                .add(p().textContent(metadataPart.getDescription().getDescription()))
                .add(participantsTable)
                .add(participantsForm)
                .asElement();

        String txPageId = Ids.build(TRANSACTIONS, PAGE);
        pages = new Pages(txPageId, section);
        pages.addPage(txPageId, Ids.TRANSACTION_PARTICIPANTS_PAGE,
                () -> Names.TRANSACTION + ": " + selectedTx, () -> Names.PARTICIPANTS, sectionParticipants);

        registerAttachable(transactionsTable, transactionsForm, participantsTable, participantsForm);

        initElement(row()
                .add(column()
                        .addAll(section, sectionParticipants)));
    }

    private void showParticipants(final NamedNode transactionNode) {
        selectedTx = transactionNode.getName();
        List<NamedNode> participants = asNamedNodes(transactionNode.asPropertyList());
        participantsForm.clear();
        participantsTable.update(participants);
        pages.showPage(Ids.TRANSACTION_PARTICIPANTS_PAGE);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        transactionsTable.bindForm(transactionsForm);
        participantsTable.bindForm(participantsForm);
    }

    @Override
    public void setPresenter(final TransactionsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final List<NamedNode> model) {
        transactionsForm.clear();
        transactionsTable.update(model);
    }

}
