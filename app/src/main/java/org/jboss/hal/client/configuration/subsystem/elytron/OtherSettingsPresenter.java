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
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;
import org.jetbrains.annotations.NonNls;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;


/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class OtherSettingsPresenter extends MbuiPresenter<OtherSettingsPresenter.MyView, OtherSettingsPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @Requires(value ={
        KEY_STORE_ADDRESS, KEY_MANAGER_ADDRESS, SERVER_SSL_CONTEXT_ADDRESS, CLIENT_SSL_CONTEXT_ADDRESS, TRUST_MANAGER_ADDRESS, CREDENTIAL_STORE_ADDRESS,
        FILTERING_KEY_STORE_ADDRESS, LDAP_KEY_STORE_ADDRESS, PROVIDER_LOADER_ADDRESS, AGGREGATE_PROVIDERS_ADDRESS, SECURITY_DOMAIN_ADDRESS,
        DIR_CONTEXT_ADDRESS, AUTHENTICATION_CONTEXT_ADDRESS, AUTHENTICATION_CONFIGURATION_ADDRESS, FILE_AUDIT_LOG_ADDRESS, SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS,
        PERIODIC_FILE_AUDIT_LOG_ADDRESS, SYSLOG_AUDIT_LOG_ADDRESS, POLICY_ADDRESS, AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS
    })
    @NameToken(NameTokens.ELYTRON_OTHER)
    public interface MyProxy extends ProxyPlace<OtherSettingsPresenter> {}

    public interface MyView extends MbuiView<OtherSettingsPresenter> {
        void updateResourceElement(String resource, List<NamedNode> nodes);
        void updateLdapKeyStore(List<NamedNode> model);
        void updatePolicy(List<NamedNode> model);
    }
    @NonNls private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OtherSettingsPresenter.class);
    // @formatter:on
    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private Dispatcher dispatcher;

    @Inject
    public OtherSettingsPresenter(final EventBus eventBus,
            final OtherSettingsPresenter.MyView view,
            final OtherSettingsPresenter.MyProxy proxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final ComplexAttributeOperations ca,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
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
        return ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(Ids.ELYTRON)
                .append(Ids.ELYTRON, Ids.asId(Names.OTHER_SETTINGS),
                        resources.constants().settings(), Names.OTHER_SETTINGS);
    }

    @Override
    public void reload() {

        ResourceAddress address = ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(
                ElytronResource.KEY_STORE.resource,
                ElytronResource.KEY_MANAGER.resource,
                ElytronResource.SERVER_SSL_CONTEXT.resource,
                ElytronResource.CLIENT_SSL_CONTEXT.resource,
                ElytronResource.TRUST_MANAGER.resource,
                ElytronResource.CREDENTIAL_STORE.resource,
                ElytronResource.FILTERING_KEY_STORE.resource,
                ElytronResource.LDAP_KEY_STORE.resource,
                ElytronResource.PROVIDER_LOADER.resource,
                ElytronResource.AGGREGATE_PROVIDERS.resource,
                ElytronResource.SECURITY_DOMAIN.resource,
                ElytronResource.DIR_CONTEXT.resource,
                ElytronResource.AUTHENTICATION_CONTEXT.resource,
                ElytronResource.AUTHENTICATION_CONFIGURATION.resource,
                ElytronResource.FILE_AUDIT_LOG.resource,
                ElytronResource.SIZE_ROTATING_FILE_AUDIT_LOG.resource,
                ElytronResource.PERIODIC_ROTATING_FILE_AUDIT_LOG.resource,
                ElytronResource.SYSLOG_AUDIT_LOG.resource,
                ElytronResource.POLICY.resource,
                ElytronResource.AGGREGATE_SECURITY_EVENT_LISTENER.resource
                ),
                result -> {
                    int i = 0;
                    getView().updateResourceElement(ElytronResource.KEY_STORE.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.KEY_MANAGER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.SERVER_SSL_CONTEXT.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CLIENT_SSL_CONTEXT.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.TRUST_MANAGER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.CREDENTIAL_STORE.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.FILTERING_KEY_STORE.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateLdapKeyStore(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.PROVIDER_LOADER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.AGGREGATE_PROVIDERS.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.SECURITY_DOMAIN.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.DIR_CONTEXT.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.AUTHENTICATION_CONTEXT.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.AUTHENTICATION_CONFIGURATION.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.FILE_AUDIT_LOG.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.SIZE_ROTATING_FILE_AUDIT_LOG.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.PERIODIC_ROTATING_FILE_AUDIT_LOG.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.SYSLOG_AUDIT_LOG.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updatePolicy(asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(ElytronResource.AGGREGATE_SECURITY_EVENT_LISTENER.resource,
                            asNamedNodes(result.step(i++).get(RESULT).asPropertyList()));
                });
    }

    void reload(String resource, Consumer<List<NamedNode>> callback) {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, resource,
                children -> callback.accept(asNamedNodes(children)));
    }

    // ------------------------------------------------------ LDAP key store

    void reloadLdapKeyStores() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, ModelDescriptionConstants.LDAP_KEY_STORE,
                children -> getView().updateLdapKeyStore(asNamedNodes(children)));
    }

    void saveLdapKeyStore(final String name, final Map<String, Object> changedValues) {
        crud.save(Names.LDAP_KEY_STORE, name, AddressTemplates.LDAP_KEY_STORE_TEMPLATE, changedValues,
                this::reloadLdapKeyStores);
    }

    void addNewItemTemplate(final String ldapKeyStore) {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.LDAP_KEY_STORE_TEMPLATE)
                .forComplexAttribute(NEW_ITEM_TEMPLATE);
        String id = Ids.build(Ids.ELYTRON_LDAP_KEY_STORE, NEW_ITEM_TEMPLATE, Ids.ADD_SUFFIX);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .include(NEW_ITEM_PATH, NEW_ITEM_RDN, NEW_ITEM_ATTRIBUTES)
                .customFormItem(NEW_ITEM_ATTRIBUTES,
                        (attributeDescription) -> new MultiValueListItem(NEW_ITEM_ATTRIBUTES))
                .unsorted()
                .addOnly()
                .build();
        String type = new LabelBuilder().label(NEW_ITEM_TEMPLATE);
        new AddResourceDialog(resources.messages().addResourceTitle(type), form, (name, model) ->
                ca.add(ldapKeyStore, NEW_ITEM_TEMPLATE, Names.NEW_ITEM_TEMPLATE,
                        AddressTemplates.LDAP_KEY_STORE_TEMPLATE, model, this::reloadLdapKeyStores)).show();
    }

    Operation pingNewItemTemplate(final String ldapKeyStore) {
        ResourceAddress address = AddressTemplates.LDAP_KEY_STORE_TEMPLATE.resolve(statementContext, ldapKeyStore);
        return new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                .param(NAME, NEW_ITEM_TEMPLATE)
                .build();
    }

    void saveNewItemTemplate(final String ldapKeyStore, final Map<String, Object> changedValues) {
        ca.save(ldapKeyStore, NEW_ITEM_TEMPLATE, Names.NEW_ITEM_TEMPLATE, AddressTemplates.LDAP_KEY_STORE_TEMPLATE,
                changedValues, this::reloadLdapKeyStores);
    }


    void removeNewItemTemplate(final String ldapKeyStore, final Form<ModelNode> form) {
        ca.remove(ldapKeyStore, NEW_ITEM_TEMPLATE, Names.NEW_ITEM_TEMPLATE, AddressTemplates.LDAP_KEY_STORE_TEMPLATE,
                new Form.FinishRemove<ModelNode>(form) {
                    @Override
                    public void afterRemove(final Form<ModelNode> form) {
                        reloadLdapKeyStores();
                    }
                });
    }

    // -------------------------------------------- Policy

    void addPolicy() {
        Metadata metadata = metadataRegistry.lookup(POLICY_TEMPLATE);
        String id = Ids.build(Ids.ELYTRON_POLICY, Ids.ADD_SUFFIX);
        NameItem nameItem = new NameItem();
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly()
                .requiredOnly()
                .unboundFormItem(nameItem, 0)
                .build();

        new AddResourceDialog(Names.POLICY, form, (name, model) -> {
            // sets the "default-policy" to the same name as the policy name as the default value is "policy"
            // repackage the model because it is not possible to add a policy with no parameter see WFLY-9056
            model.get("default-policy").set(nameItem.getValue());
            ModelNode jaccPolicy = new ModelNode();
            jaccPolicy.get(NAME).set(nameItem.getValue());
            model.get(JACC_POLICY).add(jaccPolicy);
            ResourceAddress address = POLICY_TEMPLATE.resolve(statementContext, nameItem.getValue());
            crud.add(Names.POLICY, name, address, model, (n, a) -> reloadPolicy());
        }).show();

    }

    public void savePolicy(final String name, final Map<String, Object> changedValues) {
        crud.save(Names.POLICY, name, AddressTemplates.POLICY_TEMPLATE, changedValues, this::reloadPolicy);
    }

    public void reloadPolicy() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, ModelDescriptionConstants.POLICY,
                children -> getView().updatePolicy(asNamedNodes(children)));
    }

    public void addCustomPolicy(final String selectedPolicyRealm) {
        ca.listAdd(Ids.ELYTRON_CUSTOM_POLICY_ADD, selectedPolicyRealm, CUSTOM_POLICY, Names.CUSTOM_POLICY,
                POLICY_TEMPLATE, asList(NAME, CLASS_NAME, MODULE), this::reloadPolicy);
    }

    public void removeCustomPolicy(final String selectedPolicyRealm, final int customPolicyIndex) {
        ca.remove(selectedPolicyRealm, CUSTOM_POLICY, Names.CUSTOM_POLICY, customPolicyIndex, POLICY_TEMPLATE,
                this::reloadPolicy);
    }

    public void saveCustomPolicy(final String selectedPolicyRealm, final int i,
            final Map<String, Object> changedValues) {
        ResourceAddress address = POLICY_TEMPLATE.resolve(statementContext, selectedPolicyRealm);
        Metadata metadata = metadataRegistry.lookup(POLICY_TEMPLATE).forComplexAttribute(CUSTOM_POLICY);
        ca.save(CUSTOM_POLICY, Names.CUSTOM_POLICY, i, address, changedValues, metadata, this::reloadPolicy);
    }

    public void addJaccPolicy(final String selectedPolicyRealm) {
        ca.listAdd(Ids.ELYTRON_JACC_POLICY_ADD, selectedPolicyRealm, JACC_POLICY, Names.JACC_POLICY,
                POLICY_TEMPLATE, asList(NAME, POLICY, "configuration-factory", MODULE), this::reloadPolicy);
    }

    public void removeJaccmPolicy(final String selectedPolicyRealm, final int jaccPolicyIndex) {
        ca.remove(selectedPolicyRealm, JACC_POLICY, Names.JACC_POLICY, jaccPolicyIndex, POLICY_TEMPLATE,
                this::reloadPolicy);
    }

    public void saveJaccPolicy(final String selectedPolicyRealm, final int i, final Map<String, Object> changedValues) {
        ResourceAddress address = POLICY_TEMPLATE.resolve(statementContext, selectedPolicyRealm);
        Metadata metadata = metadataRegistry.lookup(POLICY_TEMPLATE).forComplexAttribute(JACC_POLICY);
        ca.save(JACC_POLICY, Names.JACC_POLICY, i, address, changedValues, metadata, this::reloadPolicy);
    }
}
