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

    public final static String CONNECT_PARAMETER = "connect";
    private static final Logger logger = LoggerFactory.getLogger(EndpointManager.class);

    private final Endpoints endpoints;
    private final EndpointStorage storage;

    private ScheduledCommand andThen;
    private EndpointDialog dialog;

    @Inject
    public EndpointManager(Endpoints endpoints, EndpointStorage storage) {
        this.endpoints = endpoints;
        this.storage = storage;
    }

    public void select(ScheduledCommand andThen) {
        this.andThen = andThen;

        String connect = Window.Location.getParameter(CONNECT_PARAMETER);
        if (connect != null) {
            // Connect to a server given as a request parameter
            Endpoint endpoint = storage.get(connect);
            if (endpoint != null) {
                pingServer(endpoint, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        openDialog();
                    }

                    @Override
                    public void onSuccess(Void whatever) {
                        onConnect(endpoint);
                    }
                });
            } else {
                openDialog();
            }

        } else {
            // Test whether this console is served from a WildFly / EAP instance
            String managementEndpoint = Endpoints.getBaseUrl() + "/management";
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
                            andThen.execute();
                            break;
                        default:
                            logger.info("Unable to serve HAL from '{}'", managementEndpoint);
                            openDialog();
                            break;
                    }
                }
            });
            xhr.open("GET", managementEndpoint, true);
            xhr.setWithCredentials(true);
            xhr.send();
        }
    }

    private void openDialog() {
        dialog = new EndpointDialog(this, storage);
        dialog.show();
    }

    void pingServer(final Endpoint endpoint, final AsyncCallback<Void> callback) {
        String managementEndpoint = endpoint.getUrl() + "/management";
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
        xhr.open("GET", managementEndpoint, true);
        xhr.setWithCredentials(true);
        xhr.send();
    }

    void onConnect(Endpoint endpoint) {
        storage.saveSelection(endpoint);
        endpoints.useBase(endpoint.getUrl());
        andThen.execute();
    }
}
