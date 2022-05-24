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
package org.jboss.hal.core.mbui.form;

import org.jboss.hal.ballroom.form.EncryptExpressionEvent.EncryptExpressionHandler;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.ballroom.form.ResolveExpressionEvent.ResolveExpressionHandler;
import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.dmr.Deprecation;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import elemental2.dom.HTMLElement;

public class TestableFormItem implements FormItem<String> {

    private String name;

    TestableFormItem(String name) {
        this.name = name;
    }

    @Override
    public HTMLElement element(Form.State state) {
        return null;
    }

    @Override
    public void attach() {

    }

    @Override
    public void clearValue() {

    }

    @Override
    public void registerSuggestHandler(SuggestHandler suggestHandler) {

    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public String getId(Form.State state) {
        return null;
    }

    @Override
    public void addValidationHandler(FormItemValidation<String> validationHandler) {

    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public void clearError() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public boolean isExpressionValue() {
        return false;
    }

    @Override
    public void setExpressionValue(String expressionValue) {

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
    public void addResolveExpressionHandler(ResolveExpressionHandler handler) {

    }

    @Override
    public void addEncryptExpressionHandler(EncryptExpressionHandler handler) {

    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public void setLabel(String label) {

    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public void setRequired(boolean required) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void setModified(boolean modified) {

    }

    @Override
    public boolean isUndefined() {
        return false;
    }

    @Override
    public void setUndefined(boolean undefined) {

    }

    @Override
    public boolean isExpressionAllowed() {
        return false;
    }

    @Override
    public void setExpressionAllowed(boolean expressionAllowed) {

    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public void setRestricted(boolean restricted) {

    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    @Override
    public void setDeprecated(Deprecation deprecation) {

    }

    @Override
    public int getTabIndex() {
        return 0;
    }

    @Override
    public void setAccessKey(char c) {

    }

    @Override
    public void setFocus(boolean b) {

    }

    @Override
    public void setTabIndex(int i) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void setEnabled(boolean b) {

    }

    @Override
    public void setName(String s) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public void setValue(String s) {

    }

    @Override
    public void setValue(String s, boolean b) {

    }

    @Override
    public void assignDefaultValue(String defaultValue) {

    }

    @Override
    public void mask() {

    }

    @Override
    public void unmask() {

    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
        return null;
    }

    @Override
    public void fireEvent(GwtEvent<?> gwtEvent) {

    }
}
