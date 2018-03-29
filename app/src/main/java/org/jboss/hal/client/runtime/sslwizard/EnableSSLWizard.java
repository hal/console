/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.client.runtime.sslwizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.SuccessfulOutcome;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.flow.Task;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.client.runtime.sslwizard.AbstractConfiguration.*;
import static org.jboss.hal.client.runtime.sslwizard.EnableSSLState.CONFIGURATION;
import static org.jboss.hal.client.runtime.sslwizard.EnableSSLState.DEFINE_STRATEGY;
import static org.jboss.hal.client.runtime.sslwizard.EnableSSLState.REVIEW;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.flow.Flow.series;

public class EnableSSLWizard {

    private final Resources resources;
    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private Host host;
    private EnableSSLPresenter presenter;
    private Provider<Progress> progress;
    private EventBus eventBus;
    private Map<String, List<String>> existingResources;

    public EnableSSLWizard(Map<String, List<String>> existingResources, Resources resources, Environment environment,
            StatementContext statementContext, Dispatcher dispatcher, Host host, EnableSSLPresenter presenter,
            Provider<Progress> progress, EventBus eventBus) {
        this.existingResources = existingResources;
        this.resources = resources;
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.host = host;
        this.presenter = presenter;
        this.progress = progress;
        this.eventBus = eventBus;
    }

