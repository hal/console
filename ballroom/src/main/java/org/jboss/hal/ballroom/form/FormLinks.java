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
import elemental.dom.Element;
import elemental.events.EventListener;
import org.jboss.gwt.waiwai.Elements;
import org.jboss.gwt.waiwai.IsElement;
import org.jboss.hal.resources.HalConstants;

/**
 * @author Harald Pehl
 */
public class FormLinks implements IsElement, FormLayout {

    static class Builder {

        private final String formId;
        private final boolean supportsUndefine;
        private final boolean supportsHelp;
        private EventListener onAdd;
        private EventListener onEdit;
        private EventListener onUndefine;

        Builder(final String formId, final boolean supportsUndefine, final boolean supportsHelp) {
            this.formId = formId;
            this.supportsUndefine = supportsUndefine;
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


    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final boolean supportsHelp;

    private final Element root;
    private final elemental.html.LIElement addElement;
    private final elemental.html.LIElement editElement;
    private final elemental.html.LIElement removeElement;
    private Element helpContent;

    public FormLinks(final Builder flb) {
        this.supportsHelp = flb.supportsHelp;

        Elements.Builder rootBuilder = new Elements.Builder()
                .div().css("form form-horizontal")
                .ul().css("form-links clearfix").rememberAs("links").end()
                .end();
        Element links = rootBuilder.referenceFor("links");
        root = rootBuilder.build();

        addElement = new Elements.Builder()
                .li().rememberAs("add")
                .a().start("i").css("pficon pficon-add").end().innerText(CONSTANTS.add()).end()
                .end().build();
        if (flb.onAdd != null) {
            addElement.setOnclick(flb.onAdd);
        }

        editElement = new Elements.Builder()
                .li().rememberAs("edit")
                .a().start("i").css("pficon pficon-edit").end().innerText(CONSTANTS.edit()).end()
                .end().build();
        if (flb.onEdit != null) {
            editElement.setOnclick(flb.onEdit);
        }

        removeElement = new Elements.Builder()
                .li().rememberAs("remove")
                .a().start("i").css("pficon pficon-delete").end().innerText(CONSTANTS.remove()).end()
                .end().build();
        if (flb.onUndefine != null) {
            removeElement.setOnclick(flb.onUndefine);
        }

        if (flb.supportsUndefine) {
            links.appendChild(addElement);
            links.appendChild(editElement);
            links.appendChild(removeElement);
        } else {
            links.appendChild(editElement);
        }

        if (flb.supportsHelp) {
            String helpId = flb.formId + "-help";
            // @formatter:off
            Element helpLink = new Elements.Builder()
                .li().css("pull-right")
                    .a().attr("href", "#" + helpId + "")
                            .data("toggle", "collapse")
                            .aria("expanded", "false")
                            .aria("controls", helpId)
                        .start("i").css("pficon pficon-help").end()
                        .innerText(CONSTANTS.help())
                    .end()
                .end().build();
            // @formatter:on
            links.appendChild(helpLink);

            // @formatter:off
            Elements.Builder helpContentBuilder = new Elements.Builder()
                .div().id(helpId).css("form-help-content collapse")
                    .div().rememberAs("helpContent").end()
                .end();
            // @formatter:on
            helpContent = helpContentBuilder.referenceFor("helpContent");
            root.appendChild(helpContentBuilder.build());
        }
    }

    @Override
    public Element asElement() {
        return root;
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
        if (supportsHelp && helpContent != null) {
            // @formatter:off
            Element content = new Elements.Builder()
                .div().css("Form-group")
                    .label().css("col-" + COLUMN_DISCRIMINATOR + "-" + LABEL_COLUMNS + " control-label")
                        .innerText(label)
                    .end()
                    .div().css("col-" + COLUMN_DISCRIMINATOR + "-" + INPUT_COLUMNS)
                        .p().css("form-control-static")
                            .innerText(description)
                        .end()
                    .end()
                .end().build();
            // @formatter:on
            helpContent.appendChild(content);
        }
    }
}
