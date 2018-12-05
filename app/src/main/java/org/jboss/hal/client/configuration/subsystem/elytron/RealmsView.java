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

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.ResourceElement;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.elytron.ElytronResource.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.USERS_PROPERTIES;
import static org.jboss.hal.resources.Ids.ITEM;

public class RealmsView extends HalViewImpl implements RealmsPresenter.MyView {

    private final Map<String, ResourceElement> elements;
    private final JdbcRealmElement jdbcRealmElement;
    private final LdapRealmElement ldapRealmElement;
    private RealmsPresenter presenter;
    private VerticalNavigation navigation;

    @Inject
    @SuppressWarnings("HardCodedStringLiteral")
    public RealmsView(MbuiContext mbuiContext) {

        elements = new HashMap<>();
        navigation = new VerticalNavigation();
        registerAttachable(navigation);

        String primaryIdSecurityRealm = "security-realm-item";
        String primaryIdRealmMapper = "realm-mapper-item";
        navigation.addPrimary(primaryIdSecurityRealm, "Security Realm", "fa fa-file-o");
        navigation.addPrimary(primaryIdRealmMapper, "Realm Mappers", "fa fa-desktop");

        // ========= security realm

        addResourceElement(AGGREGATE_REALM,
                AGGREGATE_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(AGGREGATE_REALM.resource,
                                nodes -> updateResourceElement(AGGREGATE_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(AGGREGATE_REALM.baseId, ITEM),
                "Aggregate Realm");

        addResourceElement(CACHING_REALM,
                CACHING_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(CACHING_REALM.resource,
                                nodes -> updateResourceElement(CACHING_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(CACHING_REALM.baseId, ITEM),
                "Caching Realm");

        addResourceElement(CUSTOM_MODIFIABLE_REALM,
                CUSTOM_MODIFIABLE_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(CUSTOM_MODIFIABLE_REALM.resource,
                                nodes -> updateResourceElement(CUSTOM_MODIFIABLE_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(CUSTOM_MODIFIABLE_REALM.baseId, ITEM),
                "Custom Modifiable Realm");

        addResourceElement(CUSTOM_REALM,
                CUSTOM_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(CUSTOM_REALM.resource,
                                nodes -> updateResourceElement(CUSTOM_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(CUSTOM_REALM.baseId, ITEM),
                "Custom Realm");

        addResourceElement(FILESYSTEM_REALM,
                FILESYSTEM_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(FILESYSTEM_REALM.resource,
                                nodes -> updateResourceElement(FILESYSTEM_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(FILESYSTEM_REALM.baseId, ITEM),
                "Filesystem Realm");

        addResourceElement(IDENTITY_REALM,
                IDENTITY_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(IDENTITY_REALM.resource,
                                nodes -> updateResourceElement(IDENTITY_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(IDENTITY_REALM.baseId, ITEM),
                "Identity Realm");

        Metadata metadata = mbuiContext.metadataRegistry().lookup(AddressTemplates.JDBC_REALM_TEMPLATE);
        jdbcRealmElement = new JdbcRealmElement(metadata, mbuiContext.tableButtonFactory(), mbuiContext.resources());
        registerAttachable(jdbcRealmElement);
        navigation.addSecondary(primaryIdSecurityRealm, Ids.build(Ids.ELYTRON_JDBC_REALM, ITEM), Names.JDBC_REALM,
                jdbcRealmElement.element());

        addResourceElement(KEY_STORE_REALM,
                KEY_STORE_REALM.resourceElement(mbuiContext,
                        () -> presenter.reload(KEY_STORE_REALM.resource,
                                nodes -> updateResourceElement(KEY_STORE_REALM.resource, nodes))),
                primaryIdSecurityRealm,
                Ids.build(KEY_STORE_REALM.baseId, ITEM),
                "Key Store Realm");

        Metadata mtLdapRealm = mbuiContext.metadataRegistry().lookup(AddressTemplates.LDAP_REALM_TEMPLATE);
        ldapRealmElement = new LdapRealmElement(mtLdapRealm, mbuiContext.tableButtonFactory(), mbuiContext.resources());
        registerAttachable(ldapRealmElement);
        navigation.addSecondary(primaryIdSecurityRealm, Ids.build(Ids.ELYTRON_LDAP_REALM, ITEM), Names.LDAP_REALM,
                ldapRealmElement.element());

        addResourceElement(PROPERTIES_REALM,
                PROPERTIES_REALM.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(PROPERTIES_REALM.resource,
                                nodes -> updateResourceElement(PROPERTIES_REALM.resource, nodes)))
                        .onAdd(() -> presenter.addPropertiesRealm())
                        .addComplexObjectAttribute("groups-properties")
                        .addComplexObjectAttribute(USERS_PROPERTIES)
                        .build(),
                primaryIdSecurityRealm,
                Ids.build(PROPERTIES_REALM.baseId, ITEM),
                Names.PROPERTIES_REALM);

        addResourceElement(TOKEN_REALM,
                TOKEN_REALM.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(TOKEN_REALM.resource,
                                nodes -> updateResourceElement(TOKEN_REALM.resource, nodes)))
                        .addComplexObjectAttribute("jwt")
                        .addComplexObjectAttribute("oauth2-introspection")
                        .build(),
                primaryIdSecurityRealm,
                Ids.build(TOKEN_REALM.baseId, ITEM),
                "Token Realm");

        // =========== realm mapper

        addResourceElement(CONSTANT_REALM_MAPPER,
                CONSTANT_REALM_MAPPER.resourceElement(mbuiContext,
                        () -> presenter.reload(CONSTANT_REALM_MAPPER.resource,
                                nodes -> updateResourceElement(CONSTANT_REALM_MAPPER.resource, nodes))),
                primaryIdRealmMapper,
                Ids.build(CONSTANT_REALM_MAPPER.baseId, ITEM),
                "Constant Realm Mapper");

        addResourceElement(CUSTOM_REALM_MAPPER,
                CUSTOM_REALM_MAPPER.resourceElement(mbuiContext,
                        () -> presenter.reload(CUSTOM_REALM_MAPPER.resource,
                                nodes -> updateResourceElement(CUSTOM_REALM_MAPPER.resource, nodes))),
                primaryIdRealmMapper,
                Ids.build(CUSTOM_REALM_MAPPER.baseId, ITEM),
                "Custom Realm Mapper");

        addResourceElement(MAPPED_REGEX_REALM_MAPPER,
                MAPPED_REGEX_REALM_MAPPER.resourceElement(mbuiContext,
                        () -> presenter.reload(MAPPED_REGEX_REALM_MAPPER.resource,
                                nodes -> updateResourceElement(MAPPED_REGEX_REALM_MAPPER.resource, nodes))),
                primaryIdRealmMapper,
                Ids.build(MAPPED_REGEX_REALM_MAPPER.baseId, ITEM),
                "Mapped Regex Realm Mapper");

        addResourceElement(SIMPLE_REGEX_REALM_MAPPER,
                SIMPLE_REGEX_REALM_MAPPER.resourceElement(mbuiContext,
                        () -> presenter.reload(SIMPLE_REGEX_REALM_MAPPER.resource,
                                nodes -> updateResourceElement(SIMPLE_REGEX_REALM_MAPPER.resource, nodes))),
                primaryIdRealmMapper,
                Ids.build(SIMPLE_REGEX_REALM_MAPPER.baseId, ITEM),
                "Simple Regex Realm Mapper");

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));

    }

    private void addResourceElement(ElytronResource resource, ResourceElement element,
            String primaryId, String secondaryId, @NonNls String text) {
        elements.put(resource.resource, element);
        registerAttachable(element);
        navigation.addSecondary(primaryId, secondaryId, text, element.element());
    }

    @Override
    public void updateResourceElement(String resource, List<NamedNode> nodes) {
        ResourceElement resourceElement = elements.get(resource);
        if (resourceElement != null) {
            resourceElement.update(nodes);
        }
    }

    @Override
    public void updateJdbcRealm(List<NamedNode> nodes) {
        jdbcRealmElement.update(nodes);
    }

    @Override
    public void updateLdapRealm(List<NamedNode> nodes) {
        ldapRealmElement.update(nodes);
    }

    @Override
    public void setPresenter(RealmsPresenter presenter) {
        this.presenter = presenter;
        jdbcRealmElement.setPresenter(presenter);
        ldapRealmElement.setPresenter(presenter);
    }
}