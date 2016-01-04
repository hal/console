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
package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.hal.resources.CSS.*;

class Feedback implements IsElement {

    private static final String ICON = "icon";
    private static final String MESSAGE = "message";

    private final Element root;
    private final Element icon;
    private final Element message;

    Feedback() {
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(alert)
                .span().rememberAs(ICON).end()
                .span().rememberAs(MESSAGE).end()
            .end();
        // @formatter:on
        root = builder.build();
        icon = builder.referenceFor(ICON);
        message = builder.referenceFor(MESSAGE);
    }

    @Override
    public Element asElement() {
        return root;
    }

    void reset() {
        Elements.setVisible(root, false);
    }

    void ok(SafeHtml message) {
        root.getClassList().add(alertSuccess);
        root.getClassList().remove(alertDanger);
        icon.setClassName(pfIcon("ok"));
        this.message.setInnerHTML(message.asString());
        Elements.setVisible(root, true);
    }

    void error(SafeHtml message) {
        root.getClassList().add(alertDanger);
        root.getClassList().remove(alertSuccess);
        icon.setClassName(pfIcon(errorCircleO));
        this.message.setInnerHTML(message.asString());
        Elements.setVisible(root, true);
    }
}
