/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import static java.util.Arrays.asList;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.configuration.subsystem.elytron.ElytronResource.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FILTERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PATTERN_FILTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PREDEFINED_FILTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_NAME;

public class FactoriesView extends HalViewImpl implements FactoriesPresenter.MyView {

    private final Map<String, ResourceElement> elements;
    private final VerticalNavigation navigation;
    private final HttpAuthenticationFactoryElement httpAuthenticationFactoryElement;
    private final SaslAuthenticationFactoryElement saslAuthenticationFactoryElement;
    private FactoriesPresenter presenter;

    @Inject
    @SuppressWarnings("HardCodedStringLiteral")
    FactoriesView(MbuiContext mbuiContext) {
        elements = new HashMap<>();
        navigation = new VerticalNavigation();
        registerAttachable(navigation);

        String primaryIdHttpFactories = "http-factories-item";
        String primaryIdSaslFactories = "sasl-factories-item";
        String primaryIdOtherFactories = "other-factories-item";
        String primaryIdTransformers = "transformers-item";
        navigation.addPrimary(primaryIdHttpFactories, "HTTP Factories", "fa fa-file-o");
        navigation.addPrimary(primaryIdSaslFactories, "SASL Factories", "fa fa-exchange");
        navigation.addPrimary(primaryIdOtherFactories, "Other Factories", "fa fa-desktop");
        navigation.addPrimary(primaryIdTransformers, "Principal Transformers", "fa fa-archive");

        // ------------------------------------------------------ http factories

        addResourceElement(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY,
                AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                nodes -> updateResourceElement(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                        nodes))),
                primaryIdHttpFactories,
                Ids.build(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY.baseId, Ids.ITEM),
                "Aggregate HTTP Server Mechanism");

        addResourceElement(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY,
                CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                nodes -> updateResourceElement(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                        nodes)))
                        .setComplexListAttribute(FILTERS, PATTERN_FILTER,
                                modelNode -> Ids.build(modelNode.get(PATTERN_FILTER).asString()))
                        .build(),
                primaryIdHttpFactories,
                Ids.build(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY.baseId, Ids.ITEM),
                "Configurable HTTP Server Mechanism");

        // HTTP_AUTHENTICATION_FACTORY uses a custom element
        Metadata metadata = mbuiContext.metadataRegistry().lookup(HTTP_AUTHENTICATION_FACTORY.template);
        httpAuthenticationFactoryElement = new HttpAuthenticationFactoryElement(metadata,
                mbuiContext.tableButtonFactory());
        registerAttachable(httpAuthenticationFactoryElement);
        navigation.addSecondary(primaryIdHttpFactories, Ids.build(HTTP_AUTHENTICATION_FACTORY.baseId, Ids.ITEM),
                Names.HTTP_AUTHENTICATION_FACTORY, httpAuthenticationFactoryElement.element());

        addResourceElement(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY,
                PROVIDER_HTTP_SERVER_MECHANISM_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                nodes -> updateResourceElement(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                        nodes))),
                primaryIdHttpFactories,
                Ids.build(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY.baseId, Ids.ITEM),
                "Provider HTTP Server Mechanism");

        addResourceElement(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY,
                SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                nodes -> updateResourceElement(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY.resource,
                                        nodes))),
                primaryIdHttpFactories,
                Ids.build(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY.baseId, Ids.ITEM),
                "Service Loader HTTP Server Mechanism");

        // ------------------------------------------------------ sasl factories

        addResourceElement(AGGREGATE_SASL_SERVER_FACTORY,
                AGGREGATE_SASL_SERVER_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(AGGREGATE_SASL_SERVER_FACTORY.resource,
                                nodes -> updateResourceElement(AGGREGATE_SASL_SERVER_FACTORY.resource, nodes))),
                primaryIdSaslFactories,
                Ids.build(AGGREGATE_SASL_SERVER_FACTORY.baseId, Ids.ITEM),
                "Aggregate SASL Server");

        addResourceElement(CONFIGURABLE_SASL_SERVER_FACTORY,
                CONFIGURABLE_SASL_SERVER_FACTORY.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(CONFIGURABLE_SASL_SERVER_FACTORY.resource,
                                nodes -> updateResourceElement(CONFIGURABLE_SASL_SERVER_FACTORY.resource, nodes)))
                        .setComplexListAttribute(FILTERS, asList(PREDEFINED_FILTER, PATTERN_FILTER),
                                asList(PREDEFINED_FILTER, PATTERN_FILTER),
                                modelNode -> Ids.build(modelNode.get(PATTERN_FILTER).asString()))
                        .build(),
                primaryIdSaslFactories,
                Ids.build(CONFIGURABLE_SASL_SERVER_FACTORY.baseId, Ids.ITEM),
                "Configurable SASL Server");

        addResourceElement(MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY,
                MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY.resourceElementBuilder(mbuiContext,
                        () -> presenter.reload(
                                MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY.resource,
                                nodes -> updateResourceElement(
                                        MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY.resource, nodes)))
                        .setComplexListAttribute(FILTERS, PROVIDER_NAME,
                                modelNode -> Ids.build(modelNode.get(PROVIDER_NAME).asString()))
                        .build(),
                primaryIdSaslFactories,
                Ids.build(MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY.baseId, Ids.ITEM),
                "Mechanism Provider Filtering SASL Server");

        addResourceElement(PROVIDER_SASL_SERVER_FACTORY,
                PROVIDER_SASL_SERVER_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(PROVIDER_SASL_SERVER_FACTORY.resource,
                                nodes -> updateResourceElement(PROVIDER_SASL_SERVER_FACTORY.resource, nodes))),
                primaryIdSaslFactories,
                Ids.build(PROVIDER_SASL_SERVER_FACTORY.baseId, Ids.ITEM),
                "Provider SASL Server");

        // SASL_AUTHENTICATION_FACTORY uses a custom element
        Metadata metadataSaslAuthFactory = mbuiContext.metadataRegistry().lookup(SASL_AUTHENTICATION_FACTORY.template);
        saslAuthenticationFactoryElement = new SaslAuthenticationFactoryElement(metadataSaslAuthFactory,
                mbuiContext.tableButtonFactory());
        registerAttachable(saslAuthenticationFactoryElement);
        navigation.addSecondary(primaryIdSaslFactories, Ids.build(SASL_AUTHENTICATION_FACTORY.baseId, Ids.ITEM),
                Names.SASL_AUTHENTICATION_FACTORY, saslAuthenticationFactoryElement.element());

        addResourceElement(SERVICE_LOADER_SASL_SERVER_FACTORY,
                SERVICE_LOADER_SASL_SERVER_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(SERVICE_LOADER_SASL_SERVER_FACTORY.resource,
                                nodes -> updateResourceElement(SERVICE_LOADER_SASL_SERVER_FACTORY.resource, nodes))),
                primaryIdSaslFactories,
                Ids.build(SERVICE_LOADER_SASL_SERVER_FACTORY.baseId, Ids.ITEM),
                "Service Loader SASL Server");

        // ------------------------------------------------------ other factories

        addResourceElement(KERBEROS_SECURITY_FACTORY,
                KERBEROS_SECURITY_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(KERBEROS_SECURITY_FACTORY.resource,
                                nodes -> updateResourceElement(KERBEROS_SECURITY_FACTORY.resource, nodes))),
                primaryIdOtherFactories,
                Ids.build(KERBEROS_SECURITY_FACTORY.baseId, Ids.ITEM),
                "Kerberos Security");

        addResourceElement(CUSTOM_CREDENTIAL_SECURITY_FACTORY,
                CUSTOM_CREDENTIAL_SECURITY_FACTORY.resourceElement(mbuiContext,
                        () -> presenter.reload(CUSTOM_CREDENTIAL_SECURITY_FACTORY.resource,
                                nodes -> updateResourceElement(CUSTOM_CREDENTIAL_SECURITY_FACTORY.resource, nodes))),
                primaryIdOtherFactories,
                Ids.build(CUSTOM_CREDENTIAL_SECURITY_FACTORY.baseId, Ids.ITEM),
                "Custom Credential Security");

        // ------------------------------------------------------ transformers

        addResourceElement(AGGREGATE_PRINCIPAL_TRANSFORMER,
                AGGREGATE_PRINCIPAL_TRANSFORMER.resourceElement(mbuiContext,
                        () -> presenter.reload(AGGREGATE_PRINCIPAL_TRANSFORMER.resource,
                                nodes -> updateResourceElement(AGGREGATE_PRINCIPAL_TRANSFORMER.resource, nodes))),
                primaryIdTransformers,
                Ids.build(AGGREGATE_PRINCIPAL_TRANSFORMER.baseId, Ids.ITEM),
                "Aggregate");

        addResourceElement(CHAINED_PRINCIPAL_TRANSFORMER,
                CHAINED_PRINCIPAL_TRANSFORMER.resourceElement(mbuiContext,
                        () -> presenter.reload(CHAINED_PRINCIPAL_TRANSFORMER.resource,
                                nodes -> updateResourceElement(CHAINED_PRINCIPAL_TRANSFORMER.resource, nodes))),
                primaryIdTransformers,
                Ids.build(CHAINED_PRINCIPAL_TRANSFORMER.baseId, Ids.ITEM),
                "Chained");

        addResourceElement(CONSTANT_PRINCIPAL_TRANSFORMER,
                CONSTANT_PRINCIPAL_TRANSFORMER.resourceElement(mbuiContext,
                        () -> presenter.reload(CONSTANT_PRINCIPAL_TRANSFORMER.resource,
                                nodes -> updateResourceElement(CONSTANT_PRINCIPAL_TRANSFORMER.resource, nodes))),
                primaryIdTransformers,
                Ids.build(CONSTANT_PRINCIPAL_TRANSFORMER.baseId, Ids.ITEM),
                "Constant");

        addResourceElement(CUSTOM_PRINCIPAL_TRANSFORMER,
                CUSTOM_PRINCIPAL_TRANSFORMER.resourceElement(mbuiContext,
                        () -> presenter.reload(CUSTOM_PRINCIPAL_TRANSFORMER.resource,
                                nodes -> updateResourceElement(CUSTOM_PRINCIPAL_TRANSFORMER.resource, nodes))),
                primaryIdTransformers,
                Ids.build(CUSTOM_PRINCIPAL_TRANSFORMER.baseId, Ids.ITEM),
                "Custom");

        addResourceElement(REGEX_PRINCIPAL_TRANSFORMER,
                REGEX_PRINCIPAL_TRANSFORMER.resourceElement(mbuiContext,
                        () -> presenter.reload(REGEX_PRINCIPAL_TRANSFORMER.resource,
                                nodes -> updateResourceElement(REGEX_PRINCIPAL_TRANSFORMER.resource, nodes))),
                primaryIdTransformers,
                Ids.build(REGEX_PRINCIPAL_TRANSFORMER.baseId, Ids.ITEM),
                "Regex");

        addResourceElement(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER,
                REGEX_VALIDATING_PRINCIPAL_TRANSFORMER.resourceElement(mbuiContext,
                        () -> presenter.reload(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER.resource,
                                nodes -> updateResourceElement(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER.resource,
                                        nodes))),
                primaryIdTransformers,
                Ids.build(REGEX_VALIDATING_PRINCIPAL_TRANSFORMER.baseId, Ids.ITEM),
                "Regex Validating");

        initElement(row()
                .add(column()
                        .addAll(navigation.panes())));
    }

    private void addResourceElement(ElytronResource resource, ResourceElement element,
            String primaryId, String secondaryId, String text) {
        elements.put(resource.resource, element);
        registerAttachable(element);
        navigation.addSecondary(primaryId, secondaryId, text, element.element());
    }

    public void setPresenter(FactoriesPresenter presenter) {
        this.presenter = presenter;
        httpAuthenticationFactoryElement.setPresenter(presenter);
        saslAuthenticationFactoryElement.setPresenter(presenter);
    }

    @Override
    public void updateResourceElement(String resource, List<NamedNode> nodes) {
        ResourceElement resourceElement = elements.get(resource);
        if (resourceElement != null) {
            resourceElement.update(nodes);
        }
    }

    @Override
    public void updateHttpAuthentication(List<NamedNode> nodes) {
        httpAuthenticationFactoryElement.update(nodes);
    }

    @Override
    public void updateSaslAuthentication(List<NamedNode> nodes) {
        saslAuthenticationFactoryElement.update(nodes);
    }
}