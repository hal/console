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

public class LdapRealmElement implements IsElement<HTMLElement>, Attachable, HasPresenter<RealmsPresenter> {

    private final Table<NamedNode> ldapRealmTable;
    private final Form<NamedNode> ldapRealmForm;
    private final Form<ModelNode> identityMappingForm;
    private final Form<ModelNode> userPasswordMapperForm;
    private final Form<ModelNode> otpCredentialMapperForm;
    private final Form<ModelNode> x509CredentialMapperForm;
    private final Table<ModelNode> iamTable; // iam = identity mapping -> attribute-mapping
    private final Form<ModelNode> iamForm;
    private final Pages pages;
    private RealmsPresenter presenter;
    private String selectedLdapRealm;
    private int iamIndex;

    LdapRealmElement(Metadata metadata, TableButtonFactory tableButtonFactory, Resources resources) {

        // LDAP Realm
        ldapRealmTable = new ModelNodeTable.Builder<NamedNode>(id(Ids.TABLE), metadata)
                .button(tableButtonFactory.add(metadata.getTemplate(), table -> presenter.addLdapRealm()))
                .button(tableButtonFactory.remove(Names.LDAP_REALM, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadLdapRealms()))
                .nameColumn()
                .column(new InlineAction<>(Names.IDENTITY_ATTRIBUTE_MAPPING, this::showIdentityAttributeMapping),
                        "15em")
                .build();

        ldapRealmForm = new ModelNodeForm.Builder<NamedNode>(id(FORM), metadata)
                .exclude(IDENTITY_MAPPING + ".*")
                .onSave((form, changedValues) -> presenter.saveLdapRealm(form, changedValues))
                .build();

        Metadata imMetadata = metadata.forComplexAttribute(IDENTITY_MAPPING);
        identityMappingForm = new ModelNodeForm.Builder<>(id(IDENTITY_MAPPING, FORM), imMetadata)
                .exclude(USER_PASSWORD_MAPPER + ".*", OTP_CREDENTIAL_MAPPER + ".*", X509_CREDENTIAL_MAPPER + ".*")
                .customFormItem(NEW_IDENTITY_ATTRIBUTES, (ad) -> new MultiValueListItem(NEW_IDENTITY_ATTRIBUTES))
                .onSave((form, changedValues) -> presenter.saveIdentityMapping(selectedLdapRealm, changedValues))
                .prepareReset(form -> presenter.resetIdentityMapping(selectedLdapRealm, form))
                .build();

        Metadata upMetadata = metadata
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(USER_PASSWORD_MAPPER);
        userPasswordMapperForm = new ModelNodeForm.Builder<>(id(USER_PASSWORD_MAPPER, FORM), upMetadata)
                .singleton(
                        () -> presenter.pingIdentityMappingComplexAttribute(selectedLdapRealm, USER_PASSWORD_MAPPER),
                        () -> presenter.addIdentityMappingComplexAttribute(selectedLdapRealm, USER_PASSWORD_MAPPER,
                                Names.USER_PASSWORD_MAPPER))
                .onSave((form, changedValues) -> presenter.saveIdentityMappingComplexAttribute(selectedLdapRealm,
                        USER_PASSWORD_MAPPER, Names.USER_PASSWORD_MAPPER, changedValues))
                .prepareReset(form -> presenter.resetIdentityMappingComplexAttribute(selectedLdapRealm,
                        USER_PASSWORD_MAPPER, Names.USER_PASSWORD_MAPPER, form))
                .prepareRemove(form -> presenter.removeIdentityMappingComplexAttribute(selectedLdapRealm,
                        USER_PASSWORD_MAPPER, Names.USER_PASSWORD_MAPPER, form))
                .build();

        Metadata otpMetadata = metadata
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(OTP_CREDENTIAL_MAPPER);
        otpCredentialMapperForm = new ModelNodeForm.Builder<>(id(OTP_CREDENTIAL_MAPPER, FORM), otpMetadata)
                .singleton(
                        () -> presenter.pingIdentityMappingComplexAttribute(selectedLdapRealm, OTP_CREDENTIAL_MAPPER),
                        () -> presenter.addIdentityMappingComplexAttribute(selectedLdapRealm, OTP_CREDENTIAL_MAPPER,
                                Names.OTP_CREDENTIAL_MAPPER))
                .onSave((form, changedValues) -> presenter.saveIdentityMappingComplexAttribute(selectedLdapRealm,
                        OTP_CREDENTIAL_MAPPER, Names.OTP_CREDENTIAL_MAPPER, changedValues))
                .prepareReset(form -> presenter.resetIdentityMappingComplexAttribute(selectedLdapRealm,
                        OTP_CREDENTIAL_MAPPER, Names.OTP_CREDENTIAL_MAPPER, form))
                .prepareRemove(form -> presenter.removeIdentityMappingComplexAttribute(selectedLdapRealm,
                        OTP_CREDENTIAL_MAPPER, Names.OTP_CREDENTIAL_MAPPER, form))
                .build();

        Metadata x509Metadata = metadata
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(X509_CREDENTIAL_MAPPER);
        x509CredentialMapperForm = new ModelNodeForm.Builder<>(id(X509_CREDENTIAL_MAPPER, FORM), x509Metadata)
                .singleton(
                        () -> presenter.pingIdentityMappingComplexAttribute(selectedLdapRealm, X509_CREDENTIAL_MAPPER),
                        () -> presenter.addIdentityMappingComplexAttribute(selectedLdapRealm, X509_CREDENTIAL_MAPPER,
                                Names.X509_CREDENTIAL_MAPPER))
                .onSave((form, changedValues) -> presenter.saveIdentityMappingComplexAttribute(selectedLdapRealm,
                        X509_CREDENTIAL_MAPPER, Names.X509_CREDENTIAL_MAPPER, changedValues))
                .prepareReset(form -> presenter.resetIdentityMappingComplexAttribute(selectedLdapRealm,
                        X509_CREDENTIAL_MAPPER, Names.X509_CREDENTIAL_MAPPER, form))
                .prepareRemove(form -> presenter.removeIdentityMappingComplexAttribute(selectedLdapRealm,
                        X509_CREDENTIAL_MAPPER, Names.X509_CREDENTIAL_MAPPER, form))
                .build();

        Tabs tabs = new Tabs(id(TAB_CONTAINER));
        tabs.add(id(TAB), resources.constants().attributes(), ldapRealmForm.element());
        tabs.add(id(IDENTITY_MAPPING, TAB), Names.IDENTITY_MAPPING, identityMappingForm.element());
        tabs.add(id(USER_PASSWORD_MAPPER, TAB), Names.USER_PASSWORD_MAPPER, userPasswordMapperForm.element());
        tabs.add(id(OTP_CREDENTIAL_MAPPER, TAB), Names.OTP_CREDENTIAL_MAPPER,
                otpCredentialMapperForm.element());
        tabs.add(id(X509_CREDENTIAL_MAPPER, TAB), Names.X509_CREDENTIAL_MAPPER,
                x509CredentialMapperForm.element());

        HTMLElement ldapRealmSection = section()
                .add(h(1).textContent(Names.LDAP_REALM))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(ldapRealmTable)
                .add(tabs).element();

        // identity mapping -> attribute mapping
        Metadata iamMetadata = metadata
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(ATTRIBUTE_MAPPING);
        iamTable = new ModelNodeTable.Builder<>(id(ATTRIBUTE_MAPPING, Ids.TABLE), iamMetadata)
                .button(tableButtonFactory.add(iamMetadata.getTemplate(),
                        table -> presenter.addIdentityAttributeMapping(selectedLdapRealm)))
                .button(tableButtonFactory.remove(iamMetadata.getTemplate(),
                        table -> presenter.removeIdentityAttributeMapping(selectedLdapRealm, iamIndex)))
                .columns(FROM, TO)
                .build();
        iamForm = new ModelNodeForm.Builder<>(id(ATTRIBUTE_MAPPING, FORM), iamMetadata)
                .onSave(((form, changedValues) -> presenter.saveIdentityAttributeMapping(selectedLdapRealm,
                        iamIndex, changedValues)))
                .unsorted()
                .build();
        HTMLElement iamSection = section()
                .add(h(1).textContent(Names.IDENTITY_ATTRIBUTE_MAPPING))
                .add(p().textContent(iamMetadata.getDescription().getDescription()))
                .addAll(iamTable, iamForm).element();

        pages = new Pages(id(PAGES), id(PAGE), ldapRealmSection);
        pages.addPage(id(PAGE), id(ATTRIBUTE_MAPPING, PAGE),
                () -> Names.LDAP_REALM + ": " + selectedLdapRealm,
                () -> Names.IDENTITY_ATTRIBUTE_MAPPING,
                iamSection);
    }

