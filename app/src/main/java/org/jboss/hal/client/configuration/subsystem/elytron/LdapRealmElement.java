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

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class LdapRealmElement implements IsElement<HTMLElement>, Attachable, HasPresenter<RealmsPresenter> {

    private final Table<NamedNode> ldapRealmTable;
    private final Form<NamedNode> ldapRealmForm;
    private final Form<NamedNode> identityMappingForm;
    private final Form<NamedNode> userPasswordMapperForm;
    private final Form<NamedNode> otpCredentialMapperForm;
    private final Form<NamedNode> x509CredentialMapperForm;
    private final Table<ModelNode> iamTable; // iam = identity mapping -> attribute-mapping
    private final Form<ModelNode> iamForm;
    private final Pages pages;
    private RealmsPresenter presenter;
    private String selectedLdapRealm;
    private NewItemAttributesItem newIdentityAttributes;
    private String selectedIam;
    private int iamIndex;

    LdapRealmElement(final Metadata metadata, final TableButtonFactory tableButtonFactory, final Resources resources) {

        // LDAP Realm
        ldapRealmTable = new ModelNodeTable.Builder<NamedNode>(Ids.ELYTRON_LDAP_REALM_TABLE, metadata)
                .button(tableButtonFactory.add(metadata.getTemplate(), table -> presenter.addLdapRealm()))
                .button(tableButtonFactory.remove(Names.LDAP_REALM, metadata.getTemplate(),
                        (table) -> table.selectedRow().getName(), () -> presenter.reloadLdapRealms()))
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .column(Names.IDENTITY_ATTRIBUTE_MAPPING, this::showIdentityAttributeMapping, "15em") //NON-NLS
                .build();

        ldapRealmForm = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_LDAP_REALM_FORM, metadata)
                .onSave((form, changedValues) -> presenter.saveLdapRealm(form, changedValues))
                .build();

        Metadata identMetadata = metadata.forComplexAttribute(IDENTITY_MAPPING);
        newIdentityAttributes = new NewItemAttributesItem("new-identity-attributes");
        identityMappingForm = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_LDAP_REALM_FORM, identMetadata)
                .unboundFormItem(newIdentityAttributes)
                .onSave((form, changedValues) -> presenter.saveComplexForm(Names.IDENTITY_MAPPING,
                        ldapRealmTable.selectedRow().getName(), IDENTITY_MAPPING, changedValues, identMetadata))
                .build();

        Metadata userPwdMetadata = metadata
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(USER_PASSWORD_MAPPER);
        userPasswordMapperForm = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_LDAP_REALM_FORM, userPwdMetadata)
                .onSave((form, changedValues) -> presenter.saveComplexForm(Names.USER_PASSWORD_MAPPER,
                        ldapRealmTable.selectedRow().getName(), IDENTITY_MAPPING + "." + USER_PASSWORD_MAPPER,
                        changedValues, userPwdMetadata))
                .build();

        Metadata otpMetadata = metadata.forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(OTP_CREDENTIAL_MAPPER);
        otpCredentialMapperForm = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_LDAP_REALM_FORM, otpMetadata)
                .onSave((form, changedValues) -> presenter.saveComplexForm(Names.OTP_CREDENTIAL_MAPPER,
                        ldapRealmTable.selectedRow().getName(), IDENTITY_MAPPING + "." + OTP_CREDENTIAL_MAPPER,
                        changedValues, otpMetadata))
                .build();

        Metadata x509Metadata = metadata.repackageComplexAttribute(IDENTITY_MAPPING + "." + X509_CREDENTIAL_MAPPER, false, false, false);
        x509CredentialMapperForm = new ModelNodeForm.Builder<NamedNode>(Ids.ELYTRON_LDAP_REALM_FORM, x509Metadata)
                .onSave((form, changedValues) -> presenter.saveComplexForm(Names.X509_CREDENTIAL_MAPPER,
                        ldapRealmTable.selectedRow().getName(), IDENTITY_MAPPING + "." + X509_CREDENTIAL_MAPPER,
                        changedValues, x509Metadata))
                .build();

        Tabs tabs = new Tabs();
        tabs.add(Ids.ELYTRON_LDAP_REALM_TAB, resources.constants().attributes(), ldapRealmForm.asElement());
        tabs.add(Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_TAB, Names.IDENTITY_MAPPING, identityMappingForm.asElement());
        tabs.add(Ids.ELYTRON_USER_PASSWORD_MAPPER_TAB, Names.USER_PASSWORD_MAPPER, userPasswordMapperForm.asElement());
        tabs.add(Ids.ELYTRON_OTP_CREDENTIAL_MAPPER_TAB, Names.OTP_CREDENTIAL_MAPPER,
                otpCredentialMapperForm.asElement());
        tabs.add(Ids.ELYTRON_X509_CREDENTIAL_MAPPER_TAB, Names.X509_CREDENTIAL_MAPPER,
                x509CredentialMapperForm.asElement());

        HTMLElement ldapRealmSection = section()
                .add(h(1).textContent(Names.LDAP_REALM))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(ldapRealmTable)
                .add(tabs)
                .asElement();

        // identity mapping - attribute mapping
        Metadata iamMetadata = metadata
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(ATTRIBUTE_MAPPING);
        iamTable = new ModelNodeTable.Builder<>(Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_TABLE, iamMetadata)
                .button(tableButtonFactory.add(iamMetadata.getTemplate(),
                        table -> presenter.addIdentityAttributeMapping(selectedLdapRealm)))
                .button(tableButtonFactory.remove(iamMetadata.getTemplate(),
                        table -> presenter.removeIdentityAttributeMapping(selectedLdapRealm, iamIndex)))
                .column("from")
                //.column("to")
                //.column("reference")
                .build();
        iamForm = new ModelNodeForm.Builder<>(Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_FORM, iamMetadata)
                .onSave(((form, changedValues) -> presenter.saveIdentityAttributeMapping(selectedLdapRealm,
                        form.getModel().get(HAL_INDEX).asInt(), changedValues)))
                .unsorted()
                .build();
        HTMLElement iamSection = section()
                .add(h(1).textContent(Names.IDENTITY_ATTRIBUTE_MAPPING))
                .add(p().textContent(iamMetadata.getDescription().getDescription()))
                .addAll(iamTable, iamForm)
                .asElement();

        pages = new Pages(Ids.ELYTRON_LDAP_REALM_PAGE, ldapRealmSection);
        pages.addPage(Ids.ELYTRON_LDAP_REALM_PAGE, Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_PAGE,
                () -> Names.LDAP_REALM + ": " + selectedLdapRealm,
                () -> Names.IDENTITY_ATTRIBUTE_MAPPING,
                iamSection);
    }

    @Override
    public HTMLElement asElement() {
        return pages.asElement();
    }

    @Override
    public void attach() {
        ldapRealmTable.attach();
        ldapRealmForm.attach();
        identityMappingForm.attach();
        userPasswordMapperForm.attach();
        otpCredentialMapperForm.attach();
        x509CredentialMapperForm.attach();
        newIdentityAttributes.attach();

        ldapRealmTable.bindForm(ldapRealmForm);

        // special binding because of the nested complex attributes
        ldapRealmTable.onSelectionChange(table -> {
            if (table.hasSelection()) {
                NamedNode row = table.selectedRow();
                if (row.get(IDENTITY_MAPPING).hasDefined(NEW_IDENTITY_ATTRIBUTES)) {
                    newIdentityAttributes.setValue(row.get(IDENTITY_MAPPING).get(NEW_IDENTITY_ATTRIBUTES));
                }
                identityMappingForm.view(new NamedNode(row.get(IDENTITY_MAPPING)));
                userPasswordMapperForm.view(new NamedNode(row.get(IDENTITY_MAPPING).get(USER_PASSWORD_MAPPER)));
                otpCredentialMapperForm.view(new NamedNode(row.get(IDENTITY_MAPPING).get(OTP_CREDENTIAL_MAPPER)));
                x509CredentialMapperForm.view(new NamedNode(row.get(IDENTITY_MAPPING).get(X509_CREDENTIAL_MAPPER)));
            } else {
                newIdentityAttributes.clearValue();
                userPasswordMapperForm.clear();
                otpCredentialMapperForm.clear();
                x509CredentialMapperForm.clear();
            }
        });

        iamTable.attach();
        iamForm.attach();
        iamTable.bindForm(iamForm);

    }

    @Override
    public void setPresenter(final RealmsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> nodes) {
        ldapRealmForm.clear();
        ldapRealmTable.update(nodes);

        if (Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_PAGE.equals(pages.getCurrentId())) {
            nodes.stream()
                    .filter(resource -> selectedLdapRealm.equals(resource.getName()))
                    .findFirst()
                    .ifPresent(this::showIdentityAttributeMapping);
        }
    }

    private void showIdentityAttributeMapping(final NamedNode resourceName) {
        selectedLdapRealm = resourceName.getName();
        List<ModelNode> iamNodes = failSafeList(resourceName, IDENTITY_MAPPING + "/" + ATTRIBUTE_MAPPING);
        storeIndex(iamNodes);
        iamForm.clear();
        iamTable.update(iamNodes, modelNode -> modelNode.get(FROM).asString());
        pages.showPage(Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_PAGE);
    }
}
