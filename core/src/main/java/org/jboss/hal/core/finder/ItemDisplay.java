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
package org.jboss.hal.core.finder;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.spi.Message;

/**
 * Controls the layout of a finder item <em>w/o</em> the actions. For simple items implement the {@link #getText()}
 * method and let the {@link #asElement()} return {@code null}. If you need a special layout implement the
 * {@link #asElement()} method, but provide a text as well (used for the breadcrumb).
 *
 * @author Harald Pehl
 */
public interface ItemDisplay extends IsElement {

    String getText();

    default Element asElement() {
        return null;
    }

    /**
     * The data which is used to filter items. Defaults to {@link #getText()}.
     */
    default String getFilterData() {
        return getText();
    }

    default Message.Level getMarker() {
        return null;
    }

    default String getTooltip() {
        return null;
    }

    default boolean isFolder() {
        return false;
    }
}
