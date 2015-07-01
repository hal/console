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
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import elemental.events.EventListener;
import org.jboss.hal.ballroom.Elements;
import org.jboss.hal.ballroom.IsElement;
import org.jboss.hal.resources.HalConstants;

/**
 * @author Harald Pehl
 */
public class FormLinks implements IsElement, FormLayout {

    static class Builder {

        private final String formId;
        private final boolean supportsHelp;
        private EventListener onAdd;
        private EventListener onEdit;
        private EventListener onUndefine;

        Builder(final String formId, final boolean supportsHelp) {
            this.formId = formId;
            this.supportsHelp = supportsHelp;
        }

        Builder onAdd(EventListener onAdd) {
            this.onAdd = onAdd;
            return this;
        }

        Builder onEdit(EventListener onEdit) {
            this.onEdit = onEdit;
            return this;
        }

        Builder onUndefine(EventListener onUndefine) {
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

    private final elemental.html.LIElement addElement;
    private final elemental.html.LIElement editElement;
    private final elemental.html.LIElement removeElement;
    private FlowPanel helpContentPanel;

    public FormLinks(final Builder flb) {
        // @formatter:off
        Elements.Builder eb = new Elements.Builder()
            .div().css("form form-horizontal")
                .ul().css("form-links clearfix").rememberAs("links")
                    .li().rememberAs("add")
                        .a().start("i").css("pficon pficon-add").end().innerText(CONSTANTS.add()).end()
                    .end()
                    .li().rememberAs("edit")
                        .a().start("i").css("pficon pficon-edit").end().innerText(CONSTANTS.edit()).end()
                    .end()
                    .li().rememberAs("remove")
                        .a().start("i").css("pficon pficon-delete").end().innerText(CONSTANTS.remove()).end()
                    .end()
                .end()
            .end();
        // @formatter:on

        addElement = eb.referenceFor("add");
        if (flb.onAdd != null) {
            addElement.setOnclick(flb.onAdd);
        }
        editElement = eb.referenceFor("edit");
        if (flb.onEdit != null) {
            editElement.setOnclick(flb.onEdit);
        }
        removeElement = eb.referenceFor("remove");
        if (flb.onUndefine != null) {
            removeElement.setOnclick(flb.onUndefine);
        }

//        @SafeHtmlTemplates.Template("<a data-toggle=\"collapse\" href=\"#{0}\" aria-expanded=\"false\" aria-controls=\"{0}\"><i class=\"pficon pficon-help\"></i> {1}</a>")
//        SafeHtml helpLink(String formId, String label);

        if (flb.supportsHelp) {
            String helpId = flb.formId + "-help";
            // @formatter:off
            new Elements.Builder()
                .li().css("pull-right")
                    .a().attr("href", "#" + helpId + "")
                            .data("toggle", "collapse")
                            .aria("expanded", "false")
                            .aria("controls", helpId)
                        .start("i").css("pficon pficon-help").end()
                .end()
                .build();
            // @formatter:on
        }

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
                Elements.setVisible(addElement, true);
                Elements.setVisible(editElement, false);
                Elements.setVisible(removeElement, false);
                break;

            case VIEW:
                Elements.setVisible(addElement, false);
                Elements.setVisible(editElement, true);
                Elements.setVisible(removeElement, true);
                break;

            case EDIT:
                Elements.setVisible(addElement, false);
                Elements.setVisible(editElement, false);
                Elements.setVisible(removeElement, false);
                break;
        }
    }

    void addHelpText(String label, String description) {
        if (helpContentPanel != null) {
            helpContentPanel.add(new HTML(TEMPLATES.content(label, description)));
        }
    }
}
