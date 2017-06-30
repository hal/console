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

import java.util.List;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
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

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

public class LdapKeyStoreElement implements IsElement<HTMLElement>, Attachable, HasPresenter<OtherSettingsPresenter> {

    private final Metadata metadata;
    private final Table<NamedNode> table;
    private final Form<NamedNode> attributes;
    private final Form<ModelNode> newItemTemplate;
    private final HTMLElement root;
    private OtherSettingsPresenter presenter;

    public LdapKeyStoreElement(final Metadata metadata, final TableButtonFactory tableButtonFactory,
            final Resources resources) {
        this.metadata = metadata;

        this.table = new ModelNodeTable.Builder<NamedNode>(Ids.ELYTRON_LDAP_KEY_STORE_TABLE,
                metadata)
                .button(tableButtonFactory.add(Ids.ELYTRON_LDAP_KEY_STORE_ADD, Names.LDAP_KEY_STORE,
                        metadata.getTemplate(), asList(DIR_CONTEXT, SEARCH_PATH),
                        (n, a) -> presenter.reloadLdapKeyStores()))
                .button(tableButtonFactory.remove(Names.LDAP_KEY_STORE, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadLdapKeyStores()))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .build();

        this.attributes = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_LDAP_KEY_STORE_ATTRIBUTES_FORM, metadata)
                .onSave(((form, changedValues) -> presenter.saveLdapKeyStore(form.getModel().getName(), changedValues)))
                .build();
        Metadata nitMetadata = metadata.forComplexAttribute(NEW_ITEM_TEMPLATE, false);
        this.newItemTemplate = new ModelNodeForm.Builder<>(Ids.ELYTRON_LDAP_KEY_STORE_NEW_ITEM_TEMPLATE_FORM,
                nitMetadata)
                .include(NEW_ITEM_PATH, NEW_ITEM_RDN, NEW_ITEM_ATTRIBUTES)
                .unsorted()
                .customFormItem(NEW_ITEM_ATTRIBUTES, (attributeDescription) -> new NewItemAttributesItem())
                .onSave((form, changedValues) -> presenter.saveNewItemTemplate(table.selectedRow().getName(),
                        changedValues))
                .build();

        Tabs tabs = new Tabs();
        tabs.add(Ids.ELYTRON_LDAP_KEY_STORE_ATTRIBUTES_TAB, resources.constants().attribute(), attributes.asElement());
        tabs.add(Ids.ELYTRON_LDAP_KEY_STORE_NEW_ITEM_TEMPLATE_TAB, Names.NEW_ITEM_TEMPLATE,
                newItemTemplate.asElement());

        this.root = section()
                .add(h(1).textContent(Names.LDAP_KEY_STORES))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .addAll(table, tabs)
                .asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void attach() {
        table.attach();
        attributes.attach();
        newItemTemplate.attach();

        table.bindForm(attributes);
        table.onSelectionChange(table -> {
            if (table.hasSelection()) {
                newItemTemplate.view(failSafeGet(table.selectedRow(), NEW_ITEM_TEMPLATE));
            } else {
                newItemTemplate.clear();
            }
        });
    }

    @Override
    public void setPresenter(final OtherSettingsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        attributes.clear();
        newItemTemplate.clear();
        table.update(nodes);
    }
}
