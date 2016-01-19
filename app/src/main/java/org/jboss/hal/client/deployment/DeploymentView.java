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
package org.jboss.hal.client.deployment;

import com.google.gwt.core.client.GWT;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import elemental.js.util.JsArrayOf;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.util.ArrayOf;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.AddOnlyStateMachine;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.DefaultForm;
import org.jboss.hal.ballroom.form.ExistingModelStateMachine;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.PasswordItem;
import org.jboss.hal.ballroom.form.SelectBoxItem;
import org.jboss.hal.ballroom.form.TextAreaItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.typeahead.AjaxSettings;
import org.jboss.hal.ballroom.typeahead.AsyncCallback;
import org.jboss.hal.ballroom.typeahead.Bloodhound;
import org.jboss.hal.ballroom.typeahead.Dataset;
import org.jboss.hal.ballroom.typeahead.RemoteOptions;
import org.jboss.hal.ballroom.typeahead.SyncCallback;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.client.bootstrap.endpoint.Endpoint;
import org.jboss.hal.client.bootstrap.endpoint.EndpointResources;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.DmrPayloadProcessor;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.jboss.hal.ballroom.PatternFly.$;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.POST;

/**
 * @author Harald Pehl
 */
public class DeploymentView extends ViewImpl implements DeploymentPresenter.MyView {

    class SampleForm extends DefaultForm<String> {

        final TextBoxItem name;

        protected SampleForm(final String id, boolean nested) {
            super(id, nested ? new AddOnlyStateMachine() : new ExistingModelStateMachine(), SecurityContext.RWX);

            name = new TextBoxItem("name", "Name");
            name.setRequired(true);
            name.setExpressionAllowed(false);
            TextBoxItem formula = new TextBoxItem("formula", "Formula");
            formula.addValidationHandler(value -> "${magic}".equals(value) ?
                    ValidationResult.OK :
                    ValidationResult.invalid("Please provide the magic expression"));
            NumberItem age = new NumberItem("age", "Age");
            age.setRestricted(true);

            addFormItem(name, formula, new PasswordItem("password", "Password"), age,
                    new TextAreaItem("hobbies", "Hobbies"),
                    new SelectBoxItem("color", "Favorite Color", Arrays.asList("Red", "Green", "Blue")));
            if (!nested) {
                ButtonItem button = new ButtonItem("click", "Click Me");
                button.onClick(event -> dialog.show());
                addFormItem(button);
            }

            addHelp("Name", "Your name");
            addHelp("Formula", "Try to enter an expression");
            addHelp("Password", "Top secret");
            addHelp("Age", "How old are you?");
            addHelp("Hobbies", "Things you like to do in your spare time");
            addHelp("Color", "What's your favorite color?");
        }
    }


    private final Endpoints endpoint;
    private final StatementContext statementContext;
    private final Dialog dialog;
    private final SampleForm sampleForm;
    private final Form<Endpoint> endpointForm;
    private DeploymentPresenter presenter;

