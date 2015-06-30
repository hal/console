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
package org.jboss.hal.ballroom.form;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import org.jboss.hal.resources.HalConstants;

/**
 * @author Harald Pehl
 */
class FormHelp extends Composite implements FormLayout {

    interface Templates extends SafeHtmlTemplates {

        @Template("<a data-toggle=\"collapse\" href=\"#{0}\" class=\"pull-right\" aria-expanded=\"false\" aria-controls=\"{0}\"><i class=\"pficon pficon-help\"></i> {1}</a>")
        SafeHtml link(String formId, String label);

        @Template("<div class=\"form-group\">" +
                "  <label class=\"col-" + COLUMN_DISCRIMINATOR + "-" + LABEL_COLUMNS + " control-label\">{0}</label>" +
                "  <div class=\"col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS + "\"><p class=\"form-control-static\">{1}</p></div>" +
                "</div>")
        SafeHtml content(String label, String description);
    }


    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);
    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private final FlowPanel contentPanel;

    FormHelp(final String formId) {
        String id = formId + "-help";

        FlowPanel root = new FlowPanel();
        initWidget(root);
        root.addStyleName("form-horizontal form-help");

        HTML linkPanel = new HTML(TEMPLATES.link(id, CONSTANTS.help()));
        linkPanel.addStyleName("form-help-link clearfix");

        FlowPanel contentHolder = new FlowPanel();
        contentHolder.getElement().setId(id);
        contentHolder.addStyleName("form-help-content collapse");
        contentPanel = new FlowPanel();

        contentHolder.add(contentPanel);
        root.add(linkPanel);
        root.add(contentHolder);
    }

    void add(String label, String description) {
        contentPanel.add(new HTML(TEMPLATES.content(label, description)));
    }
}
