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

import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.AbstractForm;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.PasswordItem;
import org.jboss.hal.ballroom.form.SelectBoxItem;
import org.jboss.hal.ballroom.form.TextAreaItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.layout.LayoutBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import static org.jboss.hal.ballroom.form.Form.State.*;

/**
 * @author Harald Pehl
 */
public class DeploymentView extends ViewImpl implements DeploymentPresenter.MyView {

    class SampleForm extends AbstractForm<String> {

        protected SampleForm(final String id, final EnumSet<State> supportedStates) {
            super(id, supportedStates);

            TextBoxItem name = new TextBoxItem("name", "Name");
            name.setRequired(true);
            name.setExpressionAllowed(false);
            TextBoxItem formula = new TextBoxItem("formula", "Formula");
            formula.addValidationHandler(value -> "${magic}".equals(value) ?
                    ValidationResult.OK :
                    ValidationResult.invalid("Please provide the magic expression"));
            NumberItem age = new NumberItem("age", "Age");
            age.setRestricted(true);
            ButtonItem button = new ButtonItem("click", "Click Me");
            button.onClick(event -> dialog.show());

            addFormItem(name, formula, new PasswordItem("password", "Password"), age,
                    new TextAreaItem("hobbies", "Hobbies"),
                    new SelectBoxItem("color", "Favorite Color", Arrays.asList("Red", "Green", "Blue")),
                    button);

            addHelp("Name", "Your name");
            addHelp("Formula", "Try to enter an expression");
            addHelp("Password", "Top secret");
            addHelp("Age", "How old are you?");
            addHelp("Hobbies", "Things you like to do in your spare time");
            addHelp("Color", "What's your favorite color?");
        }

        @Override
        public String newModel() {
            return "n/a";
        }

        @Override
        public Map<String, Object> getChangedValues() {
            return Collections.emptyMap();
        }

        @Override
        protected void updateModel(final Map<String, Object> changedValues) {}
    }

    private Dialog dialog;

    public DeploymentView() {
        SampleForm dialogForm = new SampleForm("dialog", EnumSet.of(EDIT));
        Element dialogBody = new Elements.Builder().p().innerText("A form inside a dialog").end()
                .add(dialogForm.asElement()).build();
        dialog = new Dialog.Builder("sample-dialog", "Sample Dialog").add(dialogBody).closeOnly().build();

        Element element = new LayoutBuilder()
                .header("Sample Form")
                .add(new SampleForm("deployment", EnumSet.of(EMPTY, VIEW, EDIT)).asElement())
                        .build();
        initWidget(Elements.asWidget(element));
    }
}
