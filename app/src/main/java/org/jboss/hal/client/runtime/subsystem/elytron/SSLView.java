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

import javax.inject.Inject;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.PreTextItem;
import org.jboss.hal.ballroom.table.Button;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Constants;
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
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.KEY_MANAGER_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.SECURITY_DOMAIN_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.elytron.AddressTemplates.TRUST_MANAGER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CERTIFICATE_AUTHORITY_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHANGE_ACCOUNT_KEY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CREATE_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEACTIVATE_ACCOUNT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GET_METADATA;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INIT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEY_MANAGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.METADATA;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_IDENTITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RELOAD_CERTIFICATE_REVOCATION_LIST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_DOMAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRUST_MANAGER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UPDATE_ACCOUNT;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.TABLE;

public class SSLView extends HalViewImpl implements SSLPresenter.MyView {

    private final Table<NamedNode> caaTable;
    private final Form<NamedNode> caaForm;
    private final Table<NamedNode> keyManagerTable;
    private final Form<NamedNode> keyManagerForm;
    private final Table<NamedNode> securityDomainTable;
    private final Form<NamedNode> securityDomainForm;
    private final Table<NamedNode> trustManagerTable;
    private final Form<NamedNode> trustManagerForm;
    private final PreTextItem caaMetadata;
    private Map<String, String> caaMetadataCache = new HashMap<>();
    private SSLPresenter presenter;

    @Inject
    public SSLView(MetadataRegistry metadataRegistry, Resources resources) {

        VerticalNavigation nav = new VerticalNavigation();
        LabelBuilder labelBuilder = new LabelBuilder();
        Constants cons = resources.constants();

        // ----------------- certificate authority account
        Metadata certAuthorityMeta = metadataRegistry.lookup(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE);
        String createAccDesc = certAuthorityMeta.forOperation(CREATE_ACCOUNT).getDescription().getDescription();
        String deactivateAccDesc = certAuthorityMeta.forOperation(DEACTIVATE_ACCOUNT).getDescription().getDescription();
        String updateAccDesc = certAuthorityMeta.forOperation(UPDATE_ACCOUNT).getDescription().getDescription();
        String metadataAccDesc = certAuthorityMeta.forOperation(GET_METADATA).getDescription().getDescription();
        String changeAccDesc = certAuthorityMeta.forOperation(CHANGE_ACCOUNT_KEY).getDescription().getDescription();
        caaTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(CERTIFICATE_AUTHORITY_ACCOUNT, TABLE),
                certAuthorityMeta)
                .button(new Button<>(cons.create(), createAccDesc,
                        table -> presenter.createAccount(table.selectedRow().getName()),
                        Constraint.executable(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE, CREATE_ACCOUNT)))

                .button(new Button<>(cons.deactivate(), deactivateAccDesc,
                        table -> presenter.deactivateAccount(table.selectedRow().getName()),
                        Constraint.executable(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE, DEACTIVATE_ACCOUNT)))

                .button(new Button<>(cons.onlineUpdates(), updateAccDesc,
                        table -> presenter.updateAccount(table.selectedRow().getName()),
                        Constraint.executable(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE, UPDATE_ACCOUNT)))

                .button(new Button<>(cons.getMetadata(), metadataAccDesc,
                        table -> presenter.getMetadata(table.selectedRow().getName(), this::updateCertificateMetadata),
                        Constraint.executable(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE, GET_METADATA)))

                .button(new Button<>(cons.changeAccountKey(), changeAccDesc,
                        table -> presenter.changeAccountKey(table.selectedRow().getName()),
                        Constraint.executable(CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE, CHANGE_ACCOUNT_KEY)))

                .nameColumn()
                .build();

