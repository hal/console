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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.html.File;
import elemental.html.FormData;
import elemental.html.InputElement;
import elemental.xml.XMLHttpRequest;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.ResponseHeadersProcessor.Header;
import org.jboss.hal.dmr.macro.Action;
import org.jboss.hal.dmr.macro.Macro;
import org.jboss.hal.dmr.macro.MacroFinishedEvent;
import org.jboss.hal.dmr.macro.MacroOperationEvent;
import org.jboss.hal.dmr.macro.MacroOptions;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.dmr.macro.RecordingEvent;
import org.jboss.hal.dmr.macro.RecordingEvent.RecordingHandler;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Sets.difference;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;
import static org.jboss.hal.dmr.dispatch.RequestHeader.ACCEPT;
import static org.jboss.hal.dmr.dispatch.RequestHeader.CONTENT_TYPE;
import static org.jboss.hal.dmr.dispatch.RequestHeader.X_MANAGEMENT_CLIENT_NAME;

/**
 * Executes operations against the management endpoint.
 *
 * @author Harald Pehl
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
@JsType(namespace = "hal.dmr")
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


    @FunctionalInterface
    private interface ReadyListener {

        void onReady(XMLHttpRequest xhr);
    }


    static final FailedCallback NOOP_FAILED_CALLBACK = (op, failure) -> {/* noop */};
    static final ExceptionCallback NOOP_EXCEPTIONAL_CALLBACK = (op, exception) -> {/* noop */};

    static final String APPLICATION_DMR_ENCODED = "application/dmr-encoded";
    static final String APPLICATION_JSON = "application/json";

    private static final String HEADER_MANAGEMENT_CLIENT_VALUE = "HAL";

    @NonNls private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private static boolean pendingLifecycleAction = false;

    @JsIgnore
    public static void setPendingLifecycleAction(final boolean value) {
        pendingLifecycleAction = value;
        logger.debug("Dispatcher.pendingLifecycleAction = {}", pendingLifecycleAction);
    }


    private final Environment environment;
    private final Endpoints endpoints;
    private final Settings settings;
    private final EventBus eventBus;
    private final ResponseHeadersProcessors responseHeadersProcessors;
    private final Macros macros;
    private final FailedCallback failedCallback;
    private final ExceptionCallback exceptionCallback;

    @Inject
    @JsIgnore
    public Dispatcher(final Environment environment, final Endpoints endpoints, final Settings settings,
            final EventBus eventBus, final ResponseHeadersProcessors responseHeadersProcessors,
            final Macros macros, final Resources resources) {
        this.environment = environment;
        this.endpoints = endpoints;
        this.settings = settings;
        this.eventBus = eventBus;
        this.responseHeadersProcessors = responseHeadersProcessors;
        this.macros = macros;

        this.eventBus.addHandler(RecordingEvent.getType(), this);
        this.failedCallback = (operation, failure) -> {
            logger.error("Dispatcher failed: {}, operation: {}", failure, operation);
            if (!pendingLifecycleAction) {
                eventBus.fireEvent(
                        new MessageEvent(Message.error(resources.messages().lastOperationFailed(), failure)));
            }
        };
        this.exceptionCallback = (operation, t) -> {
            logger.error("Dispatcher exception: {}, operation {}", t.getMessage(), operation);
            if (!pendingLifecycleAction) {
                eventBus.fireEvent(
                        new MessageEvent(Message.error(resources.messages().lastOperationException(), t.getMessage())));
            }
        };
    }


    // ------------------------------------------------------ execute composite

    @JsIgnore
    public void execute(final Composite composite, final CompositeCallback callback) {
        dmr(composite, payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Composite composite, final CompositeCallback callback,
            final FailedCallback failedCallback) {
        dmr(composite, payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Composite composite, final CompositeCallback callback,
            final FailedCallback failedCallback, final ExceptionCallback exceptionCallback) {
        dmr(composite, payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public <T extends FunctionContext> void executeInFunction(final Control<T> control, final Composite composite,
            final CompositeCallback callback) {
        dmr(composite, payload -> payload.get(RESULT), callback, new FailedFunctionCallback<>(control),
                new ExceptionalFunctionCallback<>(control));
    }

    @JsIgnore
    public <T extends FunctionContext> void executeInFunction(final Control<T> control, final Composite composite,
            final CompositeCallback callback, FailedCallback failedCallback) {
        dmr(composite, payload -> payload.get(RESULT), callback, failedCallback,
                new ExceptionalFunctionCallback<>(control));
    }


    // ------------------------------------------------------ execute operation

    @JsIgnore
    public void execute(final Operation operation, final OperationCallback callback) {
        dmr(operation, payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback) {
        dmr(operation, payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, final ExceptionCallback exceptionCallback) {
        dmr(operation, payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Operation operation, final Function<ModelNode, ModelNode> getResult,
            final OperationCallback callback) {
        dmr(operation, getResult, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Operation operation, final Function<ModelNode, ModelNode> getResult,
            final OperationCallback callback, final FailedCallback failedCallback) {
        dmr(operation, getResult, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(final Operation operation, final Function<ModelNode, ModelNode> getResult,
            final OperationCallback callback, final FailedCallback failedCallback,
            final ExceptionCallback exceptionCallback) {
        dmr(operation, getResult, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public <T extends FunctionContext> void executeInFunction(final Control<T> control, Operation operation,
            final OperationCallback callback) {
        dmr(operation, payload -> payload.get(RESULT), callback, new FailedFunctionCallback<>(control),
                new ExceptionalFunctionCallback<>(control));
    }

    @JsIgnore
    public <T extends FunctionContext> void executeInFunction(final Control<T> control, Operation operation,
            final OperationCallback callback, FailedCallback failedCallback) {
        dmr(operation, payload -> payload.get(RESULT), callback, failedCallback,
                new ExceptionalFunctionCallback<>(control));
    }


    // ------------------------------------------------------ dmr

    private <T> void dmr(final Operation operation, final Function<ModelNode, ModelNode> getResult,
            final SuccessCallback<T> callback, final FailedCallback failedCallback,
            final ExceptionCallback exceptionCallback) {
        String url;
        HttpMethod method;
        Operation dmrOperation = runAs(operation);

        if (GetOperation.isSupported(dmrOperation.getName())) {
            url = operationUrl(dmrOperation);
            method = GET;
        } else {
            url = endpoints.dmr();
            method = POST;
        }

        XMLHttpRequest xhr = newDmrXhr(url, method, dmrOperation, new DmrPayloadProcessor(), getResult, callback,
                failedCallback, exceptionCallback);
        xhr.setRequestHeader(ACCEPT.header(), APPLICATION_DMR_ENCODED);
        xhr.setRequestHeader(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);
        if (method == GET) {
            xhr.send();
        } else {
            xhr.send(dmrOperation.toBase64String());
        }
        recordOperation(operation);
    }


    // ------------------------------------------------------ upload

    @JsIgnore
    public void upload(final File file, final Operation operation, final OperationCallback callback) {
        upload(file, operation, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void upload(final File file, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback) {
        upload(file, operation, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void upload(final File file, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        Operation uploadOperation = runAs(operation);
        FormData formData = createFormData(file, uploadOperation.toBase64String());
        uploadFormData(formData, uploadOperation, callback, failedCallback, exceptionCallback);
    }

    private native FormData createFormData(File file, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(file.name, file);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;

    @JsIgnore
    public void upload(final InputElement fileInput, final Operation operation, final OperationCallback callback) {
        upload(fileInput, operation, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void upload(final InputElement fileInput, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback) {
        upload(fileInput, operation, callback, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void upload(final InputElement fileInput, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        Operation uploadOperation = runAs(operation);
        FormData formData = createFormData(fileInput, uploadOperation.toBase64String());
        uploadFormData(formData, uploadOperation, callback, failedCallback, exceptionCallback);
    }

    private native FormData createFormData(InputElement fileInput, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(fileInput.name, fileInput.files[0]);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;

    private void uploadFormData(FormData formData, final Operation operation, final OperationCallback callback,
            final FailedCallback failedCallback, ExceptionCallback exceptionCallback) {
        XMLHttpRequest xhr = newDmrXhr(endpoints.upload(), POST, operation, new UploadPayloadProcessor(),
                payload -> payload.get(RESULT), callback, failedCallback, exceptionCallback);
        xhr.send(formData);
        // Uploads are not supported in macros!
    }


    // ------------------------------------------------------ download

    @JsIgnore
    public void download(final Operation operation, final SuccessCallback<String> successCallback) {
        Operation downloadOperation = runAs(operation);
        String url = downloadUrl(downloadOperation);
        XMLHttpRequest request = newXhr(url, GET, downloadOperation, exceptionCallback, xhr -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                int status = xhr.getStatus();
                String responseText = xhr.getResponseText();

                if (status == 200) {
                    successCallback.onSuccess(responseText);
                } else {
                    handleErrorCodes(url, status, downloadOperation, exceptionCallback);
                }
            }
        });
        request.setRequestHeader(ACCEPT.header(), APPLICATION_DMR_ENCODED);
        request.setRequestHeader(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);
        request.send();
        // Downloads are not supported in macros!
    }

    @JsIgnore
    public String downloadUrl(final Operation operation) {
        return operationUrl(operation) + "&useStreamAsResponse"; //NON-NLS
    }


    // ------------------------------------------------------ run-as and urls

    private Operation runAs(Operation operation) {
        if (environment.getAccessControlProvider() == AccessControlProvider.RBAC) {
            Set<String> runAs = settings.get(RUN_AS).asSet();
            if (!runAs.isEmpty() && !difference(runAs, operation.getRoles()).isEmpty()) {
                return operation.runAs(runAs);
            }
        }
        return operation;
    }

    private String operationUrl(Operation operation) {
        StringBuilder builder = new StringBuilder();
        builder.append(endpoints.dmr()).append("/");

        // 1. address
        ResourceAddress address = operation.getAddress();
        if (!address.isEmpty()) {
            String path = address.asPropertyList().stream()
                    .map(property -> Browser.encodeURIComponent(property.getName()) + "/" +
                            Browser.encodeURIComponent(property.getValue().asString()))
                    .collect(joining("/"));
            builder.append(path);
        }

        // 2. operation
        String name = operation.getName();
        if (GetOperation.isSupported(name)) {
            GetOperation getOperation = GetOperation.get(name);
            name = getOperation.httpGetOperation();
        }
        builder.append("?").append(OP).append("=").append(name);

        // 3. parameter
        if (operation.hasParameter()) {
            operation.getParameter().asPropertyList().forEach(property -> {
                builder.append("&").append(Browser.encodeURIComponent(property.getName()));
                if (property.getValue().isDefined()) {
                    builder.append("=").append(Browser.encodeURIComponent(property.getValue().asString()));
                }
            });
        }

        // TODO operation headers

        return builder.toString();
    }


    // ------------------------------------------------------ xhr

    private <T> XMLHttpRequest newDmrXhr(final String url, final HttpMethod method, final Operation operation,
            final PayloadProcessor payloadProcessor, final Function<ModelNode, ModelNode> getResult,
            final SuccessCallback<T> callback, final FailedCallback failedCallback,
            final ExceptionCallback exceptionCallback) {
        return newXhr(url, method, operation, exceptionCallback, xhr -> {
            int readyState = xhr.getReadyState();
            if (readyState == 4) {
                int status = xhr.getStatus();
                String responseText = xhr.getResponseText();
                String contentType = xhr.getResponseHeader(CONTENT_TYPE.header());

                if (status == 200 || status == 500) {
                    ModelNode payload = payloadProcessor.processPayload(method, contentType, responseText);
                    if (!payload.isFailure()) {
                        if (environment.isStandalone()) {
                            if (payload.hasDefined(RESPONSE_HEADERS)) {
                                Header[] headers = new Header[]{new Header(payload.get(RESPONSE_HEADERS))};
                                for (ResponseHeadersProcessor processor : responseHeadersProcessors.processors()) {
                                    processor.process(headers);
                                }
                            }
                        } else {
                            if (payload.hasDefined(SERVER_GROUPS)) {
                                Header[] headers = collectHeaders(payload.get(SERVER_GROUPS));
                                if (headers.length != 0) {
                                    for (ResponseHeadersProcessor processor : responseHeadersProcessors.processors()) {
                                        processor.process(headers);
                                    }
                                }
                            }
                        }
                        ModelNode result = getResult.apply(payload);
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
                } else {
                    if (!pendingLifecycleAction) {
                        handleErrorCodes(url, status, operation, exceptionCallback);
                    }
                }
            }
        });
    }

    private XMLHttpRequest newXhr(final String url, final HttpMethod method, final Operation operation,
            final ExceptionCallback exceptionCallback, final ReadyListener readyListener) {
        XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();

        // The order of the XHR methods is important! Do not rearrange the code unless you know what you're doing!
        xhr.setOnreadystatechange(event -> readyListener.onReady(xhr));
        xhr.addEventListener("error",  //NON-NLS
                event -> handleErrorCodes(url, xhr.getStatus(), operation, exceptionCallback), false);
        xhr.open(method.name(), url, true);
        xhr.setRequestHeader(X_MANAGEMENT_CLIENT_NAME.header(), HEADER_MANAGEMENT_CLIENT_VALUE);
        xhr.setWithCredentials(true);

        return xhr;
    }

    private void handleErrorCodes(String url, int status, Operation operation, ExceptionCallback exceptionCallback) {
        switch (status) {
            case 0:
                exceptionCallback.onException(operation,
                        new DispatchException("The response for '" + url + "' could not be processed.", status));
                break;
            case 401:
            case 403:
                exceptionCallback.onException(operation, new DispatchException("Authentication required.", status));
                break;
            case 404:
                exceptionCallback.onException(operation,
                        new DispatchException("Management interface at '" + url + "' not found.", status));
                break;
            case 500:
                exceptionCallback.onException(operation,
                        new DispatchException("Internal Server Error for '" + operation.asCli() + "'.", status));
                break;
            case 503:
                exceptionCallback.onException(operation,
                        new DispatchException("Service temporarily unavailable. Is the server still booting?", status));
                break;
            default:
                exceptionCallback.onException(operation, new DispatchException("Unexpected status code.", status));
                break;
        }
    }


    // ------------------------------------------------------ response headers in domain

    private Header[] collectHeaders(ModelNode serverGroups) {
        List<Header> headers = new ArrayList<>();
        for (Property serverGroup : serverGroups.asPropertyList()) {
            ModelNode serverGroupValue = serverGroup.getValue();
            if (serverGroupValue.hasDefined(HOST)) {
                List<Property> hosts = serverGroupValue.get(HOST).asPropertyList();
                for (Property host : hosts) {
                    ModelNode hostValue = host.getValue();
                    List<Property> servers = hostValue.asPropertyList();
                    for (Property server : servers) {
                        ModelNode serverResponse = server.getValue().get(RESPONSE);
                        if (serverResponse.hasDefined(RESPONSE_HEADERS)) {
                            headers.add(new Header(serverGroup.getName(), host.getName(), server.getName(),
                                    serverResponse.get(RESPONSE_HEADERS)));
                        }
                    }
                }
            }
        }
        return headers.toArray(new Header[headers.size()]);
    }


    // ------------------------------------------------------ macro recording

    @Override
    @JsIgnore
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

    @SuppressWarnings("HardCodedStringLiteral")
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
            return operation.getName().startsWith("read") || operation.getName().equals(QUERY);
        }
    }


    // ------------------------------------------------------ JS methods


    @JsFunction
    public interface JsOperationCallback {

        void onSuccess(ModelNode result);
    }


    @JsFunction
    public interface JsCompositeCallback {

        void onSuccess(CompositeResult result);
    }

    /**
     * Executes the specified composite operation.
     *
     * @param composite The composite operation to execute.
     * @param callback  The callback receiving the result.
     */
    @JsMethod(name = "executeComposite")
    public void jsExecuteComposite(Composite composite,
            @EsParam("function(result: CompositeResult)") JsCompositeCallback callback) {
        CompositeCallback cc = callback::onSuccess;
        dmr(composite, payload -> payload.get(RESULT), cc, failedCallback, exceptionCallback);
    }

    /**
     * Executes the specified operation. The callback contains just the result w/o surrounding nodes like "outcome".
     *
     * @param operation The operation to execute.
     * @param callback  The callback receiving the result.
     */
    @JsMethod(name = "execute")
    public void jsExecute(Operation operation, @EsParam("function(result: ModelNode)") JsOperationCallback callback) {
        OperationCallback oc = callback::onSuccess;
        dmr(operation, payload -> payload.get(RESULT), oc, failedCallback, exceptionCallback);
    }
}
