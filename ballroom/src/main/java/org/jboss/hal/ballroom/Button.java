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
package org.jboss.hal.ballroom;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class Button implements IsElement {

    public static final String DEFAULT_CSS = btn + " " + btnHal + " " + btnDefault;

    protected final ButtonElement element;

    public Button(final String label, final EventListener listener) {
        this(label, DEFAULT_CSS, listener);
    }

    public Button(final String label, final String css, final EventListener listener) {
        element = Browser.getDocument().createButtonElement();
        element.setInnerText(label);
        if (css != null) {
            element.setClassName(css);
        }
        element.setOnclick(listener);
    }

    @Override
    public Element asElement() {
        return element;
    }
}
