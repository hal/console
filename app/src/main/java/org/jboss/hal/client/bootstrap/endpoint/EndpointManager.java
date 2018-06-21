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
package org.jboss.hal.client.bootstrap.endpoint;

import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.dom.HTMLScriptElement;
import elemental2.dom.XMLHttpRequest;
import org.jboss.hal.client.bootstrap.tasks.BootstrapTask;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.keycloak.Keycloak;
import org.jboss.hal.config.keycloak.KeycloakHolder;
import org.jboss.hal.js.Json;
import org.jboss.hal.js.JsonObject;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setInterval;
import static org.jboss.hal.dmr.ModelDescriptionConstants.AUTH_SERVER_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.KEYCLOAK;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.js.JsHelper.requestParameter;
import static org.jboss.hal.resources.Urls.MANAGEMENT;

/**
 * Class which connects to a running management endpoint or triggers the selection of an arbitrary management
 * endpoint. By default this class first tries to connect to the management endpoint the console was loaded from.
 * If no endpoint was found, the selection is triggered by {@link EndpointDialog}.
 * <p>
 * Please note: This class must run <em>before</em> any {@linkplain BootstrapTask
 * bootstrap function}!
 */
public class EndpointManager {

    // wildfly-console is the name convention to set it as the configuration specifically for HAL
    // as there is no setting specifically to associate /subsystem=keycloak/secure-server=*
    // for http management authentication
    private static final String KEYCLOAK_ADAPTER_WILDFLY_CONSOLE = "/keycloak/adapter/wildfly-console";
    public static final String CONNECT_PARAMETER = "connect";
    static final String DEFAULT_HOST = "localhost"; // must be in sync with the default value in endpoint.dmr!
    static final int DEFAULT_PORT = 9990; // must be in sync with the default value in endpoint.dmr!
    @NonNls private static final Logger logger = LoggerFactory.getLogger(EndpointManager.class);

    private final Endpoints endpoints;
    private final EndpointStorage storage;
    private KeycloakHolder keycloakHolder;

    private Callback callback;

    @Inject
    public EndpointManager(Endpoints endpoints, EndpointStorage storage, KeycloakHolder keycloakHolder) {
        this.endpoints = endpoints;
        this.storage = storage;
        this.keycloakHolder = keycloakHolder;
    }

