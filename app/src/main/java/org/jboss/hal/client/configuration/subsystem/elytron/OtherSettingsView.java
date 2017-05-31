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
import javax.inject.Inject;

import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class OtherSettingsView extends HalViewImpl implements OtherSettingsPresenter.MyView, ElytronView {

    // stores
    private ResourceView credentialStoreView;
    private ResourceView filteringKeyStoreView;
    private ResourceView keystoreView;
    private ResourceView ldapKeyStoreView;

    // ssl
    private ResourceView aggregateProvidersView;
    private ResourceView clientSslContextView;
    private ResourceView keyManagerView;
    private ResourceView providerLoaderView;
    private ResourceView securityDomainView;
    private ResourceView securityPropertyView;
    private ResourceView serverSslContextView;
    private ResourceView trustManagerView;

    // authentication
    private ResourceView authenticationContextView;
    private ResourceView authenticationConfigurationView;

    // dir context
    private ResourceView dirContextView;

    private OtherSettingsPresenter presenter;

    @Inject
    OtherSettingsView(final MetadataRegistry metadataRegistry, final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        String primaryIdStores = "stores-item";
        String primaryIdSsl = "ssl-item";
        String primaryIdAuth = "authentication-item";
        String primaryIdDirCtx = "dir-context-item";
        navigation.addPrimary(primaryIdStores, "Stores", "fa fa-exchange");
        navigation.addPrimary(primaryIdSsl, "SSL", "fa fa-file-o");
        navigation.addPrimary(primaryIdAuth, "Authentication", "fa fa-terminal");

        credentialStoreView = new ResourceView.Builder(tableButtonFactory, primaryIdStores,
                Ids.ELYTRON_CREDENTIAL_STORE, "Credential Store", CREDENTIAL_STORE_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("credential-reference")
                .create();

        filteringKeyStoreView = new ResourceView.Builder(tableButtonFactory, primaryIdStores,
                Ids.ELYTRON_FILTERING_KEY_STORE, "Filtering Key Store", FILTERING_KEY_STORE_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();


        keystoreView = new ResourceView.Builder(tableButtonFactory, primaryIdStores,
                Ids.ELYTRON_KEY_STORE, "Key Store", KEY_STORE_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("credential-reference")
                .create();

        CustomPropertiesItem newItemAttributes = new CustomPropertiesItem("new-item-attributes",
                resources.messages().mappingHint(), " | ");
        newItemAttributes.setPropertyValue(NAME, VALUE);
        ldapKeyStoreView = new ResourceView.Builder(tableButtonFactory, primaryIdStores,
                Ids.ELYTRON_LDAP_KEY_STORE, "LDAP Key Store", LDAP_KEY_STORE_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("new-item-template", asList(newItemAttributes))
                .create();

        aggregateProvidersView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_AGGREGATE_PROVIDERS, "Aggregate Providers", AGGREGATE_PROVIDERS_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        clientSslContextView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_CLIENT_SSL_CONTEXT, "Client SSL Context", CLIENT_SSL_CONTEXT_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        keyManagerView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_KEY_MANAGER, "Key Manager", KEY_MANAGER_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("credential-reference")
                .create();

        providerLoaderView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_PROVIDER_LOADER, "Provider Loader", PROVIDER_LOADER_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        serverSslContextView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_SERVER_SSL_CONTEXT, "Server SSL Context", SERVER_SSL_CONTEXT_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        securityDomainView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_SECURITY_DOMAIN, "Security Domain", SECURITY_DOMAIN_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsPage("realms")
                .create();

        securityPropertyView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_SECURITY_PROPERTY, "Security Property", SECURITY_PROPERTY_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        trustManagerView = new ResourceView.Builder(tableButtonFactory, primaryIdSsl,
                Ids.ELYTRON_TRUST_MANAGER, "Trust Manager", TRUST_MANAGER_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("certificate-revocation-list")
                .create();

        authenticationConfigurationView = new ResourceView.Builder(tableButtonFactory,
                primaryIdAuth, Ids.ELYTRON_AUTHENTICATION_CONFIGURATION, "Authentication Configuration",
                AUTHENTICATION_CONF_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("credential-reference")
                .create();

        authenticationContextView = new ResourceView.Builder(tableButtonFactory, primaryIdAuth,
                Ids.ELYTRON_AUTHENTICATION_CONTEXT, "Authentication Context", AUTHENTICATION_CONTEXT_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsPage("match-rules")
                .create();

        dirContextView = new ResourceView.Builder(tableButtonFactory, primaryIdDirCtx,
                Ids.ELYTRON_DIR_CONTEXT, "Dir Context", DIR_CONTEXT_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("credential-reference")
                .primaryLevel("fa fa-bug")
                .create();

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));

    }

    @Override
    public void registerComponents(final Attachable first, final Attachable... rest) {
        registerAttachable(first, rest);
    }

    @Override
    public void attach() {
        super.attach();

        credentialStoreView.bindTableToForm();
        filteringKeyStoreView.bindTableToForm();
        keystoreView.bindTableToForm();
        ldapKeyStoreView.bindTableToForm();
        aggregateProvidersView.bindTableToForm();
        clientSslContextView.bindTableToForm();
        keyManagerView.bindTableToForm();
        providerLoaderView.bindTableToForm();
        serverSslContextView.bindTableToForm();
        securityDomainView.bindTableToForm();
        securityPropertyView.bindTableToForm();
        trustManagerView.bindTableToForm();
        authenticationConfigurationView.bindTableToForm();
        authenticationContextView.bindTableToForm();
        dirContextView.bindTableToForm();
    }

    @Override
    public void updateKeyStore(final List<NamedNode> model) {
        keystoreView.update(model);
    }

    @Override
    public void updateKeyManagers(final List<NamedNode> model) {
        keyManagerView.update(model);
    }

    @Override
    public void updateServerSslContext(final List<NamedNode> model) {
        serverSslContextView.update(model);
    }

    @Override
    public void updateClientSslContext(final List<NamedNode> model) {
        clientSslContextView.update(model);
    }

    @Override
    public void updateTrustManagers(final List<NamedNode> model) {
        trustManagerView.update(model);
    }

    @Override
    public void updateCredentialStore(final List<NamedNode> model) {
        credentialStoreView.update(model);
    }

    @Override
    public void updateFilteringKeyStore(final List<NamedNode> model) {
        filteringKeyStoreView.update(model);
    }

    @Override
    public void updateLdapKeyStore(final List<NamedNode> model) {
        ldapKeyStoreView.update(model);
    }


    @Override
    public void updateProviderLoader(final List<NamedNode> model) {
        providerLoaderView.update(model);
    }

    @Override
    public void updateAggregateProviders(final List<NamedNode> model) {
        aggregateProvidersView.update(model);
    }

    @Override
    public void updateSecurityDomain(final List<NamedNode> model) {
        securityDomainView.update(model);
    }

    @Override
    public void updateSecurityProperty(final List<NamedNode> model) {
        securityPropertyView.update(model);
    }

    @Override
    public void updateDirContext(final List<NamedNode> model) {
        dirContextView.update(model);
    }

    @Override
    public void updateAuthenticationContext(final List<NamedNode> model) {
        authenticationContextView.update(model);
    }

    @Override
    public void updateAuthenticationConfiguration(final List<NamedNode> model) {
        authenticationConfigurationView.update(model);
    }

    @Override
    public void setPresenter(final OtherSettingsPresenter presenter) {
        this.presenter = presenter;

        credentialStoreView.setPresenter(presenter);
        filteringKeyStoreView.setPresenter(presenter);
        keystoreView.setPresenter(presenter);
        ldapKeyStoreView.setPresenter(presenter);
        aggregateProvidersView.setPresenter(presenter);
        clientSslContextView.setPresenter(presenter);
        keyManagerView.setPresenter(presenter);
        providerLoaderView.setPresenter(presenter);
        serverSslContextView.setPresenter(presenter);
        securityDomainView.setPresenter(presenter);
        securityPropertyView.setPresenter(presenter);
        trustManagerView.setPresenter(presenter);
        authenticationConfigurationView.setPresenter(presenter);
        authenticationContextView.setPresenter(presenter);
        dirContextView.setPresenter(presenter);
    }
}