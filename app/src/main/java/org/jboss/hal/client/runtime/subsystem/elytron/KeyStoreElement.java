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
package org.jboss.hal.client.runtime.subsystem.elytron;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.InlineAction;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_STORE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.PAGE;
import static org.jboss.hal.resources.Ids.PAGES;
import static org.jboss.hal.resources.Ids.TABLE;

public class KeyStoreElement implements IsElement<HTMLElement>, Attachable {

    private final Table<NamedNode> table;
    private final Form<NamedNode> form;
    private final Table<ModelNode> aliasesTable;
    private Form<ModelNode> formAlias;
    private final Pages pages;
    private StoresPresenter presenter;
    private HTMLElement root;
    private String selectedKeystore;
    private PreTextItem aliasDetails;
    private Map<String, String> aliasDetailsMapping = new HashMap<>();

    KeyStoreElement(Resources resources, Metadata metadata) {

        LabelBuilder labelBuilder = new LabelBuilder();
        Constants cons = resources.constants();
        table = new ModelNodeTable.Builder<NamedNode>(id(TABLE), metadata)
                .button(new Button<>(cons.load(), table -> presenter.loadKeyStore(table.selectedRow().getName()),
                        Constraint.executable(KEY_STORE_TEMPLATE, LOAD)))
                .button(new Button<>(cons.store(), table -> presenter.storeKeyStore(table.selectedRow().getName()),
                        Constraint.executable(KEY_STORE_TEMPLATE, STORE)))
                .button(new Button<>(cons.generateKeyPair(),
                        table -> presenter.generateKeyPair(metadata, table.selectedRow().getName()),
                        Constraint.executable(KEY_STORE_TEMPLATE, GENERATE_KEY_PAIR)))
                .button(new Button<>(cons.importCertificate(),
                        table -> presenter.importCertificate(metadata, table.selectedRow().getName()),
                        Constraint.executable(KEY_STORE_TEMPLATE, IMPORT_CERTIFICATE)))
                .button(new Button<>(cons.obtain(), cons.obtainCertificate(),
                        table -> presenter.obtainCertificate(metadata, table.selectedRow().getName()),
                        Constraint.executable(KEY_STORE_TEMPLATE, OBTAIN_CERTIFICATE)))
                .nameColumn()
                .column(new InlineAction<>(cons.aliases(),
                        row -> {
                            selectedKeystore = row.getName();
                            showAliases(metadata.getTemplate(), row.getName());
                        }))
                .build();

        form = new ModelNodeForm.Builder<NamedNode>(id(FORM), metadata)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement mainSection = section()
                .add(h(1).textContent(Names.KEY_STORE))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(table)
                .add(form).element();

        aliasesTable = new ModelNodeTable.Builder<>(id(ALIAS, TABLE), metadata)
                .button(new Button<>(cons.changeAlias(), table -> changeAlias(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, CHANGE_ALIAS)))
                .button(new Button<>(cons.exportCertificate(),
                        table -> exportCertificate(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, EXPORT_CERTIFICATE)))
                .button(new Button<>(cons.generateCSR(), labelBuilder.label(GENERATE_CERTIFICATE_SIGNING_REQUEST),
                        table -> generateCSR(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, GENERATE_CERTIFICATE_SIGNING_REQUEST)))
                .button(new Button<>(cons.removeAlias(),
                        table -> removeKeyStoreAlias(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, REMOVE_ALIAS)))
                .button(new Button<>(cons.details(), cons.viewDetailsAlias(),
                        table -> readKeystoreAlias(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, READ_ALIAS)))
                .button(new Button<>(cons.revoke(), cons.revokeCertificate(),
                        table -> revokeCertificate(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, REVOKE_CERTIFICATE)))
                .button(new Button<>(cons.verifyRenew(), cons.verifyRenewCertificate(),
                        table -> verifyRenewCertificate(metadata, table.selectedRow().asString()),
                        Constraint.executable(KEY_STORE_TEMPLATE, SHOULD_RENEW_CERTIFICATE)))
                .column(ALIAS, (cell, t, row, meta) -> row.asString())
                .build();

        aliasDetails = new PreTextItem(CERTIFICATE_DETAILS);
        aliasDetails.setEnabled(false);
        formAlias = new ModelNodeForm.Builder<>(id(ALIAS, FORM), Metadata.empty())
                .readOnly()
                .unboundFormItem(aliasDetails)
                .build();

        HTMLElement aliasesSection = section()
                .add(h(1).textContent(cons.aliases()))
                .add(aliasesTable)
                .add(formAlias).element();

        pages = new Pages(id(PAGES), id(PAGE), mainSection);
        pages.addPage(id(PAGE), id(ALIAS, PAGE),
                () -> Names.KEY_STORE + ": " + selectedKeystore,
                () -> cons.aliases(), aliasesSection);

        root = section()
                .add(pages).element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        form.attach();
        aliasesTable.attach();
        formAlias.attach();

        table.bindForm(form);
    }

