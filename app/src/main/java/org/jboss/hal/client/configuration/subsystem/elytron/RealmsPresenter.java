/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.configuration.PathsAutoComplete;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.RequireAtLeastOneAttributeValidation;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeBoolean;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.move;

public class RealmsPresenter extends MbuiPresenter<RealmsPresenter.MyView, RealmsPresenter.MyProxy>
        implements SupportsExpertMode {

    static final String[] KEY_MAPPERS = new String[]{
            "clear-password-mapper",
            "bcrypt-mapper",
            "modular-crypt-mapper",
            "salted-simple-digest-mapper",
            "simple-digest-mapper",
            "scram-mapper",
    };
    private static final String DOT = ".";


    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private MetadataRegistry metadataRegistry;

    @Inject
    public RealmsPresenter(EventBus eventBus,
            RealmsPresenter.MyView view,
            RealmsPresenter.MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            ComplexAttributeOperations ca,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            MetadataRegistry metadataRegistry,
            Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.ca = ca;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.SECURITY_REALMS),
                        resources.constants().settings(), Names.SECURITY_REALMS);
    }

    @Override
    public void reload() {
        ResourceAddress address = ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(
                ElytronResource.AGGREGATE_REALM.resource,
                ElytronResource.CACHING_REALM.resource,
                ElytronResource.CUSTOM_MODIFIABLE_REALM.resource,
                ElytronResource.CUSTOM_REALM.resource,
                ElytronResource.FILESYSTEM_REALM.resource,
                ElytronResource.IDENTITY_REALM.resource,
                ElytronResource.JDBC_REALM.resource,
                ElytronResource.KEY_STORE_REALM.resource,
                ElytronResource.LDAP_REALM.resource,
                ElytronResource.PROPERTIES_REALM.resource,
                ElytronResource.TOKEN_REALM.resource,
                ElytronResource.CONSTANT_REALM_MAPPER.resource,
                ElytronResource.CUSTOM_REALM_MAPPER.resource,
                ElytronResource.MAPPED_REGEX_REALM_MAPPER.resource,
                ElytronResource.SIMPLE_REGEX_REALM_MAPPER.resource),
                result -> {
                    int i = 0;
                    getView().updateResourceElement(ElytronResource.AGGREGATE_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CACHING_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CUSTOM_MODIFIABLE_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CUSTOM_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.FILESYSTEM_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.IDENTITY_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateJdbcRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.KEY_STORE_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateLdapRealm(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.PROPERTIES_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.TOKEN_REALM.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CONSTANT_REALM_MAPPER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CUSTOM_REALM_MAPPER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.MAPPED_REGEX_REALM_MAPPER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    //noinspection UnusedAssignment
                    getView().updateResourceElement(ElytronResource.SIMPLE_REGEX_REALM_MAPPER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                });
    }

    void reload(String resource, Consumer<List<NamedNode>> callback) {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, resource,
                children -> callback.accept(asNamedNodes(children)));
    }


    // ------------------------------------------------------ JDBC realm

    void reloadJdbcRealms() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, ModelDescriptionConstants.JDBC_REALM,
                children -> getView().updateJdbcRealm(asNamedNodes(children)));
    }

    void addJdbcRealm() {
        Metadata metadata = metadataRegistry.lookup(JDBC_REALM_TEMPLATE)
                .forComplexAttribute(PRINCIPAL_QUERY);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(Ids.ELYTRON_JDBC_REALM, Ids.ADD), metadata)
                .addOnly()
                .requiredOnly()
                .unboundFormItem(nameItem, 0)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.JDBC_REALM), form,
                (n1, model) -> {
                    ModelNode payload = new ModelNode();
                    payload.get(PRINCIPAL_QUERY).add(model);
                    crud.add(Names.JDBC_REALM, nameItem.getValue(), JDBC_REALM_TEMPLATE, payload,
                            (n2, address) -> reloadJdbcRealms());
                });
        dialog.show();
    }

    void addPrincipalQuery(String jdbcRealm) {
        ca.listAdd(Ids.build(Ids.ELYTRON_JDBC_REALM, PRINCIPAL_QUERY, Ids.ADD), jdbcRealm, PRINCIPAL_QUERY,
                Names.PRINCIPAL_QUERY, JDBC_REALM_TEMPLATE, this::reloadJdbcRealms);
    }

    void savePrincipalQuery(String jdbcRealm, int pqIndex, Map<String, Object> changedValues) {
        ca.save(jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY, pqIndex, JDBC_REALM_TEMPLATE,
                changedValues, this::reloadJdbcRealms);
    }

    void removePrincipalQuery(String jdbcRealm, int pqIndex) {
        ca.remove(jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY, pqIndex, JDBC_REALM_TEMPLATE,
                this::reloadJdbcRealms);
    }

    void addKeyMapper(String jdbcRealm, ModelNode principalQuery, int pqIndex,
            String keyMapper) {
        // WFLYELY00034: A principal query can only have a single key mapper
        boolean moreThanOne = false;
        for (String km : KEY_MAPPERS) {
            if (failSafeGet(principalQuery, km).isDefined()) {
                moreThanOne = true;
                break;
            }
        }

        if (moreThanOne) {
            MessageEvent.fire(getEventBus(),
                    Message.error(resources.messages().moreThanOneKeyMapperForPrincipalQuery()));

        } else {
            String type = new LabelBuilder().label(keyMapper);
            Metadata metadata = metadataRegistry.lookup(JDBC_REALM_TEMPLATE)
                    .forComplexAttribute(PRINCIPAL_QUERY)
                    .forComplexAttribute(keyMapper);
            String id = Ids.build(Ids.ELYTRON_JDBC_REALM, PRINCIPAL_QUERY, keyMapper, Ids.ADD);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                    .addOnly()
                    .requiredOnly()
                    .build();
            AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type), form,
                    (name, model) -> ca.add(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type,
                            JDBC_REALM_TEMPLATE, model, RealmsPresenter.this::reloadJdbcRealms));
            dialog.show();
        }
    }

    Operation pingKeyMapper(String jdbcRealm, ModelNode principalQuery, String keyMapper) {
        if (jdbcRealm != null) {
            ResourceAddress address = JDBC_REALM_TEMPLATE.resolve(statementContext, jdbcRealm);
            int pqIndex = principalQuery.get(HAL_INDEX).asInt();
            return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                    .param(NAME, keyMapperAttribute(pqIndex, keyMapper))
                    .build();
        }
        return null;
    }

    void saveKeyMapper(String jdbcRealm, int pqIndex, String keyMapper, Map<String, Object> changedValues) {
        String type = new LabelBuilder().label(keyMapper);
        ca.save(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, JDBC_REALM_TEMPLATE,
                changedValues, this::reloadJdbcRealms);
    }

    void resetKeyMapper(String jdbcRealm, int pqIndex, String keyMapper, Form<ModelNode> form) {
        String type = new LabelBuilder().label(keyMapper);
        Metadata metadata = metadataRegistry.lookup(JDBC_REALM_TEMPLATE)
                .forComplexAttribute(PRINCIPAL_QUERY)
                .forComplexAttribute(keyMapper);
        ca.reset(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, JDBC_REALM_TEMPLATE,
                metadata, form, new Form.FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reloadJdbcRealms();
                    }
                });
    }

    void removeKeyMapper(String jdbcRealm, int pqIndex, String keyMapper, Form<ModelNode> form) {
        String type = new LabelBuilder().label(keyMapper);
        ca.remove(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, JDBC_REALM_TEMPLATE,
                new Form.FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(Form<ModelNode> form) {
                        reloadJdbcRealms();
                    }
                });
    }

    void addAttributeMapping(String jdbcRealm, int pqIndex) {
        Metadata metadata = metadataRegistry.lookup(JDBC_REALM_TEMPLATE)
                .forComplexAttribute(PRINCIPAL_QUERY)
                .forComplexAttribute(ATTRIBUTE_MAPPING);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(
                Ids.build(Ids.ELYTRON_JDBC_REALM, ATTRIBUTE_MAPPING, Ids.ADD), metadata)
                .addOnly()
                .include(TO, INDEX)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.ATTRIBUTE_MAPPING),
                form, (name, model) -> ca.listAdd(jdbcRealm, attributeMappingAttribute(pqIndex),
                Names.ATTRIBUTE_MAPPING, JDBC_REALM_TEMPLATE, model, this::reloadJdbcRealms));
        dialog.show();
    }

    void saveAttributeMapping(String jdbcRealm, int pqIndex, int amIndex,
            Map<String, Object> changedValues) {
        ca.save(jdbcRealm, attributeMappingAttribute(pqIndex), Names.ATTRIBUTE_MAPPING, amIndex,
                JDBC_REALM_TEMPLATE, changedValues, this::reloadJdbcRealms);
    }

    void removeAttributeMapping(String jdbcRealm, int pqIndex, int amIndex) {
        ca.remove(jdbcRealm, attributeMappingAttribute(pqIndex), Names.ATTRIBUTE_MAPPING, amIndex,
                JDBC_REALM_TEMPLATE, this::reloadJdbcRealms);
    }

    private String keyMapperAttribute(int pqIndex, String keyMapper) {
        return PRINCIPAL_QUERY + "[" + pqIndex + "]." + keyMapper;
    }

    private String attributeMappingAttribute(int pqIndex) {
        return PRINCIPAL_QUERY + "[" + pqIndex + "]." + ATTRIBUTE_MAPPING;
    }


    // ------------------------------------------------------ LDAP Realm

    void reloadLdapRealms() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, ModelDescriptionConstants.LDAP_REALM,
                children -> getView().updateLdapRealm(asNamedNodes(children)));
    }

    void addLdapRealm() {
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE);
        Metadata imMetadata = metadata.forComplexAttribute(IDENTITY_MAPPING, true);
        imMetadata.copyComplexAttributeAttributes(asList(RDN_IDENTIFIER, SEARCH_BASE_DN, USE_RECURSIVE_SEARCH),
                metadata);

        NameItem nameItem = new NameItem();
        String id = Ids.build(Ids.ELYTRON_LDAP_REALM, Ids.ADD);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly()
                .unboundFormItem(nameItem, 0)
                .include(DIR_CONTEXT, DIRECT_VERIFICATION, ALLOW_BLANK_PASSWORD, RDN_IDENTIFIER, SEARCH_BASE_DN,
                        USE_RECURSIVE_SEARCH)
                .build();

        new AddResourceDialog(resources.messages().addResourceTitle(Names.LDAP_REALM), form,
                (name, model) -> {
                    if (model != null) {
                        move(model, RDN_IDENTIFIER, IDENTITY_MAPPING + "/" + RDN_IDENTIFIER);
                        move(model, SEARCH_BASE_DN, IDENTITY_MAPPING + "/" + SEARCH_BASE_DN);
                        // workaround for "WFLYCTL0380: Attribute 'identity-mapping.search-base-dn' needs to be set or
                        // passed before attribute 'identity-mapping.use-recursive-search' can be correctly set"
                        // only pass USE_RECURSIVE_SEARCH if true (not default-value)
                        if (failSafeBoolean(model, USE_RECURSIVE_SEARCH)) {
                            move(model, USE_RECURSIVE_SEARCH, IDENTITY_MAPPING + "/" + USE_RECURSIVE_SEARCH);
                        } else {
                            model.remove(USE_RECURSIVE_SEARCH);
                        }
                    }
                    ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, nameItem.getValue());
                    crud.add(Names.IDENTITY_ATTRIBUTE_MAPPING, name, address, model, (n, a) -> reloadLdapRealms());
                }).show();
    }

    void saveLdapRealm(Form<NamedNode> form, Map<String, Object> changedValues) {
        crud.save(Names.LDAP_REALM, form.getModel().getName(), AddressTemplates.LDAP_REALM_TEMPLATE, changedValues,
                this::reloadLdapRealms);
    }

    void addIdentityMappingComplexAttribute(String ldapRealm, String complexAttribute, String type) {
        String id = Ids.build(Ids.ELYTRON_LDAP_REALM, complexAttribute, Ids.ADD);
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(complexAttribute);
        boolean requiredAttributes = !metadata.getDescription().getRequiredAttributes(ATTRIBUTES).isEmpty();
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly();
        if (requiredAttributes) {
            builder.requiredOnly();
        }
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type), builder.build(),
                (name, model) -> ca.add(ldapRealm, IDENTITY_MAPPING + DOT + complexAttribute, type,
                        LDAP_REALM_TEMPLATE, model, this::reloadLdapRealms));
        dialog.show();
    }

    Operation pingIdentityMappingComplexAttribute(String ldapRealm, String complexAttribute) {
        if (ldapRealm != null) {
            ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, ldapRealm);
            return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                    .param(NAME, IDENTITY_MAPPING + DOT + complexAttribute)
                    .build();
        }
        return null;
    }

    void saveIdentityMapping(String ldapRealm, Map<String, Object> changedValues) {
        ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, ldapRealm);
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING);
        ca.save(IDENTITY_MAPPING, Names.IDENTITY_MAPPING, address, changedValues, metadata, this::reloadLdapRealms);
    }

    void resetIdentityMapping(String ldapRealm, Form<ModelNode> form) {
        ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, ldapRealm);
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING);
        ca.reset(IDENTITY_MAPPING, Names.IDENTITY_MAPPING, address, metadata, form, this::reloadLdapRealms);
    }

    void saveIdentityMappingComplexAttribute(String ldapRealm, String complexAttribute, String type,
            Map<String, Object> changedValues) {
        ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, ldapRealm);
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(complexAttribute);
        ca.save(IDENTITY_MAPPING + DOT + complexAttribute, type, address, changedValues, metadata,
                this::reloadLdapRealms);
    }

    void resetIdentityMappingComplexAttribute(String ldapRealm, String complexAttribute, String type,
            Form<ModelNode> form) {
        ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, ldapRealm);
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(complexAttribute);
        ca.reset(IDENTITY_MAPPING + DOT + complexAttribute, type, address, metadata, form, this::reloadLdapRealms);
    }

    void removeIdentityMappingComplexAttribute(String ldapRealm, String complexAttribute, String type,
            Form<ModelNode> form) {
        ca.remove(ldapRealm, IDENTITY_MAPPING + DOT + complexAttribute, type, LDAP_REALM_TEMPLATE,
                new Form.FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(Form<ModelNode> form) {
                        reloadLdapRealms();
                    }
                });
    }

    // TODO Fix requires validation:
    // If 'search-rekursive' which requires 'filter' and which defaults to true is not touched
    // and 'filter' is set, the RequiredByValidation does not detect an validation error, because
    // 'search-rekursive' isEmptyOrDefault()
    void addIdentityAttributeMapping(String selectedLdapRealm) {
        Metadata iamMetadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(ATTRIBUTE_MAPPING);
        String id = Ids.build(Ids.ELYTRON_LDAP_REALM, ATTRIBUTE_MAPPING, Ids.ADD);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, iamMetadata)
                .addOnly()
                .include(FROM, TO)
                .build();
        form.addFormValidation(new RequireAtLeastOneAttributeValidation<>(
                asList(FROM, TO), resources));
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.ATTRIBUTE_MAPPING),
                form, (name, model) ->
                ca.listAdd(selectedLdapRealm, IDENTITY_MAPPING + DOT + ATTRIBUTE_MAPPING,
                        Names.IDENTITY_ATTRIBUTE_MAPPING, LDAP_REALM_TEMPLATE, model, this::reloadLdapRealms));
        dialog.show();
    }

    void saveIdentityAttributeMapping(String selectedLdapRealm, int iamIndex, Map<String, Object> changedValues) {
        // passed before attribute 'identity-mapping.attribute-mapping[n].search-recursive' can be correctly set"

        ca.save(selectedLdapRealm, IDENTITY_MAPPING + DOT + ATTRIBUTE_MAPPING,
                Names.IDENTITY_ATTRIBUTE_MAPPING, iamIndex, AddressTemplates.LDAP_REALM_TEMPLATE,
                changedValues, this::reloadLdapRealms);
    }

    void removeIdentityAttributeMapping(String selectedLdapRealm, int iamIndex) {
        ca.remove(selectedLdapRealm, IDENTITY_MAPPING + DOT + ATTRIBUTE_MAPPING,
                Names.IDENTITY_ATTRIBUTE_MAPPING, iamIndex, AddressTemplates.LDAP_REALM_TEMPLATE,
                this::reloadLdapRealms);
    }


    // ------------------------------------------------------  properties realm

    void addPropertiesRealm() {
        Metadata metadata = metadataRegistry.lookup(PROPERTIES_REALM_TEMPLATE);
        Metadata upMetadata = metadata.forComplexAttribute(USERS_PROPERTIES, true);
        upMetadata.copyComplexAttributeAttributes(asList(PATH, RELATIVE_TO), metadata);

        String id = Ids.build(Ids.ELYTRON_PROPERTIES_REALM, Ids.ADD);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly()
                .unboundFormItem(nameItem, 0)
                .include(PATH, RELATIVE_TO, GROUPS_ATTRIBUTE)
                .build();
        form.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());

        new AddResourceDialog(resources.messages().addResourceTitle(Names.PROPERTIES_REALM), form, (name, model) -> {
            if (model != null) {
                move(model, PATH, USERS_PROPERTIES + "/" + PATH);
                move(model, RELATIVE_TO, USERS_PROPERTIES + "/" + RELATIVE_TO);
            }
            ResourceAddress address = PROPERTIES_REALM_TEMPLATE.resolve(statementContext, nameItem.getValue());
            crud.add(Names.PROPERTIES_REALM, name, address, model, (n, a) ->
                    reload(PROPERTIES_REALM, nodes ->
                            getView().updateResourceElement(PROPERTIES_REALM, nodes)));
        }).show();
    }


    @ProxyCodeSplit
    @Requires(value = {
            AGGREGATE_REALM_ADDRESS,
            CACHING_REALM_ADDRESS,
            CONSTANT_REALM_MAPPER_ADDRESS,
            CUSTOM_MODIFIABLE_REALM_ADDRESS,
            CUSTOM_REALM_ADDRESS,
            CUSTOM_REALM_MAPPER_ADDRESS,
            FILESYSTEM_REALM_ADDRESS,
            IDENTITY_REALM_ADDRESS,
            JDBC_REALM_ADDRESS,
            KEY_STORE_REALM_ADDRESS,
            LDAP_REALM_ADDRESS,
            MAPPED_REGEX_REALM_MAPPER_ADDRESS,
            PROPERTIES_REALM_ADDRESS,
            SIMPLE_REGEX_REALM_MAPPER_ADDRESS,
            TOKEN_REALM_ADDRESS})
    @NameToken(NameTokens.ELYTRON_SECURITY_REALMS)
    public interface MyProxy extends ProxyPlace<RealmsPresenter> {
    }


    // @formatter:off
    public interface MyView extends MbuiView<RealmsPresenter> {
        void updateResourceElement(String resource, List<NamedNode> nodes);
        void updateJdbcRealm(List<NamedNode> nodes);
        void updateLdapRealm(List<NamedNode> nodes);
    }
    // @formatter:on
}
