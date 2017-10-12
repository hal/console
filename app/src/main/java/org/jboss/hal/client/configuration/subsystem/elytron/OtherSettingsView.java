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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.ResourceElement;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.elytron.ElytronResource.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREDENTIAL_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SSL_CONTEXT;

public class OtherSettingsView extends HalViewImpl implements OtherSettingsPresenter.MyView {

    private final Map<String, ResourceElement> elements;
    private LdapKeyStoreElement ldapKeyStoreElement;
    private PolicyElement policyElement;
    private VerticalNavigation navigation;
    private OtherSettingsPresenter presenter;

    @Inject
    OtherSettingsView(MbuiContext mbuiContext) {

        elements = new HashMap<>();
        navigation = new VerticalNavigation();
        registerAttachable(navigation);

        String primaryIdStores = "stores-item";
        String primaryIdSsl = "ssl-item";
        String primaryIdAuth = "authentication-item";
        String primaryIdLogs = "logs-item";
        String primaryIdOther = "other-item";
        navigation.addPrimary(primaryIdStores, "Stores", "fa fa-exchange");
        navigation.addPrimary(primaryIdSsl, "SSL", "fa fa-file-o");
        navigation.addPrimary(primaryIdAuth, "Authentication", "fa fa-terminal");
        navigation.addPrimary(primaryIdLogs, "Logs", "fa fa-folder-o");
        navigation.addPrimary(primaryIdOther, "Other Settings", "fa fa-address-card-o");

        LabelBuilder labelBuilder = new LabelBuilder();

        // ===== store

        addResourceElement(CREDENTIAL_STORE,
                CREDENTIAL_STORE.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(CREDENTIAL_STORE.resource,
                                nodes -> updateResourceElement(CREDENTIAL_STORE.resource, nodes)))
                        .addComplexObjectAttribute(CREDENTIAL_REFERENCE)
                        .build(),
                primaryIdStores,
                Ids.build(CREDENTIAL_STORE.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(CREDENTIAL_STORE.resource));

        addResourceElement(FILTERING_KEY_STORE,
                FILTERING_KEY_STORE.resourceElement(mbuiContext,
                        () -> presenter.reload(FILTERING_KEY_STORE.resource,
                                nodes -> updateResourceElement(FILTERING_KEY_STORE.resource, nodes))),
                primaryIdStores,
                Ids.build(FILTERING_KEY_STORE.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(FILTERING_KEY_STORE.resource));

        addResourceElement(KEY_STORE,
                KEY_STORE.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(KEY_STORE.resource,
                                nodes -> updateResourceElement(KEY_STORE.resource, nodes)))
                        .addComplexObjectAttribute(CREDENTIAL_REFERENCE)
                        .build(),
                primaryIdStores,
                Ids.build(KEY_STORE.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(KEY_STORE.resource));

        Metadata metadata = mbuiContext.metadataRegistry().lookup(AddressTemplates.LDAP_KEY_STORE_TEMPLATE);
        ldapKeyStoreElement = new LdapKeyStoreElement(metadata, mbuiContext.tableButtonFactory(),
                mbuiContext.resources());
        registerAttachable(ldapKeyStoreElement);
        navigation.addSecondary(primaryIdStores, Ids.ELYTRON_LDAP_KEY_STORE, Names.LDAP_KEY_STORE,
                ldapKeyStoreElement.asElement());

        // ==== SSL elements

        addResourceElement(AGGREGATE_PROVIDERS,
                AGGREGATE_PROVIDERS.resourceElement(mbuiContext,
                        () -> presenter.reload(AGGREGATE_PROVIDERS.resource,
                                nodes -> updateResourceElement(AGGREGATE_PROVIDERS.resource, nodes))),
                primaryIdSsl,
                Ids.build(AGGREGATE_PROVIDERS.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(AGGREGATE_PROVIDERS.resource));

        addResourceElement(CLIENT_SSL_CONTEXT,
                CLIENT_SSL_CONTEXT.resourceElement(mbuiContext,
                        () -> presenter.reload(CLIENT_SSL_CONTEXT.resource,
                                nodes -> updateResourceElement(CLIENT_SSL_CONTEXT.resource, nodes))),
                primaryIdSsl,
                Ids.build(CLIENT_SSL_CONTEXT.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(CLIENT_SSL_CONTEXT.resource));

        addResourceElement(KEY_MANAGER,
                KEY_MANAGER.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(KEY_MANAGER.resource,
                                nodes -> updateResourceElement(KEY_MANAGER.resource, nodes)))
                        .addComplexObjectAttribute(CREDENTIAL_REFERENCE)
                        .build(),
                primaryIdSsl,
                Ids.build(KEY_MANAGER.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(KEY_MANAGER.resource));

        addResourceElement(PROVIDER_LOADER,
                PROVIDER_LOADER.resourceElement(mbuiContext,
                        () -> presenter.reload(PROVIDER_LOADER.resource,
                                nodes -> updateResourceElement(PROVIDER_LOADER.resource, nodes))),
                primaryIdSsl,
                Ids.build(PROVIDER_LOADER.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(PROVIDER_LOADER.resource));

        addResourceElement(SERVER_SSL_CONTEXT,
                SERVER_SSL_CONTEXT.resourceElement(mbuiContext,
                        () -> presenter.reload(SERVER_SSL_CONTEXT.resource,
                                nodes -> updateResourceElement(SERVER_SSL_CONTEXT.resource, nodes))),
                primaryIdSsl,
                Ids.build(SERVER_SSL_CONTEXT.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(SERVER_SSL_CONTEXT.resource));

        addResourceElement(SECURITY_DOMAIN,
                SECURITY_DOMAIN.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(SECURITY_DOMAIN.resource,
                                nodes -> updateResourceElement(SECURITY_DOMAIN.resource, nodes)))
                        .setComplexListAttribute("realms", "realm")
                        .build(),
                primaryIdSsl,
                Ids.build(SECURITY_DOMAIN.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(SECURITY_DOMAIN.resource));

        addResourceElement(TRUST_MANAGER,
                TRUST_MANAGER.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(TRUST_MANAGER.resource,
                                nodes -> updateResourceElement(TRUST_MANAGER.resource, nodes)))
                        .addComplexObjectAttribute("certificate-revocation-list")
                        .build(),
                primaryIdSsl,
                Ids.build(TRUST_MANAGER.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(TRUST_MANAGER.resource));

        // ===== Authentication

        addResourceElement(AUTHENTICATION_CONFIGURATION,
                AUTHENTICATION_CONFIGURATION.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(AUTHENTICATION_CONFIGURATION.resource,
                                nodes -> updateResourceElement(AUTHENTICATION_CONFIGURATION.resource, nodes)))
                        .addComplexObjectAttribute(CREDENTIAL_REFERENCE)
                        .build(),
                primaryIdAuth,
                Ids.build(AUTHENTICATION_CONFIGURATION.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(AUTHENTICATION_CONFIGURATION.resource));

        addResourceElement(AUTHENTICATION_CONTEXT,
                AUTHENTICATION_CONTEXT.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(AUTHENTICATION_CONTEXT.resource,
                                nodes -> updateResourceElement(AUTHENTICATION_CONTEXT.resource, nodes)))
                        // display all attributes as none of them are required=true
                        .setComplexListAttribute("match-rules", asList(
                                "match-abstract-type",
                                "match-abstract-type-authority",
                                "match-host",
                                "match-local-security-domain",
                                "match-no-user",
                                "match-path",
                                "match-port",
                                "match-protocol",
                                "match-urn",
                                "match-user",
                                ModelDescriptionConstants.AUTHENTICATION_CONFIGURATION,
                                SSL_CONTEXT))
                        .build(),
                primaryIdAuth,
                Ids.build(AUTHENTICATION_CONTEXT.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(AUTHENTICATION_CONTEXT.resource));

        // ======= Logs

        addResourceElement(FILE_AUDIT_LOG,
                FILE_AUDIT_LOG.resourceElement(mbuiContext,
                        () -> presenter.reload(FILE_AUDIT_LOG.resource,
                                nodes -> updateResourceElement(FILE_AUDIT_LOG.resource, nodes))),
                primaryIdLogs,
                Ids.build(FILE_AUDIT_LOG.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(FILE_AUDIT_LOG.resource));

        addResourceElement(PERIODIC_ROTATING_FILE_AUDIT_LOG,
                PERIODIC_ROTATING_FILE_AUDIT_LOG.resourceElement(mbuiContext,
                        () -> presenter.reload(PERIODIC_ROTATING_FILE_AUDIT_LOG.resource,
                                nodes -> updateResourceElement(PERIODIC_ROTATING_FILE_AUDIT_LOG.resource, nodes))),
                primaryIdLogs,
                Ids.build(PERIODIC_ROTATING_FILE_AUDIT_LOG.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(PERIODIC_ROTATING_FILE_AUDIT_LOG.resource));

        addResourceElement(SIZE_ROTATING_FILE_AUDIT_LOG,
                SIZE_ROTATING_FILE_AUDIT_LOG.resourceElement(mbuiContext,
                        () -> presenter.reload(SIZE_ROTATING_FILE_AUDIT_LOG.resource,
                                nodes -> updateResourceElement(SIZE_ROTATING_FILE_AUDIT_LOG.resource, nodes))),
                primaryIdLogs,
                Ids.build(SIZE_ROTATING_FILE_AUDIT_LOG.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(SIZE_ROTATING_FILE_AUDIT_LOG.resource));

        addResourceElement(SYSLOG_AUDIT_LOG,
                SYSLOG_AUDIT_LOG.resourceElement(mbuiContext,
                        () -> presenter.reload(SYSLOG_AUDIT_LOG.resource,
                                nodes -> updateResourceElement(SYSLOG_AUDIT_LOG.resource, nodes))),
                primaryIdLogs,
                Ids.build(SYSLOG_AUDIT_LOG.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(SYSLOG_AUDIT_LOG.resource));

        addResourceElement(AGGREGATE_SECURITY_EVENT_LISTENER,
                AGGREGATE_SECURITY_EVENT_LISTENER.resourceElement(mbuiContext,
                        () -> presenter.reload(AGGREGATE_SECURITY_EVENT_LISTENER.resource,
                                nodes -> updateResourceElement(AGGREGATE_SECURITY_EVENT_LISTENER.resource, nodes))),
                primaryIdLogs,
                Ids.build(AGGREGATE_SECURITY_EVENT_LISTENER.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(AGGREGATE_SECURITY_EVENT_LISTENER.resource));

        // ====== Other settings

        Metadata policyMetadata = mbuiContext.metadataRegistry().lookup(AddressTemplates.POLICY_TEMPLATE);
        policyElement = new PolicyElement(policyMetadata, mbuiContext.resources());
        registerAttachable(policyElement);
        navigation.addSecondary(primaryIdOther, Ids.ELYTRON_POLICY, Names.POLICY, policyElement.asElement());

        addResourceElement(DIR_CONTEXT,
                DIR_CONTEXT.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(DIR_CONTEXT.resource,
                                nodes -> updateResourceElement(DIR_CONTEXT.resource, nodes)))
                        .addComplexObjectAttribute(CREDENTIAL_REFERENCE)
                        .build(),
                primaryIdOther,
                Ids.build(DIR_CONTEXT.baseId, Ids.ENTRY_SUFFIX),
                labelBuilder.label(DIR_CONTEXT.resource));

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));

    }

    private void addResourceElement(ElytronResource resource, ResourceElement element,
            String primaryId, String secondaryId, @NonNls String text) {
        elements.put(resource.resource, element);
        registerAttachable(element);
        navigation.addSecondary(primaryId, secondaryId, text, element.asElement());
    }


    @Override
    public void updateResourceElement(String resource, List<NamedNode> nodes) {
        ResourceElement resourceElement = elements.get(resource);
        if (resourceElement != null) {
            resourceElement.update(nodes);
        }
    }

    @Override
    public void attach() {
        super.attach();

        ldapKeyStoreElement.attach();
        policyElement.attach();
    }

    @Override
    public void updateLdapKeyStore(List<NamedNode> model) {
        ldapKeyStoreElement.update(model);
    }

    @Override
    public void updatePolicy(NamedNode policy) {
        policyElement.update(policy);
    }

    @Override
    public void setPresenter(OtherSettingsPresenter presenter) {
        this.presenter = presenter;

        ldapKeyStoreElement.setPresenter(presenter);
        policyElement.setPresenter(presenter);
    }
}