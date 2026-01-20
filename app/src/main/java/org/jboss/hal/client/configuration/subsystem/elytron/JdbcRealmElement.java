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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.Pages;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.InlineAction;
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

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;
import static org.jboss.hal.resources.Ids.*;

class JdbcRealmElement implements IsElement<HTMLElement>, Attachable, HasPresenter<RealmsPresenter> {

    private final Table<NamedNode> jdbcRealmTable;
    private final Table<ModelNode> pqTable; // pq = principal-query
    private final Form<ModelNode> pqForm;
    private final Map<String, Form<ModelNode>> keyMappers;
    private final Pages pages;
    private RealmsPresenter presenter;
    private String selectedJdbcRealm;
    private int pqIndex = -1;

    JdbcRealmElement(Metadata metadata, TableButtonFactory tableButtonFactory, Resources resources) {

        // JDBC realm
        jdbcRealmTable = new ModelNodeTable.Builder<NamedNode>(id(Ids.TABLE), metadata)
                .button(tableButtonFactory.add(metadata.getTemplate(), table -> presenter.addJdbcRealm()))
                .button(tableButtonFactory.remove(Names.JDBC_REALM, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadJdbcRealms()))
                .nameColumn()
                .column(new InlineAction<>(Names.PRINCIPAL_QUERY, this::showPrincipalQuery))
                .build();
        HTMLElement jdbcRealmSection = section()
                .add(h(1).textContent(Names.JDBC_REALM))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(jdbcRealmTable).element();

        // principal query and key mappers
        Metadata pqMetadata = metadata.forComplexAttribute(PRINCIPAL_QUERY);
        pqTable = new ModelNodeTable.Builder<>(id(PRINCIPAL_QUERY, Ids.TABLE), pqMetadata)
                .button(tableButtonFactory.add(pqMetadata.getTemplate(),
                        table -> presenter.addPrincipalQuery(selectedJdbcRealm)))
                .button(tableButtonFactory.remove(pqMetadata.getTemplate(),
                        table -> presenter.removePrincipalQuery(selectedJdbcRealm, pqIndex)))
                .columns(SQL, DATA_SOURCE)
                .build();

        Tabs tabs = new Tabs(id(PRINCIPAL_QUERY, TAB_CONTAINER));

        pqForm = new ModelNodeForm.Builder<>(id(PRINCIPAL_QUERY, Ids.ATTRIBUTES, FORM), pqMetadata)
                .include(SQL, DATA_SOURCE)
                .customFormItem(ATTRIBUTE_MAPPING, (ad) -> new CustomListItem(ATTRIBUTE_MAPPING, TO, INDEX))
                .unsorted()
                .onSave((f, changedValues) -> presenter.savePrincipalQuery(selectedJdbcRealm, pqIndex, changedValues))
                .build();
        tabs.add(id(PRINCIPAL_QUERY, TAB), resources.constants().attributes(), pqForm.element());

        keyMappers = new LinkedHashMap<>();
        for (String keyMapper : RealmsPresenter.KEY_MAPPERS) {
            Form<ModelNode> form = keyMapperForm(pqMetadata, keyMapper);
            keyMappers.put(keyMapper, form);
            tabs.add(id(PRINCIPAL_QUERY, keyMapper, TAB), new LabelBuilder().label(keyMapper), form.element());
        }

        HTMLElement pqSection = section()
                .add(h(1).textContent(Names.PRINCIPAL_QUERY))
                .add(p().textContent(pqMetadata.getDescription().getDescription()))
                .addAll(pqTable, tabs).element();

        pages = new Pages(id(PAGES), id(PAGE), jdbcRealmSection);
        pages.addPage(id(PAGE), id(PRINCIPAL_QUERY, PAGE),
                () -> Names.JDBC_REALM + ": " + selectedJdbcRealm,
                () -> Names.PRINCIPAL_QUERY,
                pqSection);
    }

    private String id(String... ids) {
        return Ids.build(Ids.ELYTRON_JDBC_REALM, ids);
    }

    private Form<ModelNode> keyMapperForm(Metadata metadata, String keyMapper) {
        Metadata keyMapperMetadata = metadata.forComplexAttribute(keyMapper);
        return new ModelNodeForm.Builder<>(id(PRINCIPAL_QUERY, keyMapper, FORM), keyMapperMetadata)
                .singleton(
                        () -> pqTable.hasSelection()
                                ? presenter.pingKeyMapper(selectedJdbcRealm, pqTable.selectedRow(), keyMapper)
                                : null,
                        () -> presenter.addKeyMapper(selectedJdbcRealm, pqTable.selectedRow(), pqIndex, keyMapper))
                .onSave((f, changedValues) -> presenter.saveKeyMapper(selectedJdbcRealm, pqIndex,
                        keyMapper, changedValues))
                .prepareReset(f -> presenter.resetKeyMapper(selectedJdbcRealm, pqIndex,
                        keyMapper, f))
                .prepareRemove(f -> presenter.removeKeyMapper(selectedJdbcRealm, pqIndex,
                        keyMapper, f))
                .build();
    }

    @Override
    public HTMLElement element() {
        return pages.element();
    }

    @Override
    public void attach() {
        jdbcRealmTable.attach();

        pqTable.attach();
        pqForm.attach();
        pqTable.bindForm(pqForm);

        for (Form<ModelNode> form : keyMappers.values()) {
            form.attach();
        }
        pqTable.onSelectionChange(table -> {
            pqTable.enableButton(1, pqTable.hasSelection() && pqTable.getRows().size() > 1);
            if (table.hasSelection()) {
                pqIndex = table.selectedRow().get(HAL_INDEX).asInt();
                keyMappers.forEach((attribute, form) -> form.view(failSafeGet(table.selectedRow(), attribute)));
            } else {
                pqIndex = -1;
                for (Form<ModelNode> form : keyMappers.values()) {
                    form.clear();
                }
            }
        });
    }

    @Override
    public void setPresenter(RealmsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        jdbcRealmTable.update(nodes);

        if (id(PRINCIPAL_QUERY, PAGE).equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(jdbcRealm -> selectedJdbcRealm.equals(jdbcRealm.getName()))
                    .findFirst()
                    .ifPresent(this::showPrincipalQuery);
        } else if (id(ATTRIBUTE_MAPPING, PAGE).equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(jdbcRealm -> selectedJdbcRealm.equals(jdbcRealm.getName()))
                    .findFirst()
                    .ifPresent(jdbcRealm -> {
                        List<ModelNode> pqNodes = failSafeList(jdbcRealm, PRINCIPAL_QUERY);
                        storeIndex(pqNodes);
                        for (Form<ModelNode> form : keyMappers.values()) {
                            form.clear();
                        }
                        pqTable.update(pqNodes,
                                node -> Ids.build(node.get(SQL).asString(), node.get(DATA_SOURCE).asString()));
                    });
        }
    }

    private void showPrincipalQuery(NamedNode jdbcRealm) {
        selectedJdbcRealm = jdbcRealm.getName();
        List<ModelNode> pqNodes = failSafeList(jdbcRealm, PRINCIPAL_QUERY);
        storeIndex(pqNodes);
        for (Form<ModelNode> form : keyMappers.values()) {
            form.clear();
        }
        pqTable.update(pqNodes, node -> Ids.build(node.get(SQL).asString(), node.get(DATA_SOURCE).asString()));
        // a minimum of one item is required
        pqTable.enableButton(1, pqNodes.size() > 1);
        pages.showPage(id(PRINCIPAL_QUERY, PAGE));
    }
}
