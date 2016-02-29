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
package org.jboss.hal.ballroom.typeahead;

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

import java.util.List;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.dispatch.Dispatcher.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;

/**
 * A type ahead engine based on <a href="https://twitter.github.io/typeahead.js/">typeahead.js</a> ready to be used
 * with form items. Use one of the builders to setup an instance and call {@link Typeahead#attach()} after the form
 * item was attached to the DOM.
 * <p>
 * <pre>
 * ResourceAddress address = AddressTemplate.of("/socket-binding-group=standard-sockets")
 *         .resolve(statementContext);
 * Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
 *         .param(CHILD_TYPE, "socket-binding")
 *         .build();
 *
 * Form&lt;Foo&gt; form = ...;
 * form.getItem("foo").registerSuggestHandler(new Typeahead.ReadChildrenNamesBuilder(operation).build());
 * </pre>
 *
 * @see <a href="https://twitter.github.io/typeahead.js/">https://twitter.github.io/typeahead.js/</a>
 */
public class Typeahead implements SuggestHandler, Attachable {

    public static class Builder {

        private final Operation operation;
        private final ResultProcessor resultProcessor;
        private final Identifier identifier;
        protected DataTokenizer dataTokenizer;
        protected Display display;

        public Builder(final Operation operation, final ResultProcessor resultProcessor, final Identifier identifier) {
            this.operation = operation;
            this.resultProcessor = resultProcessor;
            this.identifier = identifier;
        }

        public Builder dataTokenizer(DataTokenizer dataTokenizer) {
            this.dataTokenizer = dataTokenizer;
            return this;
        }

        public Builder display(Display display) {
            this.display = display;
            return this;
        }

        public Typeahead build() {
            return new Typeahead(this);
        }
    }


    public static class ReadChildrenNamesBuilder extends Builder {

        public ReadChildrenNamesBuilder(final Operation readChildrenNames) {
            super(readChildrenNames,
                    (query, result) -> {
                        List<ModelNode> children = result.asList();
                        JsArrayOf<JsJsonObject> objects = JsArrayOf.create();
                        for (ModelNode child : children) {
                            String value = child.asString();
                            if (SHOW_ALL_VALUE.equals(query) || value.contains(query)) {
                                JsJsonObject object = JsJsonObject.create();
                                object.put(VALUE, value);
                                objects.push(object);
                            }
                        }
                        return objects;
                    },
                    data -> data.getString(VALUE));

            dataTokenizer = data -> data.getString(VALUE).split("\\s+"); //NON-NLS
            display = data -> data.getString(VALUE);
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


    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String CLOSE = "close";
    private static final String CHANGE_EVENT = "typeahead:change";
    private static final String VAL = "val";
    private static final String VALUE = "value";

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
                return builder.resultProcessor.process(query, result);
            }
            return JsArrayOf.<JsJsonObject>create();
        };

        Bloodhound.Options bloodhoundOptions = new Bloodhound.Options();
        bloodhoundOptions.datumTokenizer = builder.dataTokenizer == null
                ? data -> builder.identifier.identify(data).split("\\s+") //NON-NLS
                : builder.dataTokenizer;
        bloodhoundOptions.queryTokenizer = query -> query.split("\\s+"); //NON-NLS
        bloodhoundOptions.identify = builder.identifier;
        bloodhoundOptions.sufficient = Integer.MAX_VALUE; // we'd like to always have fresh results from the backend
        bloodhoundOptions.remote = remoteOptions;
        bloodhound = new Bloodhound(bloodhoundOptions);

        Dataset.Templates templates = new Dataset.Templates();
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

    public Dataset getDataset() {
        return dataset;
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
