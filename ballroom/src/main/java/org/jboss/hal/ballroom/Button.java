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
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;

/**
 * @author Harald Pehl
 */
public class Button implements IsElement, SecurityContextAware {

    private final ButtonElement element;
    private final String operation;
    private SecurityContext securityContext;

    public Button(final String label) {
        this(label, null, null);
    }

    public Button(final String label, final String css) {
        this(label, css, null);
    }

    public Button(final String label, final String css, final String operation) {
        this.operation = operation;

        element = Browser.getDocument().createButtonElement();
        element.setInnerText(label);
        if (css != null) {
            element.setClassName(css);
        }
    }

    @Override
    public Element asElement() {
        return element;
    }

    @Override
    public void updateSecurityContext(final SecurityContext securityContext) {
        if (operation != null) {
            this.securityContext = securityContext;
            if (!securityContext.isExecutable(operation)) {
                element.getDataset().setAt("rbac", "restricted");
                element.setDisabled(true);
            } else {
                boolean wasRestricted = element.getDataset().at("rbac") != null;
                if (wasRestricted) {
                    element.getDataset().setAt("rbac", null);
                    if (element.isDisabled()) {
                        element.setDisabled(false);
                    }
                }
            }
        }
    }

    public void setDisabled(boolean disabled) {
        if (operation != null && securityContext != null && !securityContext.isExecutable(operation)) {
            element.setDisabled(true);
        } else {
            element.setDisabled(disabled);
        }
    }
}
