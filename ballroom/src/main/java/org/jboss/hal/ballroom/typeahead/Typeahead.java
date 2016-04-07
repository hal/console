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
package org.jboss.hal.ballroom.typeahead;

import java.util.List;

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.events.JsEvent;
import elemental.js.json.JsJsonObject;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.DmrPayloadProcessor;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.Constants;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.dispatch.Dispatcher.APPLICATION_DMR_ENCODED;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_NAME;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_VALUE;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;

/**
 * A type ahead engine based on <a href="https://twitter.github.io/typeahead.js/">typeahead.js</a> ready to be used
 * with form items.
 *
 * @see <a href="https://twitter.github.io/typeahead.js/">https://twitter.github.io/typeahead.js/</a>
 */
public class Typeahead implements SuggestHandler, Attachable {

    public static class Builder {

        private final Operation operation;
        private final ResultProcessor resultProcessor;
        private final Identifier identifier;
        private Templates.SuggestionTemplate suggestion;
        private Display display;
        private DataTokenizer dataTokenizer;

        public Builder(final Operation operation, final ResultProcessor resultProcessor,
                final Identifier identifier) {
            this.operation = operation;
            this.resultProcessor = resultProcessor;
            this.identifier = identifier;
        }

        public Builder suggestion(Templates.SuggestionTemplate suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public Builder display(Display display) {
            this.display = display;
            return this;
        }

        public Builder dataTokenizer(DataTokenizer dataTokenizer) {
            this.dataTokenizer = dataTokenizer;
            return this;
        }

        public Typeahead build() {
            return new Typeahead(this);
        }
    }


    @JsFunction
    @FunctionalInterface
    public interface ChangeListener {

        void onSelect(JsEvent event);
    }


    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void focus();

        public native void bind(String event, ChangeListener listener);

        public native void typeahead(Options options, Dataset dataset);

        public native String typeahead(String method);

        public native void typeahead(String method, String value);

        @JsMethod(name = "typeahead")
        public native void typeaheadClose(String method);

        @JsOverlay
        public final void onChange(ChangeListener listener) {
            bind(CHANGE_EVENT, listener);
        }

        @JsOverlay
        public final String getValue() {
            return typeahead(VAL);
        }

        @JsOverlay
        public final void setValue(String value) {
            typeahead(VAL, value);
        }

        @JsOverlay
        public final void close() {
            typeaheadClose(CLOSE);
        }
    }


    public static final String WHITESPACE = "\\s+";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String CLOSE = "close";
    private static final String CHANGE_EVENT = "typeahead:change";
    private static final String VAL = "val";

    private final Options options;
    private final Dataset dataset;
    private FormItem formItem;
    private final Bloodhound bloodhound;

    Typeahead(final Builder builder) {
        options = new Options();
        options.highlight = true;
        options.minLength = 1;

        RemoteOptions remoteOptions = new RemoteOptions();
        remoteOptions.url = Endpoints.INSTANCE.dmr();
        remoteOptions.prepare = (query, settings) -> {
            AjaxSettings.Accepts accepts = new AjaxSettings.Accepts();
            accepts.text = APPLICATION_DMR_ENCODED;

            AjaxSettings.XHRFields xhrFields = new AjaxSettings.XHRFields();
            xhrFields.withCredentials = true;

            settings.accepts = accepts;
            settings.beforeSend = (xhr, sttngs) ->
                    xhr.setRequestHeader(HEADER_MANAGEMENT_CLIENT_NAME, HEADER_MANAGEMENT_CLIENT_VALUE);
            settings.contentType = APPLICATION_DMR_ENCODED;
            settings.data = builder.operation.toBase64String();
            settings.dataType = "text"; //NON-NLS
            settings.method = POST.name();
            settings.xhrFields = xhrFields;
            return settings;
        };
        remoteOptions.transform = response -> {
            DmrPayloadProcessor payloadProcessor = new DmrPayloadProcessor();
            ModelNode payload = payloadProcessor.processPayload(POST, APPLICATION_DMR_ENCODED, response);
            if (!payload.isFailure()) {
                String query = Bridge.select(formItemSelector()).getValue();
                ModelNode result = payload.get(RESULT);
                //noinspection unchecked
                List<JsJsonObject> objects = (List<JsJsonObject>) builder.resultProcessor.process(query, result);
                return JsHelper.asJsArray(objects);
            }
            return JsArrayOf.create();
        };

        Bloodhound.Options bloodhoundOptions = new Bloodhound.Options();
        bloodhoundOptions.datumTokenizer = builder.dataTokenizer == null
                ? data -> builder.identifier.identify(data).split(WHITESPACE)
                : builder.dataTokenizer;
        bloodhoundOptions.queryTokenizer = query -> query.split(WHITESPACE);
        bloodhoundOptions.identify = builder.identifier;
        bloodhoundOptions.sufficient = Integer.MAX_VALUE; // we'd like to always have fresh results from the backend
        bloodhoundOptions.remote = remoteOptions;
        bloodhound = new Bloodhound(bloodhoundOptions);

        Templates templates = new Templates();
        templates.suggestion = builder.suggestion;
        //noinspection HardCodedStringLiteral
        templates.notFound = context -> "<div class=\"empty-message\">" +
                "<span class=\"pficon pficon-warning-triangle-o\"></span>" + CONSTANTS.noItems() +
                "</div>";

        dataset = new Dataset();
        dataset.source = bloodhound::search;
        dataset.async = true;
        dataset.limit = Integer.MAX_VALUE;
        dataset.display = builder.display == null ? builder.identifier::identify : builder.display;
        dataset.templates = templates;
    }

    @Override
    public void setFormItem(FormItem formItem) {
        this.formItem = formItem;
    }

    @Override
    public void attach() {
        Bridge.select(formItemSelector()).typeahead(options, dataset);
    }

    public void showAll() {
        Bridge bridge = Bridge.select(formItemSelector());
        bridge.setValue(SHOW_ALL_VALUE);
        bridge.focus();
    }

    @Override
    public void close() {
        Bridge.select(formItemSelector()).close();
        Element menu = Browser.getDocument()
                .querySelector(formItemSelector() + " .twitter-typeahead .tt-menu.tt-open"); //NON-NLS
        Elements.setVisible(menu, false);
    }

    public void clearRemoteCache() {
        if (bloodhound != null) {
            bloodhound.clearRemoteCache();
        }
    }

    private FormItem formItem() {
        if (formItem == null) {
            throw new IllegalStateException(
                    "No form item assigned. Please call Typeahead.setFormItem(FormItem) before using this as a SuggestHandler.");
        }
        return formItem;
    }

    private String formItemSelector() {
        return "#" + formItem().getId(EDITING);
    }
}
