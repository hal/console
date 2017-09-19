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
import java.util.function.Consumer;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.XMLHttpRequest;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
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
import org.jboss.hal.flow.Control;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.EsParam;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;
import rx.SingleSubscriber;

import static com.google.common.collect.Sets.difference;
import static elemental2.core.Global.encodeURIComponent;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;
import static org.jboss.hal.dmr.dispatch.RequestHeader.ACCEPT;
import static org.jboss.hal.dmr.dispatch.RequestHeader.CONTENT_TYPE;
import static org.jboss.hal.dmr.dispatch.RequestHeader.X_MANAGEMENT_CLIENT_NAME;

/** Executes operations against the management endpoint. */
@SuppressWarnings("DuplicateStringLiteralInspection")
@JsType(namespace = "hal.dmr")
public class Dispatcher implements RecordingHandler {

    @FunctionalInterface
    public interface OnFail {

        void onFailed(Operation operation, String failure);
    }


    @FunctionalInterface
    public interface OnError {

        void onException(Operation operation, Throwable exception);
    }


    public enum HttpMethod {GET, POST}


    @FunctionalInterface
    private interface OnLoad {

        void onLoad(XMLHttpRequest xhr);
    }


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
    private final OnFail failedCallback;
    private final OnError exceptionCallback;

    @Inject
    @JsIgnore
    public Dispatcher(Environment environment, Endpoints endpoints, Settings settings,
            EventBus eventBus, ResponseHeadersProcessors responseHeadersProcessors,
            Macros macros, Resources resources) {
        this.environment = environment;
        this.endpoints = endpoints;
        this.settings = settings;
        this.eventBus = eventBus;
        this.responseHeadersProcessors = responseHeadersProcessors;
        this.macros = macros;

        this.eventBus.addHandler(RecordingEvent.getType(), this);
        this.failedCallback = (operation, failure) -> {
            logger.error("Dispatcher failed: {}, operation: {}", failure, operation.asCli());
            if (!pendingLifecycleAction) {
                eventBus.fireEvent(
                        new MessageEvent(Message.error(resources.messages().lastOperationFailed(), failure)));
            }
        };
        this.exceptionCallback = (operation, t) -> {
            logger.error("Dispatcher exception: {}, operation {}", t.getMessage(), operation.asCli());
            if (!pendingLifecycleAction) {
                eventBus.fireEvent(
                        new MessageEvent(Message.error(resources.messages().lastOperationException(), t.getMessage())));
            }
        };
    }


    // ------------------------------------------------------ execute composite

