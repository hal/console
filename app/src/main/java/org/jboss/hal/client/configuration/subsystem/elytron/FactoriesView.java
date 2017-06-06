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

import elemental.dom.Element;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.mbui.table.TableButtonFactory;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;

import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.*;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class FactoriesView extends HalViewImpl implements FactoriesPresenter.MyView, ElytronView {

    // http factories
    ResourceView aggregateHttpServerMechanismFactory;
    ResourceView configurableHttpServerMechanismFactory;
    ResourceView httpAuthenticationFactory;
    ResourceView providerHttpServerMechanismFactory;
    ResourceView serviceLoaderHttpServerMechanismFactory;

    // sasl factories
    ResourceView aggregateSaslServerFactory;
    ResourceView configurableSaslServerFactory;
    ResourceView mechanismProviderFilteringSaslServerFactory;
    ResourceView providerSaslServerFactory;
    ResourceView saslAuthenticationFactory;
    ResourceView serviceLoaderSaslServerFactory;

    // other factories
    ResourceView kerberosSecurityFactory;
    ResourceView customCredentialSecurityFactory;

    // transformers
    ResourceView aggregatePrincipalTransformer;
    ResourceView chainedPrincipalTransformer;
    ResourceView constantPrincipalTransformer;
    ResourceView customPrincipalTransformer;
    ResourceView regexPrincipalTransformer;
    ResourceView regexValidatingPrincipalTransformer;

    private FactoriesPresenter presenter;

    @Inject
    FactoriesView(final MetadataRegistry metadataRegistry,
            final TableButtonFactory tableButtonFactory) {

        VerticalNavigation navigation = new VerticalNavigation();
        registerAttachable(navigation);

        String primaryIdHttpFactories = "http-factories";
        String primaryIdSaslFactories = "sasl-factories";
        String primaryIdOtherFactories = "other-factories";
        String primaryIdTransformers = "transformers";
        navigation.addPrimary(primaryIdHttpFactories, "HTTP Factories", "fa fa-file-o");
        navigation.addPrimary(primaryIdSaslFactories, "SASL Factories", "fa fa-exchange");
        navigation.addPrimary(primaryIdOtherFactories, "Other Factories", "fa fa-desktop");
        navigation.addPrimary(primaryIdTransformers, "Principal Transformers", "fa fa-archive");

        // http factories
        aggregateHttpServerMechanismFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdHttpFactories,
                Ids.ELYTRON_AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY, "Aggregate HTTP Server Mechanism", AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        configurableHttpServerMechanismFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdHttpFactories,
                Ids.ELYTRON_CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY, "Configurable HTTP Server Mechanism", CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        httpAuthenticationFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdHttpFactories,
                Ids.ELYTRON_HTTP_AUTHENTICATION_FACTORY, "HTTP Authentication Factory", HTTP_AUTHENTICATION_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        providerHttpServerMechanismFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdHttpFactories,
                Ids.ELYTRON_PROVIDER_HTTP_SERVER_MECHANISM_FACTORY, "Provider HTTP Server Mechanism", PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        serviceLoaderHttpServerMechanismFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdHttpFactories,
                Ids.ELYTRON_SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY, "Service Loader HTTP Server Mechanism", SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        // sasl factories
        aggregateSaslServerFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdSaslFactories,
                Ids.ELYTRON_AGGREGATE_SASL_SERVER_FACTORY, "Aggregate SASL Server", AGGREGATE_SASL_SERVER_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        configurableSaslServerFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdSaslFactories,
                Ids.ELYTRON_CONFIGURABLE_SASL_SERVER_FACTORY, "Configurable SASL Server", CONFIGURABLE_SASL_SERVER_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        mechanismProviderFilteringSaslServerFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdSaslFactories,
                Ids.ELYTRON_MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY, "Mechanism Provider Filtering SASL Server", MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        providerSaslServerFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation, primaryIdSaslFactories,
                Ids.ELYTRON_PROVIDER_SASL_SERVER_FACTORY, "Provider SASL Server", PROVIDER_SASL_SERVER_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        saslAuthenticationFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdSaslFactories,
                Ids.ELYTRON_SASL_AUTHENTICATION_FACTORY, "SASL Authentication", SASL_AUTHENTICATION_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        serviceLoaderSaslServerFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdSaslFactories,
                Ids.ELYTRON_SERVICE_LOADER_SASL_SERVER_FACTORY, "Service Loader SASL Server", SERVICE_LOADER_SASL_SERVER_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        // other factories
        kerberosSecurityFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdOtherFactories,
                Ids.ELYTRON_KERBEROS_SECURITY_FACTORY, "Kerberos Security", KERBEROS_SECURITY_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        customCredentialSecurityFactory = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdOtherFactories,
                Ids.ELYTRON_CUSTOM_CREDENTIAL_SECURITY_FACTORY, "Custom Credential Security", CUSTOM_CREDENTIAL_SECURITY_FACTORY_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        // transformers
        aggregatePrincipalTransformer = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdTransformers,
                Ids.ELYTRON_AGGREGATE_PRINCIPAL_TRANSFORMER, "Aggregate", AGGREGATE_TRANSFORMER_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        chainedPrincipalTransformer = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdTransformers,
                Ids.ELYTRON_CHAINED_PRINCIPAL_TRANSFORMER, "Chained", CHAINED_TRANSFORMER_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        constantPrincipalTransformer = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdTransformers,
                Ids.ELYTRON_CONSTANT_PRINCIPAL_TRANSFORMER, "Constant", CONSTANT_TRANSFORMER_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        customPrincipalTransformer = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdTransformers,
                Ids.ELYTRON_CUSTOM_PRINCIPAL_TRANSFORMER, "Custom", CUSTOM_TRANSFORMER_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        regexPrincipalTransformer = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdTransformers,
                Ids.ELYTRON_REGEX_PRINCIPAL_TRANSFORMER, "Regex", REGEX_TRANSFORMER_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();

        regexValidatingPrincipalTransformer = new ResourceView(metadataRegistry, tableButtonFactory, navigation,
                primaryIdTransformers,
                Ids.ELYTRON_REGEX_VALIDATING_PRINCIPAL_TRANSFORMER, "Regex Validating", REGEX_VALIDATING_TRANSFORMER_ADDRESS, this,
                (name, address) -> presenter.reload(), () -> presenter.reload())
            .build();


        LayoutBuilder layoutBuilder = new LayoutBuilder()
                .row()
                .column()
                .addAll(navigation.panes())
                .end()
                .end();

        Element root = layoutBuilder.build();
        initElement(root);


    }

    @Override
    public void attach() {
        super.attach();

        aggregateHttpServerMechanismFactory.bindTableToForm();
        configurableHttpServerMechanismFactory.bindTableToForm();
        httpAuthenticationFactory.bindTableToForm();
        providerHttpServerMechanismFactory.bindTableToForm();
        serviceLoaderHttpServerMechanismFactory.bindTableToForm();

        // sasl factories
        aggregateSaslServerFactory.bindTableToForm();
        configurableSaslServerFactory.bindTableToForm();
        mechanismProviderFilteringSaslServerFactory.bindTableToForm();
        providerSaslServerFactory.bindTableToForm();
        saslAuthenticationFactory.bindTableToForm();
        serviceLoaderSaslServerFactory.bindTableToForm();

        // other factories
        kerberosSecurityFactory.bindTableToForm();
        customCredentialSecurityFactory.bindTableToForm();

        // transformers
        aggregatePrincipalTransformer.bindTableToForm();
        chainedPrincipalTransformer.bindTableToForm();
        constantPrincipalTransformer.bindTableToForm();
        customPrincipalTransformer.bindTableToForm();
        regexPrincipalTransformer.bindTableToForm();
        regexValidatingPrincipalTransformer.bindTableToForm();

    }


    @Override
    public void updateAggregateHttpServerMechanism(final List<NamedNode> model) {
        aggregateHttpServerMechanismFactory.getForm().clear();
        aggregateHttpServerMechanismFactory.getTable().update(model);
    }

    @Override
    public void updateAggregateSaslServer(final List<NamedNode> model) {
        aggregateSaslServerFactory.getForm().clear();
        aggregateSaslServerFactory.getTable().update(model);
    }

    @Override
    public void updateConfigurableHttpServerMechanism(final List<NamedNode> model) {
        configurableHttpServerMechanismFactory.getForm().clear();
        configurableHttpServerMechanismFactory.getTable().update(model);
    }

    @Override
    public void updateConfigurableSaslServer(final List<NamedNode> model) {
        configurableSaslServerFactory.getForm().clear();
        configurableSaslServerFactory.getTable().update(model);
    }

    @Override
    public void updateCustomCredentialSecurity(final List<NamedNode> model) {
        customCredentialSecurityFactory.getForm().clear();
        customCredentialSecurityFactory.getTable().update(model);
    }

    @Override
    public void updateHttpAuthentication(final List<NamedNode> model) {
        httpAuthenticationFactory.getForm().clear();
        httpAuthenticationFactory.getTable().update(model);
    }

    @Override
    public void updateKerberosSecurity(final List<NamedNode> model) {
        kerberosSecurityFactory.getForm().clear();
        kerberosSecurityFactory.getTable().update(model);
    }

    @Override
    public void updateMechanismProviderFilteringSaslServer(final List<NamedNode> model) {
        mechanismProviderFilteringSaslServerFactory.getForm().clear();
        mechanismProviderFilteringSaslServerFactory.getTable().update(model);
    }

    @Override
    public void updateProviderHttpServerMechanism(final List<NamedNode> model) {
        providerHttpServerMechanismFactory.getForm().clear();
        providerHttpServerMechanismFactory.getTable().update(model);
    }

    @Override
    public void updateProviderSaslServer(final List<NamedNode> model) {
        providerSaslServerFactory.getForm().clear();
        providerSaslServerFactory.getTable().update(model);
    }

    @Override
    public void updateSaslAuthentication(final List<NamedNode> model) {
        saslAuthenticationFactory.getForm().clear();
        saslAuthenticationFactory.getTable().update(model);
    }

    @Override
    public void updateServiceLoaderHttpServerMechanism(final List<NamedNode> model) {
        serviceLoaderHttpServerMechanismFactory.getForm().clear();
        serviceLoaderHttpServerMechanismFactory.getTable().update(model);
    }

    @Override
    public void updateServiceLoaderSaslServer(final List<NamedNode> model) {
        serviceLoaderSaslServerFactory.getForm().clear();
        serviceLoaderSaslServerFactory.getTable().update(model);
    }

    @Override
    public void updateAggregatePrincipalTransformer(final List<NamedNode> model) {
        aggregatePrincipalTransformer.getForm().clear();
        aggregatePrincipalTransformer.getTable().update(model);
    }

    @Override
    public void updateChainedPrincipalTransformer(final List<NamedNode> model) {
        chainedPrincipalTransformer.getForm().clear();
        chainedPrincipalTransformer.getTable().update(model);
    }

    @Override
    public void updateConstantPrincipalTransformer(final List<NamedNode> model) {
        constantPrincipalTransformer.getForm().clear();
        constantPrincipalTransformer.getTable().update(model);
    }

    @Override
    public void updateCustomPrincipalTransformer(final List<NamedNode> model) {
        customPrincipalTransformer.getForm().clear();
        customPrincipalTransformer.getTable().update(model);
    }

    @Override
    public void updateRegexPrincipalTransformer(final List<NamedNode> model) {
        regexPrincipalTransformer.getForm().clear();
        regexPrincipalTransformer.getTable().update(model);
    }

    @Override
    public void updateRegexValidatingPrincipalTransformer(final List<NamedNode> model) {
        regexValidatingPrincipalTransformer.getForm().clear();
        regexValidatingPrincipalTransformer.getTable().update(model);
    }

    public void setPresenter(final FactoriesPresenter presenter) {
        this.presenter = presenter;

        aggregateHttpServerMechanismFactory.setPresenter(presenter);
        configurableHttpServerMechanismFactory.setPresenter(presenter);
        httpAuthenticationFactory.setPresenter(presenter);
        providerHttpServerMechanismFactory.setPresenter(presenter);
        serviceLoaderHttpServerMechanismFactory.setPresenter(presenter);

        // sasl factories
        aggregateSaslServerFactory.setPresenter(presenter);
        configurableSaslServerFactory.setPresenter(presenter);
        mechanismProviderFilteringSaslServerFactory.setPresenter(presenter);
        providerSaslServerFactory.setPresenter(presenter);
        saslAuthenticationFactory.setPresenter(presenter);
        serviceLoaderSaslServerFactory.setPresenter(presenter);

        // other factories
        kerberosSecurityFactory.setPresenter(presenter);
        customCredentialSecurityFactory.setPresenter(presenter);

        // transformers
        aggregatePrincipalTransformer.setPresenter(presenter);
        chainedPrincipalTransformer.setPresenter(presenter);
        constantPrincipalTransformer.setPresenter(presenter);
        customPrincipalTransformer.setPresenter(presenter);
        regexPrincipalTransformer.setPresenter(presenter);
        regexValidatingPrincipalTransformer.setPresenter(presenter);
    }

    @Override
    public void registerComponents(final Attachable first, final Attachable... rest) {
        registerAttachable(first, rest);
    }
}