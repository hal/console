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
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import elemental.events.EventListener;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.form.Form.State;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.UIConstants;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.Operation.EDIT;
import static org.jboss.hal.ballroom.form.Form.Operation.RESET;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class FormLinks<T> implements IsElement {

    private static final String LINKS = "links";
    private static final String HELP_CONTENT = "helpContent";
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final StateMachine stateMachine;
    private final LinkedHashMap<String, SafeHtml> helpTexts;

    private final Element root;
    private Element editLink;
    private Element resetLink;
    private Element helpLink;

    FormLinks(final String formId,
            final StateMachine stateMachine,
            final LinkedHashMap<String, SafeHtml> helpTexts,
            final EventListener onEdit,
            final EventListener onReset) {

        this.stateMachine = stateMachine;
        this.helpTexts = helpTexts;

        String linksId = IdBuilder.build(formId, LINKS);
        String helpId = IdBuilder.build(formId, "help");

        // @formatter:off
        Elements.Builder rootBuilder = new Elements.Builder()
            .div().css(form, formHorizontal)
                .ul().id(linksId).css(formLinks, clearfix).rememberAs(LINKS).end()
                .div().id(helpId).css(formHelpContent, collapse)
                    .div().rememberAs(HELP_CONTENT).end()
                .end()
            .end();
        // @formatter:on

        Element links = rootBuilder.referenceFor(LINKS);
        Element helpContent = rootBuilder.referenceFor(HELP_CONTENT);

        if (stateMachine.supports(EDIT)) {
            editLink = link(CONSTANTS.edit(), pfIcon("edit"), onEdit);
            links.appendChild(editLink);
        }
        if (stateMachine.supports(RESET)) {
            resetLink = link(CONSTANTS.reset(), pfIcon("restart"), onReset);
            resetLink.getDataset().setAt(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            resetLink.getDataset().setAt(UIConstants.PLACEMENT, "right"); //NON-NLS
            resetLink.setTitle(CONSTANTS.formResetDesc());

            links.appendChild(resetLink);
        }
        if (!helpTexts.isEmpty()) {
            // @formatter:off
            helpLink = new Elements.Builder()
                .li()
                    .a().attr("href", "#" + helpId + "")
                            .data(UIConstants.TOGGLE,  UIConstants.COLLAPSE) //NON-NLS
                            .aria(UIConstants.EXPANDED, String.valueOf(false))
                            .aria(UIConstants.CONTROLS, helpId)
                        .start("i").css(pfIcon("help")).end()
                        .span().css(formLinkLabel).textContent(CONSTANTS.help()).end()
                    .end()
                .end().build();
            // @formatter:on
            for (Map.Entry<String, SafeHtml> entry : helpTexts.entrySet()) {
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
                .a().css(clickable).on(click, onclick)
                    .start("i").css(css).end()
                    .span().css(formLinkLabel).textContent(text).end()
                .end()
            .end().build();
        // @formatter:on
    }

    private Element help(String label, SafeHtml description) {
        // @formatter:off
        return new Elements.Builder()
            .div().css(formGroup)
                .label().css(column(labelColumns), controlLabel)
                    .textContent(label)
                .end()
                .div().css(column(inputColumns))
                    .p().css(formControlStatic)
                        .innerHtml(description)
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

    void switchTo(State state, T model, SecurityContext securityContext) {
        switch (state) {
            case READONLY:
                Elements.setVisible(editLink,
                        model != null && stateMachine.supports(EDIT) && securityContext.isWritable());
                Elements.setVisible(resetLink,
                        model != null && stateMachine.supports(RESET) && securityContext.isWritable());
                Elements.setVisible(helpLink, !helpTexts.isEmpty());
                break;

            case EDITING:
                Elements.setVisible(editLink, false);
                Elements.setVisible(resetLink, false);
                Elements.setVisible(helpLink, !helpTexts.isEmpty());
                break;
        }
        Elements.setVisible(root,
                Elements.isVisible(editLink) || Elements.isVisible(resetLink) || Elements.isVisible(helpLink));
    }
}
