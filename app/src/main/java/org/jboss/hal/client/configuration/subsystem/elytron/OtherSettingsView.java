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

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class OtherSettingsView extends MbuiViewImpl<OtherSettingsPresenter>
        implements OtherSettingsPresenter.MyView {

    public static OtherSettingsView create(final MbuiContext mbuiContext) {
        return new Mbui_OtherSettingsView(mbuiContext);
    }

    // @formatter:off
    @MbuiElement("other-vertical-navigation") VerticalNavigation navigation;

    @MbuiElement("other-key-store-table") NamedNodeTable<NamedNode> keyStoreTable;
    @MbuiElement("other-key-store-form") Form<NamedNode> keyStoreForm;
    @MbuiElement("other-key-managers-table") NamedNodeTable<NamedNode> keyManagersTable;
    @MbuiElement("other-key-managers-form") Form<NamedNode> keyManagersForm;
    @MbuiElement("other-server-ssl-context-table") NamedNodeTable<NamedNode> serverSslContextTable;
    @MbuiElement("other-server-ssl-context-form") Form<NamedNode> serverSslContextForm;
    @MbuiElement("other-client-ssl-context-table") NamedNodeTable<NamedNode> clientSslContextTable;
    @MbuiElement("other-client-ssl-context-form") Form<NamedNode> clientSslContextForm;
    @MbuiElement("other-trust-managers-table") NamedNodeTable<NamedNode> trustManagersTable;
    @MbuiElement("other-trust-managers-form") Form<NamedNode> trustManagersForm;
    @MbuiElement("other-credential-store-table") NamedNodeTable<NamedNode> credentialStoreTable;
    @MbuiElement("other-credential-store-form") Form<NamedNode> credentialStoreForm;
    @MbuiElement("other-filtering-key-store-table") NamedNodeTable<NamedNode> filteringKeyStoreTable;
    @MbuiElement("other-filtering-key-store-form") Form<NamedNode> filteringKeyStoreForm;
    @MbuiElement("other-ldap-key-store-table") NamedNodeTable<NamedNode> ldapKeyStoreTable;
    @MbuiElement("other-ldap-key-store-form") Form<NamedNode> ldapKeyStoreForm;
    @MbuiElement("other-provider-loader-table") NamedNodeTable<NamedNode> providerLoaderTable;
    @MbuiElement("other-provider-loader-form") Form<NamedNode> providerLoaderForm;
    @MbuiElement("other-aggregate-providers-table") NamedNodeTable<NamedNode> aggregateProvidersTable;
    @MbuiElement("other-aggregate-providers-form") Form<NamedNode> aggregateProvidersForm;
    @MbuiElement("other-security-domain-table") NamedNodeTable<NamedNode> securityDomainTable;
    @MbuiElement("other-security-domain-form") Form<NamedNode> securityDomainForm;
    @MbuiElement("other-security-property-table") NamedNodeTable<NamedNode> securityPropertyTable;
    @MbuiElement("other-security-property-form") Form<NamedNode> securityPropertyForm;
    @MbuiElement("other-dir-context-table") NamedNodeTable<NamedNode> dirContextTable;
    @MbuiElement("other-dir-context-form") Form<NamedNode> dirContextForm;
    @MbuiElement("other-authentication-context-table") NamedNodeTable<NamedNode> authenticationContextTable;
    @MbuiElement("other-authentication-context-form") Form<NamedNode> authenticationContextForm;
    @MbuiElement("other-authentication-configuration-table") NamedNodeTable<NamedNode> authenticationConfigurationTable;
    @MbuiElement("other-authentication-configuration-form") Form<NamedNode> authenticationConfigurationForm;

    // @formatter:on

    OtherSettingsView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateKeyStore(final List<NamedNode> model) {
        keyStoreForm.clear();
        keyStoreTable.update(model);
    }

    @Override
    public void updateKeyManagers(final List<NamedNode> model) {
        keyManagersForm.clear();
        keyManagersTable.update(model);
    }

    @Override
    public void updateServerSslContext(final List<NamedNode> model) {
        serverSslContextForm.clear();
        serverSslContextTable.update(model);
    }

    @Override
    public void updateClientSslContext(final List<NamedNode> model) {
        clientSslContextForm.clear();
        clientSslContextTable.update(model);
    }

    @Override
    public void updateTrustManagers(final List<NamedNode> model) {
        trustManagersForm.clear();
        trustManagersTable.update(model);
    }

    @Override
    public void updateCredentialStore(final List<NamedNode> model) {
        credentialStoreForm.clear();
        credentialStoreTable.update(model);
    }

    @Override
    public void updateFilteringKeyStore(final List<NamedNode> model) {
        filteringKeyStoreForm.clear();
        filteringKeyStoreTable.update(model);
    }

    @Override
    public void updateLdapKeyStore(final List<NamedNode> model) {
        ldapKeyStoreForm.clear();
        ldapKeyStoreTable.update(model);
    }


    @Override
    public void updateProviderLoader(final List<NamedNode> model) {
        providerLoaderForm.clear();
        providerLoaderTable.update(model);
    }

    @Override
    public void updateAggregateProviders(final List<NamedNode> model) {
        aggregateProvidersForm.clear();
        aggregateProvidersTable.update(model);
    }

    @Override
    public void updateSecurityDomain(final List<NamedNode> model) {
        securityDomainForm.clear();
        securityDomainTable.update(model);
    }

    @Override
    public void updateSecurityProperty(final List<NamedNode> model) {
        securityPropertyForm.clear();
        securityPropertyTable.update(model);
    }

    @Override
    public void updateDirContext(final List<NamedNode> model) {
        dirContextForm.clear();
        dirContextTable.update(model);
    }

    @Override
    public void updateAuthenticationContext(final List<NamedNode> model) {
        authenticationContextForm.clear();
        authenticationContextTable.update(model);
    }

    @Override
    public void updateAuthenticationConfiguration(final List<NamedNode> model) {
        authenticationConfigurationForm.clear();
        authenticationConfigurationTable.update(model);
    }
}