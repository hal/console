package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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
public class EndpointSelection {

    public final static String CONNECT_PARAMETER = "connect";
    private static final Logger logger = LoggerFactory.getLogger(EndpointSelection.class);

    private final Endpoints endpoints;
    private final EndpointStorage storage;

    private ScheduledCommand andThen;
    private EndpointDialog dialog;

    @Inject
    public EndpointSelection(Endpoints endpoints, EndpointStorage storage) {
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
            String baseUrl = Endpoints.getBaseUrl();
            // Test whether this console is served from a WildFly / EAP instance
            RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, baseUrl + "/management");
            requestBuilder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(final Request request, final Response response) {
                    int statusCode = response.getStatusCode();
                    // anything but 404 is considered successful
                    if (statusCode == 0 || statusCode == 200 || statusCode == 401) {
                        endpoints.useBase(baseUrl);
                        andThen.execute();
                    } else {
                        openDialog();
                    }
                }

                @Override
                public void onError(final Request request, final Throwable exception) {
                    // This is a 'standalone' console. Show selection dialog
                    openDialog();
                }
            });
            try {
                requestBuilder.send();
            } catch (RequestException e) {
                openDialog();
            }
        }
    }

    private void openDialog() {
        dialog = new EndpointDialog(this, storage);
        dialog.open();
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
        xhr.addEventListener("error", event -> {
            logger.error("Failed to ping '{}'", managementEndpoint);
            callback.onFailure(new IllegalStateException());
        });
        xhr.open("GET", managementEndpoint, true);
        xhr.setWithCredentials(true);
        xhr.send();
    }

    void onConnect(Endpoint endpoint) {
        storage.saveSelection(endpoint);
        if (dialog != null) {
            dialog.hide();
        }
        endpoints.useBase(endpoint.getUrl());
        andThen.execute();
    }
}
