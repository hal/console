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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.hal.ballroom.form.ButtonItem;
import org.jboss.hal.ballroom.form.ComboBoxItem;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.NumberItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.client.Presenter;
import org.jboss.hal.core.autobean.AutoBeanForm;
import org.jboss.hal.core.autobean.BeanFactory;
import org.jboss.hal.core.autobean.ManagementEndpoint;
import org.jboss.hal.security.Gatekeeper;
import org.slf4j.Logger;

import javax.inject.Inject;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
@Templated("#content")
@Page(path = "deployments")
//@RequiredResources(resources = "/deployment=*")
public class Deployments extends Presenter {

    @Inject Logger logger;
    @Inject BeanFactory beanFactory;
    @DataField FlowPanel formPanel = new FlowPanel();

    @Inject
    public Deployments(Gatekeeper gatekeeper) {
        super(gatekeeper);
    }

    @Override
    public void onBind() {
        TextBoxItem nameItem = new TextBoxItem("name", "Name");
        nameItem.setRequired(true);
        ComboBoxItem schemeItem = new ComboBoxItem("scheme", "Scheme", asList("http", "https"));
        TextBoxItem hostItem = new TextBoxItem("hostname", "Hostname");
        hostItem.addValidationHandler(value -> "localhost".equals(value) ? ValidationResult
                .invalid("Localhost does not work reliably. Please use 127.0.0.1 instead.") : ValidationResult.OK);
        hostItem.setRequired(true);
        NumberItem portItem = new NumberItem("port", "Port", 1, 22);
        ButtonItem pingItem = new ButtonItem("ping", "Ping");

        Form<ManagementEndpoint> form = new AutoBeanForm.Builder<>(beanFactory, ManagementEndpoint.class)
                .addItems(nameItem, schemeItem, hostItem, portItem, pingItem)
                .onSave((model, changedValues) -> Window.alert("Save not yet implemented"))
                .build();
        formPanel.add(form);

        pingItem.onClick(event -> {
            if (hostItem.isEmpty()) {
                form.invalidate("hostname", "No host given");
            } else {
                form.clearError("hostname");
                Window.alert("Ping successful");
            }
        });
    }

    @Override
    public void onReset() {
        logger.debug("{}.onReset()", Deployments.class.getSimpleName());
    }
}
