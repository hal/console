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
package org.jboss.hal.ballroom.table;

import elemental.events.EventListener;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;

/**
 * @author Harald Pehl
 */
public class DataTableButton extends Button implements SecurityContextAware {

    public enum Target { ROW, TABLE}

    public static final String DEFAULT_CSS = "btn btn-default";

    private final Target target;
    private final String operation;
    private SecurityContext securityContext;

    public DataTableButton(final String label, final Target target, final EventListener listener) {
        this(label, target, null, listener);
    }

    public DataTableButton(final String label, final Target target, final String operation,
            final EventListener listener) {
        super(label, DEFAULT_CSS, listener);
        this.target = target;
        this.operation = operation;
    }

    @Override
    public void updateSecurityContext(final SecurityContext securityContext) {
        if (operation != null) {
            this.securityContext = securityContext;
            if (!securityContext.isExecutable(operation)) {
                element.getDataset().setAt("rbac", "restricted");
                setDisabled(true);
            } else {
                boolean wasRestricted = element.getDataset().at("rbac") != null;
                if (wasRestricted) {
                    element.getDataset().setAt("rbac", null);
                    if (element.isDisabled()) {
                        setDisabled(false);
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

    Target getTarget() {
        return target;
    }
}
