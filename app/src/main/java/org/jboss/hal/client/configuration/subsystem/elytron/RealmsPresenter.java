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
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.configuration.PathsAutoComplete;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
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
import org.jetbrains.annotations.NonNls;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.move;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class RealmsPresenter extends MbuiPresenter<RealmsPresenter.MyView, RealmsPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
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
    public interface MyProxy extends ProxyPlace<RealmsPresenter> {}

    public interface MyView extends MbuiView<RealmsPresenter> {
        void updateResourceElement(String resource, List<NamedNode> nodes);
        void updateJdbcRealm(List<NamedNode> nodes);
        void updateLdapRealm(List<NamedNode> nodes);
    }
    // @formatter:on


    final static String[] KEY_MAPPERS = new String[]{
            "clear-password-mapper",
            "bcrypt-mapper",
            "salted-simple-digest-mapper",
            "simple-digest-mapper",
            "scram-mapper"
    };
    @NonNls private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RealmsPresenter.class);

    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private MetadataRegistry metadataRegistry;

    @Inject
    public RealmsPresenter(final EventBus eventBus,
            final RealmsPresenter.MyView view,
            final RealmsPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final ComplexAttributeOperations ca,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
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
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
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

    void saveComplexForm(final String title, final String name, String complexAttributeName,
            final Map<String, Object> changedValues, final Metadata metadata) {
        String type = new LabelBuilder().label(metadata.getTemplate().lastName());
        ca.save(name, complexAttributeName, type, metadata.getTemplate(), changedValues, this::reload);

    }

    // ------------------------------------------------------ JDBC realm

    void reloadJdbcRealms() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, ModelDescriptionConstants.JDBC_REALM,
                children -> getView().updateJdbcRealm(asNamedNodes(children)));
    }

    void addJdbcRealm() {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_TEMPLATE)
                .forComplexAttribute(PRINCIPAL_QUERY);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(Ids.ELYTRON_JDBC_REALM, Ids.ADD_SUFFIX), metadata)
                .addOnly()
                .requiredOnly()
                .unboundFormItem(nameItem, 0)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.JDBC_REALM), form,
                (n1, model) -> {
                    ModelNode payload = new ModelNode();
                    payload.get(PRINCIPAL_QUERY).add(model);
                    crud.add(Names.JDBC_REALM, nameItem.getValue(), AddressTemplates.JDBC_REALM_TEMPLATE, payload,
                            (n2, address) -> reloadJdbcRealms());
                });
        dialog.show();
    }

    void addPrincipalQuery(String jdbcRealm) {
        ca.listAdd(Ids.build(Ids.ELYTRON_JDBC_REALM, PRINCIPAL_QUERY, Ids.ADD_SUFFIX), jdbcRealm, PRINCIPAL_QUERY,
                Names.PRINCIPAL_QUERY, AddressTemplates.JDBC_REALM_TEMPLATE, this::reloadJdbcRealms);
    }

    void savePrincipalQuery(String jdbcRealm, int pqIndex, Map<String, Object> changedValues) {
        ca.save(jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY, pqIndex, AddressTemplates.JDBC_REALM_TEMPLATE,
                changedValues, this::reloadJdbcRealms);
    }

    void removePrincipalQuery(String jdbcRealm, int pqIndex) {
        ca.remove(jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY, pqIndex, AddressTemplates.JDBC_REALM_TEMPLATE,
                this::reloadJdbcRealms);
    }

    void addKeyMapper(String jdbcRealm, ModelNode principalQuery, int pqIndex,
            final String keyMapper) {
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
            Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_TEMPLATE)
                    .forComplexAttribute(PRINCIPAL_QUERY)
                    .forComplexAttribute(keyMapper);
            String id = Ids.build(Ids.ELYTRON_JDBC_REALM, PRINCIPAL_QUERY, keyMapper, Ids.ADD_SUFFIX);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                    .addOnly()
                    .requiredOnly()
                    .build();
            AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type), form,
                    (name, model) -> ca.add(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type,
                            AddressTemplates.JDBC_REALM_TEMPLATE, model, RealmsPresenter.this::reloadJdbcRealms));
            dialog.show();
        }
    }

    Operation pingKeyMapper(String jdbcRealm, ModelNode principalQuery, String keyMapper) {
        ResourceAddress address = AddressTemplates.JDBC_REALM_TEMPLATE.resolve(statementContext, jdbcRealm);
        int pqIndex = principalQuery.get(HAL_INDEX).asInt();
        return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                .param(NAME, keyMapperAttribute(pqIndex, keyMapper))
                .build();
    }

    void saveKeyMapper(String jdbcRealm, int pqIndex, String keyMapper, Map<String, Object> changedValues) {
        String type = new LabelBuilder().label(keyMapper);
        ca.save(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, AddressTemplates.JDBC_REALM_TEMPLATE,
                changedValues, this::reloadJdbcRealms);
    }

    void resetKeyMapper(String jdbcRealm, int pqIndex, String keyMapper, Form<ModelNode> form) {
        String type = new LabelBuilder().label(keyMapper);
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_TEMPLATE)
                .forComplexAttribute(PRINCIPAL_QUERY)
                .forComplexAttribute(keyMapper);
        ca.reset(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, AddressTemplates.JDBC_REALM_TEMPLATE,
                metadata, form, new Form.FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reloadJdbcRealms();
                    }
                });
    }

    void removeKeyMapper(String jdbcRealm, int pqIndex, String keyMapper, Form<ModelNode> form) {
        String type = new LabelBuilder().label(keyMapper);
        ca.remove(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, AddressTemplates.JDBC_REALM_TEMPLATE,
                new Form.FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(final Form<ModelNode> form) {
                        reloadJdbcRealms();
                    }
                });
    }

    void addAttributeMapping(String jdbcRealm, int pqIndex) {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_TEMPLATE)
                .forComplexAttribute(PRINCIPAL_QUERY)
                .forComplexAttribute(ATTRIBUTE_MAPPING);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(
                Ids.build(Ids.ELYTRON_JDBC_REALM, ATTRIBUTE_MAPPING, Ids.ADD_SUFFIX), metadata)
                .addOnly()
                .include(TO, INDEX)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.ATTRIBUTE_MAPPING),
                form, (name, model) -> ca.listAdd(jdbcRealm, attributeMappingAttribute(pqIndex),
                Names.ATTRIBUTE_MAPPING, AddressTemplates.JDBC_REALM_TEMPLATE, model, this::reloadJdbcRealms));
        dialog.show();
    }

    void saveAttributeMapping(String jdbcRealm, int pqIndex, int amIndex,
            final Map<String, Object> changedValues) {
        ca.save(jdbcRealm, attributeMappingAttribute(pqIndex), Names.ATTRIBUTE_MAPPING, amIndex,
                AddressTemplates.JDBC_REALM_TEMPLATE, changedValues, this::reloadJdbcRealms);
    }

    void removeAttributeMapping(String jdbcRealm, int pqIndex, int amIndex) {
        ca.remove(jdbcRealm, attributeMappingAttribute(pqIndex), Names.ATTRIBUTE_MAPPING, amIndex,
                AddressTemplates.JDBC_REALM_TEMPLATE, this::reloadJdbcRealms);
    }

    private String keyMapperAttribute(int pqIndex, String keyMapper) {
        return PRINCIPAL_QUERY + "[" + pqIndex + "]." + keyMapper;
    }

    private String attributeMappingAttribute(int pqIndex) {
        return PRINCIPAL_QUERY + "[" + pqIndex + "]." + ATTRIBUTE_MAPPING;
    }


    // ============ LDAP Realm

    void reloadLdapRealms() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, ModelDescriptionConstants.LDAP_REALM,
                children -> getView().updateLdapRealm(asNamedNodes(children)));
    }

    void addLdapRealm() {
        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE);

        // repackage "identity-mapping" as it is a required attribute to be displayed in the form of the ADD dialog.
        // the repackaged attribute must be prefixed so, the user knowns where it comes from.
        Metadata nestedMetadata = metadata.repackageComplexAttribute(IDENTITY_MAPPING, true, true, true);

        new AddResourceDialog(Ids.ELYTRON_LDAP_REALM_ADD,
                resources.messages().addResourceTitle(Names.LDAP_REALM), nestedMetadata,
                (name, model) -> {
                    // once the model is posted, it must be correctly assembled as the attributes are not correct,
                    // related to the r-r-d
                    reassembleComplexAttribute(IDENTITY_MAPPING, model, true);
                    ResourceAddress address = LDAP_REALM_TEMPLATE.resolve(statementContext, name);
                    crud.add(Names.LDAP_REALM, name, address, model, (name1, address1) -> reload());
                }).show();

    }

    public void addLdapRealm2() {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.LDAP_REALM_TEMPLATE)
                .repackageComplexAttribute(IDENTITY_MAPPING, true, true, true);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.ELYTRON_LDAP_REALM_ADD, metadata)
                .addOnly()
                .fromRequestProperties()
                .requiredOnly()
                .unboundFormItem(nameItem, 0)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.LDAP_REALM), form,
                (n1, model) -> {
                    ModelNode payload = new ModelNode();
                    payload.get(IDENTITY_MAPPING).add(model);
                    logger.info(" add ldap payload: " + payload);
                    crud.add(Names.LDAP_REALM, nameItem.getValue(), AddressTemplates.LDAP_REALM_TEMPLATE, payload,
                            (n2, address) -> reloadLdapRealms());
                });
        dialog.show();
    }

    /**
     * Given a model as
     * <pre>
     * {
     *   other-attr: "value1"
     *   complexAttr-name1: "some value 1",
     *   complexAttr-name2: "some value 2"
     * }
     * </pre>
     * This method extracts the complex attribute name and adds the nested attributes into the complex attribute.
     * If createComplexAttribute=true, the resulting model node is:
     * <p>
     * <pre>
     * {
     *   other-attr: "value1"
     *   complexAttr: {
     *     name1: "some value 1",
     *     name2: "some value 2"
     *     }
     * }
     * </pre>
     * <p>
     * If createComplexAttribute=false, the resulting model node is:
     * <p>
     * <pre>
     * {
     *   other-attr: "value1"
     *   name1: "some value 1",
     *   name2: "some value 2"
     * }
     * </pre>
     *
     * @param complexAttributeName   The complex attribute name
     * @param model                  The model
     * @param createComplexAttribute Control if the resulting model should add the complex attribute name, see above
     *                               example.
     */
    private void reassembleComplexAttribute(String complexAttributeName, ModelNode model,
            boolean createComplexAttribute) {
        if (model.isDefined()) {
            for (Property property : model.asPropertyList()) {
                String pName = property.getName();

                String nestedAttrName;

                boolean propertyRepackagedName = pName.length() > complexAttributeName.length()
                        && complexAttributeName.equals(pName.substring(0, complexAttributeName.length()));

                if (propertyRepackagedName) {
                    nestedAttrName = pName.substring(complexAttributeName.length() + 1);
                } else {
                    continue;
                }

                if (createComplexAttribute) {
                    model.get(complexAttributeName).get(nestedAttrName).set(property.getValue());
                    model.remove(pName);
                } else {
                    model.get(nestedAttrName).set(property.getValue());
                }
            }
        }
    }

    void saveLdapRealm(final Form<NamedNode> form, final Map<String, Object> changedValues) {
        crud.save(Names.LDAP_REALM, form.getModel().getName(), AddressTemplates.LDAP_REALM_TEMPLATE, changedValues,
                this::reloadLdapRealms);
    }

    void saveIdentityMapping(Map<String, Object> changedValues) {
    }

    void addIdentityAttributeMapping(final String selectedLdapRealm) {
        Metadata caMetadata = metadataRegistry.lookup(LDAP_REALM_TEMPLATE)
                .forComplexAttribute(IDENTITY_MAPPING)
                .forComplexAttribute(ATTRIBUTE_MAPPING);
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(
                 Ids.ELYTRON_IDENTITY_ATTRIBUTE_MAPPING_ADD, caMetadata)
                .addOnly();

        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.ATTRIBUTE_MAPPING),
                builder.build(), (name, model) ->
                ca.listAdd(selectedLdapRealm, IDENTITY_MAPPING + "." + ATTRIBUTE_MAPPING,
                        Names.IDENTITY_ATTRIBUTE_MAPPING, LDAP_REALM_TEMPLATE, model, () -> reloadLdapRealms()));
        dialog.show();


    }

    void removeIdentityAttributeMapping(final String selectedLdapRealm, final int iamIndex) {
        ca.remove(selectedLdapRealm, IDENTITY_MAPPING + "." + ATTRIBUTE_MAPPING, Names.IDENTITY_ATTRIBUTE_MAPPING,
                iamIndex, AddressTemplates.LDAP_REALM_TEMPLATE, this::reloadLdapRealms);
    }

    void saveIdentityAttributeMapping(final String selectedLdapRealm, final int iamIndex,
            final Map<String, Object> changedValues) {

        ca.save(selectedLdapRealm, IDENTITY_MAPPING + "." + ATTRIBUTE_MAPPING, Names.IDENTITY_ATTRIBUTE_MAPPING,
                iamIndex, AddressTemplates.LDAP_REALM_TEMPLATE, changedValues,
                this::reloadLdapRealms);


    }

    // ==== properties realm

    void addPropertiesRealm() {
        Metadata metadata = metadataRegistry.lookup(PROPERTIES_REALM_TEMPLATE);
        Metadata upMetadata = metadata.forComplexAttribute(USERS_PROPERTIES, true);
        upMetadata.copyComplexAttributeAtrributes(asList(PATH, RELATIVE_TO), metadata);

        String id = Ids.build(Ids.ELYTRON_PROPERTIES_REALM, Ids.ADD_SUFFIX);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly()
                .unboundFormItem(nameItem, 0)
                .include(PATH, RELATIVE_TO, GROUPS_ATTRIBUTE)
                .build();
        form.getFormItem(RELATIVE_TO).registerSuggestHandler(new PathsAutoComplete());

        new AddResourceDialog(Names.PROPERTIES_REALM, form, (name, model) -> {
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
}
