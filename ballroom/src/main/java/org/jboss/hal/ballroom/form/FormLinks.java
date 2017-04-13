/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import elemental.events.EventListener;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Form.Operation.EDIT;
import static org.jboss.hal.ballroom.form.Form.Operation.REMOVE;
import static org.jboss.hal.ballroom.form.Form.Operation.RESET;
import static org.jboss.hal.resources.CSS.*;

/**
 * Links for commons form {@linkplain Form.Operation operations} placed above the actual form. Depending on the
 * {@linkplain Form.State state} links are displayed or hidden.
 * <p>
 * The following links are part of this element:
 * <ol>
 * <li>Edit: Visible in the {@linkplain Form.State#READONLY read-only} state. Switches to the {@linkplain
 * Form.State#EDITING editing} state</li>
 * <li>Reset: Visible in the {@linkplain Form.State#READONLY read-only} state. Resets the form's model. If a {@link
 * org.jboss.hal.ballroom.form.Form.PrepareReset} callback is defined, the callback is called. Otherwise {@link
 * Form#reset()} is called.</li>
 * <li>Remove: Visible in the {@linkplain Form.State#READONLY read-only} state. Removes the form's model. If a {@link
 * org.jboss.hal.ballroom.form.Form.PrepareRemove} callback is defined, the callback is called, otherwise {@link
 * Form#remove()} is called.</li>
 * <li>Help: Visible in the {@linkplain Form.State#READONLY read-only} and the {@linkplain Form.State#EDITING editing}
 * states. Provides access to the help texts.</li>
 * </ol>
 *
 * @author Harald Pehl
 */
public class FormLinks<T> implements IsElement {

    private static final String LINKS = "links";
    private static final String HELP_CONTENT = "helpContent";
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final AbstractForm<T> form;
    private final LinkedHashMap<String, SafeHtml> helpTexts;

    private final Element root;
    private Element editLink;
    private Element resetLink;
    private Element removeLink;
    private Element helpLink;

    FormLinks(final AbstractForm<T> form,
            final StateMachine stateMachine,
            final LinkedHashMap<String, SafeHtml> helpTexts,
            final EventListener onEdit,
            final EventListener onReset,
            final EventListener onRemove) {
        this.form = form;

        this.helpTexts = helpTexts;

        String linksId = Ids.build(form.getId(), LINKS);
        String helpId = Ids.build(form.getId(), "help");

        // @formatter:off
        Elements.Builder rootBuilder = new Elements.Builder()
            .div().css(CSS.form, formHorizontal)
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
            resetLink = link(CONSTANTS.reset(), fontAwesome("undo"), onReset);
            resetLink.getDataset().setAt(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            resetLink.getDataset().setAt(UIConstants.PLACEMENT, "right"); //NON-NLS
            resetLink.setTitle(CONSTANTS.formResetDesc());
            links.appendChild(resetLink);
        }
        if (stateMachine.supports(REMOVE)) {
            removeLink = link(CONSTANTS.remove(), CSS.pfIcon("remove"), onRemove);
            links.appendChild(removeLink);
        }
        if (!helpTexts.isEmpty()) {
            // @formatter:off
            helpLink = new Elements.Builder()
                .li()
                    .a().attr("href", "#" + helpId + "")
                            .data(UIConstants.TOGGLE,  UIConstants.COLLAPSE)
                            .aria(UIConstants.EXPANDED, UIConstants.FALSE)
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
                .label().css(controlLabel, halFormLabel)
                    .textContent(label)
                .end()
                .div().css(halFormInput)
                    .p().css(formControlStatic)
                        .innerHtml(description)
                    .end()
                .end()
            .end().build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        if (editLink == null && resetLink == null && removeLink == null && helpTexts.isEmpty()) {
            Elements.setVisible(root, false);
        }
        return root;
    }

    public void setVisible(boolean edit, boolean reset, boolean remove, boolean help) {
        Elements.setVisible(editLink, edit);
        Elements.setVisible(resetLink, reset);
        Elements.setVisible(removeLink, remove);
        Elements.setVisible(helpLink, help && !helpTexts.isEmpty());

        Elements.setVisible(root, Elements.isVisible(editLink) ||
                Elements.isVisible(resetLink) ||
                Elements.isVisible(removeLink) ||
                Elements.isVisible(helpLink));
    }

    public void setVisible(Form.Operation operation, boolean visible) {
        switch (operation) {
            case EDIT:
                Elements.setVisible(editLink, visible);
                break;
            case RESET:
                Elements.setVisible(resetLink, visible);
                break;
            case REMOVE:
                Elements.setVisible(removeLink, visible);
                break;
            default:
                break;
        }
    }
}
