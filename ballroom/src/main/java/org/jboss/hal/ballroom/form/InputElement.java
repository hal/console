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
package org.jboss.hal.ballroom.form;

import com.google.common.base.Splitter;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Harald Pehl
 */
abstract class InputElement<T>
        implements IsWidget, HasEnabled, Focusable, HasName, HasText /* for expression support */ {

    void setId(final String id) {
        asWidget().getElement().setId(id);
    }

    String getId() {
        return asWidget().getElement().getId();
    }

    void setClassName(final String className) {
        Iterable<String> styleNames = Splitter.on(' ').split(className);
        for (String styleName : styleNames) {
            asWidget().addStyleName(styleName);
        }
    }

    abstract T getValue();

    abstract void setValue(T value);

    abstract void clearValue();
}
