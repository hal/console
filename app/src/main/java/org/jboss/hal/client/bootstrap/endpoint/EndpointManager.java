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
package org.jboss.hal.client.bootstrap.endpoint;

import java.util.Optional;

import javax.inject.Inject;

import org.jboss.hal.client.bootstrap.BootstrapFailed;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.keycloak.Keycloak;
import org.jboss.hal.config.keycloak.KeycloakSingleton;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;

import elemental2.dom.XMLHttpRequest;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.js.JsHelper.requestParameter;
import static org.jboss.hal.resources.Urls.MANAGEMENT;

/**
 * Class which connects to a running management endpoint or triggers the selection of an arbitrary management endpoint. By
 * default, this class first tries to connect to the management endpoint the console was loaded from. If no endpoint was found,
 * the selection is triggered by {@link EndpointDialog}.
 * <p>
 * Please note: This class must run <em>before</em> any bootstrap function!
 */
public class EndpointManager {

    // wildfly-console is the name convention to set it as the configuration specifically for HAL
    // as there is no setting specifically to associate /subsystem=elytron-oidc-client/secure-server=*
    // for http management authentication
    private static final String ELYTRON_OIDC_CLIENT_WILDFLY_CONSOLE = "/oidc/wildfly-console";
    public static final String CONNECT_PARAMETER = "connect";
    static final String DEFAULT_HOST = "localhost"; // must be in sync with the default value in endpoint.dmr!
    static final int DEFAULT_PORT = 9990; // must be in sync with the default value in endpoint.dmr!
    private static final Logger logger = LoggerFactory.getLogger(EndpointManager.class);

    private final Endpoints endpoints;
    private final EndpointStorage storage;

    private Callback callback;

    @Inject
    public EndpointManager(Endpoints endpoints,
            EndpointStorage storage) {
        this.endpoints = endpoints;
        this.storage = storage;
    }

    public void select(Callback callback) {
        this.callback = callback;

        String connect = requestParameter(CONNECT_PARAMETER);
        if (Strings.emptyToNull(connect) != null) {
            if (connect.contains("://")) {
                logger.info("Use direct endpoint '{}'", connect);
                ModelNode modelNode = new ModelNode();
                modelNode.get(NAME).set(Ids.asId(connect));
                modelNode.get(URL).set(connect);
                Endpoint endpoint = new Endpoint(modelNode);
                connect(Optional.of(endpoint));
            } else {
                // Connect to a server given as a request parameter
                Endpoint endpoint = storage.get(connect);
                if (endpoint != null) {
                    logger.info("Try to connect to endpoint '{}'", endpoint.getUrl());
                    connect(Optional.of(endpoint));
                } else {
                    logger.error("Unable to get URL for named endpoint '{}' from local storage", connect);
                    openDialog();
                }
            }
        } else {
            connect(Optional.empty());
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void connect(Optional<Endpoint> endpoint) {
        String baseUrl = endpoint.map(Endpoint::getUrl).orElse(Endpoints.getBaseUrl());
        endpoints.useBase(baseUrl);
        String managementEndpoint = baseUrl + MANAGEMENT;
        XMLHttpRequest xhr = new XMLHttpRequest();
        xhr.onload = event -> {
            int status = xhr.status;
            switch (status) {
                case 0:
                case 200:
                case 401:
                    if (KeycloakSingleton.presentAndValid()) {
                        logger.info("Keycloak present and valid");
                        callback.execute();
                    } else {
                        KeycloakSingleton.check(baseUrl)
                                .then(Keycloak::authenticate)
                                .then(ignore -> {
                                    callback.execute();
                                    return null;
                                })
                                .catch_(error -> {
                                    logger.warn("Keycloak authentication failed", error);
                                    callback.execute();
                                    return null;
                                });
                    }
                    break;
                case 403:
                    BootstrapFailed.rbacProviderFailed("Status " + status + " - " + xhr.statusText);
                    break;
                case 503:
                    BootstrapFailed.generalBootstrapError("Status " + status + " - " + xhr.statusText,
                            Endpoints.INSTANCE);
                    break;
                default:
                    logger.info("Unable to serve HAL from '{}'. Please select a management interface.",
                            managementEndpoint);
                    openDialog();
                    break;
            }
        };
        xhr.onerror = (event) -> {
            BootstrapFailed.generalBootstrapError("Failed connecting to a management interface", Endpoints.INSTANCE);
            return null;
        };
        xhr.open(GET.name(), managementEndpoint, true);
        xhr.withCredentials = true;
        xhr.send();
    }

    private void openDialog() {
        new EndpointDialog(this, storage).show();
    }

    void pingServer(Endpoint endpoint, AsyncCallback<Void> callback) {
        String managementEndpoint = endpoint.getUrl() + MANAGEMENT;
        KeycloakSingleton.check(endpoint.getUrl())
                .then(keycloak -> {
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
                        xhr.onerror = event -> {
                            callback.onFailure(null);
                            return null;
                        };
                        xhr.open(GET.name(), managementEndpoint, true);
                        xhr.withCredentials = true;
                        xhr.send();
                    } catch (Exception e) {
                        logger.error("Error when pinging '{}'. cause: {}", managementEndpoint, e.getMessage());
                        callback.onFailure(e);
                    }
                    return null;
                });
    }

    void onConnect(Endpoint endpoint) {
        storage.saveSelection(endpoint);
        connect(Optional.of(endpoint));
    }
}