    @Inject
    public DeploymentView(Endpoints endpoint, StatementContext statementContext) {
        this.endpoint = endpoint;
        this.statementContext = statementContext;

        sampleForm = new SampleForm("deployment", false);
        SampleForm dialogForm = new SampleForm("dialog", true);
        Element dialogBody = new Elements.Builder().p().innerText("A form inside a dialog").end()
                .add(dialogForm.asElement()).build();
        dialog = new Dialog.Builder("Sample Dialog").add(dialogBody).closeOnly().build();

        EndpointResources endpointResources = GWT.create(EndpointResources.class);
        endpointForm = new ModelNodeForm.Builder<Endpoint>("mbui-form", SecurityContext.RWX,
                StaticResourceDescription.from(endpointResources.endpoint())).build();
        endpointForm.setSaveCallback((form, changedValues) -> presenter.saveEndpoint(form.getModel()));

        // @formatter:off
        Element element = new LayoutBuilder()
            .startRow()
                .header("Sample Forms")
                .subheader("Custom Form")
                .add(sampleForm.asElement())
                .subheader("MBUI Form")
                .add(endpointForm.asElement())
            .endRow()
        .build();
        // @formatter:on
        initWidget(Elements.asWidget(element));

        sampleForm.view("foo");
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public void attach() {
        PatternFly.initComponents();

        DmrPayloadProcessor payloadProcessor = new DmrPayloadProcessor();
        ResourceAddress address = AddressTemplate.of("/profile=full/subsystem=security")
                .resolve(statementContext);
        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, address)
                .param(CHILD_TYPE, "security-domain")
                .build();

        Typeahead.Options typeaheadOptions = Typeahead.Defaults.get();

        RemoteOptions<JsonObject> remoteOptions = new RemoteOptions<>();
        remoteOptions.url = endpoint.dmr();
        remoteOptions.prepare = (query, settings) -> {
            AjaxSettings.Accepts accepts = new AjaxSettings.Accepts();
            accepts.text = "application/dmr-encoded";

            AjaxSettings.XHRFields xhrFields = new AjaxSettings.XHRFields();
            xhrFields.withCredentials = true;

            settings.accepts = accepts;
            settings.beforeSend = (xhr, s) -> {
                xhr.setRequestHeader("X-Management-Client-Name", "HAL");
            };
            settings.contentType = "application/dmr-encoded";
            settings.data = operation.toBase64String();
            settings.dataType = "text";
            settings.method = POST.name();
            settings.xhrFields = xhrFields;
            return settings;
        };
        remoteOptions.transform = response -> {
            ModelNode payload = payloadProcessor.processPayload(POST, "application/dmr-encoded", response);
            if (!payload.isFailure()) {
                String currentValue = sampleForm.name.getValue();
                ModelNode result = payload.get(RESULT);
                List<ModelNode> nodes = result.asList();
                ArrayOf<JsonObject> objects = JsArrayOf.create();
                for (ModelNode node : nodes) {
                    String value = node.asString();
                    if (currentValue == null || currentValue.equals("") || value.contains(currentValue)) {
                        JsonObject object = Json.createObject();
                        object.put("value", value);
                        objects.push(object);
                    }
                }
                return objects;
            }
            return JsArrayOf.<JsonObject>create();
        };

        Bloodhound.Options<JsonObject> bloodhoundOptions = new Bloodhound.Options<>();
        bloodhoundOptions.datumTokenizer = datum -> datum.getString("value").split("\\s+");
        bloodhoundOptions.queryTokenizer = query -> query.split("\\s+");
        bloodhoundOptions.identify = datum -> datum.getString("value");
        bloodhoundOptions.remote = remoteOptions;
        Bloodhound<JsonObject> bloodhound = new Bloodhound<>(bloodhoundOptions);

        Dataset.Templates templates = new Dataset.Templates();
        templates.notFound = context -> "<div class=\"empty-message\">" +
                "<span class=\"pficon pficon-warning-triangle-o\"></span>" +
                "This is the error message shown if the query did not return results." +
                "</div>";

        Dataset<JsonObject> dataset = new Dataset<>();
        dataset.name = "security-domains";
        //noinspection Convert2Lambda,Anonymous2MethodRef
        dataset.source = new Dataset.Source<JsonObject>() {
            @Override
            public void source(final String query, final SyncCallback<JsonObject> syncCallback,
                    final AsyncCallback<JsonObject> asyncCallback) {
                bloodhound.search(query, syncCallback, asyncCallback);
            }
        };
        dataset.async = true;
        dataset.limit = Integer.MAX_VALUE;
        dataset.display = datum -> datum.getString("value");
        dataset.templates = templates;

        $("#deployment-name-editing").typeahead(typeaheadOptions, dataset);
    }

    @Override
    public void setPresenter(final DeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final Endpoint endpoint) {
        endpointForm.view(endpoint);
    }
}
