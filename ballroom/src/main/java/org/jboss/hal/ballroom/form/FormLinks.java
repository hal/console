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
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.UIObject;
import org.jboss.hal.resources.HalConstants;

/**
 * @author Harald Pehl
 */
public class FormLinks extends Composite implements FormLayout {

    static class Builder {

        private final String formId;
        private final boolean supportsHelp;
        private ClickHandler onAdd;
        private ClickHandler onEdit;
        private ClickHandler onUndefine;

        Builder(final String formId, final boolean supportsHelp) {
            this.formId = formId;
            this.supportsHelp = supportsHelp;
        }

        Builder onAdd(ClickHandler onAdd) {
            this.onAdd = onAdd;
            return this;
        }

        Builder onEdit(ClickHandler onEdit) {
            this.onEdit = onEdit;
            return this;
        }

        Builder onUndefine(ClickHandler onUndefine) {
            this.onUndefine = onUndefine;
            return this;
        }

        FormLinks build() {
            return new FormLinks(this);
        }
    }


    interface Templates extends SafeHtmlTemplates {

        @Template("<i class=\"pficon pficon-add\"></i> {0}")
        SafeHtml addLink(String label);

        @Template("<i class=\"pficon pficon-edit\"></i> {0}")
        SafeHtml editLink(String label);

        @Template("<i class=\"pficon pficon-delete\"></i> {0}")
        SafeHtml undefineLink(String label);

        @Template("<a data-toggle=\"collapse\" href=\"#{0}\" aria-expanded=\"false\" aria-controls=\"{0}\"><i class=\"pficon pficon-help\"></i> {1}</a>")
        SafeHtml helpLink(String formId, String label);

        @Template("<div class=\"form-group\">" +
                "  <label class=\"col-" + COLUMN_DISCRIMINATOR + "-" + LABEL_COLUMNS + " control-label\">{0}</label>" +
                "  <div class=\"col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS + "\"><p class=\"form-control-static\">{1}</p></div>" +
                "</div>")
        SafeHtml content(String label, String description);
    }


    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);
    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private final LIElement addElement;
    private final LIElement editElement;
    private final LIElement removeElement;
    private FlowPanel helpContentPanel;

    public FormLinks(final Builder builder) {
        FlowPanel root = new FlowPanel();
        initWidget(root);
        root.addStyleName("form form-horizontal");

        UListElement links = Document.get().createULElement();
        links.addClassName("form-links clearfix");

        // add
        addElement = Document.get().createLIElement();
        UIObject.setVisible(addElement, false);
        Anchor addLink = new Anchor(TEMPLATES.addLink(CONSTANTS.add()));
        if (builder.onAdd != null) {
            addLink.addClickHandler(builder.onAdd);
        }
        addElement.appendChild(addLink.getElement());
        links.appendChild(addElement);

        // edit
        editElement = Document.get().createLIElement();
        UIObject.setVisible(editElement, false);
        Anchor editLink = new Anchor(TEMPLATES.editLink(CONSTANTS.edit()));
        if (builder.onEdit != null) {
            editLink.addClickHandler(builder.onEdit);
        }
        editElement.appendChild(editLink.getElement());
        links.appendChild(editElement);

        // remove
        removeElement = Document.get().createLIElement();
        UIObject.setVisible(removeElement, false);
        Anchor undefineLink = new Anchor(TEMPLATES.undefineLink(CONSTANTS.remove()));
        if (builder.onUndefine != null) {
            undefineLink.addClickHandler(builder.onUndefine);
        }
        removeElement.appendChild(undefineLink.getElement());
        links.appendChild(removeElement);

        // help
        FlowPanel helpContentHolder = null;
        if (builder.supportsHelp) {
            String helpId = builder.formId + "-help";
            LIElement helpElement = Document.get().createLIElement();
            helpElement.addClassName("pull-right");
            helpElement.setInnerSafeHtml(TEMPLATES.helpLink(helpId, CONSTANTS.help()));
            links.appendChild(helpElement);

            helpContentHolder = new FlowPanel();
            helpContentHolder.getElement().setId(helpId);
            helpContentHolder.addStyleName("form-help-content collapse");
            helpContentPanel = new FlowPanel();
            helpContentHolder.add(helpContentPanel);
        }

        root.getElement().appendChild(links);
        if (builder.supportsHelp) {
            root.add(helpContentHolder);
        }
    }

    void switchTo(Form.State state) {
        switch (state) {
            case EMPTY:
                UIObject.setVisible(addElement, true);
                UIObject.setVisible(editElement, false);
                UIObject.setVisible(removeElement, false);
                break;

            case VIEW:
                UIObject.setVisible(addElement, false);
                UIObject.setVisible(editElement, true);
                UIObject.setVisible(removeElement, true);
                break;

            case EDIT:
                UIObject.setVisible(addElement, false);
                UIObject.setVisible(editElement, false);
                UIObject.setVisible(removeElement, false);
                break;
        }
    }

    void addHelpText(String label, String description) {
        if (helpContentPanel != null) {
            helpContentPanel.add(new HTML(TEMPLATES.content(label, description)));
        }
    }
}
