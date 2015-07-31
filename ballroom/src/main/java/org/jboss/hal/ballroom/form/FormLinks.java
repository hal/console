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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.ballroom.LayoutSpec;
import org.jboss.hal.ballroom.form.Form.State;
import org.jboss.hal.resources.HalConstants;

import java.util.Set;

import static org.jboss.hal.ballroom.form.Form.State.*;

/**
 * @author Harald Pehl
 */
public class FormLinks implements IsElement, LayoutSpec {

    static class Builder {

        private final String formId;
        private final Set<State> supportedStates;
        private EventListener onAdd;
        private EventListener onEdit;
        private EventListener onUndefine;

        Builder(final String formId, final Set<State> supportedStates) {
            this.formId = formId;
            this.supportedStates = supportedStates;
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

    private final Set<State> supportedStates;

    private final Element root;
    private final Element helpLink;
    private final Element helpContent;

    private elemental.html.LIElement addElement;
    private elemental.html.LIElement editElement;
    private elemental.html.LIElement removeElement;

    public FormLinks(final Builder builder) {
        this.supportedStates = builder.supportedStates;

        String linksId = Id.generate(builder.formId, "links");
        String helpId = Id.generate(builder.formId, "help");

        // @formatter:off
        Elements.Builder rootBuilder = new Elements.Builder()
            .div().css("form form-horizontal")
                .ul().id(linksId).css("form-links clearfix").rememberAs("links").end()
                .div().id(helpId).css("form-help-content collapse")
                    .div().rememberAs("helpContent").end()
                .end()
            .end();
        // @formatter:on

        Element links = rootBuilder.referenceFor("links");
        helpContent = rootBuilder.referenceFor("helpContent");

        // EMPTY -> EDIT?
        if (supports(EMPTY) && supports(EDIT)) {
            addElement = new Elements.Builder()
                    .li().rememberAs("add")
                    .a().css("clickable")
                    .start("i").css("pficon pficon-add").end()
                    .span().css("form-link-label").innerText(CONSTANTS.add()).end()
                    .end()
                    .end().build();
            if (builder.onAdd != null) {
                addElement.setOnclick(builder.onAdd);
            }
            links.appendChild(addElement);
        }

        // VIEW -> EDIT?
        if (supports(VIEW) || supports(EDIT)) {
            editElement = new Elements.Builder()
                    .li().rememberAs("edit")
                    .a().css("clickable")
                    .start("i").css("pficon pficon-edit").end()
                    .span().css("form-link-label").innerText(CONSTANTS.edit()).end()
                    .end()
                    .end().build();
            if (builder.onEdit != null) {
                editElement.setOnclick(builder.onEdit);
            }
            links.appendChild(editElement);
        }

        // EDIT -> EMPTY
        if (supports(EDIT) && supports(EMPTY)) {
            removeElement = new Elements.Builder()
                    .li().rememberAs("remove")
                    .a().css("clickable")
                    .start("i").css("pficon pficon-delete").end()
                    .span().css("form-link-label").innerText(CONSTANTS.remove()).end()
                    .end()
                    .end().build();
            if (builder.onUndefine != null) {
                removeElement.setOnclick(builder.onUndefine);
            }
            links.appendChild(removeElement);
        }

        // @formatter:off
        helpLink = new Elements.Builder()
            .li().css("pull-right")
                .a().attr("href", "#" + helpId + "")
                        .data("toggle", "collapse")
                        .aria("expanded", "false")
                        .aria("controls", helpId)
                    .start("i").css("pficon pficon-help").end()
                    .span().css("form-link-label").innerText(CONSTANTS.help()).end()
                .end()
            .end().build();
        // @formatter:on
        links.appendChild(helpLink);

        root = rootBuilder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    void switchTo(State state) {
        if (supports(state)) {
            switch (state) {
                case EMPTY:
                    Elements.setVisible(addElement, true);
                    Elements.setVisible(editElement, false);
                    Elements.setVisible(removeElement, false);
                    Elements.setVisible(helpLink, false);
                    break;

                case VIEW:
                    Elements.setVisible(addElement, false);
                    Elements.setVisible(editElement, true);
                    Elements.setVisible(removeElement, true);
                    Elements.setVisible(helpLink, containsHelp());
                    break;

                case EDIT:
                    Elements.setVisible(addElement, false);
                    Elements.setVisible(editElement, false);
                    Elements.setVisible(removeElement, false);
                    Elements.setVisible(helpLink, containsHelp());
                    break;
            }
        }
    }

    void addHelpText(String label, String description) {
        // @formatter:off
        Element content = new Elements.Builder()
            .div().css("form-group")
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

    private boolean supports(State state) {
        return supportedStates.contains(state);
    }

    private boolean containsHelp() {
        return helpContent.getChildren().getLength() > 0;
    }
}
