package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import elemental.client.Browser;
import elemental.xml.XMLHttpRequest;
import org.jboss.hal.config.Endpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.resources.Names.GET;
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

    private final static String CONNECT_PARAMETER = "connect";
    private final static Logger logger = LoggerFactory.getLogger(EndpointManager.class);

    private final Endpoints endpoints;
    private final EndpointStorage storage;

    private ScheduledCommand next;

    @Inject
    public EndpointManager(Endpoints endpoints, EndpointStorage storage) {
        this.endpoints = endpoints;
        this.storage = storage;
    }

    @SuppressWarnings("HardCodedStringLiteral")
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
            xhr.open(GET, managementEndpoint, true);
            xhr.setWithCredentials(true);
            xhr.send();
        }
    }

    private void openDialog() {
        new EndpointDialog(this, storage).show();
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
                    logger.error("Wrong status {} when pinging '{}'", status, managementEndpoint); //NON-NLS
                    callback.onFailure(new IllegalStateException());
                }
            }
        });
        xhr.open(GET, managementEndpoint, true);
        xhr.setWithCredentials(true);
        xhr.send();
    }

    void onConnect(Endpoint endpoint) {
        storage.saveSelection(endpoint);
        endpoints.useBase(endpoint.getUrl());
        next.execute();
    }
}
