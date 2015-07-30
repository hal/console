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
package org.jboss.hal.client.homepage;

import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.ViewImpl;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.form.AbstractForm;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.PasswordItem;
import org.jboss.hal.ballroom.form.SelectBoxItem;
import org.jboss.hal.ballroom.form.TextAreaItem;
import org.jboss.hal.ballroom.form.TextBoxItem;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.jboss.hal.ballroom.form.Form.State.EDIT;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.VIEW;

/**
 * @author Harald Pehl
 */
@Templated("Homepage.html#homepage")
public abstract class HomepageView extends ViewImpl implements HomepagePresenter.MyView, IsElement {

    static class HomepageForm extends AbstractForm<String> {

        protected HomepageForm() {
            super("homepage-form", EMPTY, VIEW, EDIT);

            TextBoxItem name = new TextBoxItem("name", "Name");
            name.setExpressionAllowed(false);
            formItems.put("name", name);
            formItems.put("formula", new TextBoxItem("formula", "Formula"));
            formItems.put("password", new PasswordItem("password", "Password"));
            formItems.put("age", new NumberItem("age", "Age"));
            formItems.put("hobbies", new TextAreaItem("hobbies", "Hobbies"));
            formItems.put("color", new SelectBoxItem("color", "Favorite Color", Arrays.asList("Red", "Green", "Blue")));
            ButtonItem button = new ButtonItem("click", "Click Me");
            button.onClick(event -> Window.alert("Clicked ;-)"));
            formItems.put("click", button);

            addHelp("Name", "Your name");
            addHelp("Formula", "Try to enter an expression");
            addHelp("Password", "Top secret");
            addHelp("Age", "How old are you?");
            addHelp("Hobbies", "Things you like to do in your spare time");
            addHelp("Color", "Things you like to do in your spare time");
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

        @Override
        protected void undefineModel() {}
    }

    public static HomepageView create() {
        return new Templated_HomepageView();
    }

    @DataElement HomepageForm form = new HomepageForm();

    @PostConstruct
    void init() {
        initWidget(Elements.asWidget(asElement()));
    }
}
