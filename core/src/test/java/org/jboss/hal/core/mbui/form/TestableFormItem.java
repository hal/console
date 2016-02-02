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
package org.jboss.hal.core.mbui.form;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import elemental.dom.Element;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.ballroom.form.ResolveExpressionEvent;
import org.jboss.hal.ballroom.form.SuggestHandler;

/**
 * @author Harald Pehl
 */
public class TestableFormItem implements FormItem<String> {

    private final String name;

    public TestableFormItem(final String name) {this.name = name;}

    @Override
    public Element asElement(final Form.State state) {
        return null;
    }

    @Override
    public void attach() {

    }

    @Override
    public void clearValue() {

    }

    @Override
    public void registerSuggestHandler(final SuggestHandler suggestHandler) {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void setId(final String id) {

    }

    @Override
    public String getId(final Form.State state) {
        return null;
    }

    @Override
    public void addValidationHandler(final FormItemValidation<String> validationHandler) {

    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public void clearError() {

    }

    @Override
    public void showError(final String message) {

    }

    @Override
    public boolean isExpressionValue() {
        return false;
    }

    @Override
    public void setExpressionValue(final String expressionValue) {

    }

    @Override
    public String getExpressionValue() {
        return null;
    }

    @Override
    public boolean supportsExpressions() {
        return false;
    }

    @Override
    public void addResolveExpressionHandler(final ResolveExpressionEvent.Handler handler) {

    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setLabel(final String label) {

    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public void setRequired(final boolean required) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void setModified(final boolean modified) {

    }

    @Override
    public boolean isUndefined() {
        return false;
    }

    @Override
    public void setUndefined(final boolean undefined) {

    }

    @Override
    public boolean isExpressionAllowed() {
        return false;
    }

    @Override
    public void setExpressionAllowed(final boolean expressionAllowed) {

    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public void setRestricted(final boolean restricted) {

    }

    @Override
    public int getTabIndex() {
        return 0;
    }

    @Override
    public void setAccessKey(final char c) {

    }

    @Override
    public void setFocus(final boolean b) {

    }

    @Override
    public void setTabIndex(final int i) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void setEnabled(final boolean b) {

    }

    @Override
    public void setName(final String s) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void setText(final String s) {

    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public void setValue(final String s) {

    }

    @Override
    public void setValue(final String s, final boolean b) {

    }

    @Override
    public void setDefaultValue(final String defaultValue) {

    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> valueChangeHandler) {
        return null;
    }

    @Override
    public void fireEvent(final GwtEvent<?> gwtEvent) {

    }
}
