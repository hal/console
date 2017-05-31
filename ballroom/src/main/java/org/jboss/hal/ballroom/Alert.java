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
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.gwt.elemento.core.EventCallbackFn;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.span;
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

    private final HTMLElement root;
    private final HTMLElement icon;
    private final HTMLElement text;

    public Alert() {
        this(null, null, null, null);
    }

    public Alert(final String icon, final SafeHtml text) {
        this(icon, text, null, null);
    }

    public Alert(final String icon, final SafeHtml text, final String linkText,
            final EventCallbackFn<MouseEvent> linkHandler) {
        this(icon, text, linkText, linkHandler, null);
    }

    public Alert(final String icon, final SafeHtml text, final String linkText,
            final EventCallbackFn<MouseEvent> linkHandler, final Constraint constraint) {
        this.root = div().css(alert, alertCss(icon))
                .add(this.icon = span().css(icon).asElement())
                .add(this.text = span().asElement())
                .asElement();

        if (text != null) {
            this.text.innerHTML = text.asString();
        }
        if (linkText != null && linkHandler != null) {
            HTMLElement a;
            this.root.appendChild(span().textContent(" ").asElement());
            this.root.appendChild(a = a().css(clickable, alertLink)
                    .on(click, linkHandler)
                    .textContent(linkText)
                    .asElement());
            if (constraint != null) {
                a.dataset.set(UIConstants.CONSTRAINT, constraint.data());
            }
        }
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
        this.root.className = alert + (alertCss != null ? (" " + alertCss) : "");
        this.icon.className = icon;
        return this;
    }

    public Alert setText(SafeHtml text) {
        this.text.innerHTML = text.asString();
        return this;
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }
}
