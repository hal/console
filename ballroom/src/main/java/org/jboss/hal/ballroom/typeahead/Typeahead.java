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
import elemental.js.json.JsJsonObject;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.form.FormItem;
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
import static org.jboss.hal.resources.Names.VALUE;

/**
 * A type ahead engine based on <a href="https://twitter.github.io/typeahead.js/">typeahead.js</a> ready to be used
 * with {@code FormItem<String>}. Use one of the builders to setup an instance and call
 * {@link Typeahead#attach()} after the form item was attached to the DOM.
 *
 * @see <a href="https://twitter.github.io/typeahead.js/">https://twitter.github.io/typeahead.js/</a>
 */
public class Typeahead {

    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select(String selector);

        public native void typeahead(Options options, Dataset dataset);
    }


    public static class Builder {

        private final FormItem formItem;
        private final Operation operation;
        private final ResultProcessor resultProcessor;
        private final Identifier identifier;
        protected DataTokenizer dataTokenizer;
        protected Display display;

        public Builder(final FormItem formItem, final Operation operation,
                final ResultProcessor resultProcessor, final Identifier identifier) {
            this.formItem = formItem;
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

        public ReadChildrenNamesBuilder(final FormItem formItem, final Operation readChildrenNames) {
            super(formItem, readChildrenNames,
                    (query, result) -> {
                        List<ModelNode> children = result.asList();
                        JsArrayOf<JsJsonObject> objects = JsArrayOf.create();
                        for (ModelNode child : children) {
                            String value = child.asString();
                            if (query == null || query.equals("") || value.contains(query)) {
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


    static final Constants CONSTANTS = GWT.create(Constants.class);

    private final FormItem formItem;
    private final Options options;
    private final Dataset dataset;

    Typeahead(final Builder builder) {
        formItem = builder.formItem;

        options = new Options();
        options.highlight = true;
        options.minLength = 0;

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
                String query = String.valueOf(formItem.getValue());
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
        bloodhoundOptions.remote = remoteOptions;
        Bloodhound bloodhound = new Bloodhound(bloodhoundOptions);

        Dataset.Templates templates = new Dataset.Templates();
        //noinspection HardCodedStringLiteral
        templates.notFound = context -> "<div class=\"empty-message\">" +
                "<span class=\"pficon pficon-warning-triangle-o\"></span>" + CONSTANTS.noResults() +
                "</div>";

        dataset = new Dataset();
        dataset.name = IdBuilder.build(formItem.getId(EDITING), "typeahead");
        dataset.source = bloodhound::search;
        dataset.async = true;
        dataset.limit = Integer.MAX_VALUE;
        dataset.display = builder.display == null ? builder.identifier::identify : builder.display;
        dataset.templates = templates;
    }

    public void attach() {
        Bridge.select("#" + formItem.getId(EDITING)).typeahead(options, dataset);
    }
}
