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
import org.jboss.hal.resources.Icons;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Alert implements IsElement {

    private static final String ALERT_ICON = "alertIconElement";
    private static final String ALERT_TEXT = "alertTextElement";
    private static final String ALERT_LINK = "alertLinkElement";

    private final Element root;
    private final Element icon;
    private final Element text;
    private final Element link;

    public Alert() {
        this(null, null, null, null);
    }

    public Alert(final String icon, final SafeHtml text) {
        this(icon, text, null, null);
    }

    public Alert(final String icon, final SafeHtml text, final String linkText, final EventListener linkHandler) {
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
                    .a().rememberAs(ALERT_LINK).css(clickable, alertLink).on(click, linkHandler)
                        .textContent(linkText)
                    .end();
                }
            builder.end();
        // @formatter:on

        this.root = builder.build();
        this.icon = builder.referenceFor(ALERT_ICON);
        this.text = builder.referenceFor(ALERT_TEXT);
        this.link = linkText != null && linkHandler != null ? builder.referenceFor(ALERT_LINK) : null;
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

    public Alert setLink(String text) {
        if (this.link != null) {
            this.link.setTextContent(text);
        }
        return this;
    }

    @Override
    public Element asElement() {
        return root;
    }
}
