/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.dmr.dispatch;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * The dispatcher executes operations against the management endpoint. You can register different callbacks to react on
 * failed management operations ({@link #setFailedCallback(FailedCallback)} or technical errors
 * {@link #setExceptionCallback(ExceptionCallback)}.
 * <p>
 * TODO Add a way to track the management operations.
 * TODO Handle bootstrap finished event and setup response processor based on operation mode
 *
 * @author Harald Pehl
 */
public class Dispatcher {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String DMR_ENCODED = "application/dmr-encoded";

    /**
     * The read resource description supports the following parameters:
     * recursive, proxies, operations, inherited plus one not documented: locale.
     * See https://docs.jboss.org/author/display/AS72/Global+operations#Globaloperations-readresourcedescription
     * for a more detailed description
     */
    private static final String[] READ_RESOURCE_DESCRIPTION_OPTIONAL_PARAMETERS = new String[]{
            RECURSIVE, PROXIES, OPERATIONS, INHERITED, LOCALE
    };

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final Endpoints endpoints;
    private ResponseProcessor responseProcessor;
    private FailedCallback failedCallback;
    private ExceptionCallback exceptionCallback;


    @Inject
    public Dispatcher(final Endpoints endpoints) {
        this.endpoints = endpoints;
        this.responseProcessor = ResponseProcessor.NOOP;
        // TODO Come up with some more useful defaults
        failedCallback = (operation, failure) -> logger.error("DMR operation {} failed: {}", operation, failure);
        exceptionCallback = (operation, t) -> logger.error("Error while executing DRM operation {}: {}", operation,
                t.getMessage());
    }


    // ------------------------------------------------------ execute methods

    public void execute(final Operation operation, final SuccessCallback successCallback) {
        execute(operation, successCallback, failedCallback, exceptionCallback);
    }

    public void execute(final Operation operation, final SuccessCallback successCallback,
            FailedCallback failedCallback) {
        execute(operation, successCallback, failedCallback, exceptionCallback);
    }

    public void execute(final Operation operation, final SuccessCallback successCallback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        final RequestBuilder requestBuilder = chooseRequestBuilder(operation);
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(final Request request, final Response response) {
                int statusCode = response.getStatusCode();
                if (200 == statusCode) {
                    ModelNode responseNode = parseResponse(requestBuilder.getHTTPMethod(), response.getText());
                    if (!responseNode.isFailure()) {
                        if (responseProcessor.accepts(responseNode)) {
                            Map<String, ServerState> serverStates = responseProcessor.process(responseNode);
                            if (!serverStates.isEmpty()) {
                                // TODO emit notifications
                            }
                        }
                        successCallback.onSuccess(responseNode.get(RESULT));
                    } else {
                        failedCallback.onFailed(operation, responseNode.getFailureDescription());
                    }
                } else if (401 == statusCode || 0 == statusCode) {
                    exceptionCallback
                            .onException(operation, new DispatchException("Authentication required.", statusCode));
                } else if (403 == statusCode) {
                    exceptionCallback
                            .onException(operation, new DispatchException("Authentication required.", statusCode));
                } else if (404 == statusCode) {
                    exceptionCallback.onException(operation, new DispatchException(
                            "Management interface at '" + requestBuilder.getUrl() + " not found'.", statusCode));
                } else if (503 == statusCode) {
                    exceptionCallback.onException(operation,
                            new DispatchException("Service temporarily unavailable. Is the server is still booting?",
                                    statusCode));
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unexpected HTTP response").append(": ").append(statusCode);
                    sb.append("\n\n");
                    sb.append("Request\n");
                    sb.append(operation.toString());
                    sb.append("\n\nResponse\n\n");
                    sb.append(response.getStatusText()).append("\n");
                    String payload = response.getText().equals("") ? "No details" :
                            ModelNode.fromBase64(response.getText()).toString();
                    sb.append(payload);
                    exceptionCallback.onException(operation, new DispatchException(sb.toString(), statusCode));
                }
            }

            @Override
            public void onError(final Request request, final Throwable throwable) {
                logger.error("Error getting DMR response for operation {}: {}", operation, throwable.getMessage());
                exceptionCallback.onException(operation, throwable);
            }
        };
        requestBuilder.setCallback(requestCallback);

        try {
            requestBuilder.send();
        } catch (RequestException e) {
            logger.error("Error sending DMR request for operation {}: {}", operation, e.getMessage());
            exceptionCallback.onException(operation, e);
        }
    }


    // ------------------------------------------------------ request / response handling

    private RequestBuilder chooseRequestBuilder(final Operation operation) {
        RequestBuilder requestBuilder;

        final String op = operation.get(OP).asString();
        if (READ_RESOURCE_DESCRIPTION_OPERATION.equals(op)) {
            String endpoint = endpoints.dmr();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            String descriptionUrl = endpoint + descriptionOperationToUrl(operation);
            requestBuilder = new RequestBuilder(RequestBuilder.GET,
                    com.google.gwt.http.client.URL.encode(descriptionUrl));
            requestBuilder.setRequestData(null);
        } else {
            requestBuilder = new RequestBuilder(RequestBuilder.POST, endpoints.dmr());
            requestBuilder.setRequestData(operation.toBase64String());
        }
        requestBuilder.setIncludeCredentials(true);
        requestBuilder.setHeader(HEADER_ACCEPT, DMR_ENCODED);
        requestBuilder.setHeader(HEADER_CONTENT_TYPE, DMR_ENCODED);
        return requestBuilder;
    }

    private String descriptionOperationToUrl(final ModelNode operation) {
        StringBuilder url = new StringBuilder();
        final List<Property> address = operation.get(ADDRESS).asPropertyList();
        for (Property property : address) {
            url.append("/").append(property.getName()).append("/").append(property.getValue().asString());
        }

        url.append("?operation=").append("resource-description");
        for (String parameter : READ_RESOURCE_DESCRIPTION_OPTIONAL_PARAMETERS) {
            if (operation.has(parameter)) {
                url.append("&").append(parameter).append("=").append(operation.get(parameter).asString());
            }
        }
        return url.toString();
    }

    private ModelNode parseResponse(String httpMethod, String responseText) {
        ModelNode response;
        try {
            response = ModelNode.fromBase64(responseText);
            if ("GET".equals(httpMethod)) {
                // For GET request the response is purely the model nodes result. The outcome
                // is not send as part of the response but expressed with the HTTP status code.
                // In order to not break existing code, we repackage the payload into a
                // new model node with an "outcome" and "result" key.
                ModelNode repackaged = new ModelNode();
                repackaged.get(OUTCOME).set(SUCCESS);
                repackaged.get(RESULT).set(response);
                response = repackaged;
            }
        } catch (Throwable e) {
            ModelNode err = new ModelNode();
            err.get(OUTCOME).set(FAILED);
            err.get(FAILURE_DESCRIPTION)
                    .set("Failed to decode response: " + e.getClass().getName() + ": " + e.getMessage());
            response = err;
        }
        return response;
    }


    // ------------------------------------------------------ callbacks

    public void setFailedCallback(final FailedCallback failedCallback) {
        this.failedCallback = failedCallback;
    }

    public void setExceptionCallback(final ExceptionCallback exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
    }


    @FunctionalInterface
    public interface SuccessCallback {

        void onSuccess(ModelNode payload);
    }


    @FunctionalInterface
    public interface FailedCallback {

        void onFailed(Operation operation, String failure);
    }


    @FunctionalInterface
    public interface ExceptionCallback {

        void onException(Operation operation, Throwable exception);
    }
}