    public void select(Callback callback) {
        this.callback = callback;

        String connect = requestParameter(CONNECT_PARAMETER);
        if (Strings.emptyToNull(connect) != null) {
            // Connect to a server given as a request parameter
            Endpoint endpoint = storage.get(connect);
            if (endpoint != null) {
                logger.info("Try to connect to endpoint '{}'", endpoint.getUrl());
                pingServer(endpoint, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        logger.error("Unable to connect to specified endpoint '{}'", endpoint.getUrl());
                        openDialog();
                    }

                    @Override
                    public void onSuccess(Void whatever) {
                        logger.info("Successfully connected to '{}'", endpoint.getUrl());
                        onConnect(endpoint);
                    }
                });
            } else {
                logger.error("Unable to get URL for named endpoint '{}' from local storage", connect);
                openDialog();
            }

        } else {
            // Test whether this console is served from a WildFly / EAP instance
            String baseUrl = Endpoints.getBaseUrl();
            String managementEndpoint = baseUrl + MANAGEMENT;
            XMLHttpRequest xhr = new XMLHttpRequest();
            xhr.onload = event -> {
                int status = xhr.status;
                switch (status) {
                    case 0:
                    case 200:
                    case 401:
                        if (keycloakPresentAndValid()) {
                            endpoints.useBase(baseUrl);
                            callback.execute();
                        } else {
                            checkKeycloakAdapter(baseUrl, keycloakServerJsUrl -> {
                                // if there is a keycloak adapter, call keycloak authentication
                                authKeycloak(getKeycloakAdapterUrl(baseUrl), keycloakServerJsUrl, callback::execute);
                            }, () -> {
                                // if there is no keycloak adapter for wildfly-console, proceed with regular authentication
                                endpoints.useBase(baseUrl);
                                callback.execute();
                            });
                        }
                        break;
                    // TODO Show an error page!
                    // case 500:
                    //     break;
                    default:
                        logger.info("Unable to serve HAL from '{}'. Please select a management interface.",
                                managementEndpoint);
                        openDialog();
                        break;
                }
            };
            xhr.open(GET.name(), managementEndpoint, true);
            xhr.withCredentials = true;
            xhr.send();
        }
    }

    private void openDialog() {
        new EndpointDialog(this, storage).show();
    }

    private String getKeycloakAdapterUrl(String baseUrl) {
        return baseUrl + KEYCLOAK_ADAPTER_WILDFLY_CONSOLE;
    }

    void pingServer(Endpoint endpoint, AsyncCallback<Void> callback) {
        String managementEndpoint = endpoint.getUrl() + MANAGEMENT;

        checkKeycloakAdapter(endpoint.getUrl(), s ->  {
            callback.onSuccess(null);
        }, () -> {
            try {
                XMLHttpRequest xhr = new XMLHttpRequest();
                xhr.onload = event -> {
                    int status = xhr.status;
                    if (status == 200) {
                        callback.onSuccess(null);
                    } else {
                        logger.error("Wrong status {} when pinging '{}'", status, managementEndpoint);
                        callback.onFailure(new IllegalStateException());
                    }
                };
                xhr.open(GET.name(), managementEndpoint, true);
                xhr.withCredentials = true;
                xhr.send();
            } catch (Exception e) {
                logger.error("Error when pinging '{}'. cause: {}", managementEndpoint, e.getMessage());
                callback.onFailure(e);
            }
        });


    }

    private void checkKeycloakAdapter(String baseUrl, Consumer<String> kcExistsCallback, Callback wildflyCallback) {
        String keycloakAdapterUrl = getKeycloakAdapterUrl(baseUrl);
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.onload = event -> {
            int status = xhr.status;
            if (status == 200) {
                JsonObject kcConfig = Json.parse(xhr.responseText);
                String keycloakServerJsUrl = kcConfig.getString(AUTH_SERVER_URL) + "/js/keycloak.js";
                kcExistsCallback.accept(keycloakServerJsUrl);
            } else {
                logger.error("Keycloak adapter '{}' doesn't exist - status: {}", keycloakAdapterUrl, status);
                wildflyCallback.execute();
            }
        };
        xhr.open(GET.name(), keycloakAdapterUrl, true);
        xhr.send();
    }

    private void authKeycloak(String kcAdapterUrl, String keycloakServerJsUrl, Callback callback) {
        // load keycloak.js from keycloak server url
        HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
        script.src = keycloakServerJsUrl;
        script.onload = onLoadEvent -> {
            Keycloak kc = new Keycloak(kcAdapterUrl);
            Keycloak.Api initOptions = new Keycloak.Api();
            kc.init(initOptions)
                .success(authenticated -> {
                    setInterval(o -> kc.updateToken(32), 30000);
                    kc.loadUserProfile().success(profile -> {
                        kc.userProfile = profile;
                    });
                    set(KEYCLOAK, kc);
                    callback.execute();
                })
                .error(() -> logger.error("Error, could not initialize keycloak authentication."));

            keycloakHolder.setKeycloak(kc);
            return null;
        };
        document.head.appendChild(script);
    }

    private native boolean set(String key, Object value)/*-{
        $wnd[key] = value;
    }-*/;

    private native boolean keycloakPresentAndValid()/*-{
        var keycloak = $wnd[keycloak];
        return keycloak != null && !keycloak.isTokenExpired();
    }-*/;

    void onConnect(Endpoint endpoint) {
        storage.saveSelection(endpoint);
        endpoints.useBase(endpoint.getUrl());

        if (keycloakPresentAndValid()) {
            callback.execute();
        } else {
            checkKeycloakAdapter(endpoint.getUrl(), keycloakServerJsUrl -> {
                // if there is a keycloak adapter, call keycloak authentication
                authKeycloak(getKeycloakAdapterUrl(endpoint.getUrl()), keycloakServerJsUrl, () -> callback.execute());
            }, () -> {
                // if there is no keycloak adapter for wildfly-console, proceed with regular authentication
                callback.execute();
            });
        }
    }
}
