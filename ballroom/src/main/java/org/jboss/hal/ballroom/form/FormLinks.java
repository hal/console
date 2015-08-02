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

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.State.*;

/**
 * @author Harald Pehl
 */
class FormLinks implements IsElement, LayoutSpec {

    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final Set<State> supportedStates;
    private final LinkedHashMap<String, String> helpTexts;

    private final Element root;
    private Element addLink;
    private Element editLink;
    private Element removeLink;
    private Element helpLink;

    FormLinks(final String formId,
            final EnumSet<State> supportedStates,
            final LinkedHashMap<String, String> helpTexts,
            final EventListener onAdd,
            final EventListener onEdit,
            final EventListener onUndefine) {

        this.supportedStates = supportedStates;
        this.helpTexts = helpTexts;

        String linksId = Id.generate(formId, "links");
        String helpId = Id.generate(formId, "help");

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
        Element helpContent = rootBuilder.referenceFor("helpContent");

        if (supports(EMPTY) && supports(EDIT)) {
            addLink = link(CONSTANTS.add(), "pficon pficon-add", onAdd);
            links.appendChild(addLink);
        }
        if (supports(VIEW) || supports(EDIT)) {
            editLink = link(CONSTANTS.edit(), "pficon pficon-edit", onEdit);
            links.appendChild(editLink);
        }
        if (supports(EDIT) && supports(EMPTY)) {
            removeLink = link(CONSTANTS.remove(), "pficon pficon-delete", onUndefine);
            links.appendChild(removeLink);
        }
        if (!helpTexts.isEmpty()) {
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
            for (Map.Entry<String, String> entry : helpTexts.entrySet()) {
                helpContent.appendChild(help(entry.getKey(), entry.getValue()));
            }
            links.appendChild(helpLink);
        }

        root = rootBuilder.build();
    }

    private Element link(String text, String css, EventListener onclick) {
        // @formatter:off
        return new Elements.Builder()
            .li()
                .a().css("clickable").on(click, onclick)
                    .start("i").css(css).end()
                    .span().css("form-link-label").innerText(text).end()
                .end()
            .end().build();
        // @formatter:on
    }

    private Element help(String label, String description) {
        // @formatter:off
        return new Elements.Builder()
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
    }

    @Override
    public Element asElement() {
        if (addLink == null && editLink == null && removeLink == null && helpTexts.isEmpty()) {
            Elements.setVisible(root, false);
        }
        return root;
    }

    void switchTo(State state) {
        if (supports(state)) {
            switch (state) {
                case EMPTY:
                    Elements.setVisible(addLink, true);
                    Elements.setVisible(editLink, false);
                    Elements.setVisible(removeLink, false);
                    Elements.setVisible(helpLink, false);
                    break;

                case VIEW:
                    Elements.setVisible(addLink, false);
                    Elements.setVisible(editLink, true);
                    Elements.setVisible(removeLink, true);
                    Elements.setVisible(helpLink, !helpTexts.isEmpty());
                    break;

                case EDIT:
                    Elements.setVisible(addLink, false);
                    Elements.setVisible(editLink, false);
                    Elements.setVisible(removeLink, false);
                    Elements.setVisible(helpLink, !helpTexts.isEmpty());
                    break;
            }
        }
    }

    private boolean supports(State state) {
        return supportedStates.contains(state);
    }
}