    private void toggleKeyStoreButtons() {
        // disable table buttons if there is no selected row
        table.enableButton(0, false);
        table.enableButton(1, false);
        table.enableButton(2, false);
        table.enableButton(3, false);
        table.enableButton(4, false);

        table.onSelectionChange(table1 -> {
            table.enableButton(0, table1.hasSelection());
            table.enableButton(1, table1.hasSelection());
            table.enableButton(2, table1.hasSelection());
            table.enableButton(3, table1.hasSelection());
            table.enableButton(4, table1.hasSelection());
        });
    }

    private void toggleAliasesButtons() {
        // disable table buttons if there is no selected row
        aliasesTable.enableButton(0, false);
        aliasesTable.enableButton(1, false);
        aliasesTable.enableButton(2, false);
        aliasesTable.enableButton(3, false);
        aliasesTable.enableButton(4, false);
        aliasesTable.enableButton(5, false);
        aliasesTable.enableButton(6, false);

        aliasesTable.onSelectionChange(table1 -> {
            table1.enableButton(0, table1.hasSelection());
            table1.enableButton(1, table1.hasSelection());
            table1.enableButton(2, table1.hasSelection());
            table1.enableButton(3, table1.hasSelection());
            table1.enableButton(4, table1.hasSelection());
            table1.enableButton(5, table1.hasSelection());
            table1.enableButton(6, table1.hasSelection());
            if (table1.hasSelection()) {
                String alias = table1.selectedRow().asString();
                String value = aliasDetailsMapping.get(alias);
                if (value != null) {
                    aliasDetails.setValue(value);
                } else {
                    aliasDetails.clearValue();
                }
            }
        });
    }

    @Override
    public void detach() {
        table.detach();
        form.detach();
        aliasesTable.detach();
        formAlias.detach();
    }

    public void setPresenter(StoresPresenter presenter) {
        this.presenter = presenter;
    }

    private String id(String... id) {
        return Ids.build(KEY_STORE, id);
    }

    public void update(List<NamedNode> items) {
        form.clear();
        table.update(items);
        aliasDetailsMapping.clear();
        toggleKeyStoreButtons();
    }

    // -------------- aliases operations

    private void showAliases(AddressTemplate template, String resource) {
        presenter.readAliases(template, resource, aliasesTable::update);
        pages.showPage(id(ALIAS, PAGE));
        toggleAliasesButtons();
    }

    private void updateAliases(List<ModelNode> items) {
        aliasesTable.update(items);
        formAlias.clear();
    }

    private void updateAliasDetails(ModelNode details) {
        String value = details.toString();
        aliasDetails.setValue(value);
        aliasDetailsMapping.put(details.get(ALIAS).asString(), value);
    }

    private void removeKeyStoreAlias(Metadata metadata, String alias) {
        presenter.removeAlias(metadata, selectedKeystore, alias, this::updateAliases);
    }

    private void readKeystoreAlias(Metadata metadata, String alias) {
        presenter.readAlias(metadata, selectedKeystore, alias, this::updateAliasDetails);
    }

    private void changeAlias(Metadata metadata, String alias) {
        presenter.changeAlias(metadata, selectedKeystore, alias, this::updateAliases);
    }

    private void exportCertificate(Metadata metadata, String alias) {
        presenter.exportCertificate(metadata, selectedKeystore, alias);
    }

    private void generateCSR(Metadata metadata, String alias) {
        presenter.generateCSR(metadata, selectedKeystore, alias);
    }

    private void revokeCertificate(Metadata metadata, String alias) {
        presenter.revokeCertificate(metadata, selectedKeystore, alias);
    }

    private void verifyRenewCertificate(Metadata metadata, String alias) {
        presenter.verifyRenewCertificate(metadata, selectedKeystore, alias);
    }
}
