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
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.DmrPayloadProcessor;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.resources.Constants;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.typeahead.Typeahead.StaticBuilder.ID;
import static org.jboss.hal.ballroom.typeahead.Typeahead.StaticBuilder.ITEM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.dispatch.Dispatcher.APPLICATION_DMR_ENCODED;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_NAME;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_VALUE;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;

/**
 * A suggest handler implementation based on <a href="https://twitter.github.io/typeahead.js/">typeahead.js</a> ready
 * to be used with form items.
 * <p>
 * Choose one of the nested builders or {@link TypeaheadProvider} to create an instance.
 *
 * @see <a href="https://twitter.github.io/typeahead.js/">https://twitter.github.io/typeahead.js/</a>
 */
public class Typeahead implements SuggestHandler, Attachable {

    private static abstract class GenericBuilder<B extends GenericBuilder<B>> {

        final Identifier identifier;
        Templates.SuggestionTemplate suggestion;
        Display display;
        DataTokenizer dataTokenizer;

        protected GenericBuilder(final Identifier identifier) {this.identifier = identifier;}

        public B suggestion(Templates.SuggestionTemplate suggestion) {
            this.suggestion = suggestion;
            return that();
        }

        public B display(Display display) {
            this.display = display;
            return that();
        }

        public B dataTokenizer(DataTokenizer dataTokenizer) {
            this.dataTokenizer = dataTokenizer;
            return that();
        }

        protected abstract B that();
    }


    /**
     * Builder which uses a list of static items for the suggestions.
     */
    public static class StaticBuilder extends GenericBuilder<StaticBuilder> {

        static final String ID = "id";
        static final String ITEM = "item";

        private final List<String> items;

        public StaticBuilder(final List<String> items) {
            super(data -> String.valueOf(data.getNumber(ID)));
            this.items = items;
        }

        public Typeahead build() {
            return new Typeahead(this);
        }

        @Override
        protected StaticBuilder that() {
            return this;
        }
    }


    /**
     * Builder which uses the provided operation together with the result processor to suggest items.
     */
    public static class OperationBuilder extends GenericBuilder<OperationBuilder> {

        private final Operation operation;
        private final ResultProcessor resultProcessor;

        public OperationBuilder(final Operation operation, final ResultProcessor resultProcessor,
                final Identifier identifier) {
            super(identifier);
            this.operation = operation;
            this.resultProcessor = resultProcessor;
        }

        @Override
        protected OperationBuilder that() {
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


    static final String WHITESPACE = "\\s+";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String CLOSE = "close";
    private static final String CHANGE_EVENT = "typeahead:change";
    private static final String VAL = "val";

    private final Options options;
    private final Bloodhound bloodhound;
    private final Dataset dataset;
    private FormItem formItem;

    private Typeahead(final StaticBuilder builder) {
        options = initOptions();

        int index = 0;
        JsArrayOf<JsJsonObject> items = JsArrayOf.create();
        for (String item : builder.items) {
            JsJsonObject object = JsJsonObject.create();
            object.put(ID, index);
            object.put(ITEM, item);
            items.push(object);
            index++;
        }

        Bloodhound.Options bloodhoundOptions = new Bloodhound.Options();
        bloodhoundOptions.datumTokenizer = builder.dataTokenizer == null
                ? data -> data.getString(ITEM).split(WHITESPACE)
                : builder.dataTokenizer;
        bloodhoundOptions.queryTokenizer = query -> query.split(WHITESPACE);
        bloodhoundOptions.identify = builder.identifier;
        bloodhoundOptions.local = items;
        bloodhound = new Bloodhound(bloodhoundOptions);

        dataset = new Dataset();
        dataset.async = false;
        dataset.display = builder.display == null ? data -> data.getString(ITEM) : builder.display;
        dataset.source = (query, syncCallback, asyncCallback) -> {
            if (SHOW_ALL_VALUE.equals(query)) {
                syncCallback.sync(items);
            } else {
                bloodhound.search(query, syncCallback, asyncCallback);
            }
        };
        dataset.templates = initTemplates(builder);
    }

    private Typeahead(final OperationBuilder builder) {
        this.options = initOptions();

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
                return builder.resultProcessor.process(query, result);
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

        dataset = new Dataset();
        dataset.async = true;
        dataset.display = builder.display == null ? builder.identifier::identify : builder.display;
        dataset.limit = Integer.MAX_VALUE;
        dataset.source = bloodhound::search;
        dataset.templates = initTemplates(builder);
    }

    private Options initOptions() {
        Options options = new Options();
        options.highlight = true;
        options.minLength = 1;
        return options;
    }

    private <B extends GenericBuilder<B>> Templates initTemplates(GenericBuilder<B> builder) {
        Templates templates = new Templates();
        templates.suggestion = builder.suggestion;
        //noinspection HardCodedStringLiteral
        templates.notFound = context -> "<div class=\"empty-message\">" +
                "<span class=\"pficon pficon-warning-triangle-o\"></span>" + CONSTANTS.noItems() +
                "</div>";
        return templates;
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
