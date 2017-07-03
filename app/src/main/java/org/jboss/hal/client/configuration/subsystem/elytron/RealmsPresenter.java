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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.OperationFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.JDBC_REALM;
import static org.jboss.hal.client.configuration.subsystem.elytron.ResourceView.HAL_INDEX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;


/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class RealmsPresenter extends MbuiPresenter<RealmsPresenter.MyView, RealmsPresenter.MyProxy>
        implements SupportsExpertMode, ElytronPresenter {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value ={
        PROPERTIES_REALM, FILESYSTEM_REALM, CACHING_REALM, JDBC_REALM, LDAP_REALM, KEYSTORE_REALM, AGGREGATE_REALM,
        CUSTOM_MODIFIABLE_REALM, CUSTOM_REALM, IDENTITY_REALM, TOKEN_REALM, MAPPED_REGEX_REALM_MAPPER,
        SIMPLE_REGEX_REALM_MAPPER, CUSTOM_REALM_MAPPER, CONSTANT_REALM_MAPPER
    })
    @NameToken(NameTokens.ELYTRON_SECURITY_REALMS)
    public interface MyProxy extends ProxyPlace<RealmsPresenter> {}

    public interface MyView extends MbuiView<RealmsPresenter> {
        void updateAggregateRealm(List<NamedNode> model);
        void updateCachingRealm(List<NamedNode> model);
        void updateCustomModifiableRealm(List<NamedNode> model);
        void updateCustomRealm(List<NamedNode> model);
        void updateFilesystemRealm(List<NamedNode> model);
        void updateIdentityRealm(List<NamedNode> model);
        void updateJdbcRealm(List<NamedNode> model);
        void updateKeyStoreRealm(List<NamedNode> model);
        void updateLdapRealm(List<NamedNode> model);
        void updatePropertiesRealm(List<NamedNode> model);
        void updateTokenRealm(List<NamedNode> model);
        void updateConstantRealmMapper(List<NamedNode> model);
        void updateCustomRealmMapper(List<NamedNode> model);
        void updateMappedRegexRealmMapper(List<NamedNode> model);
        void updateSimpleRegexRealmMapper(List<NamedNode> model);

    }
    // @formatter:on

    final static String[] KEY_MAPPERS = new String[]{
            "clear-password-mapper",
            "bcrypt-mapper",
            "salted-simple-digest-mapper",
            "simple-digest-mapper",
            "scram-mapper"
    };

    private EventBus eventBus;
    private Dispatcher dispatcher;
    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private MetadataRegistry metadataRegistry;
    private final Resources resources;

    @Inject
    public RealmsPresenter(final EventBus eventBus,
            final RealmsPresenter.MyView view,
            final RealmsPresenter.MyProxy proxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final ComplexAttributeOperations ca,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
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
        return ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.SECURITY_REALMS),
                        resources.constants().settings(), Names.SECURITY_REALMS);
    }

    public void addLDAPRealm() {

        Metadata metadata = metadataRegistry.lookup(LDAP_REALM_ADDRESS);

        // repackage "identity-mapping" as it is a required attribute to be displayed in the form of the ADD dialog.
        String complexAttributeName = "identity-mapping";
        // the repackaged attribute must be prefixed so, the user knowns where it comes from.
        Metadata nestedMetadata = metadata.repackageComplexAttribute(complexAttributeName, true, true, true);

        new AddResourceDialog(Ids.build(Ids.ELYTRON_LDAP_REALM, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(Names.ELYTRON_LDAP_REALM), nestedMetadata,
                (name, model) -> {
                    // once the model is posted, it must be correctly assembled as the attributes are not correct,
                    // related to the r-r-d
                    ModelNodeHelper.reassembleComplexAttribute(complexAttributeName, model, true);
                    ResourceAddress address = LDAP_REALM_ADDRESS.resolve(statementContext, name);
                    crud.add(Names.ELYTRON_LDAP_REALM, name, address, model, (name1, address1) -> reload());
                }).show();

    }

    public void addPropertiesRealm() {

        Metadata metadata = metadataRegistry.lookup(PROPERTIES_REALM_ADDRESS);

        // repackage "users-properties" as it is a required attribute to be displayed in the form of the ADD dialog.
        String complexAttributeName = "users-properties";
        // the repackaged attribute must be prefixed so, the user knowns where it comes from.
        Metadata nestedMetadata = metadata.repackageComplexAttribute(complexAttributeName, true, true, true);

        new AddResourceDialog(Ids.build(Ids.ELYTRON_PROPERTIES_REALM, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(Names.ELYTRON_PROPERTIES_REALM), nestedMetadata,
                (name, model) -> {
                    ModelNodeHelper.reassembleComplexAttribute(complexAttributeName, model, true);
                    ResourceAddress address = PROPERTIES_REALM_ADDRESS.resolve(statementContext, name);
                    crud.add(Names.ELYTRON_PROPERTIES_REALM, name, address, model, (name1, address1) -> reload());
                }).show();

    }

    @Override
    public void reload() {

        ResourceAddress address = ELYTRON_SUBSYSTEM_ADDRESS.resolve(statementContext);
        crud.readChildren(address, asList(
                "aggregate-realm",
                "caching-realm",
                "custom-modifiable-realm",
                "custom-realm",
                "filesystem-realm",
                "identity-realm",
                "jdbc-realm",
                "key-store-realm",
                "ldap-realm",
                "properties-realm",
                "token-realm",
                "constant-realm-mapper",
                "custom-realm-mapper",
                "mapped-regex-realm-mapper",
                "simple-regex-realm-mapper"
                ),
                result -> {
                    // @formatter:off
                    getView().updateAggregateRealm(asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateCachingRealm(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateCustomModifiableRealm(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateCustomRealm(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateFilesystemRealm(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateIdentityRealm(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateJdbcRealm(asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateKeyStoreRealm(asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateLdapRealm(asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updatePropertiesRealm(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updateTokenRealm(asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                    getView().updateConstantRealmMapper(asNamedNodes(result.step(11).get(RESULT).asPropertyList()));
                    getView().updateCustomRealmMapper(asNamedNodes(result.step(12).get(RESULT).asPropertyList()));
                    getView().updateMappedRegexRealmMapper(asNamedNodes(result.step(13).get(RESULT).asPropertyList()));
                    getView().updateSimpleRegexRealmMapper(asNamedNodes(result.step(14).get(RESULT).asPropertyList()));
                    // @formatter:on
                });
    }

    @Override
    public void saveForm(final String title, final String name, final Map<String, Object> changedValues,
            final Metadata metadata) {

        ResourceAddress address = metadata.getTemplate().resolve(statementContext, name);
        crud.save(title, name, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void saveComplexForm(final String title, final String name, String complexAttributeName,
            final Map<String, Object> changedValues, final Metadata metadata) {

        ResourceAddress address = metadata.getTemplate().resolve(statementContext, name);
        String type = new LabelBuilder().label(metadata.getTemplate().lastName());
        crud.save(type, name, complexAttributeName, address, changedValues, metadata, () -> reload());
    }

    @Override
    public void saveFormPage(String resource, String listAttributeName, Metadata metadata,
            NamedNode payload, Map<String, Object> changedValues) {
        ResourceAddress address = metadata.getTemplate().resolve(statementContext, resource);
        // the HAL_INDEX is an index added by HAL to properly identify each lis item, as lists may not contain
        // a proper name identifier. The HAL_INDEX is added in ResourceView.bindTableToForm method and follow the
        // HAL_INDEX usage.
        OperationFactory operationFactory = new OperationFactory(
                name -> listAttributeName + "[" + payload.get(HAL_INDEX).asInt() + "]." + name);
        Composite operations = operationFactory.fromChangeSet(address, changedValues, metadata);
        dispatcher.execute(operations, (CompositeResult result) -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().modifySingleResourceSuccess(address.lastName())));
            reload();
        });
    }


    @Override
    public void resetComplexAttribute(final String type, final String name, final String attribute,
            final Metadata metadata, final Callback callback) {

        ResourceAddress address = metadata.getTemplate().resolve(statementContext, name);
        // check if the attribute to reset is in enhanced syntax form: attribute.attribute2
        if (attribute.contains(".")) {

            Operation operation = new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                    .param(NAME, attribute)
                    .build();

            SafeHtml question = resources.messages().resetConfirmationQuestion(type);
            DialogFactory.showConfirmation(
                    resources.messages().resetConfirmationTitle(type), question,
                    () -> dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().resetResourceSuccess(type, name)));
                        reload();
                    }));


        } else {
            Set<String> attributeToReset = new HashSet<>();
            attributeToReset.add(attribute);
            crud.reset(type, name, address, attributeToReset, metadata, callback);
        }
    }

    public void launchOnAddJDBCRealm() {

        String complexAttributeName = "principal-query";
        String id = Ids.build(Ids.ELYTRON_JDBC_REALM, Ids.FORM_SUFFIX, Ids.ADD_SUFFIX);
        Metadata metadata = metadataRegistry.lookup(JDBC_REALM_ADDRESS);
        metadata = metadata.repackageComplexAttribute(complexAttributeName, true, true, true);
        AddResourceDialog dialog = new AddResourceDialog(id, resources.messages().addResourceTitle("JDBC Realm"),
                metadata, (name, payload) -> {

            ModelNode nestedAttrs = new ModelNode();
            // as the "principal-query" attribute description is repackaged in the root node
            // it needs to be re-assembled as a nested attribute of "principal-query"
            payload.asPropertyList().forEach(property -> {
                String _name = property.getName();
                if (complexAttributeName.equals(_name.substring(0, complexAttributeName.length()))) {
                    _name = _name.substring(complexAttributeName.length() + 1);
                    nestedAttrs.get(_name).set(property.getValue());
                    payload.remove(property.getName());
                }
            });
            payload.get(complexAttributeName).add(nestedAttrs);

            crud.add("JDBC Realm", name, JDBC_REALM_ADDRESS, payload, (name1, address) -> reload());
        });
        dialog.show();

    }

    @Override
    public void launchAddDialog(Function<String, String> resourceNameFunction, String complexAttributeName,
            Metadata metadata, String title) {

        String id = Ids.build(complexAttributeName, Ids.FORM_SUFFIX, Ids.ADD_SUFFIX);
        ResourceAddress address = metadata.getTemplate().resolve(statementContext, resourceNameFunction.apply(null));

        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .fromRequestProperties()
                .build();

        AddResourceDialog dialog = new AddResourceDialog(title, form,
                (name, model) -> crud.listAdd(title, name, complexAttributeName, address, model, () -> reload()));
        dialog.show();
    }

    @Override
    public void listRemove(String title, String resourceName, String complexAttributeName, int index,
            AddressTemplate template) {

        ResourceAddress address = template.resolve(statementContext, resourceName);
        crud.listRemove(title, resourceName, complexAttributeName, index, address, () -> reload());
    }


    // ------------------------------------------------------ JDBC realm

    void reloadJdbcRealms() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_ADDRESS, ModelDescriptionConstants.JDBC_REALM,
                children -> getView().updateJdbcRealm(asNamedNodes(children)));
    }

    void addJdbcRealm() {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_ADDRESS)
                .forComplexAttribute(PRINCIPAL_QUERY);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.ELYTRON_JDBC_REALM_ADD, metadata)
                .addOnly()
                .requiredOnly()
                .unboundFormItem(nameItem, 0)
                .build();
        AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(Names.JDBC_REALM), form,
                (n1, model) -> {
                    ModelNode payload = new ModelNode();
                    payload.get(PRINCIPAL_QUERY).add(model);
                    crud.add(Names.JDBC_REALM, nameItem.getValue(), AddressTemplates.JDBC_REALM_ADDRESS, payload,
                            (n2, address) -> reloadJdbcRealms());
                });
        dialog.show();
    }

    void addPrincipalQuery(final String jdbcRealm) {
        ca.listAdd(Ids.ELYTRON_PRINCIPAL_QUERY_ADD, jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY,
                AddressTemplates.JDBC_REALM_ADDRESS, this::reloadJdbcRealms);
    }

    void savePrincipalQuery(final String jdbcRealm, final int pqIndex, final Map<String, Object> changedValues) {
        ca.save(jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY, pqIndex, AddressTemplates.JDBC_REALM_ADDRESS,
                changedValues, this::reloadJdbcRealms);
    }

    void removePrincipalQuery(final String jdbcRealm, final int pqIndex) {
        ca.remove(jdbcRealm, PRINCIPAL_QUERY, Names.PRINCIPAL_QUERY, pqIndex, AddressTemplates.JDBC_REALM_ADDRESS,
                this::reloadJdbcRealms);
    }

    void addKeyMapper(final String jdbcRealm, final ModelNode principalQuery, final int pqIndex,
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
            Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_ADDRESS)
                    .forComplexAttribute(PRINCIPAL_QUERY)
                    .forComplexAttribute(keyMapper);
            String id = Ids.build(Ids.ELYTRON_PRINCIPAL_QUERY, keyMapper, Ids.ADD_SUFFIX);
            Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                    .addOnly()
                    .requiredOnly()
                    .build();
            AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(type), form,
                    (name, model) -> ca.add(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type,
                            AddressTemplates.JDBC_REALM_ADDRESS, model, RealmsPresenter.this::reloadJdbcRealms));
            dialog.show();
        }
    }

    Operation pingKeyMapper(final String jdbcRealm, final ModelNode principalQuery, final String keyMapper) {
        ResourceAddress address = AddressTemplates.JDBC_REALM_ADDRESS.resolve(statementContext, jdbcRealm);
        int pqIndex = principalQuery.get(HAL_INDEX).asInt();
        return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                .param(NAME, keyMapperAttribute(pqIndex, keyMapper))
                .build();
    }

    void saveKeyMapper(final String jdbcRealm, final int pqIndex, final String keyMapper,
            final Map<String, Object> changedValues) {
        String type = new LabelBuilder().label(keyMapper);
        ca.save(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, AddressTemplates.JDBC_REALM_ADDRESS,
                changedValues, this::reloadJdbcRealms);
    }

    void resetKeyMapper(final String jdbcRealm, final int pqIndex, final String keyMapper, final Form<ModelNode> form) {
        String type = new LabelBuilder().label(keyMapper);
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.JDBC_REALM_ADDRESS)
                .forComplexAttribute(PRINCIPAL_QUERY)
                .forComplexAttribute(keyMapper);
        ca.reset(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, AddressTemplates.JDBC_REALM_ADDRESS, metadata,
                form, new Form.FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(final Form<ModelNode> form) {
                        reloadJdbcRealms();
                    }
                });
    }

    void removeKeyMapper(final String jdbcRealm, final int pqIndex, final String keyMapper,
            final Form<ModelNode> form) {
        String type = new LabelBuilder().label(keyMapper);
        ca.remove(jdbcRealm, keyMapperAttribute(pqIndex, keyMapper), type, AddressTemplates.JDBC_REALM_ADDRESS,
                new Form.FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(final Form<ModelNode> form) {
                        reloadJdbcRealms();
                    }
                });
    }

    void addAttributeMapping(final String jdbcRealm, final int pqIndex) {

    }

    void saveAttributeMapping(final String jdbcRealm, final int pqIndex, final int amIndex,
            final Map<String, Object> changedValues) {

    }

    void removeAttributeMapping(final String jdbcRealm, final int pqIndex, final int amIndex) {

    }

    private String keyMapperAttribute(int pqIndex, String keyMapper) {
        return PRINCIPAL_QUERY + "[" + pqIndex + "]." + keyMapper;
    }
}
