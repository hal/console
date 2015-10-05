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

import com.ekuefler.supereventbus.EventBus;
import com.google.inject.Provider;
import elemental.client.Browser;
import elemental.html.FormData;
import elemental.html.InputElement;
import elemental.xml.XMLHttpRequest;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.I18n;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;

/**
 * The dispatcher executes operations / uploads against the management endpoint.
 * <p>
 * TODO Add a way to track management operations.
 *
 * @author Harald Pehl
 */
public class Dispatcher {

    @FunctionalInterface
    public interface SuccessCallback<T> {

        void onSuccess(T result);
    }


    @FunctionalInterface
    public interface OperationCallback extends SuccessCallback<ModelNode> {}


    @FunctionalInterface
    public interface CompositeCallback extends SuccessCallback<CompositeResult> {}


    @FunctionalInterface
    public interface FailedCallback {

        void onFailed(Operation operation, String failure);
    }


    @FunctionalInterface
    public interface ExceptionCallback {

        void onException(Operation operation, Throwable exception);
    }


    enum HttpMethod {GET, POST}


    static final String APPLICATION_DMR_ENCODED = "application/dmr-encoded";
    static final String APPLICATION_JSON = "application/json";

    static final String HEADER_ACCEPT = "Accept";
    static final String HEADER_CONTENT_TYPE = "Content-Type";
    static final String HEADER_MANAGEMENT_CLIENT_NAME = "X-Management-Client-Name";
    static final String HEADER_MANAGEMENT_CLIENT_VALUE = "HAL";

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
    private final Provider<ProcessStateProcessor> processStateProcessor;
    private FailedCallback failedCallback;
    private ExceptionCallback exceptionCallback;

    @Inject
    public Dispatcher(final Endpoints endpoints, final EventBus eventBus, final I18n i18n,
            Provider<ProcessStateProcessor> processStateProcessor) {
        this.endpoints = endpoints;
        this.eventBus = eventBus;
        this.processStateProcessor = processStateProcessor;

        this.failedCallback = (operation, failure) -> {
            logger.error("Dispatcher failed: {}, operation: {}", failure, operation);
            eventBus.post(Message.error(i18n.constants().dispatcher_failed(), failure));
        };
        this.exceptionCallback = (operation, t) -> {
            logger.error("Dispatcher exception: {}, operation {}", t.getMessage(), operation);
            eventBus.post(Message.error(i18n.constants().dispatcher_exception(), t.getMessage()));
        };
    }


    // ------------------------------------------------------ execute composite

    public void execute(final Composite composite, final CompositeCallback callback) {
        dmr(composite, callback, failedCallback, exceptionCallback);
    }

    public void execute(final Composite composite, final CompositeCallback callback,
            final FailedCallback failedCallback) {
        dmr(composite, callback, failedCallback, exceptionCallback);
    }

    public void execute(final Composite composite, final CompositeCallback callback,
            final FailedCallback failedCallback, final ExceptionCallback exceptionCallback) {
        dmr(composite, callback, failedCallback, exceptionCallback);
    }

    public void executeInFunction(final Control<FunctionContext> control, final Composite composite,
            final CompositeCallback callback) {
        dmr(composite, callback, new FailedFunctionCallback(control), new ExceptionalFunctionCallback(control));
    }


    // ------------------------------------------------------ execute operation

    public void execute(final Operation operation, final OperationCallback callback) {
        dmr(operation, callback, failedCallback, exceptionCallback);
    }

    public void execute(final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback) {
        dmr(operation, callback, failedCallback, exceptionCallback);
    }

    public void execute(final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, final ExceptionCallback exceptionCallback) {
        dmr(operation, callback, failedCallback, exceptionCallback);
    }

    public void executeInFunction(final Control<FunctionContext> control, Operation operation,
            final OperationCallback callback) {
        dmr(operation, callback, new FailedFunctionCallback(control), new ExceptionalFunctionCallback(control));
    }


