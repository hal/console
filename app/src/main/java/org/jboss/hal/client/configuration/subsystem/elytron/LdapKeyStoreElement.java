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

import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.Attachable;
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

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.resources.Ids.TAB_CONTAINER;

class LdapKeyStoreElement implements IsElement<HTMLElement>, Attachable, HasPresenter<OtherSettingsPresenter> {

    private final Table<NamedNode> table;
    private final Form<NamedNode> attributes;
    private final Form<ModelNode> newItemTemplate;
    private final HTMLElement root;
    private OtherSettingsPresenter presenter;

    LdapKeyStoreElement(Metadata metadata, TableButtonFactory tableButtonFactory,
            Resources resources) {
        table = new ModelNodeTable.Builder<NamedNode>(id(Ids.TABLE), metadata)
                .button(tableButtonFactory.add(id(Ids.ADD), Names.LDAP_KEY_STORE, metadata.getTemplate(),
                        asList(DIR_CONTEXT, SEARCH_PATH), (n, a) -> presenter.reloadLdapKeyStores()))
                .button(tableButtonFactory.remove(Names.LDAP_KEY_STORE, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadLdapKeyStores()))
                .nameColumn()
                .build();

        attributes = new ModelNodeForm.Builder<NamedNode>(id(FORM), metadata)
                .exclude(NEW_ITEM_TEMPLATE + ".*")
                .onSave(((form, changedValues) -> presenter.saveLdapKeyStore(form.getModel().getName(), changedValues)))
                .build();

        Metadata nitMetadata = metadata.forComplexAttribute(NEW_ITEM_TEMPLATE);
        newItemTemplate = new ModelNodeForm.Builder<>(id(NEW_ITEM_TEMPLATE, FORM), nitMetadata)
                .include(NEW_ITEM_PATH, NEW_ITEM_RDN, NEW_ITEM_ATTRIBUTES)
                .customFormItem(NEW_ITEM_ATTRIBUTES,
                        (attributeDescription) -> new MultiValueListItem(NEW_ITEM_ATTRIBUTES))
                .unsorted()
                .singleton(
                        () -> {
                            if (table.hasSelection()) {
                                return presenter.pingNewItemTemplate(table.selectedRow().getName());
                            }
                            return null;
                        },
                        () -> presenter.addNewItemTemplate(table.selectedRow().getName()))
                .onSave((form, changedValues) -> presenter.saveNewItemTemplate(table.selectedRow().getName(),
                        changedValues))
                .prepareRemove(form -> presenter.removeNewItemTemplate(table.selectedRow().getName(), form))
                .build();

        Tabs tabs = new Tabs(id(TAB_CONTAINER));
        tabs.add(id(TAB), resources.constants().attributes(), attributes.element());
        tabs.add(id(NEW_ITEM_TEMPLATE, TAB), Names.NEW_ITEM_TEMPLATE, newItemTemplate.element());

        root = section()
                .add(h(1).textContent(Names.LDAP_KEY_STORE))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .addAll(table, tabs).element();
    }

    private String id(String... ids) {
        return Ids.build(Ids.ELYTRON_LDAP_KEY_STORE, ids);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        attributes.attach();
        newItemTemplate.attach();

        table.bindForm(attributes);
        table.onSelectionChange(table -> {
            table.enableButton(1, table.hasSelection());
            if (table.hasSelection()) {
                newItemTemplate.view(failSafeGet(table.selectedRow(), NEW_ITEM_TEMPLATE));
            } else {
                newItemTemplate.clear();
            }
        });
    }

    @Override
    public void setPresenter(OtherSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        attributes.clear();
        newItemTemplate.clear();
        table.update(nodes);
        table.enableButton(1, table.hasSelection());
    }
}
