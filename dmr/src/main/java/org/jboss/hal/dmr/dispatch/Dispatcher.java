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
package org.jboss.hal.dmr.dispatch;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.html.File;
import elemental.html.FormData;
import elemental.html.InputElement;
import elemental.xml.XMLHttpRequest;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.macro.Action;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.macro.MacroFinishedEvent;
import org.jboss.hal.dmr.macro.MacroOperationEvent;
import org.jboss.hal.dmr.macro.MacroOptions;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.dmr.macro.RecordingEvent;
import org.jboss.hal.dmr.macro.RecordingEvent.RecordingHandler;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
@SuppressWarnings("HardCodedStringLiteral")
public class Dispatcher implements RecordingHandler {

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


    public enum HttpMethod {GET, POST}


    public static final String APPLICATION_DMR_ENCODED = "application/dmr-encoded";
    public static final String APPLICATION_JSON = "application/json";
    public static final String HEADER_MANAGEMENT_CLIENT_NAME = "X-Management-Client-Name";
    public static final String HEADER_MANAGEMENT_CLIENT_VALUE = "HAL";

    public static OperationCallback NOOP_OPERATION_CALLBACK = (result) -> {};
    public static FailedCallback NOOP_FAILED_CALLBACK = (op, failure) -> {};
    public static ExceptionCallback NOOP_EXCEPTIONAL_CALLBACK = (op, exception) -> {};

    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * The read resource description supports the following parameters:
     * recursive, proxies, operations, inherited plus one not documented: locale.
     * See https://docs.jboss.org/author/display/WFLY9/Global+operations#Globaloperations-readresourcedescription
     * for a more detailed description
     */
    private static final String[] READ_RESOURCE_DESCRIPTION_OPTIONAL_PARAMETERS = new String[]{
            RECURSIVE, PROXIES, OPERATIONS, INHERITED, LOCALE
    };

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final Endpoints endpoints;
    private final EventBus eventBus;
    private final Provider<ProcessStateProcessor> processStateProcessor;
    private final FailedCallback failedCallback;
    private final ExceptionCallback exceptionCallback;
    private Macros macros;

    @Inject
    public Dispatcher(final Endpoints endpoints, final EventBus eventBus, final Resources resources,
            Provider<ProcessStateProcessor> processStateProcessor, final Macros macros) {
        this.endpoints = endpoints;
        this.eventBus = eventBus;
        this.eventBus.addHandler(RecordingEvent.getType(), this);
        this.processStateProcessor = processStateProcessor;
        this.macros = macros;

        this.failedCallback = (operation, failure) -> {
            logger.error("Dispatcher failed: {}, operation: {}", failure, operation);
            eventBus.fireEvent(new MessageEvent(Message.error(resources.messages().dispatcherFailed(), failure)));
        };
        this.exceptionCallback = (operation, t) -> {
            logger.error("Dispatcher exception: {}, operation {}", t.getMessage(), operation);
            eventBus.fireEvent(
                    new MessageEvent(Message.error(resources.messages().dispatcherException(), t.getMessage())));
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

    public <T extends FunctionContext> void executeInFunction(final Control<T> control, final Composite composite,
            final CompositeCallback callback) {
        dmr(composite, callback, new FailedFunctionCallback<>(control), new ExceptionalFunctionCallback<>(control));
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

    public <T extends FunctionContext> void executeInFunction(final Control<T> control, Operation operation,
            final OperationCallback callback) {
        dmr(operation, callback, new FailedFunctionCallback<>(control), new ExceptionalFunctionCallback<>(control));
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
        recordOperation(operation);
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

    public void upload(final File file, final Operation operation, final OperationCallback callback) {
        upload(file, operation, callback, failedCallback, exceptionCallback);
    }

    public void upload(final File file, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback) {
        upload(file, operation, callback, failedCallback, exceptionCallback);
    }

    public void upload(final File file, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        FormData formData = createFormData(file, operation.toBase64String());
        uploadFormData(formData, operation, callback, failedCallback, exceptionCallback);
    }

    private native FormData createFormData(File file, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(file.name, file);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;

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
        uploadFormData(formData, operation, callback, failedCallback, exceptionCallback);
    }

    private native FormData createFormData(InputElement fileInput, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(fileInput.name, fileInput.files[0]);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;

    private void uploadFormData(FormData formData, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        XMLHttpRequest xhr = newXhr(endpoints.upload(), POST, operation, new UploadPayloadProcessor(), callback,
                failedCallback, exceptionCallback);
        xhr.send(formData);
        // TODO Support uploads in macros?
        // recordOperation(operation);
    }


    // ------------------------------------------------------ create and setup xhr

    private <T> XMLHttpRequest newXhr(final String url, final HttpMethod method, final Operation operation,
            final PayloadProcessor payloadProcessor, SuccessCallback<T> callback, final FailedCallback failedCallback,
            final ExceptionCallback exceptionCallback) {
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();

        // The order of the XHR methods is important! Do not rearrange the code unless you know what you're doing!
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
                                eventBus.fireEvent(new ProcessStateEvent(processState));
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
                .onException(operation, new DispatchException("Communication error.", xhr.getStatus())), false);
        xhr.open(method.name(), url, true);
        xhr.setRequestHeader(HEADER_MANAGEMENT_CLIENT_NAME, HEADER_MANAGEMENT_CLIENT_VALUE);
        xhr.setWithCredentials(true);

        return xhr;
    }


    // ------------------------------------------------------ macro recording

    @Override
    public void onRecording(final RecordingEvent event) {
        if (event.getAction() == Action.START && macros.current() == null) {
            MacroOptions options = event.getOptions();
            String description = options.hasDefined(DESCRIPTION) ? options.get(DESCRIPTION).asString() : null;
            macros.startRecording(new Macro(options.getName(), description), options);

        } else if (event.getAction() == Action.STOP && macros.current() != null) {
            Macro finished = macros.current();
            MacroOptions options = macros.currentOptions();
            macros.stopRecording();
            eventBus.fireEvent(new MacroFinishedEvent(finished, options));
        }
    }

    private void recordOperation(Operation operation) {
        if (macros.current() != null && !macros.current().isSealed()) {
            if (macros.currentOptions().omitReadOperations() && readOnlyOperation(operation)) {
                return;
            }
            if (operation instanceof Composite && ((Composite) operation).size() == 1) {
                // TODO Is it ok to record a composite with one step as a single op?
                macros.current().addOperation(((Composite) operation).iterator().next());
            } else {
                macros.current().addOperation(operation);
            }
            eventBus.fireEvent(new MacroOperationEvent(macros.current(), operation));
        }
    }

    private boolean readOnlyOperation(Operation operation) {
        if (operation instanceof Composite) {
            Composite composite = (Composite) operation;
            for (Operation op : composite) {
                if (!op.getName().startsWith("read")) {
                    return false;
                }
            }
            return true;
        } else {
            return operation.getName().startsWith("read");
        }
    }


    // ------------------------------------------------------ getter

    public FailedCallback defaultFailedCallback() {
        return failedCallback;
    }

    public ExceptionCallback defaultExceptionCallback() {
        return exceptionCallback;
    }
}
