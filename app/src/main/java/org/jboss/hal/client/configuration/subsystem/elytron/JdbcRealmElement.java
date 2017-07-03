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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.ArrayList;
import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

class JdbcRealmElement implements IsElement<HTMLElement>, Attachable, HasPresenter<RealmsPresenter> {

    private final Table<NamedNode> jdbcRealmTable;
    private final Table<ModelNode> pqTable; // pq = principal-query
    private final List<Form<ModelNode>> pqForms;
    private final Pages pages;
    private RealmsPresenter presenter;
    private String selectedJdbcRealm;
    private int pqIndex;

    JdbcRealmElement(final Metadata metadata, final TableButtonFactory tableButtonFactory, final Resources resources) {

        jdbcRealmTable = new ModelNodeTable.Builder<NamedNode>(Ids.ELYTRON_JDBC_REALM_TABLE, metadata)
                .button(tableButtonFactory.add(metadata.getTemplate(), table -> presenter.addJdbcRealm()))
                .button(tableButtonFactory.remove(Names.JDBC_REALM, AddressTemplates.JDBC_REALM_ADDRESS,
                        () -> presenter.reloadJdbcRealms()))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .column(Names.PRINCIPAL_QUERY, this::showPrincipalQuery)
                .build();
        HTMLElement jdbcRealmSection = section()
                .add(h(1).textContent(Names.JDBC_REALM))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(jdbcRealmTable)
                .asElement();

        Metadata pqMetadata = metadata.forComplexAttribute(PRINCIPAL_QUERY);
        pqTable = new ModelNodeTable.Builder<>(Ids.ELYTRON_PRINCIPAL_QUERY_TABLE, pqMetadata)
                .button(tableButtonFactory.add(pqMetadata.getTemplate(),
                        table -> presenter.addPrincipalQuery(selectedJdbcRealm)))
                .button(tableButtonFactory.remove(pqMetadata.getTemplate(),
                        table -> presenter.removePrincipalQuery(selectedJdbcRealm, pqIndex)))
                .columns(SQL, DATA_SOURCE)
                .build();

        Tabs tabs = new Tabs();
        pqForms = new ArrayList<>();

        String tabId = Ids.build(Ids.ELYTRON_PRINCIPAL_QUERY, "attributes", Ids.TAB_SUFFIX);
        String formId = Ids.build(Ids.ELYTRON_PRINCIPAL_QUERY, "attributes", Ids.FORM_SUFFIX);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(formId, pqMetadata)
                .include(SQL, DATA_SOURCE)
                .onSave((f, changedValues) -> presenter.savePrincipalQuery(selectedJdbcRealm, pqIndex, changedValues))
                .build();
        tabs.add(tabId, resources.constants().attributes(), form.asElement());
        pqForms.add(form);

        addPqComplexForm(tabs, pqForms, pqMetadata, "clear-password-mapper");
        addPqComplexForm(tabs, pqForms, pqMetadata, "bcrypt-mapper");
        addPqComplexForm(tabs, pqForms, pqMetadata, "salted-simple-digest-mapper");
        addPqComplexForm(tabs, pqForms, pqMetadata, "simple-digest-mapper");
        addPqComplexForm(tabs, pqForms, pqMetadata, "scram-mapper");

        HTMLElement pqSection = section()
                .add(h(1).textContent(Names.PRINCIPAL_QUERY))
                .add(p().textContent(pqMetadata.getDescription().getDescription()))
                .addAll(pqTable, tabs)
                .asElement();

        pages = new Pages(Ids.ELYTRON_JDBC_REALM_PAGE, jdbcRealmSection);
        pages.addPage(Ids.ELYTRON_JDBC_REALM_PAGE, Ids.ELYTRON_PRINCIPAL_QUERY_PAGE,
                () -> Names.JDBC_REALM + ": " + selectedJdbcRealm,
                () -> Names.PRINCIPAL_QUERY,
                pqSection);
    }

    private void addPqComplexForm(Tabs tabs, List<Form<ModelNode>> forms, final Metadata metadata,
            @NonNls String complexAttribute) {
        String title = new LabelBuilder().label(complexAttribute);
        String tabId = Ids.build(Ids.ELYTRON_PRINCIPAL_QUERY, complexAttribute, Ids.TAB_SUFFIX);
        String formId = Ids.build(Ids.ELYTRON_PRINCIPAL_QUERY, complexAttribute, Ids.FORM_SUFFIX);
        Metadata caMetadata = metadata.forComplexAttribute(complexAttribute);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(formId, caMetadata)
                .onSave((f, changedValues) -> presenter.savePrincipalQueryComplexAttribute(selectedJdbcRealm, pqIndex,
                        complexAttribute, changedValues))
                .singleton(
                        () -> presenter.pingPrincipalQueryComplexAttribute(selectedJdbcRealm, pqIndex,
                                complexAttribute),
                        () -> presenter.addPrincipalQueryComplexAttribute(selectedJdbcRealm, pqIndex, complexAttribute))
                .prepareReset(f -> presenter.resetPrincipalQueryComplexAttribute(selectedJdbcRealm, pqIndex,
                        complexAttribute, f))
                .prepareRemove(f -> presenter.removePrincipalQueryComplexAttribute(selectedJdbcRealm, pqIndex,
                        complexAttribute, f))
                .build();
        forms.add(form);
        tabs.add(tabId, title, form.asElement());
    }

    @Override
    public HTMLElement asElement() {
        return pages.asElement();
    }

    @Override
    public void attach() {
        jdbcRealmTable.attach();

        pqTable.attach();
        for (Form<ModelNode> form : pqForms) {
            form.attach();
        }
        pqTable.bindForms(pqForms);
        pqTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                pqIndex = table.selectedRow().get(HAL_INDEX).asInt();
            }
        });
    }

    @Override
    public void setPresenter(final RealmsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        jdbcRealmTable.update(nodes);

        if (Ids.ELYTRON_PRINCIPAL_QUERY_PAGE.equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(jdbcRealm -> selectedJdbcRealm.equals(jdbcRealm.getName()))
                    .findFirst()
                    .ifPresent(this::showPrincipalQuery);
        }
    }

    private void showPrincipalQuery(final NamedNode jdbcRealm) {
        selectedJdbcRealm = jdbcRealm.getName();
        List<ModelNode> pqNodes = failSafeList(jdbcRealm, PRINCIPAL_QUERY);
        storeIndex(pqNodes);
        for (Form<ModelNode> form : pqForms) {
            form.clear();
        }
        pqTable.update(pqNodes);
        pages.showPage(Ids.ELYTRON_PRINCIPAL_QUERY_PAGE);
    }
}
