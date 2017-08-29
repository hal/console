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
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSACTIONS;
import static org.jboss.hal.resources.Ids.FORM_SUFFIX;
import static org.jboss.hal.resources.Ids.TABLE_SUFFIX;

public class TransactionsView extends HalViewImpl implements TransactionsPresenter.MyView {

    private final Table<NamedNode> transactionsTable;
    private final Form<NamedNode> transactionsForm;
    private TransactionsPresenter presenter;

    @Inject
    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    public TransactionsView(final MetadataRegistry metadataRegistry, final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE);

        transactionsTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(TRANSACTIONS, TABLE_SUFFIX), metadata)
                .button(resources.constants().probe(), table -> presenter.reload(),
                        Constraint.executable(TRANSACTIONS_LOGSTORE_RUNTIME_TEMPLATE, READ_RESOURCE_OPERATION))
                .column(Names.TRANSACTION, (cell, type, row, meta) -> row.getName())
                .build();

        transactionsForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.build(TRANSACTIONS, FORM_SUFFIX)), metadata)
                .includeRuntime()
                .readOnly()
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(Names.TRANSACTIONS))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(transactionsTable)
                .add(transactionsForm)
                .asElement();

        registerAttachable(transactionsTable, transactionsForm);

        initElement(row()
                .add(column()
                        .addAll(section)));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        super.attach();
        transactionsTable.bindForm(transactionsForm);
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
