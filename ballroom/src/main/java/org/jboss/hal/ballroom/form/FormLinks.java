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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.Operation.EDIT;
import static org.jboss.hal.ballroom.form.Form.Operation.RESET;

/**
 * @author Harald Pehl
 */
class FormLinks implements IsElement, LayoutSpec {

    private final static HalConstants CONSTANTS = GWT.create(HalConstants.class);

    private final StateMachine stateMachine;
    private final LinkedHashMap<String, String> helpTexts;

    private final Element root;
    private Element editLink;
    private Element resetLink;
    private Element helpLink;

    FormLinks(final String formId,
            final StateMachine stateMachine,
            final LinkedHashMap<String, String> helpTexts,
            final EventListener onEdit,
            final EventListener onReset) {

        this.stateMachine = stateMachine;
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

        if (stateMachine.supports(EDIT)) {
            editLink = link(CONSTANTS.edit(), "pficon pficon-edit", onEdit);
            links.appendChild(editLink);
        }
        if (stateMachine.supports(RESET)) {
            resetLink = link(CONSTANTS.reset(), "fa fa-file-o", onReset);
            resetLink.getDataset().setAt("toggle", "tooltip");
            resetLink.getDataset().setAt("placement", "right");
            resetLink.setTitle(CONSTANTS.form_reset_desc());

            links.appendChild(resetLink);
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
        if (editLink == null && resetLink == null && helpTexts.isEmpty()) {
            Elements.setVisible(root, false);
        }
        return root;
    }

    void switchTo(State state) {
        switch (state) {
            case READONLY:
                Elements.setVisible(editLink, stateMachine.supports(EDIT));
                Elements.setVisible(resetLink, stateMachine.supports(RESET));
                Elements.setVisible(helpLink, !helpTexts.isEmpty());
                break;

            case EDITING:
                Elements.setVisible(editLink, false);
                Elements.setVisible(resetLink, false);
                Elements.setVisible(helpLink, !helpTexts.isEmpty());
                break;
        }
    }
}
