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

import java.util.Collection;
import java.util.List;
import javax.inject.Provider;

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
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
import org.jboss.hal.ballroom.typeahead.Templates.SuggestionTemplate;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.dispatch.DmrPayloadProcessor;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singleton;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.APPLICATION_DMR_ENCODED;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_NAME;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HEADER_MANAGEMENT_CLIENT_VALUE;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;

/**
 * A suggest handler implementation based on <a href="https://twitter.github.io/typeahead.js/">typeahead.js</a> ready
 * to be used with form items.
 *
 * @see <a href="https://twitter.github.io/typeahead.js/">https://twitter.github.io/typeahead.js/</a>
 */
public class Typeahead implements SuggestHandler, Attachable {

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


    public static final String WHITESPACE = "\\s+";

    private static final String CHANGE_EVENT = "typeahead:change";
    private static final String CLOSE = "close";
    private static final String ID = "id";
    private static final String ITEM = "item";
    private static final String VAL = "val";

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Logger logger = LoggerFactory.getLogger(Typeahead.class);

    private final Options options;
    private final Bloodhound bloodhound;
    private final Dataset dataset;
    private FormItem formItem;


    // ------------------------------------------------------ new instance based on static items

    /**
     * Creates a typeahead instance based on a list of static items.
     */
    public Typeahead(final Collection<String> items) {
        options = initOptions();

        int index = 0;
        JsArrayOf<JsJsonObject> jsItems = JsArrayOf.create();
        for (String item : items) {
            JsJsonObject object = JsJsonObject.create();
            object.put(ID, index);
            object.put(ITEM, item);
            jsItems.push(object);
            index++;
        }

        Bloodhound.Options bloodhoundOptions = new Bloodhound.Options();
        bloodhoundOptions.datumTokenizer = data -> data.getString(ITEM).split(WHITESPACE);
        bloodhoundOptions.queryTokenizer = query -> query.split(WHITESPACE);
        bloodhoundOptions.identify = data -> String.valueOf(data.getNumber(ID));
        bloodhoundOptions.local = jsItems;
        bloodhound = new Bloodhound(bloodhoundOptions);

        dataset = new Dataset();
        dataset.async = false;
        dataset.display = data -> data.getString(ITEM);
        dataset.source = (query, syncCallback, asyncCallback) -> {
            if (SHOW_ALL_VALUE.equals(query)) {
                syncCallback.sync(jsItems);
            } else {
                bloodhound.search(query, syncCallback, asyncCallback);
            }
        };
        dataset.templates = initTemplates();
    }


    // ------------------------------------------------------ new instance based on address templates

    /**
     * Creates a typeahead instance based one ore multiple address templates.
     */
    public Typeahead(final AddressTemplate template, final StatementContext statementContext) {
        this(singleton(template), statementContext);
    }

    public Typeahead(final Iterable<AddressTemplate> templates, final StatementContext statementContext) {
        verifyTemplates(templates);

        ResultProcessor resultProcessor;
        Identifier identifier;
        DataTokenizer dataTokenizer;
        Display display;
        SuggestionTemplate suggestionTemplate;

        int numberOfTemplates = Iterables.size(templates);
        if (numberOfTemplates == 1) {
            AddressTemplate template = templates.iterator().next();
            int wildcards = Iterables.size(Splitter.on('*').split(template.toString())) - 1;
            if (wildcards == 0 || (wildcards == 1 && "*".equals(template.lastValue()))) {
                resultProcessor = new NamesResultProcessor();
                identifier = data -> data.getString(NAME);
                dataTokenizer = data -> data.getString(NAME).split(WHITESPACE);
                display = data -> data.getString(NAME);
                suggestionTemplate = null;

            } else {
                resultProcessor = new NestedResultProcessor(false);
                identifier = new NestedIdentifier();
                dataTokenizer = new NestedTokenizer();
                display = data -> data.getString(NAME);
                suggestionTemplate = new NestedSuggestionTemplate();
            }

        } else {
            resultProcessor = new NestedResultProcessor(true);
            identifier = new NestedIdentifier();
            dataTokenizer = new NestedTokenizer();
            display = data -> data.getString(NAME);
            suggestionTemplate = new NestedSuggestionTemplate();
        }

        options = initOptions();
        bloodhound = initBloodhound(identifier, dataTokenizer, resultProcessor, () -> {
                    //noinspection Guava
                    List<Operation> operations = FluentIterable.from(templates)
                            .transform(template -> template.resolve(statementContext))
                            .transform(address -> operation(address, numberOfTemplates))
                            .toList();

                    return operations.size() == 1 ? operations.get(0) : new Composite(operations);
                }
        );
        dataset = initDataset(display, suggestionTemplate);
    }