    @JsIgnore
    public void execute(Composite operations, Consumer<CompositeResult> success) {
        dmr(operations, payload -> success.accept(compositeResult(payload)), failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(Composite operations, Consumer<CompositeResult> success, OnFail fail) {
        dmr(operations, payload -> success.accept(compositeResult(payload)), fail, exceptionCallback);
    }

    @JsIgnore
    public void execute(Composite operations, Consumer<CompositeResult> success, OnFail fail, OnError error) {
        dmr(operations, payloadres -> success.accept(compositeResult(payloadres)), fail, error);
    }


    // ------------------------------------------------------ execute composite in flow

    @JsIgnore
    public void executeInFlow(Control control, Composite operations, Consumer<CompositeResult> success) {
        dmr(operations, payload -> success.accept(compositeResult(payload)),
                new FailedFlowCallback<>(control), new ExceptionalFlowCallback<>(control));
    }

    @JsIgnore
    public void executeInFlow(Control control, Composite operations, Consumer<CompositeResult> success, OnFail fail) {
        dmr(operations, payload -> success.accept(compositeResult(payload)), fail,
                new ExceptionalFlowCallback<>(control));
    }

    private CompositeResult compositeResult(ModelNode payload) { return new CompositeResult(payload.get(RESULT)); }


    // ------------------------------------------------------ execute operation

    @JsIgnore
    public void execute(Operation operation, Consumer<ModelNode> success) {
        dmr(operation, payload -> success.accept(payload.get(RESULT)), failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void execute(Operation operation, Consumer<ModelNode> success, OnFail fail) {
        dmr(operation, payload -> success.accept(payload.get(RESULT)), fail, exceptionCallback);
    }

    @JsIgnore
    public void execute(Operation operation, Consumer<ModelNode> success, OnFail fail, OnError error) {
        dmr(operation, payload -> success.accept(payload.get(RESULT)), fail, error);
    }

    @JsIgnore
    public Single<ModelNode> execute(Operation operation) {
        return dmr(operation).map(payload -> payload.get(RESULT));
    }


    // ------------------------------------------------------ execute operation in flow

    @JsIgnore
    public void executeInFlow(Control control, Operation operation, Consumer<ModelNode> success) {
        dmr(operation, payload -> success.accept(payload.get(RESULT)),
                new FailedFlowCallback<>(control), new ExceptionalFlowCallback<>(control));
    }

    @JsIgnore
    public void executeInFlow(Control control, Operation operation, Consumer<ModelNode> success, OnFail fail) {
        dmr(operation, payload -> success.accept(payload.get(RESULT)), fail, new ExceptionalFlowCallback<>(control));
    }


    // ------------------------------------------------------ dmr

    private void dmr(Operation operation, Consumer<ModelNode> success, OnFail fail, OnError error) {
        dmr(operation).subscribe(new SingleSubscriber<ModelNode>() {
            @Override public void onSuccess(ModelNode modelNode) {
                success.accept(modelNode);
            }
            @Override public void onError(Throwable ex) {
                if (ex instanceof DispatchFailure) fail.onFailed(operation, ex.getMessage());
                else error.onException(operation, ex);
            }
        });
    }

    @JsIgnore
    public Single<ModelNode> dmr(Operation operation) {
        Operation dmrOperation = runAs(operation); // runAs might mutate the operation, so do it synchronously
        boolean get = GetOperation.isSupported(dmrOperation.getName());
        String url = get ? operationUrl(dmrOperation) : endpoints.dmr();
        HttpMethod method = get ? GET : POST;
        // ^-- those eager fields are useful if you don't want to evaluate it on each Single subscription
        return Single.fromEmitter(emitter -> {
            // in general, code inside the RX type should be able to be executed multiple times and always returns
            // the same result, so you should be careful to not mutate anything (like the operation). This is useful
            // for example if you use the retry operator that will try again (subscribe again) if it fails.
            XMLHttpRequest xhr = newDmrXhr(url, method, dmrOperation, new DmrPayloadProcessor(), emitter::onSuccess,
                    (op, fail) -> emitter.onError(new DispatchFailure(operation, fail)),
                    (op, error) -> emitter.onError(error));
            xhr.setRequestHeader(ACCEPT.header(), APPLICATION_DMR_ENCODED);
            xhr.setRequestHeader(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);
            if (get) xhr.send();
            else xhr.send(dmrOperation.toBase64String());
            recordOperation(operation);
        });
    }


    // ------------------------------------------------------ upload

    @JsIgnore
    public void upload(File file, Operation operation, Consumer<ModelNode> success) {
        upload(file, operation, success, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void upload(File file, Operation operation, Consumer<ModelNode> success, OnFail fail) {
        upload(file, operation, success, fail, exceptionCallback);
    }

    @JsIgnore
    public void upload(File file, Operation operation, Consumer<ModelNode> success, OnFail fail, OnError error) {
        Operation uploadOperation = runAs(operation);
        FormData formData = createFormData(file, uploadOperation.toBase64String());
        uploadFormData(formData, uploadOperation, success, fail, error);
    }

    private native FormData createFormData(File file, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(file.name, file);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;

    @JsIgnore
    public void upload(HTMLInputElement fileInput, Operation operation, Consumer<ModelNode> success) {
        upload(fileInput, operation, success, failedCallback, exceptionCallback);
    }

    @JsIgnore
    public void upload(HTMLInputElement fileInput, Operation operation, Consumer<ModelNode> callback, OnFail fail) {
        upload(fileInput, operation, callback, fail, exceptionCallback);
    }

    @JsIgnore
    public void upload(HTMLInputElement fileInput, Operation operation, Consumer<ModelNode> success, OnFail fail,
            OnError error) {
        Operation uploadOperation = runAs(operation);
        FormData formData = createFormData(fileInput, uploadOperation.toBase64String());
        uploadFormData(formData, uploadOperation, success, fail, error);
    }

    private native FormData createFormData(HTMLInputElement fileInput, String operation) /*-{
        var formData = new $wnd.FormData();
        formData.append(fileInput.name, fileInput.files[0]);
        formData.append("operation", new Blob([operation], {type: "application/dmr-encoded"}));
        return formData;
    }-*/;

    private void uploadFormData(FormData formData, Operation operation, Consumer<ModelNode> success, OnFail fail,
            OnError error) {
        XMLHttpRequest xhr = newDmrXhr(endpoints.upload(), POST, operation, new UploadPayloadProcessor(),
                res -> success.accept(res.get(RESULT)), fail, error);
        xhr.send(formData);
        // Uploads are not supported in macros!
    }


    // ------------------------------------------------------ download

    @JsIgnore
    public void download(Operation operation, Consumer<String> success) {
        Operation downloadOperation = runAs(operation);
        String url = downloadUrl(downloadOperation);
        XMLHttpRequest request = newXhr(url, GET, downloadOperation, exceptionCallback, xhr -> {
            int status = (int) xhr.status;
            String responseText = xhr.responseText;

            if (status == 200) {
                success.accept(responseText);
            } else {
                handleErrorCodes(url, status, downloadOperation, exceptionCallback);
            }
        });
        request.setRequestHeader(ACCEPT.header(), APPLICATION_DMR_ENCODED);
        request.setRequestHeader(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);
        request.send();
        // Downloads are not supported in macros!
    }

    @JsIgnore
    public String downloadUrl(Operation operation) {
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
                    .map(property -> encodeURIComponent(property.getName()) + "/" +
                            encodeURIComponent(property.getValue().asString()))
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
                builder.append("&").append(encodeURIComponent(property.getName()));
                if (property.getValue().isDefined()) {
                    builder.append("=").append(encodeURIComponent(property.getValue().asString()));
                }
            });
        }

        // TODO operation headers
        return builder.toString();
    }


    // ------------------------------------------------------ xhr

    private XMLHttpRequest newDmrXhr(String url, HttpMethod method, Operation operation,
            PayloadProcessor payloadProcessor, Consumer<ModelNode> success, OnFail fail, OnError error) {
        return newXhr(url, method, operation, error, xhr -> {
            int status = (int) xhr.status;
            String responseText = xhr.responseText;
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
                    success.accept(payload);
                } else {
                    fail.onFailed(operation, payload.getFailureDescription());
                }
            } else {
                if (!pendingLifecycleAction) {
                    handleErrorCodes(url, status, operation, error);
                }
            }
        });
    }

    private XMLHttpRequest newXhr(String url, HttpMethod method, Operation operation, OnError error, OnLoad onLoad) {
        XMLHttpRequest xhr = new XMLHttpRequest();

        // The order of the XHR methods is important! Do not rearrange the code unless you know what you're doing!
        xhr.onload = event -> onLoad.onLoad(xhr);
        xhr.addEventListener("error",  //NON-NLS
                event -> handleErrorCodes(url, (int) xhr.status, operation, error), false);
        xhr.open(method.name(), url, true);
        xhr.setRequestHeader(X_MANAGEMENT_CLIENT_NAME.header(), HEADER_MANAGEMENT_CLIENT_VALUE);
        xhr.withCredentials = true;

        return xhr;
    }

    private void handleErrorCodes(String url, int status, Operation operation, OnError error) {
        switch (status) {
            case 0:
                error.onException(operation, new DispatchError(operation,
                        "The response for '" + url + "' could not be processed.", status));
                break;
            case 401:
            case 403:
                error.onException(operation, new DispatchError(operation,
                        "Authentication required.", status));
                break;
            case 404:
                error.onException(operation, new DispatchError(operation,
                        "Management interface at '" + url + "' not found.", status));
                break;
            case 500:
                error.onException(operation, new DispatchError(operation,
                        "Internal Server Error for '" + operation.asCli() + "'.", status));
                break;
            case 503:
                error.onException(operation, new DispatchError(operation,
                        "Service temporarily unavailable. Is the server still booting?", status));
                break;
            default:
                error.onException(operation, new DispatchError(operation,
                        "Unexpected status code.", status));
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
    public void onRecording(RecordingEvent event) {
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
        dmr(composite, payload -> callback.onSuccess(compositeResult(payload)), failedCallback, exceptionCallback);
    }

    /**
     * Executes the specified operation. The callback contains just the result w/o surrounding nodes like "outcome".
     *
     * @param operation The operation to execute.
     * @param callback  The callback receiving the result.
     */
    @JsMethod(name = "execute")
    public void jsExecute(Operation operation, @EsParam("function(result: ModelNode)") JsOperationCallback callback) {
        dmr(operation, payload -> callback.onSuccess(payload.get(RESULT)), failedCallback, exceptionCallback);
    }
}
