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
package org.jboss.hal.core.dispatch;

import com.ekuefler.supereventbus.EventBus;
import com.google.inject.Provider;
import elemental.client.Browser;
import elemental.html.FormData;
import elemental.html.InputElement;
import elemental.xml.XMLHttpRequest;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.core.messaging.Message;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.core.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.core.dispatch.Dispatcher.HttpMethod.POST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * The dispatcher executes operations / uploads against the management endpoints. You should register a callback for
 * successful operations using {@link #onSuccess(SuccessCallback)}. You can register callbacks to react on failed
 * management operations ({@link #onFailed(FailedCallback)} or technical errors {@link
 * #onException(ExceptionCallback)}.
 * <p>
 * TODO Add a way to track the management operations.
 *
 * @author Harald Pehl
 */
public class Dispatcher {

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


    @FunctionalInterface
    interface PayloadProcessor {

        ModelNode processPayload(HttpMethod method, String payload);
    }


    enum HttpMethod {GET, POST}


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
    private final EventBus eventBus;
    private final Provider<ResponseProcessor> responseProcessor;
    private SuccessCallback successCallback;
    private FailedCallback failedCallback;
    private ExceptionCallback exceptionCallback;

    @Inject
    public Dispatcher(final Endpoints endpoints, final EventBus eventBus, final I18n i18n,
            Provider<ResponseProcessor> responseProcessor) {
        this.endpoints = endpoints;
        this.eventBus = eventBus;
        this.responseProcessor = responseProcessor;

        this.successCallback = payload -> logger.error("No success callback defined");
        this.failedCallback = (operation, failure) -> {
            logger.error("Dispatcher failed: {}, operation: {}", failure, operation);
            eventBus.post(Message.error(i18n.constants().dispatcher_failed(), failure));
        };
        this.exceptionCallback = (operation, t) -> {
            logger.error("Dispatcher exception: {}, operation {}", t.getMessage(), operation);
            eventBus.post(Message.error(i18n.constants().dispatcher_exception(), t.getMessage()));
        };
    }


    // ------------------------------------------------------ execute dmr

    public void execute(final Operation operation) {
        HttpMethod method;
        String url;
        String op = operation.get(OP).asString();

        if (READ_RESOURCE_DESCRIPTION_OPERATION.equals(op)) {
            String endpoint = endpoints.dmr();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            method = GET;
            url = Browser.encodeURI(endpoint + descriptionOperationToUrl(operation));
        } else {
            method = POST;
            url = endpoints.dmr();
        }

        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                processResponse(xhr.getStatus(), url, method, xhr.getResponseText(), operation,
                        new DmrPayloadProcessor(), successCallback, failedCallback, exceptionCallback);
            }
        });
        xhr.addEventListener("error", event -> exceptionCallback.onException(operation,
                new DispatchException("Communication error.", xhr.getStatus())), false);

        xhr.open(method.name(), url, true);
        xhr.setWithCredentials(true);
        xhr.setRequestHeader(HEADER_ACCEPT, DMR_ENCODED);
        xhr.setRequestHeader(HEADER_CONTENT_TYPE, DMR_ENCODED);
        if (method == GET) {
            xhr.send();
        } else {
            xhr.send(operation.toBase64String());
        }
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


    // ------------------------------------------------------ upload

    public void upload(final InputElement fileInput, final Operation operation) {
        FormData formData = createFormData(fileInput, operation.toJSONString(true));
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                processResponse(xhr.getStatus(), endpoints.upload(), POST, xhr.getResponseText(), operation,
                        new UploadPayloadProcessor(), successCallback, failedCallback, exceptionCallback);
            }
        });
        xhr.addEventListener("error", event -> exceptionCallback.onException(operation,
                new DispatchException("Communication error.", xhr.getStatus())), false);

        xhr.open("POST", endpoints.upload(), true);
        xhr.setWithCredentials(true);
        xhr.send(formData);
    }

    private native FormData createFormData(InputElement fileInput, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(fileInput.name, fileInput.files[0]);
        formData.append("operation", operation);
        return formData;
    }-*/;


    // ------------------------------------------------------ response handling

    private void processResponse(final int status, final String url, final HttpMethod method, final String payload,
            final Operation operation, final PayloadProcessor payloadProcessor, final SuccessCallback successCallback,
            final FailedCallback failedCallback, final ExceptionCallback exceptionCallback) {
        if (200 == status) {
            ModelNode responseNode = payloadProcessor.processPayload(method, payload);
            if (!responseNode.isFailure()) {
                if (responseProcessor.get().accepts(responseNode)) {
                    ProcessState processState = responseProcessor.get().process(responseNode);
                    eventBus.post(processState);
                }
                successCallback.onSuccess(responseNode.get(RESULT));
            } else {
                failedCallback.onFailed(operation, responseNode.getFailureDescription());
            }
        } else if (401 == status || 0 == status) {
            exceptionCallback
                    .onException(operation, new DispatchException("Authentication required.", status));
        } else if (403 == status) {
            exceptionCallback
                    .onException(operation, new DispatchException("Authentication required.", status));
        } else if (404 == status) {
            exceptionCallback.onException(operation, new DispatchException(
                    "Management interface at '" + url + "' not found.", status));
        } else if (503 == status) {
            exceptionCallback.onException(operation,
                    new DispatchException("Service temporarily unavailable. Is the server is still booting?",
                            status));
        } else {
            exceptionCallback
                    .onException(operation, new DispatchException("Unexpected status code.", status));
        }
    }


    // ------------------------------------------------------ callbacks

    public Dispatcher onSuccess(final SuccessCallback successCallback) {
        this.successCallback = successCallback;
        return this;
    }

    public Dispatcher onFailed(final FailedCallback failedCallback) {
        this.failedCallback = failedCallback;
        return this;
    }

    public Dispatcher onException(final ExceptionCallback exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
        return this;
    }
}
