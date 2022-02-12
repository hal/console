/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.shared.sslwizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.client.shared.sslwizard.AbstractConfiguration.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class EnableSSLWizard {

    private static final AddressTemplate HTTP_INTERFACE_TEMPLATE = AddressTemplate.of(
            "{domain.controller}/core-service=management/management-interface=http-interface");
    private static final AddressTemplate UNDERTOW_HTTPS_LISTENER_TEMPLATE = AddressTemplate.of(
            "/{selected.profile}/subsystem=undertow/server=*/https-listener=*");

    // default values
    private static final String KEY_MANAGER_ALGORITHM = "SunX509";
    private static final String KEY_MANAGER_TLSV1_2 = "TLSv1.2";

    private final Resources resources;
    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private Host host;
    private EnableSSLPresenter presenter;
    private Provider<Progress> progress;
    private EventBus eventBus;
    private Map<String, List<String>> existingResources;
    private boolean undertowHttps;
    private String undertowServer;
    private String httpsListener;

    private EnableSSLWizard(Map<String, List<String>> existingResources, Resources resources, Environment environment,
            StatementContext statementContext, Dispatcher dispatcher, Host host, EnableSSLPresenter presenter,
            @Footer Provider<Progress> progress, EventBus eventBus) {
        this.existingResources = existingResources;
        this.resources = resources;
        this.environment = environment;
        this.dispatcher = dispatcher;
        this.host = host;
        this.presenter = presenter;
        this.progress = progress;
        this.eventBus = eventBus;
        this.statementContext = statementContext;
    }

    public void show() {
        Constants constants = resources.constants();
        AddressTemplate template = undertowHttps ? UNDERTOW_HTTPS_LISTENER_TEMPLATE : HTTP_INTERFACE_TEMPLATE;
        Wizard.Builder<EnableSSLContext, EnableSSLState> wb = new Wizard.Builder<>(constants.enableSSLManagementTitle(),
                new EnableSSLContext());

        wb.addStep(EnableSSLState.DEFINE_STRATEGY, new DefineStrategyStep(resources, environment.isStandalone(), undertowHttps))
                .addStep(EnableSSLState.CONFIGURATION,
                        new ConfigurationStep(existingResources, resources, environment, undertowHttps, template))
                .addStep(EnableSSLState.REVIEW,
                        new ReviewStep(dispatcher, statementContext, resources, environment, undertowHttps, template))

                .onBack((context, currentState) -> {
                    EnableSSLState previous = null;
                    switch (currentState) {
                        case DEFINE_STRATEGY:
                            break;
                        case CONFIGURATION:
                            previous = EnableSSLState.DEFINE_STRATEGY;
                            break;
                        case REVIEW:
                            previous = EnableSSLState.CONFIGURATION;
                            break;
                        default:
                            break;
                    }
                    return previous;
                })
                .onNext((context, currentState) -> {
                    EnableSSLState next = null;
                    switch (currentState) {
                        case DEFINE_STRATEGY:
                            next = EnableSSLState.CONFIGURATION;
                            break;
                        case CONFIGURATION:
                            next = EnableSSLState.REVIEW;
                            break;
                        case REVIEW:
                            break;
                        default:
                            break;
                    }
                    return next;
                })
                .stayOpenAfterFinish()
                .onFinish((wizard, context) -> {

                    ModelNode model = context.model;
                    ModelNode credRef = new ModelNode();
                    credRef.get(CLEAR_TEXT).set(asString(model, AbstractConfiguration.KEY_STORE_PASSWORD));

                    // use Flow tasks to run DMR operations as there are resources that must exists before next
                    // operations are called, as in the example of a generate-key-pair and import-certificate
                    // the key-store must exists. For this case, the Composite doesn't work.
                    List<Task<FlowContext>> tasks = new ArrayList<>();

                    // key-store is only created when user chooses to create all resources or create a key-store based on
                    // an existing JKS file
                    boolean createKeyStore = !context.strategy.equals(
                            EnableSSLContext.Strategy.KEYSTORE_RESOURCE_EXISTS);
                    String keyStoreName = createKeyStore ? asString(model,
                            AbstractConfiguration.KEY_STORE_NAME)
                            : asString(model,
                                    KEY_STORE);
                    if (createKeyStore) {
                        if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_CREATE)) {

                            ResourceAddress ksAddress = keyStoreTemplate().resolve(statementContext, keyStoreName);
                            tasks.add(flowContext -> {
                                Operation.Builder builder = new Operation.Builder(ksAddress, ADD)
                                        .param(PATH, asString(model, KEY_STORE_PATH))
                                        .param(CREDENTIAL_REFERENCE, credRef)
                                        .param(TYPE, asString(model, AbstractConfiguration.KEY_STORE_TYPE));

                                if (model.hasDefined(AbstractConfiguration.KEY_STORE_RELATIVE_TO)) {
                                    builder.param(RELATIVE_TO, asString(model, AbstractConfiguration.KEY_STORE_RELATIVE_TO));
                                }
                                Operation keyStoreOp = builder.build();

                                return dispatcher.execute(keyStoreOp)
                                        .doOnError(exception -> wizard.showError(constants.failed(),
                                                resources.messages().addKeyStoreError(keyStoreName),
                                                exception.getMessage(), false))
                                        .toCompletable();
                            });

                            tasks.add(flowContext -> {
                                Composite composite = new Composite();

                                // the generate-key=pair can only be called on an existing key-store
                                String dn = "CN=" + asString(model, AbstractConfiguration.PRIVATE_KEY_DN_CN)
                                        + ", OU=" + asString(model, AbstractConfiguration.PRIVATE_KEY_DN_OU)
                                        + ", O=" + asString(model, AbstractConfiguration.PRIVATE_KEY_DN_O)
                                        + ", L=" + asString(model, AbstractConfiguration.PRIVATE_KEY_DN_L)
                                        + ", ST=" + asString(model, AbstractConfiguration.PRIVATE_KEY_DN_ST)
                                        + ", C=" + asString(model, AbstractConfiguration.PRIVATE_KEY_DN_C);
                                Operation genKeyOp = new Operation.Builder(ksAddress, GENERATE_KEY_PAIR)
                                        .param(ALIAS, asString(model, AbstractConfiguration.PRIVATE_KEY_ALIAS))
                                        .param(DISTINGUISHED_NAME, dn)
                                        .param(VALIDITY, asString(model, AbstractConfiguration.PRIVATE_KEY_VALIDITY))
                                        .param(ModelDescriptionConstants.ALGORITHM,
                                                asString(model, AbstractConfiguration.PRIVATE_KEY_ALGORITHM))
                                        .build();
                                composite.add(genKeyOp);

                                Operation storeOp = new Operation.Builder(ksAddress, STORE)
                                        .build();
                                composite.add(storeOp);

                                return dispatcher.execute(composite)
                                        .toCompletable();
                            });

                        } else if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_FILE_EXISTS)) {

                            tasks.add(flowContext -> {
                                ResourceAddress ksAddress = keyStoreTemplate().resolve(statementContext, keyStoreName);
                                Operation.Builder builder = new Operation.Builder(ksAddress, ADD)
                                        .param(PATH, asString(model, KEY_STORE_PATH))
                                        .param(CREDENTIAL_REFERENCE, credRef)
                                        .param(TYPE, asString(model, AbstractConfiguration.KEY_STORE_TYPE))
                                        .param(REQUIRED, true);

                                if (model.hasDefined(AbstractConfiguration.KEY_STORE_RELATIVE_TO)) {
                                    builder.param(RELATIVE_TO, asString(model, AbstractConfiguration.KEY_STORE_RELATIVE_TO));
                                }
                                Operation keyStoreOp = builder.build();
                                return dispatcher.execute(keyStoreOp)
                                        .doOnError(exception -> wizard.showError(constants.failed(),
                                                resources.messages().addKeyStoreError(keyStoreName),
                                                exception.getMessage(), false))
                                        .toCompletable();
                            });
                        } else if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_OBTAIN_LETSENCRYPT)) {

                            ResourceAddress ksAddress = keyStoreTemplate().resolve(statementContext, keyStoreName);
                            tasks.add(flowContext -> {
                                Operation.Builder builder = new Operation.Builder(ksAddress, ADD)
                                        .param(PATH, asString(model, KEY_STORE_PATH))
                                        .param(CREDENTIAL_REFERENCE, credRef)
                                        .param(TYPE, asString(model, AbstractConfiguration.KEY_STORE_TYPE));

                                if (model.hasDefined(AbstractConfiguration.KEY_STORE_RELATIVE_TO)) {
                                    builder.param(RELATIVE_TO, asString(model, AbstractConfiguration.KEY_STORE_RELATIVE_TO));
                                }
                                Operation keyStoreOp = builder.build();

                                return dispatcher.execute(keyStoreOp)
                                        .doOnError(exception -> wizard.showError(constants.failed(),
                                                resources.messages().addKeyStoreError(keyStoreName),
                                                exception.getMessage(), false))
                                        .toCompletable();
                            });

                            String caaName = asString(model, CAA_NAME);
                            ResourceAddress caaAddress = certificateAuthorityAccountTemplate().resolve(statementContext,
                                    caaName);
                            tasks.add(flowContext -> {
                                Operation caaOp = new Operation.Builder(caaAddress, ADD)
                                        .param(KEY_STORE, keyStoreName)
                                        .param(ALIAS, asString(model, CAA_ALIAS))
                                        .build();

                                return dispatcher.execute(caaOp)
                                        .doOnError(exception -> wizard.showError(constants.failed(),
                                                resources.messages().addResourceError(caaName, exception.getMessage()),
                                                false))
                                        .toCompletable();
                            });

                            tasks.add(flowContext -> {
                                Composite composite = new Composite();
                                String obtainAlias = asString(model, PRIVATE_KEY_ALIAS);
                                Operation obtainOp = new Operation.Builder(ksAddress, OBTAIN_CERTIFICATE)
                                        .param(ALIAS, obtainAlias)
                                        .param(CERTIFICATE_AUTHORITY_ACCOUNT, caaName)
                                        .param("domain-names", model.get(CAA_DOMAIN_NAMES))
                                        .param("agree-to-terms-of-service", true)
                                        .param("staging", asString(model, CAA_STAGING))
                                        .build();
                                composite.add(obtainOp);

                                Operation storeOp = new Operation.Builder(ksAddress, STORE)
                                        .build();
                                composite.add(storeOp);

                                return dispatcher.execute(composite)
                                        .doOnError(ex -> wizard.showError(constants.failed(),
                                                resources.messages()
                                                        .obtainCertificateError(obtainAlias, keyStoreName,
                                                                ex.getMessage()),
                                                false))
                                        .toCompletable();
                            });
                        }
                    }

                    String trustManagerName = model.hasDefined(TRUST_MANAGER) ? asString(model, TRUST_MANAGER) : null;
                    if (context.mutualAuthentication) {
                        ModelNode tsCredRef = new ModelNode();
                        tsCredRef.get(CLEAR_TEXT).set(asString(model, AbstractConfiguration.TRUST_STORE_PASSWORD));
                        String trustStoreName = asString(model, AbstractConfiguration.TRUST_STORE_NAME);

                        ResourceAddress tsAddress = keyStoreTemplate().resolve(statementContext, trustStoreName);
                        tasks.add(flowContext -> {
                            Operation.Builder builder = new Operation.Builder(tsAddress, ADD)
                                    .param(PATH, asString(model, AbstractConfiguration.TRUST_STORE_PATH))
                                    .param(CREDENTIAL_REFERENCE, tsCredRef)
                                    .param(TYPE, asString(model, AbstractConfiguration.TRUST_STORE_TYPE));

                            if (model.hasDefined(AbstractConfiguration.TRUST_STORE_RELATIVE_TO)) {
                                builder.param(RELATIVE_TO, asString(model, AbstractConfiguration.TRUST_STORE_RELATIVE_TO));
                            }
                            Operation trustStoreOp = builder.build();

                            return dispatcher.execute(trustStoreOp)
                                    .toCompletable();
                        });

                        tasks.add(flowContext -> {
                            Composite composite = new Composite();
                            Operation importCertOp = new Operation.Builder(tsAddress, IMPORT_CERTIFICATE)
                                    .param(ALIAS, asString(model, AbstractConfiguration.CLIENT_CERTIFICATE_ALIAS))
                                    .param(PATH, asString(model, AbstractConfiguration.CLIENT_CERTIFICATE_PATH))
                                    .param(CREDENTIAL_REFERENCE, tsCredRef)
                                    .param(VALIDATE, model.get(AbstractConfiguration.CLIENT_CERTIFICATE_VALIDATE)
                                            .asBoolean(false))
                                    .param(TRUST_CACERTS,
                                            model.get(AbstractConfiguration.CLIENT_CERTIFICATE_TRUST).asBoolean(false))
                                    .build();
                            composite.add(importCertOp);

                            Operation storeOp = new Operation.Builder(tsAddress, STORE)
                                    .build();
                            composite.add(storeOp);

                            ResourceAddress etmAddress = trustManagerTemplate().resolve(statementContext, trustManagerName);
                            Operation trustManagerOp = new Operation.Builder(etmAddress, ADD)
                                    .param(KEY_STORE, trustStoreName)
                                    .param(ModelDescriptionConstants.ALGORITHM, KEY_MANAGER_ALGORITHM)
                                    .build();
                            composite.add(trustManagerOp);

                            return dispatcher.execute(composite)
                                    .toCompletable();
                        });
                    }

                    Composite composite = new Composite();
                    String keyManager = asString(model, KEY_MANAGER);
                    ResourceAddress ekmAddress = keyManagerTemplate().resolve(statementContext, keyManager);
                    Operation keyManagerOp = new Operation.Builder(ekmAddress, ADD)
                            .param(KEY_STORE, keyStoreName)
                            .param(ModelDescriptionConstants.ALGORITHM, KEY_MANAGER_ALGORITHM)
                            .param(CREDENTIAL_REFERENCE, credRef)
                            .build();
                    composite.add(keyManagerOp);

                    ModelNode protocols = new ModelNode();
                    protocols.add(KEY_MANAGER_TLSV1_2);
                    String serverSslContext = asString(model, SERVER_SSL_CONTEXT);
                    ResourceAddress sslCtxAddress = sslContextTemplate().resolve(statementContext,
                            serverSslContext);
                    Operation.Builder sslCtxBuilder = new Operation.Builder(sslCtxAddress, ADD)
                            .param(KEY_MANAGER, keyManager)
                            .param(PROTOCOLS, protocols);

                    if (context.mutualAuthentication) {
                        sslCtxBuilder.param(TRUST_MANAGER, trustManagerName);
                        sslCtxBuilder.param(WANT_CLIENT_AUTH, true);
                    }
                    Operation sslCtxOp = sslCtxBuilder.build();
                    composite.add(sslCtxOp);

                    if (undertowHttps) {
                        ResourceAddress httpsAddress = UNDERTOW_HTTPS_LISTENER_TEMPLATE.resolve(
                                statementContext, undertowServer, httpsListener);
                        Operation writeSslCtxOp = new Operation.Builder(httpsAddress, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, SSL_CONTEXT)
                                .param(VALUE, serverSslContext)
                                .build();
                        composite.add(writeSslCtxOp);
                        // undefine the "alternatives" attributes
                        composite.add(undefineAttribute(httpsAddress, SECURITY_REALM));
                        composite.add(undefineAttribute(httpsAddress, "verify-client"));
                        composite.add(undefineAttribute(httpsAddress, "enabled-cipher-suites"));
                        composite.add(undefineAttribute(httpsAddress, "enabled-protocols"));
                        composite.add(undefineAttribute(httpsAddress, "ssl-session-cache-size"));
                        composite.add(undefineAttribute(httpsAddress, "ssl-session-timeout"));
                    } else {
                        ResourceAddress httpInterfaceAddress = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
                        Operation writeSslCtxOp = new Operation.Builder(httpInterfaceAddress, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, SSL_CONTEXT)
                                .param(VALUE, serverSslContext)
                                .build();
                        composite.add(writeSslCtxOp);

                        if (environment.isStandalone()) {
                            Operation writeSecureSocketBinding = new Operation.Builder(httpInterfaceAddress,
                                    WRITE_ATTRIBUTE_OPERATION)
                                            .param(NAME, SECURE_SOCKET_BINDING)
                                            .param(VALUE, asString(model, SECURE_SOCKET_BINDING))
                                            .build();
                            composite.add(writeSecureSocketBinding);
                        } else {
                            Operation writeSecurePortOp = new Operation.Builder(httpInterfaceAddress,
                                    WRITE_ATTRIBUTE_OPERATION)
                                            .param(NAME, SECURE_PORT)
                                            .param(VALUE, asString(model, SECURE_PORT))
                                            .build();
                            composite.add(writeSecurePortOp);
                        }
                    }

                    tasks.add(flowContext -> dispatcher.execute(composite).toCompletable());

                    series(new FlowContext(progress.get()), tasks)
                            .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                                @Override
                                public void onSuccess(FlowContext flowContext) {
                                    if (undertowHttps) {
                                        wizard.showSuccess(resources.constants().success(),
                                                resources.messages().enableSSLResultsSuccessUndertow(httpsListener,
                                                        serverSslContext),
                                                context1 -> presenter.reloadView(), true);
                                    } else {
                                        // constructs the http management console url
                                        String serverName = environment.isStandalone() ? Names.STANDALONE_SERVER
                                                : Names.DOMAIN_CONTROLLER;
                                        String label = resources.constants().reload() + " " + serverName;
                                        SafeHtml description;
                                        StringBuilder location = new StringBuilder(
                                                "https://" + window.location.getHostname() + ":");
                                        if (environment.isStandalone()) {
                                            location.append(context.securePort);
                                            description = resources.messages()
                                                    .enableSSLResultsSuccessStandalone(location.toString());
                                        } else {
                                            location.append(asString(model, SECURE_PORT));
                                            description = resources.messages()
                                                    .enableSSLResultsSuccessDomain(location.toString());
                                        }
                                        // extracts the url search path, so the url shows the view the user is located
                                        String urlSuffix = window.location.getHref();
                                        urlSuffix = urlSuffix.substring(urlSuffix.indexOf("//") + 2);
                                        urlSuffix = urlSuffix.substring(urlSuffix.indexOf("/"));
                                        location.append(urlSuffix);
                                        wizard.showSuccess(resources.constants().success(), description, label,
                                                // reloads the server/host if user clicks on the success action
                                                context1 -> presenter.reloadServer(host, location.toString()),
                                                // reload only the view and displays a success message
                                                context2 -> {
                                                    presenter.reloadView();
                                                    MessageEvent.fire(eventBus,
                                                            Message.success(resources.messages().enableSSLSuccess()));
                                                }, true);
                                    }
                                }

                                @Override
                                public void onError(FlowContext context, Throwable exception) {
                                    wizard.showError(resources.constants().failed(),
                                            resources.messages().enableSSLResultsError(), exception.getMessage(),
                                            false);
                                }
                            });

                });
        Wizard<EnableSSLContext, EnableSSLState> wizard = wb.build();
        wizard.show();
    }

    private String asString(ModelNode model, String property) {
        return model.get(property).asString();
    }

    private Operation undefineAttribute(ResourceAddress address, String attribute) {
        return new Operation.Builder(address, UNDEFINE_ATTRIBUTE_OPERATION)
                .param(NAME, attribute)
                .build();
    }

    private AddressTemplate certificateAuthorityAccountTemplate() {
        return AddressTemplate.of(prefix() + "/subsystem=elytron/certificate-authority-account=*");
    }

    private AddressTemplate keyStoreTemplate() {
        return AddressTemplate.of(prefix() + "/subsystem=elytron/key-store=*");
    }

    private AddressTemplate keyManagerTemplate() {
        return AddressTemplate.of(prefix() + "/subsystem=elytron/key-manager=*");
    }

    private AddressTemplate trustManagerTemplate() {
        return AddressTemplate.of(prefix() + "/subsystem=elytron/trust-manager=*");
    }

    private AddressTemplate sslContextTemplate() {
        return AddressTemplate.of(prefix() + "/subsystem=elytron/server-ssl-context=*");
    }

    private String prefix() {
        return undertowHttps ? "{selected.profile}" : "{domain.controller}";
    }

    public static class Builder {

        private Environment environment;
        private Host host;
        private boolean undertowHttps;
        private Map<String, List<String>> existingResources;
        private Resources resources;
        private EventBus eventBus;
        private Provider<Progress> progress;
        private StatementContext statementContext;
        private Dispatcher dispatcher;
        private EnableSSLPresenter presenter;
        private String undertowServer;
        private String httpsListener;

        public Builder(Map<String, List<String>> existingResources, Resources resources, EventBus eventBus,
                StatementContext statementContext, Dispatcher dispatcher, Provider<Progress> progress,
                EnableSSLPresenter presenter, Environment environment) {
            this.existingResources = existingResources;
            this.resources = resources;
            this.eventBus = eventBus;
            this.progress = progress;
            this.statementContext = statementContext;
            this.dispatcher = dispatcher;
            this.presenter = presenter;
            this.environment = environment;
        }

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public Builder host(Host host) {
            this.host = host;
            return this;
        }

        public Builder undertowServer(String undertowServer) {
            this.undertowServer = undertowServer;
            this.undertowHttps = true;
            return this;
        }

        public Builder httpsListenerName(String httpsListener) {
            this.httpsListener = httpsListener;
            return this;
        }

        public EnableSSLWizard build() {
            EnableSSLWizard enableSSLWizard = new EnableSSLWizard(existingResources, resources, environment, statementContext,
                    dispatcher,
                    host, presenter, progress, eventBus);
            enableSSLWizard.undertowHttps = undertowHttps;
            enableSSLWizard.undertowServer = undertowServer;
            enableSSLWizard.httpsListener = httpsListener;
            return enableSSLWizard;
        }
    }
}
