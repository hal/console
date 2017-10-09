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
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;
import static org.jboss.hal.client.configuration.subsystem.elytron.ElytronResource.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MECHANISM_CONFIGURATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MECHANISM_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MECHANISM_REALM_CONFIGURATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class FactoriesPresenter extends MbuiPresenter<FactoriesPresenter.MyView, FactoriesPresenter.MyProxy>
        implements SupportsExpertMode {

    @ProxyCodeSplit
    @Requires(value = {
            AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS,
            AGGREGATE_SASL_SERVER_FACTORY_ADDRESS,
            AGGREGATE_TRANSFORMER_ADDRESS,
            CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS,
            CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS,
            CONFIGURABLE_SASL_SERVER_FACTORY_ADDRESS,
            CONSTANT_TRANSFORMER_ADDRESS,
            CUSTOM_CREDENTIAL_SECURITY_FACTORY_ADDRESS,
            CUSTOM_TRANSFORMER_ADDRESS,
            HTTP_AUTHENTICATION_FACTORY_ADDRESS,
            KERBEROS_SECURITY_FACTORY_ADDRESS,
            MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_ADDRESS,
            PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS,
            PROVIDER_SASL_SERVER_FACTORY_ADDRESS,
            REGEX_PRINCIPAL_TRANSFORMER_ADDRESS,
            REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS,
            SASL_AUTHENTICATION_FACTORY_ADDRESS,
            SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS,
            SERVICE_LOADER_SASL_SERVER_FACTORY_ADDRESS})
    @NameToken(NameTokens.ELYTRON_FACTORIES_TRANSFORMERS)
    public interface MyProxy extends ProxyPlace<FactoriesPresenter> {}


    // @formatter:off
    public interface MyView extends MbuiView<FactoriesPresenter> {
        void updateResourceElement(String resource, List<NamedNode> nodes);
        void updateHttpAuthentication(List<NamedNode> nodes);
        void updateSaslAuthentication(List<NamedNode> nodes);
    }
    // @formatter:on


    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public FactoriesPresenter(final EventBus eventBus,
            final FactoriesPresenter.MyView view,
            final FactoriesPresenter.MyProxy proxy,
            final Finder finder,
            final CrudOperations crud,
            final ComplexAttributeOperations ca,
            final FinderPathFactory finderPathFactory,
            final MetadataRegistry metadataRegistry,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.ca = ca;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
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
                .append(Ids.ELYTRON, Ids.asId(Names.FACTORIES_TRANSFORMERS),
                        resources.constants().settings(), Names.FACTORIES_TRANSFORMERS);
    }

    @Override
    public void reload() {
        ResourceAddress address = ELYTRON_SUBSYSTEM_TEMPLATE.resolve(statementContext);
        crud.readChildren(address, asList(
                AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                AGGREGATE_PRINCIPAL_TRANSFORMER.resource,
                AGGREGATE_SASL_SERVER_FACTORY.resource,
                CHAINED_PRINCIPAL_TRANSFORMER.resource,
                CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                CONFIGURABLE_SASL_SERVER_FACTORY.resource,
                CONSTANT_PRINCIPAL_TRANSFORMER.resource,
                CUSTOM_CREDENTIAL_SECURITY_FACTORY.resource,
                CUSTOM_PRINCIPAL_TRANSFORMER.resource,
                HTTP_AUTHENTICATION_FACTORY.resource,
                KERBEROS_SECURITY_FACTORY.resource,
                MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY.resource,
                PROVIDER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                PROVIDER_SASL_SERVER_FACTORY.resource,
                REGEX_PRINCIPAL_TRANSFORMER.resource,
                REGEX_VALIDATING_PRINCIPAL_TRANSFORMER.resource,
                SASL_AUTHENTICATION_FACTORY.resource,
                SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                SERVICE_LOADER_SASL_SERVER_FACTORY.resource),
                result -> {
                    getView().updateResourceElement(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                            asNamedNodes(result.step(0).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(AGGREGATE_PRINCIPAL_TRANSFORMER.resource,
                            asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(AGGREGATE_SASL_SERVER_FACTORY.resource,
                            asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(CHAINED_PRINCIPAL_TRANSFORMER.resource,
                            asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                            asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(CONFIGURABLE_SASL_SERVER_FACTORY.resource,
                            asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(CONSTANT_PRINCIPAL_TRANSFORMER.resource,
                            asNamedNodes(result.step(6).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(CUSTOM_CREDENTIAL_SECURITY_FACTORY.resource,
                            asNamedNodes(result.step(7).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(CUSTOM_PRINCIPAL_TRANSFORMER.resource,
                            asNamedNodes(result.step(8).get(RESULT).asPropertyList()));
                    getView().updateHttpAuthentication(asNamedNodes(result.step(9).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(KERBEROS_SECURITY_FACTORY.resource,
                            asNamedNodes(result.step(10).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY.resource,
                            asNamedNodes(result.step(11).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                            asNamedNodes(result.step(12).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(PROVIDER_SASL_SERVER_FACTORY.resource,
                            asNamedNodes(result.step(13).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(REGEX_PRINCIPAL_TRANSFORMER.resource,
                            asNamedNodes(result.step(14).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER.resource,
                            asNamedNodes(result.step(15).get(RESULT).asPropertyList()));
                    getView().updateSaslAuthentication(asNamedNodes(result.step(16).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                            asNamedNodes(result.step(17).get(RESULT).asPropertyList()));
                    getView().updateResourceElement(SERVICE_LOADER_SASL_SERVER_FACTORY.resource,
                            asNamedNodes(result.step(18).get(RESULT).asPropertyList()));
                });
    }

    void reload(String resource, Consumer<List<NamedNode>> callback) {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE, resource,
                children -> callback.accept(asNamedNodes(children)));
    }


    // ------------------------------------------------------ HTTP authentication factory

    void reloadHttpAuthenticationFactories() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE,
                ModelDescriptionConstants.HTTP_AUTHENTICATION_FACTORY,
                children -> getView().updateHttpAuthentication(asNamedNodes(children)));
    }

    void saveHttpAuthenticationFactory(Form<NamedNode> form, Map<String, Object> changedValues) {
        crud.save(Names.HTTP_AUTHENTICATION_FACTORY, form.getModel().getName(),
                AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE, changedValues,
                this::reloadHttpAuthenticationFactories);
    }

    void addHttpMechanismConfiguration(String httpAuthenticationFactory) {
        String id = Ids.build(Ids.ELYTRON_HTTP_AUTHENTICATION_FACTORY, MECHANISM_CONFIGURATIONS, Ids.ADD_SUFFIX);
        ca.listAdd(id, httpAuthenticationFactory, MECHANISM_CONFIGURATIONS, Names.MECHANISM_CONFIGURATION,
                AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE, singletonList(MECHANISM_NAME),
                this::reloadHttpAuthenticationFactories);
    }

    void saveHttpMechanismConfiguration(String httpAuthenticationFactory, int index,
            Map<String, Object> changedValues) {
        ca.save(httpAuthenticationFactory, MECHANISM_CONFIGURATIONS, Names.MECHANISM_CONFIGURATION, index,
                AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE, changedValues,
                this::reloadHttpAuthenticationFactories);
    }

    void removeHttpMechanismConfiguration(String httpAuthenticationFactory, int index) {
        ca.remove(httpAuthenticationFactory, MECHANISM_CONFIGURATIONS, Names.MECHANISM_CONFIGURATION, index,
                AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE, this::reloadHttpAuthenticationFactories);
    }

    void addHttpMechanismRealmConfiguration(String httpAuthenticationFactory, int mechanismIndex) {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE)
                .forComplexAttribute(MECHANISM_CONFIGURATIONS)
                .forComplexAttribute(MECHANISM_REALM_CONFIGURATIONS);
        String id = Ids.build(Ids.ELYTRON_HTTP_AUTHENTICATION_FACTORY, MECHANISM_REALM_CONFIGURATIONS, Ids.ADD_SUFFIX);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly()
                .requiredOnly()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.MECHANISM_REALM_CONFIGURATION), form,
                (name, model) -> ca.listAdd(httpAuthenticationFactory, mrcComplexAttribute(mechanismIndex),
                        Names.MECHANISM_REALM_CONFIGURATION, AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE,
                        model, this::reloadHttpAuthenticationFactories));
        dialog.show();
    }

    void saveHttpMechanismRealmConfiguration(String httpAuthenticationFactory, int mechanismIndex, int mechanismRealmIndex,
            Map<String, Object> changedValues) {
        ca.save(httpAuthenticationFactory, mrcComplexAttribute(mechanismIndex), Names.MECHANISM_REALM_CONFIGURATION,
                mechanismRealmIndex, AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE, changedValues,
                this::reloadHttpAuthenticationFactories);
    }

    void removeHttpMechanismRealmConfiguration(String httpAuthenticationFactory, int mechanismIndex,
            int mechanismRealmIndex) {
        ca.remove(httpAuthenticationFactory, mrcComplexAttribute(mechanismIndex), Names.MECHANISM_REALM_CONFIGURATION,
                mechanismRealmIndex, AddressTemplates.HTTP_AUTHENTICATION_FACTORY_TEMPLATE,
                this::reloadHttpAuthenticationFactories);
    }

    private String mrcComplexAttribute(int mechanismIndex) {
        return MECHANISM_CONFIGURATIONS + "[" + mechanismIndex + "]." + MECHANISM_REALM_CONFIGURATIONS;
    }

    // ------------------------------------------------------ SASL authentication factory

    void reloadSaslAuthenticationFactories() {
        crud.readChildren(AddressTemplates.ELYTRON_SUBSYSTEM_TEMPLATE,
                ModelDescriptionConstants.SASL_AUTHENTICATION_FACTORY,
                children -> getView().updateSaslAuthentication(asNamedNodes(children)));
    }

    void saveSaslAuthenticationFactory(Form<NamedNode> form, Map<String, Object> changedValues) {
        crud.save(Names.SASL_AUTHENTICATION_FACTORY, form.getModel().getName(),
                AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE, changedValues,
                this::reloadSaslAuthenticationFactories);
    }

    void addSaslMechanismConfiguration(String saslAuthenticationFactory) {
        String id = Ids.build(Ids.ELYTRON_SASL_AUTHENTICATION_FACTORY, MECHANISM_CONFIGURATIONS, Ids.ADD_SUFFIX);
        ca.listAdd(id, saslAuthenticationFactory, MECHANISM_CONFIGURATIONS, Names.MECHANISM_CONFIGURATION,
                AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE, singletonList(MECHANISM_NAME),
                this::reloadSaslAuthenticationFactories);
    }

    void saveSaslMechanismConfiguration(String saslAuthenticationFactory, int index,
            Map<String, Object> changedValues) {
        ca.save(saslAuthenticationFactory, MECHANISM_CONFIGURATIONS, Names.MECHANISM_CONFIGURATION, index,
                AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE, changedValues,
                this::reloadSaslAuthenticationFactories);
    }

    void removeSaslMechanismConfiguration(String saslAuthenticationFactory, int index) {
        ca.remove(saslAuthenticationFactory, MECHANISM_CONFIGURATIONS, Names.MECHANISM_CONFIGURATION, index,
                AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE, this::reloadSaslAuthenticationFactories);
    }

    void addSaslMechanismRealmConfiguration(String saslAuthenticationFactory, int mechanismIndex) {
        Metadata metadata = metadataRegistry.lookup(AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE)
                .forComplexAttribute(MECHANISM_CONFIGURATIONS)
                .forComplexAttribute(MECHANISM_REALM_CONFIGURATIONS);
        String id = Ids.build(Ids.ELYTRON_SASL_AUTHENTICATION_FACTORY, MECHANISM_REALM_CONFIGURATIONS, Ids.ADD_SUFFIX);
        Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata)
                .addOnly()
                .requiredOnly()
                .build();
        AddResourceDialog dialog = new AddResourceDialog(
                resources.messages().addResourceTitle(Names.MECHANISM_REALM_CONFIGURATION), form,
                (name, model) -> ca.listAdd(saslAuthenticationFactory, mrcComplexAttribute(mechanismIndex),
                        Names.MECHANISM_REALM_CONFIGURATION, AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE,
                        model, this::reloadSaslAuthenticationFactories));
        dialog.show();
    }

    void saveSaslMechanismRealmConfiguration(String saslAuthenticationFactory, int mechanismIndex, int mechanismRealmIndex,
            Map<String, Object> changedValues) {
        ca.save(saslAuthenticationFactory, mrcComplexAttribute(mechanismIndex), Names.MECHANISM_REALM_CONFIGURATION,
                mechanismRealmIndex, AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE, changedValues,
                this::reloadSaslAuthenticationFactories);
    }

    void removeSaslMechanismRealmConfiguration(String saslAuthenticationFactory, int mechanismIndex,
            int mechanismRealmIndex) {
        ca.remove(saslAuthenticationFactory, mrcComplexAttribute(mechanismIndex), Names.MECHANISM_REALM_CONFIGURATION,
                mechanismRealmIndex, AddressTemplates.SASL_AUTHENTICATION_FACTORY_TEMPLATE,
                this::reloadSaslAuthenticationFactories);
    }
}
