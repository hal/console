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

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.resources.HalConstants;

/**
 * @author Harald Pehl
 */
class RestrictedElement extends InputElement<String> {

    static final HalConstants CONSTANTS = GWT.create(HalConstants.class);
    final elemental.html.InputElement element;

    RestrictedElement() {
        element = Browser.getDocument().createInputElement();
        element.setType("text");
        setValue(CONSTANTS.restricted());
    }

    @Override
    public int getTabIndex() {
        return element.getTabIndex();
    }

    @Override
    public void setAccessKey(final char c) {
        element.setAccessKey(String.valueOf(c));
    }

    @Override
    public void setFocus(final boolean b) {
        element.focus();
    }

    @Override
    public void setTabIndex(final int i) {
        element.setTabIndex(i);
    }

    @Override
    public boolean isEnabled() {
        return element.isDisabled();
    }

    @Override
    public void setEnabled(final boolean b) {
        element.setDisabled(true);
    }

    @Override
    public String getValue() {
        return element.getValue();
    }

    @Override
    void setValue(final String value) {
        element.setValue(value);
    }

    @Override
    void clearValue() {
        element.setValue(""); // no events please!
    }

    @Override
    public void setName(final String s) {
        element.setName(s);
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getText() {
        return getValue();
    }

    @Override
    public void setText(final String s) {
        setValue(s);
    }

    @Override
    public Element asElement() {
        return element;
    }
}
