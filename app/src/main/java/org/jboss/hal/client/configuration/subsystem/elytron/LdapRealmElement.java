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
import org.jboss.hal.ballroom.Pages;
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
import org.jetbrains.annotations.NonNls;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.PAGE;
import static org.jboss.hal.resources.Ids.TAB;
import static org.jboss.hal.resources.Ids.TAB_CONTAINER;

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

    LdapRealmElement(final Metadata metadata, final TableButtonFactory tableButtonFactory, final Resources resources) {

        // LDAP Realm
        ldapRealmTable = new ModelNodeTable.Builder<NamedNode>(id(Ids.TABLE), metadata)
                .button(tableButtonFactory.add(metadata.getTemplate(), table -> presenter.addLdapRealm()))
                .button(tableButtonFactory.remove(Names.LDAP_REALM, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadLdapRealms()))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .column(Names.IDENTITY_ATTRIBUTE_MAPPING, this::showIdentityAttributeMapping, "15em") //NON-NLS
                .build();

        ldapRealmForm = new ModelNodeForm.Builder<NamedNode>(id(FORM), metadata)
                .onSave((form, changedValues) -> presenter.saveLdapRealm(form, changedValues))
                .build();

        Metadata imMetadata = metadata.forComplexAttribute(IDENTITY_MAPPING);
        identityMappingForm = new ModelNodeForm.Builder<>(id(IDENTITY_MAPPING, FORM), imMetadata)
                .customFormItem(NEW_IDENTITY_ATTRIBUTES, (ad) -> new MultiValueListItem(NEW_IDENTITY_ATTRIBUTES))
                .onSave((form, changedValues) -> presenter.saveIdentityMappingComplexAttribute(selectedLdapRealm,
                        IDENTITY_MAPPING, Names.IDENTITY_MAPPING, changedValues))
                .prepareReset(form -> presenter.resetIdentityMappingComplexAttribute(selectedLdapRealm,
                        IDENTITY_MAPPING, Names.IDENTITY_MAPPING, form))
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
        tabs.add(id(TAB), resources.constants().attributes(), ldapRealmForm.asElement());
        tabs.add(id(IDENTITY_MAPPING, TAB), Names.IDENTITY_MAPPING, identityMappingForm.asElement());
        tabs.add(id(USER_PASSWORD_MAPPER, TAB), Names.USER_PASSWORD_MAPPER, userPasswordMapperForm.asElement());
        tabs.add(id(OTP_CREDENTIAL_MAPPER, TAB), Names.OTP_CREDENTIAL_MAPPER,
                otpCredentialMapperForm.asElement());
        tabs.add(id(X509_CREDENTIAL_MAPPER, TAB), Names.X509_CREDENTIAL_MAPPER,
                x509CredentialMapperForm.asElement());

        HTMLElement ldapRealmSection = section()
                .add(h(1).textContent(Names.LDAP_REALM))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(ldapRealmTable)
                .add(tabs)
                .asElement();

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
                .addAll(iamTable, iamForm)
                .asElement();

        pages = new Pages(id(PAGE), ldapRealmSection);
        pages.addPage(id(PAGE), id(ATTRIBUTE_MAPPING, PAGE),
                () -> Names.LDAP_REALM + ": " + selectedLdapRealm,
                () -> Names.IDENTITY_ATTRIBUTE_MAPPING,
                iamSection);
    }

    private String id(@NonNls String... ids) {
        return Ids.build(Ids.ELYTRON_LDAP_REALM, ids);
    }

    @Override
    public HTMLElement asElement() {
        return pages.asElement();
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
            if (table.hasSelection()) {
                iamIndex = table.selectedRow().get(HAL_INDEX).asInt();
            } else {
                iamIndex = -1;
            }
        });
    }

    @Override
    public void setPresenter(final RealmsPresenter presenter) {
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

    private void showIdentityAttributeMapping(final NamedNode ldapRealm) {
        selectedLdapRealm = ldapRealm.getName();
        List<ModelNode> iamNodes = failSafeList(ldapRealm, IDENTITY_MAPPING + "/" + ATTRIBUTE_MAPPING);
        storeIndex(iamNodes);
        iamForm.clear();
        iamTable.update(iamNodes, modelNode -> Ids.build(modelNode.get(FROM).asString(), modelNode.get(TO).asString()));
        pages.showPage(id(ATTRIBUTE_MAPPING, PAGE));
    }
}
