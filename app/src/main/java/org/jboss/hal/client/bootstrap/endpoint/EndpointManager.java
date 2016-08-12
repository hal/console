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

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import elemental.client.Browser;
import elemental.xml.XMLHttpRequest;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.resources.Urls.MANAGEMENT;

/**
 * Class which connects to a running management endpoint or triggers the selection of an arbitrary management
 * endpoint. By default this class first tries to connect to the management endpoint the console was loaded from.
 * If no endpoint was found, the selection is triggered by {@link EndpointDialog}.
 * <p>
 * Please note: This class must run <em>before</em> any {@linkplain org.jboss.hal.client.bootstrap.functions.BootstrapFunction
 * bootstrap function}!
 *
 * @author Harald Pehl
 */
public class EndpointManager {

    static final int DEFAULT_PORT = 9990; // must be in sync with the default value in endpoint.dmr!
    private static final String CONNECT_PARAMETER = "connect";
    @NonNls private static final Logger logger = LoggerFactory.getLogger(EndpointManager.class);

    private final Endpoints endpoints;
    private final EndpointStorage storage;
    private final Capabilities capabilities;

    private ScheduledCommand next;

    @Inject
    public EndpointManager(Endpoints endpoints, EndpointStorage storage, Capabilities capabilities) {
        this.endpoints = endpoints;
        this.storage = storage;
        this.capabilities = capabilities;
    }

    public void select(ScheduledCommand next) {
        this.next = next;

        String connect = Window.Location.getParameter(CONNECT_PARAMETER);
        if (connect != null) {
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
            String managementEndpoint = Endpoints.getBaseUrl() + MANAGEMENT;
            XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
            xhr.setOnreadystatechange(event -> {
                int readyState = xhr.getReadyState();
                if (readyState == 4) {
                    int status = xhr.getStatus();
                    switch (status) {
                        case 0:
                        case 200:
                        case 401:
                            endpoints.useBase(Endpoints.getBaseUrl());
                            next.execute();
                            break;
                        default:
                            logger.info("Unable to serve HAL from '{}'. Please select a management interface.",
                                    managementEndpoint);
                            openDialog();
                            break;
                    }
                }
            });
            xhr.open(GET.name(), managementEndpoint, true);
            xhr.setWithCredentials(true);
            xhr.send();
        }
    }

    private void openDialog() {
        new EndpointDialog(this, storage, capabilities).show();
    }

    void pingServer(final Endpoint endpoint, final AsyncCallback<Void> callback) {
        String managementEndpoint = endpoint.getUrl() + MANAGEMENT;
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                int status = xhr.getStatus();
                if (status == 200) {
                    callback.onSuccess(null);
                } else {
                    logger.error("Wrong status {} when pinging '{}'", status, managementEndpoint);
                    callback.onFailure(new IllegalStateException());
                }
            }
        });
        xhr.open(GET.name(), managementEndpoint, true);
        xhr.setWithCredentials(true);
        xhr.send();
    }

    void onConnect(Endpoint endpoint) {
        storage.saveSelection(endpoint);
        endpoints.useBase(endpoint.getUrl());
        next.execute();
    }
}
