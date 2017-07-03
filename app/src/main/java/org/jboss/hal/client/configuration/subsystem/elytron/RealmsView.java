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
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class RealmsView extends HalViewImpl implements RealmsPresenter.MyView, ElytronView {

    private ResourceView aggregateRealmView;
    private ResourceView cachingRealmView;
    private ResourceView customModifiableRealmView;
    private ResourceView customRealmView;
    private ResourceView filesystemRealmView;
    private ResourceView identityRealmView;
    private ResourceView jdbcRealmView;
    private JdbcRealmElement jdbcRealmElement;
    private ResourceView keystoreRealmView;
    private ResourceView ldapRealmView;
    private ResourceView propertiesRealmView;
    private ResourceView tokenRealmView;
    private ResourceView constantRealmMapperView;
    private ResourceView customRealmMapperView;
    private ResourceView mappedRegexRealmView;
    private ResourceView simpleRegexRealmView;

    private RealmsPresenter presenter;

    @Inject
    public RealmsView(final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory,
            final Resources resources) {

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        String primaryIdSecurityRealm = "security-realm-item";
        String primaryIdRealmMapper = "realm-mapper-item";
        navigation.addPrimary(primaryIdSecurityRealm, "Security Realm", "fa fa-file-o");
        navigation.addPrimary(primaryIdRealmMapper, "Realm Mappers", "fa fa-desktop");


        aggregateRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_AGGREGATE_REALM, "Aggregate Realm", AGGREGATE_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        cachingRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_CACHING_REALM, "Caching Realm", CACHING_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        customModifiableRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_CUSTOM_MODIFIABLE_REALM, "Custom Modifiable Realm", CUSTOM_MODIFIABLE_REALM_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        customRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_CUSTOM_REALM, "Custom Realm", CUSTOM_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        filesystemRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_FILESYSTEM_REALM, "Filesystem Realm", FILESYSTEM_REALM_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        identityRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_IDENTITY_REALM, "Identity Realm", IDENTITY_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        jdbcRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_JDBC_REALM, "JDBC Realm", JDBC_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddButtonHandler(table -> presenter.launchOnAddJDBCRealm())
                .build()
                .addComplexAttributeAsPage("principal-query")
                .create();

        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_ADDRESS);
        jdbcRealmElement = new JdbcRealmElement(metadata, tableButtonFactory, resources);
        navigation.addSecondary(primaryIdSecurityRealm, Ids.ELYTRON_JDBC_REALM + "2", Names.JDBC_REALM + "2",
                jdbcRealmElement.asElement());

        keystoreRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_KEYSTORE_REALM, "Keystore Realm", KEYSTORE_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        ldapRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_LDAP_REALM, "LDAP Realm", LDAP_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddButtonHandler(table -> presenter.addLDAPRealm())
                .build()
                .addComplexAttributeAsTab("identity-mapping")
                .addComplexAttributeAsTab("identity-mapping.user-password-mapper")
                .addComplexAttributeAsTab("identity-mapping.otp-credential-mapper")
                .addComplexAttributeAsTab("identity-mapping.x509-credential-mapper")
                .create();

        propertiesRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_PROPERTIES_REALM, "Properties Realm", PROPERTIES_REALM_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddButtonHandler(table -> presenter.addPropertiesRealm())
                .build()
                .addComplexAttributeAsTab("users-properties")
                .addComplexAttributeAsTab("groups-properties")
                .create();

        tokenRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdSecurityRealm,
                Ids.ELYTRON_TOKEN_REALM, "Token Realm", TOKEN_REALM_ADDRESS, this, () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .addComplexAttributeAsTab("jwt")
                .addComplexAttributeAsTab("oauth2-introspection")
                .create();

        constantRealmMapperView = new ResourceView.Builder(tableButtonFactory, primaryIdRealmMapper,
                Ids.ELYTRON_CONSTANT_REALM_MAPPER, "Constant Realm Mapper", CONSTANT_REALM_MAPPER_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        customRealmMapperView = new ResourceView.Builder(tableButtonFactory, primaryIdRealmMapper,
                Ids.ELYTRON_CUSTOM_REALM_MAPPER, "Custom Realm Mapper", CUSTOM_REALM_MAPPER_ADDRESS, this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        mappedRegexRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdRealmMapper,
                Ids.ELYTRON_MAPPED_REGEX_REALM_MAPPER, "Mapped Regex Realm Mapper", MAPPED_REGEX_REALM_MAPPER_ADDRESS,
                this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        simpleRegexRealmView = new ResourceView.Builder(tableButtonFactory, primaryIdRealmMapper,
                Ids.ELYTRON_SIMPLE_REGEX_REALM_MAPPER, "Simple Regex Realm Mapper", SIMPLE_REGEX_REALM_MAPPER_ADDRESS,
                this,
                () -> presenter.reload())
                .setNavigation(navigation)
                .setMetadataRegistry(metadataRegistry)
                .setTableAddCallback((name, address) -> presenter.reload())
                .build()
                .create();

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));

    }

    private void showJDBCRealmsPage(final String s, final String name) {
    }

    @Override
    public void registerComponents(final Attachable first, final Attachable... rest) {
        registerAttachable(first, rest);
    }

    @Override
    public void attach() {
        super.attach();

        aggregateRealmView.bindTableToForm();
        cachingRealmView.bindTableToForm();
        customModifiableRealmView.bindTableToForm();
        customRealmView.bindTableToForm();
        filesystemRealmView.bindTableToForm();
        identityRealmView.bindTableToForm();
        jdbcRealmView.bindTableToForm();
        jdbcRealmElement.attach();
        keystoreRealmView.bindTableToForm();
        ldapRealmView.bindTableToForm();
        propertiesRealmView.bindTableToForm();
        tokenRealmView.bindTableToForm();
        constantRealmMapperView.bindTableToForm();
        customRealmMapperView.bindTableToForm();
        mappedRegexRealmView.bindTableToForm();
        simpleRegexRealmView.bindTableToForm();
    }


    @Override
    public void updateAggregateRealm(final List<NamedNode> model) {
        aggregateRealmView.getForm().clear();
        aggregateRealmView.getTable().update(model);
    }

    @Override
    public void updateCachingRealm(final List<NamedNode> model) {
        cachingRealmView.getForm().clear();
        cachingRealmView.getTable().update(model);
    }

    @Override
    public void updateCustomModifiableRealm(final List<NamedNode> model) {
        customModifiableRealmView.getForm().clear();
        customModifiableRealmView.getTable().update(model);
    }

    @Override
    public void updateCustomRealm(final List<NamedNode> model) {
        customRealmView.getForm().clear();
        customRealmView.getTable().update(model);
    }

    @Override
    public void updateFilesystemRealm(final List<NamedNode> model) {
        filesystemRealmView.getForm().clear();
        filesystemRealmView.getTable().update(model);
    }

    @Override
    public void updateIdentityRealm(final List<NamedNode> model) {
        identityRealmView.getForm().clear();
        identityRealmView.getTable().update(model);
    }

    @Override
    public void updateJdbcRealm(final List<NamedNode> model) {
        jdbcRealmView.getForm().clear();
        jdbcRealmView.getTable().update(model);
        jdbcRealmElement.update(model);
    }

    @Override
    public void updateKeyStoreRealm(final List<NamedNode> model) {
        keystoreRealmView.getForm().clear();
        keystoreRealmView.getTable().update(model);
    }

    @Override
    public void updateLdapRealm(final List<NamedNode> model) {
        ldapRealmView.getForm().clear();
        ldapRealmView.getTable().update(model);
    }

    @Override
    public void updatePropertiesRealm(final List<NamedNode> model) {
        propertiesRealmView.getForm().clear();
        propertiesRealmView.getTable().update(model);
    }

    @Override
    public void updateTokenRealm(final List<NamedNode> model) {
        tokenRealmView.getForm().clear();
        tokenRealmView.getTable().update(model);
    }

    @Override
    public void updateConstantRealmMapper(final List<NamedNode> model) {
        constantRealmMapperView.getForm().clear();
        constantRealmMapperView.getTable().update(model);
    }

    @Override
    public void updateCustomRealmMapper(final List<NamedNode> model) {
        customRealmMapperView.getForm().clear();
        customRealmMapperView.getTable().update(model);
    }

    @Override
    public void updateMappedRegexRealmMapper(final List<NamedNode> model) {
        mappedRegexRealmView.getForm().clear();
        mappedRegexRealmView.getTable().update(model);
    }

    @Override
    public void updateSimpleRegexRealmMapper(final List<NamedNode> model) {
        simpleRegexRealmView.getForm().clear();
        simpleRegexRealmView.getTable().update(model);
    }

    @Override
    public void setPresenter(final RealmsPresenter presenter) {
        this.presenter = presenter;
        aggregateRealmView.setPresenter(presenter);
        cachingRealmView.setPresenter(presenter);
        customModifiableRealmView.setPresenter(presenter);
        customRealmView.setPresenter(presenter);
        filesystemRealmView.setPresenter(presenter);
        identityRealmView.setPresenter(presenter);
        jdbcRealmView.setPresenter(presenter);
        jdbcRealmElement.setPresenter(presenter);
        keystoreRealmView.setPresenter(presenter);
        ldapRealmView.setPresenter(presenter);
        propertiesRealmView.setPresenter(presenter);
        tokenRealmView.setPresenter(presenter);
        constantRealmMapperView.setPresenter(presenter);
        customRealmMapperView.setPresenter(presenter);
        mappedRegexRealmView.setPresenter(presenter);
        simpleRegexRealmView.setPresenter(presenter);
    }
}