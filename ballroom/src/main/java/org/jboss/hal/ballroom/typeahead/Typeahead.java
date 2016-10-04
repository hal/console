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

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.events.JsEvent;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.ballroom.typeahead.Templates.SuggestionTemplate;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.DmrPayloadProcessor;
import org.jboss.hal.resources.Constants;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.dispatch.Dispatcher.APPLICATION_DMR_ENCODED;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_NAME;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_VALUE;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * A suggest handler implementation based on <a href="https://twitter.github.io/typeahead.js/">typeahead.js</a> ready
 * to be used with form items.
 *
 * @see <a href="https://twitter.github.io/typeahead.js/">https://twitter.github.io/typeahead.js/</a>
 */
public abstract class Typeahead implements SuggestHandler, Attachable {

    @JsFunction
    @FunctionalInterface
    public interface ChangeListener {

        void onChange(JsEvent event);
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


    protected static final String WHITESPACE = "\\s+";
    private static final String CHANGE_EVENT = "typeahead:change";
    private static final String CLOSE = "close";
    private static final String VAL = "val";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    @NonNls private static final Logger logger = LoggerFactory.getLogger(Typeahead.class);

    protected Options options;
    protected Bloodhound bloodhound;
    protected Dataset dataset;
    private FormItem formItem;


    // ------------------------------------------------------ public API

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
        bridge.focus();
        bridge.setValue("");
        bridge.setValue(SHOW_ALL_VALUE);
    }

    @Override
    public void close() {
        Bridge.select(formItemSelector()).close();
        Element menu = Browser.getDocument()
                .querySelector(formItemSelector() + " .twitter-typeahead .tt-menu.tt-open"); //NON-NLS
        Elements.setVisible(menu, false);
    }


    // ------------------------------------------------------ helper methods

    protected Options initOptions() {
        Options options = new Options();
        options.highlight = true;
        options.minLength = 1;
        return options;
    }

    protected Bloodhound initBloodhound(final Identifier identifier, final DataTokenizer dataTokenizer,
            final OperationSupplier operation, final ResultProcessor resultProcessor) {

        RemoteOptions remoteOptions = new RemoteOptions();
        remoteOptions.url = Endpoints.INSTANCE.dmr();
        remoteOptions.cache = false;
        remoteOptions.ttl = 0;
        remoteOptions.prepare = (query, settings) -> {
            AjaxSettings.Accepts accepts = new AjaxSettings.Accepts();
            accepts.text = APPLICATION_DMR_ENCODED;

            AjaxSettings.XHRFields xhrFields = new AjaxSettings.XHRFields();
            xhrFields.withCredentials = true;

            settings.accepts = accepts;
            settings.beforeSend = (xhr, sttngs) -> {
                sttngs.data = operation.get().toBase64String();
                xhr.setRequestHeader(HEADER_MANAGEMENT_CLIENT_NAME, HEADER_MANAGEMENT_CLIENT_VALUE);
            };
            settings.error = (xhr, textStatus, errorThrown) -> {
                String details = errorThrown;
                ModelNode node = ModelNode.fromBase64(xhr.getResponseText());
                if (node.isFailure()) {
                    details = node.getFailureDescription();
                }
                logger.error("Unable to process typeahead operation on form item {}: {}", formItem().getId(EDITING),
                        details);
            };
            settings.contentType = APPLICATION_DMR_ENCODED;
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
                return resultProcessor.process(query, result);
            }
            return JsArrayOf.create();
        };

        Bloodhound.Options bloodhoundOptions = new Bloodhound.Options();
        bloodhoundOptions.datumTokenizer = dataTokenizer;
        bloodhoundOptions.queryTokenizer = query -> query.split(WHITESPACE);
        bloodhoundOptions.identify = identifier;
        bloodhoundOptions.sufficient = Integer.MAX_VALUE; // we'd like to always have fresh results from the backend
        bloodhoundOptions.remote = remoteOptions;
        return new Bloodhound(bloodhoundOptions);
    }

    protected Dataset initDataset(final Display display, final SuggestionTemplate suggestionTemplate) {
        Dataset dataset = new Dataset();
        dataset.async = true;
        dataset.display = display;
        dataset.limit = Integer.MAX_VALUE;
        dataset.source = bloodhound::search;
        dataset.templates = initTemplates();
        dataset.templates.suggestion = suggestionTemplate;
        return dataset;
    }

    Templates initTemplates() {
        Templates templates = new Templates();
        //noinspection HardCodedStringLiteral
        templates.notFound = context -> "<div class=\"empty-message\">" +
                "<span class=\"" + pfIcon("warning-triangle-o") + "\"></span>" + CONSTANTS.noItems() +
                "</div>";
        return templates;
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