    private Operation operation(ResourceAddress address, int numberOfTemplates) {
        Operation operation;

        int wildcards = 0;
        if (address.isDefined()) {
            for (Property property : address.asPropertyList()) {
                if ("*".equals(property.getValue().asString())) {
                    wildcards++;
                }
            }
        }

        if (numberOfTemplates == 1 &&
                (wildcards == 0 || (wildcards == 1 && "*".equals(address.lastValue())))) {
            ResourceAddress parent = address.getParent();
            String childName = address.lastName();
            operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, parent)
                    .param(CHILD_TYPE, childName).build();

        } else {
            // The address is something like /foo=*/bar=*
            // Would be nice if we could use
            // /foo=*:read-children-names(child-type=bar)
            // but it returns an empty list, so we're using
            // /foo=*/bar=*:read-resource
            // which makes parsing the response more complicated
            operation = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(ATTRIBUTES_ONLY, true)
                    .param(INCLUDE_ALIASES, false)
                    .param(INCLUDE_DEFAULTS, false)
                    .param(INCLUDE_RUNTIME, false)
                    .param(PROXIES, false)
                    .build();
        }

        return operation;
    }


    // ------------------------------------------------------ new instance based on custom implementations

    protected Typeahead(final Identifier identifier, final DataTokenizer dataTokenizer,
            final ResultProcessor resultProcessor, final Display display, final SuggestionTemplate suggestionTemplate,
            final Provider<Operation> operation) {

        options = initOptions();
        bloodhound = initBloodhound(identifier, dataTokenizer, resultProcessor, operation);
        dataset = initDataset(display, suggestionTemplate);
    }


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


    // ------------------------------------------------------ helper methods

    private void verifyTemplates(final Iterable<AddressTemplate> templates) {
        if (Iterables.isEmpty(templates)) {
            throw new IllegalArgumentException(
                    "Templates must not be empty in Typeahead(List<AddressTemplate>, StatementContext)");
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

    private Options initOptions() {
        Options options = new Options();
        options.highlight = true;
        options.minLength = 1;
        return options;
    }

    private Bloodhound initBloodhound(final Identifier identifier, final DataTokenizer dataTokenizer,
            final ResultProcessor resultProcessor, final Provider<Operation> operation) {

        RemoteOptions remoteOptions = new RemoteOptions();
        remoteOptions.url = Endpoints.INSTANCE.dmr();
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
                logger.error("Unable to process typeahead operation on form item {}: {}", //NON-NLS
                        formItem().getId(EDITING), details);
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

    private Dataset initDataset(final Display display, final SuggestionTemplate suggestionTemplate) {
        Dataset dataset = new Dataset();
        dataset.async = true;
        dataset.display = display;
        dataset.limit = Integer.MAX_VALUE;
        dataset.source = bloodhound::search;
        dataset.templates = initTemplates();
        dataset.templates.suggestion = suggestionTemplate;
        return dataset;
    }

    private Templates initTemplates() {
        Templates templates = new Templates();
        //noinspection HardCodedStringLiteral
        templates.notFound = context -> "<div class=\"empty-message\">" +
                "<span class=\"pficon pficon-warning-triangle-o\"></span>" + CONSTANTS.noItems() +
                "</div>";
        return templates;
    }
}
