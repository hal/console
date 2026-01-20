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

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.VerticalNavigation;
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

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.ELYTRON_LDAP_REALM;
import static org.jboss.hal.resources.Ids.ELYTRON_PROPERTIES_REALM;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.TABLE;

public class RealmsView extends HalViewImpl implements RealmsPresenter.MyView {

    private final Table<NamedNode> cachingRealmTable;
    private final Form<NamedNode> cachingRealmForm;
    private final ElytronRealmWithIdentity customModifiableResource;
    private final ElytronRealmWithIdentity filesystemResource;
    private final ElytronRealmWithIdentity ldapResource;
    private final Table<NamedNode> propertiesRealmTable;
    private final Form<NamedNode> propertiesRealmForm;
    private RealmsPresenter presenter;

    @Inject
    public RealmsView(MetadataRegistry metadataRegistry, Resources resources) {

        VerticalNavigation navigation = new VerticalNavigation();
        // -------------- caching realm
        Metadata cachingRealmMetadata = metadataRegistry.lookup(CACHING_REALM_TEMPLATE);
        cachingRealmTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(Ids.ELYTRON, CACHING_REALM, TABLE),
                cachingRealmMetadata)
                .button(resources.constants().clearCache(),
                        table -> presenter.clearCache(table.selectedRow().getName()),
                        Constraint.executable(CACHING_REALM_TEMPLATE, CLEAR_CACHE))
                .nameColumn()
                .build();

        cachingRealmForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.ELYTRON, CACHING_REALM, FORM),
                cachingRealmMetadata)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement cachingSection = section()
                .add(h(1).textContent(Names.CACHING_REALM))
                .add(p().textContent(cachingRealmMetadata.getDescription().getDescription()))
                .add(cachingRealmTable)
                .add(cachingRealmForm).element();

        navigation.addPrimary(Ids.ELYTRON_CACHING_REALM, Names.CACHING_REALM, pfIcon("settings"), cachingSection);

        // -------------- custom modifiable realm
        Metadata customModifiableRealmMetadata = metadataRegistry.lookup(CUSTOM_MODIFIABLE_REALM_TEMPLATE);
        customModifiableResource = new ElytronRealmWithIdentity(CUSTOM_MODIFIABLE_REALM, resources,
                customModifiableRealmMetadata, Names.CUSTOM_MODIFIABLE_REALM);

        navigation.addPrimary(Ids.ELYTRON_CUSTOM_MODIFIABLE_REALM, Names.CUSTOM_MODIFIABLE_REALM, pfIcon("service"),
                customModifiableResource);

        // -------------- filesystem realm
        Metadata filesystemRealmMetadata = metadataRegistry.lookup(FILESYSTEM_REALM_TEMPLATE);
        filesystemResource = new ElytronRealmWithIdentity(FILESYSTEM_REALM, resources, filesystemRealmMetadata,
                Names.FILESYSTEM_REALM);

        navigation.addPrimary(Ids.ELYTRON_FILESYSTEM_REALM, Names.FILESYSTEM_REALM, pfIcon("cluster"),
                filesystemResource);

        // -------------- ldap realm
        Metadata ldapRealmMetadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE);
        ldapResource = new ElytronRealmWithIdentity(ELYTRON_LDAP_REALM, resources, ldapRealmMetadata, Names.LDAP_REALM);

        navigation.addPrimary(ELYTRON_LDAP_REALM, Names.LDAP_REALM, pfIcon("replicator"), ldapResource);

        // -------------- properties realm
        Metadata propertiesRealmMetadata = metadataRegistry.lookup(PROPERTIES_REALM_TEMPLATE);
        propertiesRealmTable = new ModelNodeTable.Builder<NamedNode>(
                Ids.build(ELYTRON_PROPERTIES_REALM, TABLE), propertiesRealmMetadata)
                .button(resources.constants().load(), table -> presenter.loadProperties(table.selectedRow().getName()),
                        Constraint.executable(PROPERTIES_REALM_TEMPLATE, LOAD))
                .nameColumn()
                .build();

        propertiesRealmForm = new ModelNodeForm.Builder<NamedNode>(
                Ids.build(ELYTRON_PROPERTIES_REALM, FORM), propertiesRealmMetadata)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement propertiesSection = section()
                .add(h(1).textContent(Names.PROPERTIES_REALM))
                .add(p().textContent(propertiesRealmMetadata.getDescription().getDescription()))
                .add(propertiesRealmTable)
                .add(propertiesRealmForm).element();

        navigation.addPrimary(ELYTRON_PROPERTIES_REALM, Names.PROPERTIES_REALM, pfIcon("resource-pool"),
                propertiesSection);

        registerAttachables(asList(navigation, cachingRealmTable, cachingRealmForm, propertiesRealmTable,
                propertiesRealmForm));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void attach() {
        super.attach();
        cachingRealmTable.enableButton(0, false);
        cachingRealmTable.bindForm(cachingRealmForm);
        cachingRealmTable.onSelectionChange(table -> table.enableButton(0, table.hasSelection()));
        customModifiableResource.attach();
        filesystemResource.attach();
        ldapResource.attach();
        propertiesRealmTable.enableButton(0, false);
        propertiesRealmTable.bindForm(propertiesRealmForm);
        propertiesRealmTable.onSelectionChange(table -> table.enableButton(0, table.hasSelection()));

    }

    @Override
    public void setPresenter(RealmsPresenter presenter) {
        this.presenter = presenter;
        filesystemResource.setPresenter(presenter);
        customModifiableResource.setPresenter(presenter);
        ldapResource.setPresenter(presenter);
    }

    @Override
    public void updateCachingRealm(List<NamedNode> items) {
        cachingRealmForm.clear();
        cachingRealmTable.update(items);
    }

    @Override
    public void updateCustomModifiableRealm(List<NamedNode> items) {
        customModifiableResource.update(items);
    }

    @Override
    public void updateFilesystemRealm(List<NamedNode> items) {
        filesystemResource.update(items);
    }

    @Override
    public void updateLdapRealm(List<NamedNode> items) {
        ldapResource.update(items);
    }

    @Override
    public void updatePropertiesRealm(List<NamedNode> items) {
        propertiesRealmForm.clear();
        propertiesRealmTable.update(items);
    }
}
