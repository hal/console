/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Inject;

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
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.Blob;
import elemental2.dom.Blob.ConstructorBlobPartsArrayUnionType;
import elemental2.dom.BlobPropertyBag;
import elemental2.dom.File;
import elemental2.dom.FileList;
import elemental2.dom.FormData;
import elemental2.dom.FormData.AppendValueUnionType;
import elemental2.dom.Headers;
import elemental2.dom.Request;
import elemental2.dom.RequestInit;
import elemental2.dom.Response;
import elemental2.promise.IThenable.ThenOnFulfilledCallbackFn;
import elemental2.promise.Promise;
import elemental2.promise.Promise.CatchOnRejectedCallbackFn;

import static java.util.stream.Collectors.joining;

import static com.google.common.collect.Sets.difference;
import static elemental2.core.Global.encodeURIComponent;
import static elemental2.dom.DomGlobal.fetch;
import static elemental2.dom.DomGlobal.navigator;
import static org.jboss.hal.config.Settings.Key.RUN_AS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FIND_NON_PROGRESSING_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INSTALLED_DRIVER_LIST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESPONSE_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUPS;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;
import static org.jboss.hal.dmr.dispatch.PayloadProcessor.PARSE_ERROR;
import static org.jboss.hal.dmr.dispatch.RequestHeader.ACCEPT;
import static org.jboss.hal.dmr.dispatch.RequestHeader.CONTENT_TYPE;
import static org.jboss.hal.dmr.dispatch.RequestHeader.X_MANAGEMENT_CLIENT_NAME;

/** Executes operations against the management endpoint. */
public class Dispatcher implements RecordingHandler {

    static final String APPLICATION_DMR_ENCODED = "application/dmr-encoded";
    static final String APPLICATION_JSON = "application/json";

    private static final String HEADER_MANAGEMENT_CLIENT_VALUE = "HAL";
    private static final Set<String> READ_ONLY_OPERATIONS = new HashSet<>(Arrays.asList(QUERY, FIND_NON_PROGRESSING_OPERATION,
            INSTALLED_DRIVER_LIST));
    private static final Predicate<Operation> READ_ONLY = operation -> operation.getName().startsWith("read")
            || READ_ONLY_OPERATIONS.contains(operation.getName());

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final Environment environment;
    private final Endpoints endpoints;
    private final Settings settings;
    private final EventBus eventBus;
    private final ResponseHeadersProcessors responseHeadersProcessors;
    private final Macros macros;
    private final ErrorCallback errorCallback;

