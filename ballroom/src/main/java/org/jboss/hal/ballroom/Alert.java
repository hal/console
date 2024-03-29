/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom;

import org.jboss.elemento.EventCallbackFn;
import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.ElementGuard;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.UIConstants;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * Alert element with an optional icon and link.
 * <p>
 * {@linkplain Constraint Constraints} for the links are encoded as {@code data-constraint} attributes. Please make sure to call
 * {@link ElementGuard#processElements(AuthorisationDecision, String)} when the alert element is added to the DOM.
 *
 * @see <a href=
 *      "https://www.patternfly.org/pattern-library/communication/inline-notifications/">https://www.patternfly.org/pattern-library/communication/inline-notifications/</a>
 */
public class Alert implements IsElement<HTMLElement> {

    private final HTMLElement root;
    private final HTMLElement icon;
    private final HTMLElement text;

    public Alert() {
        this(null, null, null, null);
    }

    public Alert(String icon, SafeHtml text) {
        this(icon, text, null, null);
    }

    public Alert(String icon, SafeHtml text, String additionalText) {
        this(icon, text, additionalText, null, null, null);
    }

    public Alert(String icon, SafeHtml text, String linkText,
            EventCallbackFn<MouseEvent> linkHandler) {
        this(icon, text, linkText, linkHandler, null);
    }

    public Alert(String icon, SafeHtml text, String linkText,
            EventCallbackFn<MouseEvent> linkHandler, Constraint constraint) {
        this(icon, text, null, linkText, linkHandler, constraint);

    }

    public Alert(String icon, SafeHtml text, String additionalText, String linkText,
            EventCallbackFn<MouseEvent> linkHandler, Constraint constraint) {
        this.root = div().css(alert, alertCss(icon))
                .add(this.icon = span().css(icon).element())
                .add(this.text = span().element())
                .element();

        if (text != null) {
            this.text.innerHTML = text.asString();
        }
        if (linkText != null && linkHandler != null) {
            HTMLElement a;
            this.root.appendChild(span().textContent(" ").element());
            this.root.appendChild(a = a().css(clickable, alertLink)
                    .on(click, linkHandler)
                    .textContent(linkText)
                    .element());
            if (constraint != null) {
                a.dataset.set(UIConstants.CONSTRAINT, constraint.data());
            }
        }
        if (additionalText != null) {
            HTMLElement additionalDescription = span().element();
            this.root.appendChild(additionalDescription);
            additionalDescription.innerHTML = additionalText;
        }
    }

    private String alertCss(String icon) {
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
    public HTMLElement element() {
        return root;
    }
}
