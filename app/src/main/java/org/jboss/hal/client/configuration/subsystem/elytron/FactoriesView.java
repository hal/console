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
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public abstract class FactoriesView extends MbuiViewImpl<FactoriesPresenter>
        implements FactoriesPresenter.MyView {

    public static FactoriesView create(final MbuiContext mbuiContext) {
        return new Mbui_FactoriesView(mbuiContext);
    }

    // @formatter:off
    @MbuiElement("factories-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("factories-aggregate-http-server-mechanism-table") NamedNodeTable<NamedNode> aggregateHttpServerMechanismTable;
    @MbuiElement("factories-aggregate-http-server-mechanism-form") Form<NamedNode> aggregateHttpServerMechanismForm;
    @MbuiElement("factories-aggregate-sasl-server-table") NamedNodeTable<NamedNode> aggregateSaslServerTable;
    @MbuiElement("factories-aggregate-sasl-server-form") Form<NamedNode> aggregateSaslServerForm;
    @MbuiElement("factories-configurable-http-server-mechanism-table") NamedNodeTable<NamedNode> configurableHttpServerMechanismTable;
    @MbuiElement("factories-configurable-http-server-mechanism-form") Form<NamedNode> configurableHttpServerMechanismForm;
    @MbuiElement("factories-configurable-sasl-server-table") NamedNodeTable<NamedNode> configurableSaslServerTable;
    @MbuiElement("factories-configurable-sasl-server-form") Form<NamedNode> configurableSaslServerForm;
    @MbuiElement("factories-custom-credential-security-table") NamedNodeTable<NamedNode> customCredentialSecurityTable;
    @MbuiElement("factories-custom-credential-security-form") Form<NamedNode> customCredentialSecurityForm;
    @MbuiElement("factories-http-authentication-table") NamedNodeTable<NamedNode> httpAuthenticationTable;
    @MbuiElement("factories-http-authentication-form") Form<NamedNode> httpAuthenticationForm;
    @MbuiElement("factories-kerberos-security-table") NamedNodeTable<NamedNode> kerberosSecurityTable;
    @MbuiElement("factories-kerberos-security-form") Form<NamedNode> kerberosSecurityForm;
    @MbuiElement("factories-mechanism-provider-filtering-sasl-server-table") NamedNodeTable<NamedNode> mechanismProviderFilteringSaslServerTable;
    @MbuiElement("factories-mechanism-provider-filtering-sasl-server-form") Form<NamedNode> mechanismProviderFilteringSaslServerForm;
    @MbuiElement("factories-provider-http-server-mechanism-table") NamedNodeTable<NamedNode> providerHttpServerMechanismTable;
    @MbuiElement("factories-provider-http-server-mechanism-form") Form<NamedNode> providerHttpServerMechanismForm;
    @MbuiElement("factories-provider-sasl-server-table") NamedNodeTable<NamedNode> providerSaslServerTable;
    @MbuiElement("factories-provider-sasl-server-form") Form<NamedNode> providerSaslServerForm;
    @MbuiElement("factories-sasl-authentication-table") NamedNodeTable<NamedNode> saslAuthenticationTable;
    @MbuiElement("factories-sasl-authentication-form") Form<NamedNode> saslAuthenticationForm;
    @MbuiElement("factories-service-loader-http-server-mechanism-table") NamedNodeTable<NamedNode> serviceLoaderHttpServerMechanismTable;
    @MbuiElement("factories-service-loader-http-server-mechanism-form") Form<NamedNode> serviceLoaderHttpServerMechanismForm;
    @MbuiElement("factories-service-loader-sasl-server-table") NamedNodeTable<NamedNode> serviceLoaderSaslServerTable;
    @MbuiElement("factories-service-loader-sasl-server-form") Form<NamedNode> serviceLoaderSaslServerForm;

    @MbuiElement("transformers-aggregate-table") NamedNodeTable<NamedNode> aggregatePrincipalTransformerTable;
    @MbuiElement("transformers-aggregate-form") Form<NamedNode> aggregatePrincipalTransformerForm;
    @MbuiElement("transformers-chained-table") NamedNodeTable<NamedNode> chainedPrincipalTransformerTable;
    @MbuiElement("transformers-chained-form") Form<NamedNode> chainedPrincipalTransformerForm;
    @MbuiElement("transformers-constant-table") NamedNodeTable<NamedNode> constantPrincipalTransformerTable;
    @MbuiElement("transformers-constant-form") Form<NamedNode> constantPrincipalTransformerForm;
    @MbuiElement("transformers-custom-table") NamedNodeTable<NamedNode> customPrincipalTransformerTable;
    @MbuiElement("transformers-custom-form") Form<NamedNode> customPrincipalTransformerForm;
    @MbuiElement("transformers-regex-table") NamedNodeTable<NamedNode> regexPrincipalTransformerTable;
    @MbuiElement("transformers-regex-form") Form<NamedNode> regexPrincipalTransformerForm;
    @MbuiElement("transformers-regex-validating-table") NamedNodeTable<NamedNode> regexValidatingPrincipalTransformerTable;
    @MbuiElement("transformers-regex-validating-form") Form<NamedNode> regexValidatingPrincipalTransformerForm;
    // @formatter:on

    FactoriesView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateAggregateHttpServerMechanism(final List<NamedNode> model) {
        aggregateHttpServerMechanismForm.clear();
        aggregateHttpServerMechanismTable.update(model);
    }

    @Override
    public void updateAggregateSaslServer(final List<NamedNode> model) {
        aggregateSaslServerForm.clear();
        aggregateSaslServerTable.update(model);
    }

    @Override
    public void updateConfigurableHttpServerMechanism(final List<NamedNode> model) {
        configurableHttpServerMechanismForm.clear();
        configurableHttpServerMechanismTable.update(model);
    }

    @Override
    public void updateConfigurableSaslServer(final List<NamedNode> model) {
        configurableSaslServerForm.clear();
        configurableSaslServerTable.update(model);
    }

    @Override
    public void updateCustomCredentialSecurity(final List<NamedNode> model) {
        customCredentialSecurityForm.clear();
        customCredentialSecurityTable.update(model);
    }

    @Override
    public void updateHttpAuthentication(final List<NamedNode> model) {
        httpAuthenticationForm.clear();
        httpAuthenticationTable.update(model);
    }

    @Override
    public void updateKerberosSecurity(final List<NamedNode> model) {
        kerberosSecurityForm.clear();
        kerberosSecurityTable.update(model);
    }

    @Override
    public void updateMechanismProviderFilteringSaslServer(final List<NamedNode> model) {
        mechanismProviderFilteringSaslServerForm.clear();
        mechanismProviderFilteringSaslServerTable.update(model);
    }

    @Override
    public void updateProviderHttpServerMechanism(final List<NamedNode> model) {
        providerHttpServerMechanismForm.clear();
        providerHttpServerMechanismTable.update(model);
    }

    @Override
    public void updateProviderSaslServer(final List<NamedNode> model) {
        providerSaslServerForm.clear();
        providerSaslServerTable.update(model);
    }

    @Override
    public void updateSaslAuthentication(final List<NamedNode> model) {
        saslAuthenticationForm.clear();
        saslAuthenticationTable.update(model);
    }

    @Override
    public void updateServiceLoaderHttpServerMechanism(final List<NamedNode> model) {
        serviceLoaderHttpServerMechanismForm.clear();
        serviceLoaderHttpServerMechanismTable.update(model);
    }

    @Override
    public void updateServiceLoaderSaslServer(final List<NamedNode> model) {
        serviceLoaderSaslServerForm.clear();
        serviceLoaderSaslServerTable.update(model);
    }

    @Override
    public void updateAggregatePrincipalTransformer(final List<NamedNode> model) {
        aggregatePrincipalTransformerForm.clear();
        aggregatePrincipalTransformerTable.update(model);
    }

    @Override
    public void updateChainedPrincipalTransformer(final List<NamedNode> model) {
        chainedPrincipalTransformerForm.clear();
        chainedPrincipalTransformerTable.update(model);
    }

    @Override
    public void updateConstantPrincipalTransformer(final List<NamedNode> model) {
        constantPrincipalTransformerForm.clear();
        constantPrincipalTransformerTable.update(model);
    }

    @Override
    public void updateCustomPrincipalTransformer(final List<NamedNode> model) {
        customPrincipalTransformerForm.clear();
        customPrincipalTransformerTable.update(model);
    }

    @Override
    public void updateRegexPrincipalTransformer(final List<NamedNode> model) {
        regexPrincipalTransformerForm.clear();
        regexPrincipalTransformerTable.update(model);
    }

    @Override
    public void updateRegexValidatingPrincipalTransformer(final List<NamedNode> model) {
        regexValidatingPrincipalTransformerForm.clear();
        regexValidatingPrincipalTransformerTable.update(model);
    }
}