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
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess", "unused"})
public class RealmsView extends MbuiViewImpl<RealmsPresenter>
        implements RealmsPresenter.MyView {

    public static RealmsView create(final MbuiContext mbuiContext) {
        return new Mbui_RealmsView(mbuiContext);
    }

    // @formatter:off
    @MbuiElement("realms-vertical-navigation") VerticalNavigation navigation;

    @MbuiElement("realms-aggregate-realm-table") Table<NamedNode> aggregateRealmTable;
    @MbuiElement("realms-aggregate-realm-form") Form<NamedNode> aggregateRealmForm;
    @MbuiElement("realms-caching-realm-table") Table<NamedNode> cachingRealmTable;
    @MbuiElement("realms-caching-realm-form") Form<NamedNode> cachingRealmForm;
    @MbuiElement("realms-custom-modifiable-realm-table") Table<NamedNode> customModifiableRealmTable;
    @MbuiElement("realms-custom-modifiable-realm-form") Form<NamedNode> customModifiableRealmForm;
    @MbuiElement("realms-custom-realm-table") Table<NamedNode> customRealmTable;
    @MbuiElement("realms-custom-realm-form") Form<NamedNode> customRealmForm;
    @MbuiElement("realms-filesystem-realm-table") Table<NamedNode> filesystemRealmTable;
    @MbuiElement("realms-filesystem-realm-form") Form<NamedNode> filesystemRealmForm;
    @MbuiElement("realms-identity-realm-table") Table<NamedNode> identityRealmTable;
    @MbuiElement("realms-identity-realm-form") Form<NamedNode> identityRealmForm;
    @MbuiElement("realms-jdbc-realm-table") Table<NamedNode> jdbcRealmTable;
    @MbuiElement("realms-jdbc-realm-form") Form<NamedNode> jdbcRealmForm;
    @MbuiElement("realms-key-store-realm-table") Table<NamedNode> keyStoreRealmTable;
    @MbuiElement("realms-key-store-realm-form") Form<NamedNode> keyStoreRealmForm;
    @MbuiElement("realms-ldap-realm-table") Table<NamedNode> ldapRealmTable;
    @MbuiElement("realms-ldap-realm-form") Form<NamedNode> ldapRealmForm;
    @MbuiElement("realms-properties-realm-table") Table<NamedNode> propertiesRealmTable;
    @MbuiElement("realms-properties-realm-form") Form<NamedNode> propertiesRealmForm;
    @MbuiElement("realms-token-realm-table") Table<NamedNode> tokenRealmTable;
    @MbuiElement("realms-token-realm-form") Form<NamedNode> tokenRealmForm;
    @MbuiElement("realms-constant-realm-mapper-table") Table<NamedNode> constantRealmMapperTable;
    @MbuiElement("realms-constant-realm-mapper-form") Form<NamedNode> constantRealmMapperForm;
    @MbuiElement("realms-custom-realm-mapper-table") Table<NamedNode> customRealmMapperTable;
    @MbuiElement("realms-custom-realm-mapper-form") Form<NamedNode> customRealmMapperForm;
    @MbuiElement("realms-mapped-regex-realm-mapper-table") Table<NamedNode> mappedRegexRealmMapperTable;
    @MbuiElement("realms-mapped-regex-realm-mapper-form") Form<NamedNode> mappedRegexRealmMapperForm;
    @MbuiElement("realms-simple-regex-realm-mapper-table") Table<NamedNode> simpleRegexRealmMapperTable;
    @MbuiElement("realms-simple-regex-realm-mapper-form") Form<NamedNode> simpleRegexRealmMapperForm;


    // @formatter:on

    RealmsView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void attach() {
        super.attach();
    }


    @Override
    public void updateAggregateRealm(final List<NamedNode> model) {
        aggregateRealmForm.clear();
        aggregateRealmTable.update(model);
    }

    @Override
    public void updateCachingRealm(final List<NamedNode> model) {
        cachingRealmForm.clear();
        cachingRealmTable.update(model);
    }

    @Override
    public void updateCustomModifiableRealm(final List<NamedNode> model) {
        customModifiableRealmForm.clear();
        customModifiableRealmTable.update(model);
    }

    @Override
    public void updateCustomRealm(final List<NamedNode> model) {
        customRealmForm.clear();
        customRealmTable.update(model);
    }

    @Override
    public void updateFilesystemRealm(final List<NamedNode> model) {
        filesystemRealmForm.clear();
        filesystemRealmTable.update(model);
    }

    @Override
    public void updateIdentityRealm(final List<NamedNode> model) {
        identityRealmForm.clear();
        identityRealmTable.update(model);
    }

    @Override
    public void updateJdbcRealm(final List<NamedNode> model) {
        jdbcRealmForm.clear();
        jdbcRealmTable.update(model);
    }

    @Override
    public void updateKeyStoreRealm(final List<NamedNode> model) {
        keyStoreRealmForm.clear();
        keyStoreRealmTable.update(model);
    }

    @Override
    public void updateLdapRealm(final List<NamedNode> model) {
        ldapRealmForm.clear();
        ldapRealmTable.update(model);
    }

    @Override
    public void updatePropertiesRealm(final List<NamedNode> model) {
        propertiesRealmForm.clear();
        propertiesRealmTable.update(model);
    }

    @Override
    public void updateTokenRealm(final List<NamedNode> model) {
        tokenRealmForm.clear();
        tokenRealmTable.update(model);
    }

    @Override
    public void updateConstantRealmMapper(final List<NamedNode> model) {
        constantRealmMapperForm.clear();
        constantRealmMapperTable.update(model);
    }

    @Override
    public void updateCustomRealmMapper(final List<NamedNode> model) {
        customRealmMapperForm.clear();
        customRealmMapperTable.update(model);
    }

    @Override
    public void updateMappedRegexRealmMapper(final List<NamedNode> model) {
        mappedRegexRealmMapperForm.clear();
        mappedRegexRealmMapperTable.update(model);
    }

    @Override
    public void updateSimpleRegexRealmMapper(final List<NamedNode> model) {
        simpleRegexRealmMapperForm.clear();
        simpleRegexRealmMapperTable.update(model);
    }
}