    // ------------------------------------------------------ execute dmr

    private <T> void dmr(final Operation operation, final SuccessCallback<T> callback,
            final FailedCallback failedCallback, final ExceptionCallback exceptionCallback) {
        String url;
        HttpMethod method;
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

        XMLHttpRequest xhr = newXhr(url, method, operation, new DmrPayloadProcessor(), callback, failedCallback,
                exceptionCallback);
        xhr.setRequestHeader(HEADER_ACCEPT, APPLICATION_DMR_ENCODED);
        xhr.setRequestHeader(HEADER_CONTENT_TYPE, APPLICATION_DMR_ENCODED);
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

    public void upload(final InputElement fileInput, final Operation operation, final OperationCallback callback) {
        upload(fileInput, operation, callback, failedCallback, exceptionCallback);
    }

    public void upload(final InputElement fileInput, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback) {
        upload(fileInput, operation, callback, failedCallback, exceptionCallback);
    }

    public void upload(final InputElement fileInput, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        FormData formData = createFormData(fileInput, operation.toBase64String());
        XMLHttpRequest xhr = newXhr(endpoints.upload(), POST, operation, new UploadPayloadProcessor(), callback,
                failedCallback, exceptionCallback);
        xhr.send(formData);
    }

    private native FormData createFormData(InputElement fileInput, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(fileInput.name, fileInput.files[0]);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;


    // ------------------------------------------------------ create and setup xhr

    private <T> XMLHttpRequest newXhr(final String url, final HttpMethod method, final Operation operation,
            final PayloadProcessor payloadProcessor, SuccessCallback<T> callback, final FailedCallback failedCallback,
            final ExceptionCallback exceptionCallback) {
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();

        xhr.setOnreadystatechange(event -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                int status = xhr.getStatus();
                String responseText = xhr.getResponseText();
                String contentType = xhr.getResponseHeader(HEADER_CONTENT_TYPE);

                switch (status) {
                    case 200:
                    case 500:
                        ModelNode payload = payloadProcessor.processPayload(method, contentType, responseText);
                        if (!payload.isFailure()) {
                            if (processStateProcessor.get().accepts(payload)) {
                                ProcessState processState = processStateProcessor.get().process(payload);
                                eventBus.post(processState);
                            }
                            ModelNode result = payload.get(RESULT);
                            if (operation instanceof Composite && callback instanceof CompositeCallback) {
                                ((CompositeCallback) callback).onSuccess(new CompositeResult(result));
                            } else if (callback instanceof OperationCallback) {
                                ((OperationCallback) callback).onSuccess(result);
                            } else {
                                exceptionCallback.onException(operation,
                                        new DispatchException("Wrong combination of operation and callback.", 500));
                            }
                        } else {
                            failedCallback.onFailed(operation, payload.getFailureDescription());
                        }
                        break;
                    case 0:
                    case 401:
                        exceptionCallback.onException(operation,
                                new DispatchException("Authentication required.", status));
                        break;
                    case 403:
                        exceptionCallback.onException(operation,
                                new DispatchException("Authentication required.", status));
                        break;
                    case 404:
                        exceptionCallback.onException(operation, new DispatchException(
                                "Management interface at '" + url + "' not found.", status));
                        break;
                    case 503:
                        exceptionCallback.onException(operation, new DispatchException(
                                "Service temporarily unavailable. Is the server still booting?", status));
                        break;
                    default:
                        exceptionCallback.onException(operation,
                                new DispatchException("Unexpected status code.", status));
                        break;
                }
            }
        });

        xhr.addEventListener("error", event -> exceptionCallback
                .onException(operation, new DispatchException("Communication error.", xhr.getStatus())));

        xhr.open(POST.name(), endpoints.upload(), true);
        xhr.setRequestHeader(HEADER_MANAGEMENT_CLIENT_NAME, HEADER_MANAGEMENT_CLIENT_VALUE);
        xhr.setWithCredentials(true);

        return xhr;
    }
}