    private String id(String... ids) {
        return Ids.build(Ids.ELYTRON_LDAP_REALM, ids);
    }

    @Override
    public HTMLElement element() {
        return pages.element();
    }

    @Override
    public void attach() {
        ldapRealmForm.attach();
        ldapRealmTable.attach();
        ldapRealmTable.bindForm(ldapRealmForm);
        ldapRealmTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                NamedNode row = table.selectedRow();
                selectedLdapRealm = row.getName();
                identityMappingForm.view(failSafeGet(row, IDENTITY_MAPPING));
                userPasswordMapperForm.view(failSafeGet(row, IDENTITY_MAPPING + "/" + USER_PASSWORD_MAPPER));
                otpCredentialMapperForm.view(failSafeGet(row, IDENTITY_MAPPING + "/" + OTP_CREDENTIAL_MAPPER));
                x509CredentialMapperForm.view(failSafeGet(row, IDENTITY_MAPPING + "/" + X509_CREDENTIAL_MAPPER));
            } else {
                selectedLdapRealm = null;
                userPasswordMapperForm.clear();
                otpCredentialMapperForm.clear();
                x509CredentialMapperForm.clear();
            }
        });

        identityMappingForm.attach();
        userPasswordMapperForm.attach();
        otpCredentialMapperForm.attach();
        x509CredentialMapperForm.attach();

        iamForm.attach();
        iamTable.attach();
        iamTable.bindForm(iamForm);
        iamTable.onSelectionChange(table -> {
            iamTable.enableButton(1, iamTable.hasSelection());
            if (table.hasSelection()) {
                iamIndex = table.selectedRow().get(HAL_INDEX).asInt();
            } else {
                iamIndex = -1;
            }
        });
    }

    @Override
    public void setPresenter(RealmsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        ldapRealmForm.clear();
        identityMappingForm.clear();
        userPasswordMapperForm.clear();
        otpCredentialMapperForm.clear();
        x509CredentialMapperForm.clear();
        ldapRealmTable.update(nodes);

        if (id(ATTRIBUTE_MAPPING, PAGE).equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(resource -> selectedLdapRealm.equals(resource.getName()))
                    .findFirst()
                    .ifPresent(this::showIdentityAttributeMapping);
        }
    }

    private void showIdentityAttributeMapping(NamedNode ldapRealm) {
        selectedLdapRealm = ldapRealm.getName();
        List<ModelNode> iamNodes = failSafeList(ldapRealm, IDENTITY_MAPPING + "/" + ATTRIBUTE_MAPPING);
        storeIndex(iamNodes);
        iamForm.clear();
        iamTable.update(iamNodes, modelNode -> Ids.build(modelNode.get(FROM).asString(), modelNode.get(TO).asString()));
        iamTable.enableButton(1, iamTable.hasSelection());
        pages.showPage(id(ATTRIBUTE_MAPPING, PAGE));
    }
}