        caaMetadata = new PreTextItem(METADATA);
        caaMetadata.setEnabled(false);
        caaForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(CERTIFICATE_AUTHORITY_ACCOUNT, FORM), certAuthorityMeta)
                .readOnly()
                .includeRuntime()
                .unboundFormItem(caaMetadata, 4)
                .build();

        String caaTitle = labelBuilder.label(CERTIFICATE_AUTHORITY_ACCOUNT);
        HTMLElement caaSection = section()
                .add(h(1).textContent(caaTitle))
                .add(p().textContent(certAuthorityMeta.getDescription().getDescription()))
                .add(caaTable)
                .add(caaForm).element();

        nav.addPrimary(Ids.ELYTRON_CERTIFICATE_AUTHORITY_ACCOUNT, caaTitle, fontAwesome("exchange"), caaSection);

        // ----------------- key manager
        Metadata keyManagerMeta = metadataRegistry.lookup(KEY_MANAGER_TEMPLATE);
        keyManagerTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(KEY_MANAGER, TABLE), keyManagerMeta)
                .button(resources.constants().initialize(),
                        table -> presenter.initKeyManager(table.selectedRow().getName()),
                        Constraint.executable(KEY_MANAGER_TEMPLATE, INIT))
                .nameColumn()
                .build();

        keyManagerForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(KEY_MANAGER, FORM), keyManagerMeta)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement keyManagerSection = section()
                .add(h(1).textContent(Names.KEY_MANAGER))
                .add(p().textContent(keyManagerMeta.getDescription().getDescription()))
                .add(keyManagerTable)
                .add(keyManagerForm).element();

        nav.addPrimary(Ids.ELYTRON_KEY_MANAGER, Names.KEY_MANAGER, pfIcon("settings"), keyManagerSection);

        // ----------------- security domain
        Metadata secDomainMeta = metadataRegistry.lookup(SECURITY_DOMAIN_TEMPLATE);
        securityDomainTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(SECURITY_DOMAIN, TABLE), secDomainMeta)
                .button(resources.constants().readIdentity(),
                        table -> presenter.readIdentity(secDomainMeta, table.selectedRow().getName()),
                        Constraint.executable(SECURITY_DOMAIN_TEMPLATE, READ_IDENTITY))
                .nameColumn()
                .build();

        securityDomainForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(SECURITY_DOMAIN, FORM), secDomainMeta)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement secDomainSection = section()
                .add(h(1).textContent(Names.SECURITY_DOMAIN))
                .add(p().textContent(secDomainMeta.getDescription().getDescription()))
                .add(securityDomainTable)
                .add(securityDomainForm).element();

        nav.addPrimary(Ids.ELYTRON_SECURITY_DOMAIN, Names.SECURITY_DOMAIN, pfIcon("cluster"), secDomainSection);

        // ----------------- trust manager
        Metadata trustMeta = metadataRegistry.lookup(TRUST_MANAGER_TEMPLATE);
        String initDesc = trustMeta.forOperation(INIT).getDescription().getDescription();
        trustManagerTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(TRUST_MANAGER, TABLE), trustMeta)
                .button(new Button<>(resources.constants().initialize(), initDesc,
                        table -> presenter.initTrustManager(table.selectedRow().getName()),
                        Constraint.executable(TRUST_MANAGER_TEMPLATE, INIT)))
                .button(new Button<>(resources.constants().reloadCRL(), labelBuilder.label(RELOAD_CERTIFICATE_REVOCATION_LIST),
                        table -> presenter.reloadCRL(table.selectedRow().getName()),
                        Constraint.executable(TRUST_MANAGER_TEMPLATE, RELOAD_CERTIFICATE_REVOCATION_LIST)))
                .nameColumn()
                .build();

        trustManagerForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(TRUST_MANAGER, FORM), trustMeta)
                .readOnly()
                .includeRuntime()
                .build();

        HTMLElement trustManagerSection = section()
                .add(h(1).textContent(Names.TRUST_MANAGER))
                .add(p().textContent(trustMeta.getDescription().getDescription()))
                .add(trustManagerTable)
                .add(trustManagerForm).element();
        nav.addPrimary(Ids.ELYTRON_TRUST_MANAGER, Names.TRUST_MANAGER, pfIcon("resource-pool"), trustManagerSection);

        registerAttachables(asList(nav, caaTable, caaForm, keyManagerTable, keyManagerForm, securityDomainTable,
                securityDomainForm, trustManagerTable, trustManagerForm));

        initElement(row()
                .add(column()
                        .addAll(nav.panes())));
    }

    @Override
    public void attach() {
        super.attach();

        caaTable.enableButton(0, false);
        caaTable.enableButton(1, false);
        caaTable.enableButton(2, false);
        caaTable.enableButton(3, false);
        caaTable.enableButton(4, false);

        caaTable.bindForm(caaForm);
        caaTable.onSelectionChange(table -> {
            table.enableButton(0, table.hasSelection());
            table.enableButton(1, table.hasSelection());
            table.enableButton(2, table.hasSelection());
            table.enableButton(3, table.hasSelection());
            table.enableButton(4, table.hasSelection());
            if (table.hasSelection()) {
                String account = table.selectedRow().getName();
                String value = caaMetadataCache.get(account);
                if (value != null) {
                    caaMetadata.setValue(value);
                } else {
                    caaMetadata.clearValue();
                }
            }
        });

        keyManagerTable.bindForm(keyManagerForm);
        keyManagerTable.enableButton(0, false);
        keyManagerTable.onSelectionChange(table -> table.enableButton(0, table.hasSelection()));

        securityDomainTable.bindForm(securityDomainForm);
        securityDomainTable.enableButton(0, false);
        securityDomainTable.onSelectionChange(table -> table.enableButton(0, table.hasSelection()));

        trustManagerTable.bindForm(trustManagerForm);
        trustManagerTable.enableButton(0, false);
        trustManagerTable.enableButton(1, false);
        trustManagerTable.onSelectionChange(table -> {
            table.enableButton(0, table.hasSelection());
            table.enableButton(1, table.hasSelection());
        });
    }

    private void updateCertificateMetadata(String data) {
        caaMetadata.setValue(data);
        caaMetadataCache.put(caaTable.selectedRow().getName(), data);
    }

    @Override
    public void updateCertificateAuthorityAccount(List<NamedNode> items) {
        caaForm.clear();
        caaTable.update(items);
    }

    @Override
    public void updateKeyManager(List<NamedNode> items) {
        keyManagerForm.clear();
        keyManagerTable.update(items);
    }

    @Override
    public void updateSecurityDomain(List<NamedNode> items) {
        securityDomainForm.clear();
        securityDomainTable.update(items);
    }

    @Override
    public void updateTrustManager(List<NamedNode> items) {
        trustManagerForm.clear();
        trustManagerTable.update(items);
    }

    @Override
    public void setPresenter(SSLPresenter presenter) {
        this.presenter = presenter;
    }
}
