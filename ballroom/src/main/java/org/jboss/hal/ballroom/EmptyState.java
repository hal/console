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
package org.jboss.hal.ballroom;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Element to be used when a view is empty because no objects exists and you want to guide the user to perform specific
 * actions.
 * <p>
 * {@linkplain Constraint Constraints} for the primary and secondary actions are encoded as {@code data-constraint}
 * attributes. Please make sure to call {@link ElementGuard#processElements(AuthorisationDecision,
 * String)} when the empty state element is added to the DOM.
 *
 * @author Harald Pehl
 * @see <a href="http://www.patternfly.org/pattern-library/communication/empty-state/">http://www.patternfly.org/pattern-library/communication/empty-state/</a>
 */
public class EmptyState implements IsElement {

    private static class Action {

        public final String title;
        public final Callback callback;
        private final Constraint constraint;

        Action(final String title, final Callback callback, final Constraint constraint) {
            this.title = title;
            this.callback = callback;
            this.constraint = constraint;
        }
    }


    public static class Builder {

        private final String title;
        private final List<Element> elements;
        private final List<Action> secondaryActions;
        private String icon;
        private Action primaryAction;

        public Builder(final String title) {
            this.title = title;
            this.elements = new ArrayList<>();
            this.secondaryActions = new ArrayList<>();
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder description(String description) {
            Element p = Browser.getDocument().createElement("p"); //NON-NLS
            p.setTextContent(description);
            elements.add(p);
            return this;
        }

        public Builder description(SafeHtml description) {
            Element p = Browser.getDocument().createElement("p"); //NON-NLS
            p.setInnerHTML(description.asString());
            elements.add(p);
            return this;
        }

        public Builder add(Element element) {
            elements.add(element);
            return this;
        }

        public Builder addAll(Iterable<Element> elements) {
            Iterables.addAll(this.elements, elements);
            return this;
        }

        public Builder primaryAction(String title, Callback callback) {
            return primaryAction(title, callback, null);
        }

        public Builder primaryAction(String title, Callback callback, Constraint constraint) {
            this.primaryAction = new Action(title, callback, constraint);
            return this;
        }

        public Builder secondaryAction(String title, Callback callback) {
            return secondaryAction(title, callback, null);
        }

        public Builder secondaryAction(String title, Callback callback, Constraint constraint) {
            this.secondaryActions.add(new Action(title, callback, constraint));
            return this;
        }

        public EmptyState build() {
            return new EmptyState(this);
        }
    }


    private static final String ICON = "icon";
    private static final String HEADER = "header";
    private static final String PARAGRAPHS_DIV = "paragraphsDiv";
    private static final String PRIMARY_ACTION_DIV = "primaryActionDiv";
    private static final String SECONDARY_ACTIONS_DIV = "secondaryActionsDiv";

    private final Element root;
    private final Element icon;
    private final Element header;
    private final Element paragraphsDiv;
    private final Element primaryActionDiv;

    private EmptyState(Builder builder) {
        Elements.Builder eb = new Elements.Builder().div().css(blankSlatePf);
        if (builder.icon != null) {
            eb.div().css(blankSlatePfIcon).start("i").css(builder.icon).rememberAs(ICON).end().end();
        }
        eb.h(1).rememberAs(HEADER).textContent(builder.title).end();
        eb.div().rememberAs(PARAGRAPHS_DIV);
        builder.elements.forEach(eb::add);
        eb.end();

        eb.div().css(blankSlatePfMainAction).rememberAs(PRIMARY_ACTION_DIV);
        if (builder.primaryAction != null) {
            if (builder.primaryAction.constraint != null) {
                eb.data(UIConstants.CONSTRAINT, builder.primaryAction.constraint.data());
            }
            eb.button().css(btn, btnPrimary, btnLg).on(click, event -> builder.primaryAction.callback.execute())
                    .textContent(builder.primaryAction.title)
                    .end();
        }
        eb.end();

        eb.div().css(blankSlatePfSecondaryAction).rememberAs(SECONDARY_ACTIONS_DIV);
        if (!builder.secondaryActions.isEmpty()) {
            for (Action a : builder.secondaryActions) {
                eb.button().css(btn, btnDefault).on(click, event -> a.callback.execute());
                if (a.constraint != null) {
                    eb.data(UIConstants.CONSTRAINT, a.constraint.data());
                }
                eb.textContent(a.title).end();
            }
        }
        eb.end().end();

        icon = builder.icon != null ? eb.referenceFor(ICON) : null;
        header = eb.referenceFor(HEADER);
        paragraphsDiv = eb.referenceFor(PARAGRAPHS_DIV);
        primaryActionDiv = eb.referenceFor(PRIMARY_ACTION_DIV);
        Element secondaryActionsDiv = eb.referenceFor(SECONDARY_ACTIONS_DIV);
        root = eb.build();

        Elements.setVisible(primaryActionDiv, builder.primaryAction != null);
        Elements.setVisible(secondaryActionsDiv, !builder.secondaryActions.isEmpty());
    }

    public void setIcon(String icon) {
        if (this.icon != null) {
            this.icon.setClassName(icon);
        }
    }

    public void setHeader(String header) {
        this.header.setTextContent(header);
    }

    public void setDescription(SafeHtml description) {
        Elements.removeChildrenFrom(paragraphsDiv);
        Element p = Browser.getDocument().createElement("p"); //NON-NLS
        p.setInnerHTML(description.asString());
        paragraphsDiv.appendChild(p);
    }

    public void setPrimaryAction(String title, Callback callback) {
        Elements.removeChildrenFrom(primaryActionDiv);
        Element element = new Elements.Builder()
                .button().css(btn, btnPrimary, btnLg).on(click, event -> callback.execute())
                .textContent(title)
                .end()
                .build();
        primaryActionDiv.appendChild(element);
        Elements.setVisible(primaryActionDiv, true);
    }

    @Override
    public Element asElement() {
        return root;
    }
}
