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
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.Scope;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.CREDENTIAL_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.FILTERING_KEY_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.LDAP_KEY_STORE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.SECRET_KEY_CREDENTIAL_STORE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.ELYTRON_LDAP_KEY_STORE;

public class StoresView extends HalViewImpl implements StoresPresenter.MyView {

    private final StoreElement credentialStoreElement;
    private final StoreElement filteringStoreElement;
    private final KeyStoreElement keystoreElement;
    private final StoreElement ldapKeystoreElement;
    private final StoreElement secretKeyCredentialStoreElement;
    private StoresPresenter presenter;

    @Inject
    public StoresView(final MetadataRegistry metadataRegistry, final Resources resources) {

        VerticalNavigation navigation = new VerticalNavigation();

        // -------------- credential store
        Metadata credentialStoreMetadata = metadataRegistry.lookup(CREDENTIAL_STORE_TEMPLATE);
        credentialStoreElement = new StoreElement.Builder(CREDENTIAL_STORE, Names.CREDENTIAL_STORE, resources,
                credentialStoreMetadata)
                        .addButtonHandler(new Button<>(resources.constants().reload(), null,
                                table -> presenter.reloadCredentialStore(table.selectedRow().getName()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(CREDENTIAL_STORE_TEMPLATE, RELOAD)))
                        .addAliasButtonHandler(new Button<>(resources.constants().addAlias(), null,
                                table -> addCredentialStoreAlias(credentialStoreMetadata),
                                null,
                                Constraint.executable(CREDENTIAL_STORE_TEMPLATE, ADD_ALIAS)))
                        .addAliasButtonHandler(new Button<>(resources.constants().removeAlias(), null,
                                table -> removeCredentialStoreAlias(credentialStoreMetadata, table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(CREDENTIAL_STORE_TEMPLATE, REMOVE_ALIAS)))
                        .addAliasButtonHandler(new Button<>(resources.constants().setSecret(), null,
                                table -> setCredentialStoreSecretAlias(credentialStoreMetadata, table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(CREDENTIAL_STORE_TEMPLATE, SET_SECRET)))
                        .build();
        // enable the add-alias button, even if there are no items

        navigation.addPrimary(Ids.ELYTRON_CREDENTIAL_STORE, Names.CREDENTIAL_STORE, pfIcon("settings"), credentialStoreElement);

        // -------------- filtering keystore
        Metadata filteringMetadata = metadataRegistry.lookup(FILTERING_KEY_STORE_TEMPLATE);
        filteringStoreElement = new StoreElement.Builder(FILTERING_KEY_STORE, Names.FILTERING_KEY_STORE, resources,
                filteringMetadata)
                        .addAliasButtonHandler(new Button<>(resources.constants().removeAlias(), null,
                                table -> removeFilteringKeyStoreAlias(filteringMetadata, table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(FILTERING_KEY_STORE_TEMPLATE, REMOVE_ALIAS)))
                        .addAliasButtonHandler(new Button<>(resources.constants().details(), null,
                                table -> readFilteringAlias(filteringMetadata, table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(FILTERING_KEY_STORE_TEMPLATE, READ_ALIAS)))
                        .build();

        navigation.addPrimary(Ids.ELYTRON_FILTERING_KEY_STORE, Names.FILTERING_KEY_STORE, pfIcon("cluster"),
                filteringStoreElement);

        // -------------- key store
        Metadata keystoreMetadata = metadataRegistry.lookup(KEY_STORE_TEMPLATE);
        keystoreElement = new KeyStoreElement(resources, keystoreMetadata);

        navigation.addPrimary(Ids.ELYTRON_KEY_STORE, Names.KEY_STORE, pfIcon("resource-pool"), keystoreElement);

        // -------------- ldap key store
        Metadata ldapKeystoreMetadata = metadataRegistry.lookup(LDAP_KEY_STORE_TEMPLATE);
        ldapKeystoreElement = new StoreElement.Builder(LDAP_KEY_STORE, Names.LDAP_KEY_STORE, resources,
                ldapKeystoreMetadata)
                        .addAliasButtonHandler(new Button<>(resources.constants().removeAlias(), null,
                                table -> removeLdapKeyStoreAlias(ldapKeystoreMetadata, table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(LDAP_KEY_STORE_TEMPLATE, REMOVE_ALIAS)))
                        .addAliasButtonHandler(new Button<>(resources.constants().details(), null,
                                table -> readLdapKeystoreAlias(ldapKeystoreMetadata, table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(LDAP_KEY_STORE_TEMPLATE, READ_ALIAS)))
                        .build();

        navigation.addPrimary(ELYTRON_LDAP_KEY_STORE, Names.LDAP_KEY_STORE, pfIcon("service"), ldapKeystoreElement);

        // -------------- secret key credential store
        Metadata secretKeyCredentialStoreMetadata = metadataRegistry.lookup(SECRET_KEY_CREDENTIAL_STORE_TEMPLATE);
        secretKeyCredentialStoreElement = new StoreElement.Builder(SECRET_KEY_CREDENTIAL_STORE,
                Names.SECRET_KEY_CREDENTIAL_STORE, resources,
                secretKeyCredentialStoreMetadata)
                        .addButtonHandler(new Button<>(resources.constants().reload(), null,
                                table -> presenter.reloadCredentialStore(table.selectedRow().getName()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(SECRET_KEY_CREDENTIAL_STORE_TEMPLATE, RELOAD)))
                        .addAliasButtonHandler(new Button<>(resources.constants().removeAlias(), null,
                                table -> removeSecretKeyCredentialStoreAlias(secretKeyCredentialStoreMetadata,
                                        table.selectedRow().asString()),
                                Scope.SELECTED_SINGLE,
                                Constraint.executable(SECRET_KEY_CREDENTIAL_STORE_TEMPLATE, REMOVE_ALIAS)))
                        .build();

        navigation.addPrimary(Ids.ELYTRON_SECRET_KEY_CREDENTIAL_STORE, Names.SECRET_KEY_CREDENTIAL_STORE, pfIcon("key"),
                secretKeyCredentialStoreElement);

        registerAttachables(
                asList(navigation, credentialStoreElement, filteringStoreElement, keystoreElement, ldapKeystoreElement,
                        secretKeyCredentialStoreElement));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    @Override
    public void attach() {
        super.attach();
        credentialStoreElement.getAliasesTable().enableButton(0, true);
    }

    private void addCredentialStoreAlias(Metadata metadata) {
        presenter.addAlias(metadata, credentialStoreElement.getSelectedResource(),
                credentialStoreElement::updateAliases);
    }

    private void removeCredentialStoreAlias(Metadata metadata, String alias) {
        presenter.removeAlias(metadata, credentialStoreElement.getSelectedResource(),
                alias, credentialStoreElement::updateAliases);
    }

    private void removeFilteringKeyStoreAlias(Metadata metadata, String alias) {
        presenter.removeAlias(metadata, filteringStoreElement.getSelectedResource(), alias,
                filteringStoreElement::updateAliases);
    }

    private void removeLdapKeyStoreAlias(Metadata metadata, String alias) {
        presenter.removeAlias(metadata, ldapKeystoreElement.getSelectedResource(), alias,
                ldapKeystoreElement::updateAliases);
    }

    private void readFilteringAlias(Metadata metadata, String alias) {
        presenter.readAlias(metadata, filteringStoreElement.getSelectedResource(), alias,
                filteringStoreElement::updateAliasDetails);
    }

    private void readLdapKeystoreAlias(Metadata metadata, String alias) {
        presenter.readAlias(metadata, ldapKeystoreElement.getSelectedResource(), alias,
                ldapKeystoreElement::updateAliasDetails);
    }

    private void setCredentialStoreSecretAlias(Metadata metadata, String alias) {
        presenter.setSecret(metadata, credentialStoreElement.getSelectedResource(), alias);
    }

    private void removeSecretKeyCredentialStoreAlias(Metadata metadata, String alias) {
        presenter.removeAlias(metadata, secretKeyCredentialStoreElement.getSelectedResource(),
                alias, secretKeyCredentialStoreElement::updateAliases);
    }

    @Override
    public void updateCredentialStore(List<NamedNode> items) {
        credentialStoreElement.update(items);
    }

    @Override
    public void updateFilteringKeystore(List<NamedNode> items) {
        filteringStoreElement.update(items);
    }

    @Override
    public void updateKeystore(List<NamedNode> items) {
        keystoreElement.update(items);
    }

    @Override
    public void updateLdapKeystore(List<NamedNode> items) {
        ldapKeystoreElement.update(items);
    }

    @Override
    public void updateSecretKeyCredentialStore(List<NamedNode> items) {
        secretKeyCredentialStoreElement.update(items);
    }

    @Override
    public void setPresenter(StoresPresenter presenter) {
        this.presenter = presenter;
        credentialStoreElement.setPresenter(presenter);
        filteringStoreElement.setPresenter(presenter);
        keystoreElement.setPresenter(presenter);
        ldapKeystoreElement.setPresenter(presenter);
        secretKeyCredentialStoreElement.setPresenter(presenter);
    }
}