    @Inject
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
        this.errorCallback = (operation, error) -> {
            logger.error("Dispatcher error: {}, operation {}", error, operation.asCli());
            eventBus.fireEvent(new MessageEvent(Message.error(resources.messages().lastOperationException(), error)));
        };
    }

    // ------------------------------------------------------ execute composite

    public void execute(Composite operations, Consumer<CompositeResult> success) {
        execute(operations, success, errorCallback);
    }

    public void execute(Composite operations, Consumer<CompositeResult> success, ErrorCallback errorCallback) {
        dmr(operations)
                .then(payload -> {
                    success.accept(compositeResult(payload));
                    return null;
                })
                .catch_(error -> {
                    errorCallback.onError(operations, String.valueOf(error));
                    return null;
                });
    }

    public Promise<CompositeResult> execute(Composite operations) {
        return dmr(operations).then(payload -> Promise.resolve(compositeResult(payload)));
    }

    private CompositeResult compositeResult(ModelNode payload) {
        return new CompositeResult(payload.get(RESULT));
    }

    // ------------------------------------------------------ execute operation

    public void execute(Operation operation, Consumer<ModelNode> success) {
        execute(operation, success, errorCallback);
    }

    public void execute(Operation operation, Consumer<ModelNode> success, ErrorCallback errorCallback) {
        dmr(operation)
                .then(payload -> {
                    success.accept(operationResult(payload));
                    return null;
                })
                .catch_(error -> {
                    errorCallback.onError(operation, String.valueOf(error));
                    return null;
                });
    }

    public Promise<ModelNode> execute(Operation operation) {
        return dmr(operation).then(payload -> Promise.resolve(operationResult(payload)));
    }

    private ModelNode operationResult(ModelNode payload) {
        return payload.get(RESULT);
    }

    /**
     * Executes the operation and upon successful result, calls the success function with the response results, but doesn't
     * retrieve the "result" payload as the other execute methods does. You should use this method if the response node you want
     * is not in the "result" attribute.
     */
    public void dmr(Operation operation, Consumer<ModelNode> success, ErrorCallback errorCallback) {
        dmr(operation)
                .then(payload -> {
                    success.accept(payload);
                    return null;
                })
                .catch_(error -> {
                    errorCallback.onError(operation, String.valueOf(error));
                    return null;
                });
    }

    /**
     * Executes the operation and upon successful result, returns the response results, but doesn't retrieve the "result"
     * payload as the other execute methods does. You should use this method if the response node you want is not in the
     * "result" attribute.
     */
    public Promise<ModelNode> dmr(Operation operation) {
        RequestInit init = requestInit(POST, true);
        init.setBody(runAs(operation).toBase64String());
        Request request = new Request(endpoints.dmr(), init);

        return fetch(request)
                .then(processResponse())
                .then(processText(operation, new DmrPayloadProcessor(), true))
                .catch_(rejectWithError());
    }

    // ------------------------------------------------------ upload

    public Promise<ModelNode> upload(FileList files, Operation operation) {
        Operation uploadOperation = runAs(operation);
        ConstructorBlobPartsArrayUnionType blob = ConstructorBlobPartsArrayUnionType.of(
                uploadOperation.toBase64String());
        BlobPropertyBag options = BlobPropertyBag.create();
        options.setType("application/dmr-encoded");

        FormData formData = new FormData();
        for (int i = 0; i < files.getLength(); i++) {
            File file = files.item(i);
            appendFile(formData, file);
        }
        formData.append(OPERATION, new Blob(new ConstructorBlobPartsArrayUnionType[] { blob }, options));

        return fetch(uploadRequest(formData))
                .then(processResponse())
                .then(processText(operation, new UploadPayloadProcessor(), false))
                .then(payload -> Promise.resolve(operationResult(payload)))
                .catch_(rejectWithError());
    }

    public Promise<ModelNode> upload(File file, Operation operation) {
        Operation uploadOperation = runAs(operation);
        ConstructorBlobPartsArrayUnionType blob = ConstructorBlobPartsArrayUnionType.of(
                uploadOperation.toBase64String());
        BlobPropertyBag options = BlobPropertyBag.create();
        options.setType("application/dmr-encoded");

        FormData formData = new FormData();
        appendFile(formData, file);
        formData.append(OPERATION, new Blob(new ConstructorBlobPartsArrayUnionType[] { blob }, options));

        return fetch(uploadRequest(formData))
                .then(processResponse())
                .then(processText(operation, new UploadPayloadProcessor(), false))
                .then(payload -> Promise.resolve(operationResult(payload)))
                .catch_(rejectWithError());
    }

    private Request uploadRequest(FormData formData) {
        RequestInit init = requestInit(POST, false);
        init.setBody(formData);
        return new Request(endpoints.upload(), init);
    }

    private void appendFile(FormData formData, File file) {
        if (navigator.userAgent.contains("Safari") && !navigator.userAgent.contains("Chrome")) {
            // Safari does not support sending new files
            // https://bugs.webkit.org/show_bug.cgi?id=165081
            ConstructorBlobPartsArrayUnionType fileAsBlob = ConstructorBlobPartsArrayUnionType.of(file);
            formData.append(file.name, new Blob(new ConstructorBlobPartsArrayUnionType[] { fileAsBlob }));
        } else {
            formData.append(file.name, AppendValueUnionType.of(file));
        }
    }

    // ------------------------------------------------------ download

    public void download(Operation operation, Consumer<String> success) {
        Operation downloadOperation = runAs(operation);
        String downloadUrl = downloadUrl(downloadOperation);
        RequestInit init = requestInit(GET, true);
        Request request = new Request(downloadUrl, init);

        fetch(request)
                .then(response -> {
                    if (response.status != 200) {
                        return Promise.reject(ResponseStatus.fromStatusCode(response.status).statusText());
                    } else {
                        return response.text();

                    }
                })
                .then(text -> {
                    success.accept(text);
                    return null;
                })
                .catch_(rejectWithError());
    }

    public String downloadUrl(Operation operation) {
        return operationUrl(operation) + "&useStreamAsResponse"; // NON-NLS
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

        // 4. bearer token
        String token = getBearerToken();
        if (token != null) {
            builder.append("&access_token=").append(token);
        }

        // TODO operation headers
        return builder.toString();
    }

    // ------------------------------------------------------ request && promise handlers

    RequestInit requestInit(HttpMethod method, boolean dmr) {
        Headers headers = new Headers();
        if (dmr) {
            headers.set(ACCEPT.header(), APPLICATION_DMR_ENCODED);
            headers.set(CONTENT_TYPE.header(), APPLICATION_DMR_ENCODED);
        }
        headers.set(X_MANAGEMENT_CLIENT_NAME.header(), HEADER_MANAGEMENT_CLIENT_VALUE);
        String bearerToken = getBearerToken();
        if (bearerToken != null) {
            headers.set("Authorization", "Bearer " + bearerToken);
        }

        RequestInit init = RequestInit.create();
        init.setMethod(method.name());
        init.setHeaders(headers);
        init.setMode("cors");
        init.setCredentials("include");
        return init;
    }

    // ------------------------------------------------------ promise handlers

    ThenOnFulfilledCallbackFn<Response, String> processResponse() {
        return response -> {
            if (!response.ok && response.status != 500) {
                return Promise.reject(ResponseStatus.fromStatusCode(response.status).statusText());
            }
            String contentType = response.headers.get(CONTENT_TYPE.header());
            if (!contentType.startsWith(APPLICATION_DMR_ENCODED)) {
                return Promise.reject(PARSE_ERROR + contentType);
            }
            return response.text();
        };
    }

    ThenOnFulfilledCallbackFn<String, ModelNode> processText(Operation operation, PayloadProcessor payloadProcessor,
            boolean recordOperation) {
        return text -> {
            if (recordOperation) {
                recordOperation(operation);
            }
            logger.trace("DMR operation: {}", operation);
            ModelNode payload = payloadProcessor.processPayload(POST, APPLICATION_DMR_ENCODED, text);
            if (!payload.isFailure()) {
                if (environment.isStandalone()) {
                    if (payload.hasDefined(RESPONSE_HEADERS)) {
                        Header[] headers = new Header[] { new Header(payload.get(RESPONSE_HEADERS)) };
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
                return Promise.resolve(payload);
            } else {
                return Promise.reject(payload.getFailureDescription());
            }
        };
    }

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
        return headers.toArray(new Header[0]);
    }

    // ------------------------------------------------------ error handling

    CatchOnRejectedCallbackFn<ModelNode> rejectWithError() {
        return error -> {
            logger.error("Dispatcher error: {}", error);
            return Promise.reject(error);
        };
    }

    // ------------------------------------------------------ macro recording

    @Override
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

    private boolean readOnlyOperation(Operation operation) {
        if (operation instanceof Composite) {
            Composite composite = (Composite) operation;
            for (Operation op : composite) {
                if (!READ_ONLY.test(op)) {
                    return false;
                }
            }
            return true;
        } else {
            return READ_ONLY.test(operation);
        }
    }

    // ------------------------------------------------------ Keycloak

    /** Obtains the bearer token from keycloak object attached to the window. */
    public static native String getBearerToken()/*-{
        // keycloak javascript object is created in EndpointManager class
        // noinspection JSUnresolvedVariable
        var keycloak = $wnd.keycloak;
        if (keycloak != null && keycloak.token != null) {
            return keycloak.token;
        }
        return null;
    }-*/;

    // ------------------------------------------------------ inner classes

    @FunctionalInterface
    public interface ErrorCallback {

        void onError(Operation operation, String error);
    }

    public enum HttpMethod {
        GET, POST
    }

    public enum ResponseStatus {

        _0(0, "The response for could not be processed."),

        _401(401, "Unauthorized."),

        _403(403, "Forbidden."),

        _404(404, "Management interface not found."),

        _500(500, "Internal Server Error."),

        _503(503, "Service temporarily unavailable. Is the server still starting?"),

        UNKNOWN(-1, "Unexpected status code.");

        public static ResponseStatus fromStatusText(String statusText) {
            for (ResponseStatus responseStatus : ResponseStatus.values()) {
                if (responseStatus.statusText.equals(statusText)) {
                    return responseStatus;
                }
            }
            return UNKNOWN;
        }

        public static ResponseStatus fromStatusCode(int statusCode) {
            for (ResponseStatus responseStatus : ResponseStatus.values()) {
                if (responseStatus.statusCode == statusCode) {
                    return responseStatus;
                }
            }
            return UNKNOWN;
        }

        private final int statusCode;
        private final String statusText;

        ResponseStatus(final int statusCode, final String statusText) {
            this.statusCode = statusCode;
            this.statusText = statusText;
        }

        public boolean notAllowed() {
            return statusCode == _401.statusCode || statusCode == _403.statusCode;
        }

        public int statusCode() {
            return statusCode;
        }

        public String statusText() {
            return statusText;
        }
    }
}