    public void show() {
        Constants constants = resources.constants();
        Wizard.Builder<EnableSSLContext, EnableSSLState> wb = new Wizard.Builder<>(constants.enableSSLManagementTitle(),
                new EnableSSLContext());

        wb.addStep(DEFINE_STRATEGY, new DefineStrategyStep(resources))
                .addStep(CONFIGURATION, new ConfigurationStep(existingResources, resources, environment))
                .addStep(REVIEW, new ReviewStep(dispatcher, statementContext, resources, environment))

                .onBack((context, currentState) -> {
                    EnableSSLState previous = null;
                    switch (currentState) {
                        case DEFINE_STRATEGY:
                            break;
                        case CONFIGURATION:
                            previous = DEFINE_STRATEGY;
                            break;
                        case REVIEW:
                            previous = CONFIGURATION;
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
                            next = CONFIGURATION;
                            break;
                        case CONFIGURATION:
                            next = REVIEW;
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
                    credRef.get(CLEAR_TEXT).set(asString(model, KEY_STORE_PASSWORD));

                    // use Flow tasks to run DMR operations as there are resources that must exists before next
                    // operations are called, as in the example of a generate-key-pair and import-certificate
                    // the key-store must exists. For this case, the Composite doesn't work.
                    List<Task<FlowContext>> tasks = new ArrayList<>();

                    // key-store is only created when user chooses to create all resources or create a key-store based on
                    // an existing JKS file
                    boolean createKeyStore = !context.strategy.equals(
                            EnableSSLContext.Strategy.KEYSTORE_RESOURCE_EXISTS);
                    final String keyStoreName = createKeyStore ? asString(model, KEY_STORE_NAME) : asString(model,
                            KEY_STORE);
                    if (createKeyStore) {
                        if (context.strategy.equals(EnableSSLContext.Strategy.KEYSTORE_CREATE)) {

                            ResourceAddress ksAddress = KEY_STORE_TEMPLATE.resolve(statementContext, keyStoreName);
                            tasks.add(flowContext -> {
                                Operation.Builder builder = new Operation.Builder(ksAddress, ADD)
                                        .param(PATH, asString(model, KEY_STORE_PATH))
                                        .param(CREDENTIAL_REFERENCE, credRef)
                                        .param(TYPE, asString(model, KEY_STORE_TYPE));

                                if (model.hasDefined(KEY_STORE_RELATIVE_TO)) {
                                    builder.param(RELATIVE_TO, asString(model, KEY_STORE_RELATIVE_TO));
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
                                String dn = "CN=" + asString(model, PRIVATE_KEY_DN_CN)
                                        + ", OU=" + asString(model, PRIVATE_KEY_DN_OU)
                                        + ", O=" + asString(model, PRIVATE_KEY_DN_O)
                                        + ", L=" + asString(model, PRIVATE_KEY_DN_L)
                                        + ", ST=" + asString(model, PRIVATE_KEY_DN_ST)
                                        + ", C=" + asString(model, PRIVATE_KEY_DN_C);
                                Operation genKeyOp = new Operation.Builder(ksAddress, GENERATE_KEY_PAIR)
                                        .param(ALIAS, asString(model, PRIVATE_KEY_ALIAS))
                                        .param(DISTINGUISHED_NAME, dn)
                                        .param(VALIDITY, asString(model, PRIVATE_KEY_VALIDITY))
                                        .param(ALGORITHM, asString(model, PRIVATE_KEY_ALGORITHM))
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
                                ResourceAddress ksAddress = KEY_STORE_TEMPLATE.resolve(statementContext, keyStoreName);
                                Operation.Builder builder = new Operation.Builder(ksAddress, ADD)
                                        .param(PATH, asString(model, KEY_STORE_PATH))
                                        .param(CREDENTIAL_REFERENCE, credRef)
                                        .param(TYPE, asString(model, KEY_STORE_TYPE))
                                        .param(REQUIRED, true);

                                if (model.hasDefined(KEY_STORE_RELATIVE_TO)) {
                                    builder.param(RELATIVE_TO, asString(model, KEY_STORE_RELATIVE_TO));
                                }
                                Operation keyStoreOp = builder.build();

                                return dispatcher.execute(keyStoreOp)
                                        .doOnError(exception -> wizard.showError(constants.failed(),
                                                resources.messages().addKeyStoreError(keyStoreName),
                                                exception.getMessage(), false))
                                        .toCompletable();
                            });
                        }
                    }

                    String trustManagerName = model.hasDefined(TRUST_MANAGER) ? asString(model, TRUST_MANAGER) : null;
                    if (context.mutualAuthentication) {
                        ModelNode tsCredRef = new ModelNode();
                        tsCredRef.get(CLEAR_TEXT).set(asString(model, TRUST_STORE_PASSWORD));
                        String trustStoreName = asString(model, TRUST_STORE_NAME);

                        ResourceAddress tsAddress = KEY_STORE_TEMPLATE.resolve(statementContext, trustStoreName);
                        tasks.add(flowContext -> {
                            Operation.Builder builder = new Operation.Builder(tsAddress, ADD)
                                    .param(PATH, asString(model, TRUST_STORE_PATH))
                                    .param(CREDENTIAL_REFERENCE, tsCredRef)
                                    .param(TYPE, asString(model, TRUST_STORE_TYPE));

                            if (model.hasDefined(TRUST_STORE_RELATIVE_TO)) {
                                builder.param(RELATIVE_TO, asString(model, TRUST_STORE_RELATIVE_TO));
                            }
                            Operation trustStoreOp = builder.build();

                            return dispatcher.execute(trustStoreOp)
                                    .toCompletable();
                        });

                        tasks.add(flowContext -> {
                            Composite composite = new Composite();
                            Operation importCertOp = new Operation.Builder(tsAddress, IMPORT_CERTIFICATE_OPERATION)
                                    .param(ALIAS, asString(model, CLIENT_CERTIFICATE_ALIAS))
                                    .param(PATH, asString(model, CLIENT_CERTIFICATE_PATH))
                                    .param(CREDENTIAL_REFERENCE, tsCredRef)
                                    .param(VALIDATE, model.get(CLIENT_CERTIFICATE_VALIDATE).asBoolean())
                                    .param(TRUST_CACERTS, model.get(CLIENT_CERTIFICATE_TRUST).asBoolean())
                                    .build();
                            composite.add(importCertOp);

                            Operation storeOp = new Operation.Builder(tsAddress, STORE)
                                    .build();
                            composite.add(storeOp);

                            ResourceAddress etmAddress = TRUST_MANAGER_TEMPLATE.resolve(statementContext, trustManagerName);
                            Operation trustManagerOp = new Operation.Builder(etmAddress, ADD)
                                    .param(KEY_STORE, trustStoreName)
                                    .param(ALGORITHM, "SunX509")
                                    .build();
                            composite.add(trustManagerOp);

                            return dispatcher.execute(composite)
                                    .toCompletable();
                        });
                    }

                    Composite composite = new Composite();
                    String keyManager = asString(model, KEY_MANAGER);
                    ResourceAddress ekmAddress = KEY_MANAGER_TEMPLATE.resolve(statementContext, keyManager);
                    Operation keyManagerOp = new Operation.Builder(ekmAddress, ADD)
                            .param(KEY_STORE, keyStoreName)
                            .param(ALGORITHM, "SunX509")
                            .param(CREDENTIAL_REFERENCE, credRef)
                            .build();
                    composite.add(keyManagerOp);

                    ModelNode protocols = new ModelNode();
                    protocols.add("TLSv1.2");
                    String serverSslContext = asString(model, SERVER_SSL_CONTEXT);
                    ResourceAddress sslCtxAddress = SERVER_SSL_CONTEXT_TEMPLATE.resolve(statementContext,
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

                    ResourceAddress httpInterfaceAddress = HTTP_INTERFACE_TEMPLATE.resolve(statementContext);
                    Operation writeSslCtxOp = new Operation.Builder(httpInterfaceAddress, WRITE_ATTRIBUTE_OPERATION)
                            .param(NAME, SSL_CONTEXT)
                            .param(VALUE, serverSslContext)
                            .build();
                    composite.add(writeSslCtxOp);

                    if (environment.isStandalone()) {
                        Operation writeSecureSocketBinding = new Operation.Builder(httpInterfaceAddress, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, SECURE_SOCKET_BINDING)
                                .param(VALUE, asString(model, SECURE_SOCKET_BINDING))
                                .build();
                        composite.add(writeSecureSocketBinding);
                    } else {
                        Operation writeSecurePortOp = new Operation.Builder(httpInterfaceAddress, WRITE_ATTRIBUTE_OPERATION)
                                .param(NAME, SECURE_PORT)
                                .param(VALUE, asString(model, SECURE_PORT))
                                .build();
                        composite.add(writeSecurePortOp);
                    }

                    tasks.add(flowContext -> dispatcher.execute(composite)
                            .toCompletable()
                    );

                    series(new FlowContext(progress.get()), tasks)
                            .subscribe(new SuccessfulOutcome<FlowContext>(eventBus, resources) {
                                @Override
                                public void onSuccess(FlowContext flowContext) {
                                    String serverName = environment.isStandalone() ? resources.constants().standaloneServer()
                                            : resources.constants().domainController();
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
                                        description = resources.messages().enableSSLResultsSuccessDomain(location.toString());
                                    }
                                    // extracts the url search path, so the reload shows the same view the user is on
                                    String urlSuffix = window.location.getHref();
                                    urlSuffix = urlSuffix.substring(urlSuffix.indexOf("//") + 2);
                                    urlSuffix = urlSuffix.substring(urlSuffix.indexOf("/"));
                                    location.append(urlSuffix);
                                    wizard.showSuccess(resources.constants().success(), description, label,
                                            context1 -> presenter.reloadServer(host, location.toString()),
                                            context2 -> {
                                                presenter.reloadView();
                                                MessageEvent.fire(eventBus,
                                                        Message.success(resources.messages().enableSSLSuccess()));
                                            }, true);
                                }

                                @Override
                                public void onError(FlowContext context, Throwable exception) {
                                    wizard.showError(resources.constants().failed(),
                                            resources.messages().enableSSLResultsError(), exception.getMessage(), false);
                                }
                            });


                });
        Wizard<EnableSSLContext, EnableSSLState> wizard = wb.build();
        wizard.show();
    }

    private String asString(ModelNode model, String property) {
        return model.get(property).asString();
    }
}
