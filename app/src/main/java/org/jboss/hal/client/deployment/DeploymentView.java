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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.AddOnlyStateMachine;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.DefaultForm;
import org.jboss.hal.ballroom.form.ExistingModelStateMachine;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.ListItem;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.PasswordItem;
import org.jboss.hal.ballroom.form.PropertiesItem;
import org.jboss.hal.ballroom.form.SingleSelectBoxItem;
import org.jboss.hal.ballroom.form.TextAreaItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.typeahead.Typeahead;
import org.jboss.hal.client.bootstrap.endpoint.Endpoint;
import org.jboss.hal.client.bootstrap.endpoint.EndpointResources;
import org.jboss.hal.core.PatternFlyViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.model.Operation.Builder;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import javax.inject.Inject;
import java.util.Arrays;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.resources.Names.NAME_KEY;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class DeploymentView extends PatternFlyViewImpl implements DeploymentPresenter.MyView {

    class SampleForm extends DefaultForm<String> {

        protected SampleForm(final String id, boolean nested, final StatementContext statementContext) {
            super(id, nested ? new AddOnlyStateMachine() : new ExistingModelStateMachine(), SecurityContext.RWX);

            ResourceAddress address = AddressTemplate.of("/profile=full/subsystem=security")
                    .resolve(statementContext);
            org.jboss.hal.dmr.model.Operation operation = new Builder(READ_CHILDREN_NAMES_OPERATION, address)
                    .param(CHILD_TYPE, "security-domain")
                    .build();

            TextBoxItem name = new TextBoxItem(NAME_KEY, "Name");
            name.setRequired(true);
            name.setExpressionAllowed(false);
            name.registerSuggestHandler(new Typeahead.ReadChildrenNamesBuilder(operation).build());

            TextBoxItem formula = new TextBoxItem("formula", "Formula");
            formula.addValidationHandler(value -> "${magic}".equals(value) ?
                    ValidationResult.OK :
                    ValidationResult.invalid("Please provide the magic expression"));
            NumberItem age = new NumberItem("age", "Age", null);
            age.setRestricted(true);

            ListItem security = new ListItem("security", "Security");
            security.registerSuggestHandler(new Typeahead.ReadChildrenNamesBuilder(operation).build());

            addFormItem(name, formula, new PasswordItem("password", "Password"), age,
                    new TextAreaItem("hobbies", "Hobbies"),
                    new SingleSelectBoxItem("color", "Favorite Color", Arrays.asList("Red", "Green", "Blue")),
                    new ListItem("tags", "Tags"),
                    security,
                    new PropertiesItem("properties", "Properties"));
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
            addHelp("Security", "Security settings");
            addHelp("Tags", "Tags, tags, tags");
            addHelp("Properties", "Enter some key=value pairs");
        }
    }


    private final Dialog dialog;
    private final Form<Endpoint> endpointForm;
    private DeploymentPresenter presenter;

    @Inject
    public DeploymentView(StatementContext statementContext) {

        SampleForm sampleForm = new SampleForm("deployment", false, statementContext);
        registerAttachable(sampleForm);

        SampleForm dialogForm = new SampleForm("dialog", true, statementContext);
        Element dialogBody = new Elements.Builder().p().innerText("A form inside a dialog").end()
                .add(dialogForm.asElement()).build();
        dialog = new Dialog.Builder("Sample Dialog").add(dialogBody).closeOnly().build();
        dialog.registerAttachable(dialogForm);

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
    public void setPresenter(final DeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final Endpoint endpoint) {
        endpointForm.view(endpoint);
    }
}
