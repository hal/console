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

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class EmptyState implements IsElement {

    @FunctionalInterface
    public interface Action {

        void execute();
    }


    private static class TitleAndAction {

        public final String title;
        public final Action action;

        public TitleAndAction(final String title, final Action action) {
            this.title = title;
            this.action = action;
        }
    }


    public static class Builder {

        private final String title;
        private final List<Element> paragraphs;
        private final List<TitleAndAction> secondaryActions;
        private String icon;
        private TitleAndAction primaryAction;

        public Builder(final String title) {
            this.title = title;
            this.paragraphs = new ArrayList<>();
            this.secondaryActions = new ArrayList<>();
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder description(String description) {
            Element p = Browser.getDocument().createElement("p"); //NON-NLS
            p.setTextContent(description);
            paragraphs.add(p);
            return this;
        }

        public Builder description(SafeHtml description) {
            Element p = Browser.getDocument().createElement("p"); //NON-NLS
            p.setInnerHTML(description.asString());
            paragraphs.add(p);
            return this;
        }

        public Builder primaryAction(String title, Action action) {
            this.primaryAction = new TitleAndAction(title, action);
            return this;
        }

        public Builder secondaryAction(String title, Action action) {
            this.secondaryActions.add(new TitleAndAction(title, action));
            return this;
        }

        public EmptyState build() {
            return new EmptyState(this);
        }
    }


    private static final String HEADER = "header";
    private static final String PARAGRAPHS_DIV = "paragraphsDiv";
    private static final String PRIMARY_ACTION_DIV = "primaryActionDiv";
    private static final String SECONDARY_ACTIONS_DIV = "secondaryActionsDiv";

    private final Element root;
    private final Element header;
    private final Element paragraphsDiv;
    private final Element primaryActionDiv;
    private final Element secondaryActionsDiv;

    private EmptyState(Builder builder) {
        Elements.Builder eb = new Elements.Builder().div().css(blankSlatePf);
        if (builder.icon != null) {
            eb.div().css(blankSlatePfIcon).start("i").css(builder.icon).end().end();
        }
        eb.h(1).rememberAs(HEADER).textContent(builder.title).end();
        eb.div().rememberAs(PARAGRAPHS_DIV);
        builder.paragraphs.forEach(eb::add);
        eb.end();

        eb.div().css(blankSlatePfMainAction).rememberAs(PRIMARY_ACTION_DIV);
        if (builder.primaryAction != null) {
            eb.button().css(btn, btnPrimary, btnLg).on(click, event -> builder.primaryAction.action.execute())
                    .textContent(builder.primaryAction.title)
                    .end();
        }
        eb.end();

        eb.div().css(blankSlatePfSecondaryAction).rememberAs(SECONDARY_ACTIONS_DIV);
        if (!builder.secondaryActions.isEmpty()) {
            for (TitleAndAction tac : builder.secondaryActions) {
                eb.button().css(btn, btnDefault).on(click, event -> tac.action.execute())
                        .textContent(tac.title)
                        .end();
            }
        }
        eb.end().end();

        header = eb.referenceFor(HEADER);
        paragraphsDiv = eb.referenceFor(PARAGRAPHS_DIV);
        primaryActionDiv = eb.referenceFor(PRIMARY_ACTION_DIV);
        secondaryActionsDiv = eb.referenceFor(SECONDARY_ACTIONS_DIV);
        root = eb.build();

        Elements.setVisible(primaryActionDiv, builder.primaryAction != null);
        Elements.setVisible(secondaryActionsDiv, !builder.secondaryActions.isEmpty());
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

    public void setPrimaryAction(String title, Action action) {
        Elements.removeChildrenFrom(primaryActionDiv);
        Element element = new Elements.Builder()
                .button().css(btn, btnPrimary, btnLg).on(click, event -> action.execute())
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
