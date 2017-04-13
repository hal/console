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

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import elemental.events.EventListener;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Alert element with an optional icon and link.
 * <p>
 * {@linkplain Constraint Constraints} for the links are encoded as {@code data-constraint} attributes. Please make
 * sure to call {@link ElementGuard#processElements(AuthorisationDecision, String)} when
 * the alert element is added to the DOM.
 *
 * @author Harald Pehl
 * @see <a href="http://www.patternfly.org/pattern-library/communication/inline-notifications/">http://www.patternfly.org/pattern-library/communication/inline-notifications/</a>
 */
public class Alert implements IsElement {

    private static final String ALERT_ICON = "alertIconElement";
    private static final String ALERT_TEXT = "alertTextElement";

    private final Element root;
    private final Element icon;
    private final Element text;

    public Alert() {
        this(null, null, null, null);
    }

    public Alert(final String icon, final SafeHtml text) {
        this(icon, text, null, null);
    }

    public Alert(final String icon, final SafeHtml text, final String linkText, final EventListener linkHandler) {
        this(icon, text, linkText, linkHandler, null);
    }

    public Alert(final String icon, final SafeHtml text, final String linkText, final EventListener linkHandler,
            final Constraint constraint) {
        Elements.Builder builder = new Elements.Builder();

        // @formatter:off
        builder
            .div().css(alert, alertCss(icon))
                .span().rememberAs(ALERT_ICON).css(icon).end()
                .span().rememberAs(ALERT_TEXT);
                    if (text != null) {
                        builder.innerHtml(text);
                    }
                builder.end();
                if (linkText != null && linkHandler != null) {
                    builder.span().textContent(" ").end()
                    .a().css(clickable, alertLink).on(click, linkHandler);
                    if (constraint!= null) {
                        builder.data(UIConstants.CONSTRAINT, constraint.data());
                    }
                    builder.textContent(linkText).end();
                }
            builder.end();
        // @formatter:on

        this.root = builder.build();
        this.icon = builder.referenceFor(ALERT_ICON);
        this.text = builder.referenceFor(ALERT_TEXT);
    }

    private String alertCss(final String icon) {
        String alertCss = null;
        if (Icons.OK.equals(icon)) {
            alertCss = alertSuccess;
        } else if (Icons.INFO.equals(icon) || Icons.DISABLED.equals(icon) || Icons.STOPPED.equals(icon)) {
            alertCss = alertInfo;
        } else if (Icons.WARNING.equals(icon)) {
            alertCss = alertWarning;
        } else if (Icons.ERROR.equals(icon)) {
            alertCss = alertDanger;
        }
        return alertCss;
    }

    public Alert setIcon(String icon) {
        String alertCss = alertCss(icon);
        this.root.setClassName(alert + (alertCss != null ? (" " + alertCss) : ""));
        this.icon.setClassName(icon);
        return this;
    }

    public Alert setText(SafeHtml text) {
        this.text.setInnerHTML(text.asString());
        return this;
    }

    @Override
    public Element asElement() {
        return root;
    }
}
