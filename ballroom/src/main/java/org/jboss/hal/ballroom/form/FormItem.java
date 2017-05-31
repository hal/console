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
package org.jboss.hal.ballroom.form;

import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasValue;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.ResolveExpressionEvent.ResolveExpressionHandler;
import org.jboss.hal.dmr.Deprecation;

/**
 * @author Harald Pehl
 */
public interface FormItem<T> extends Attachable, HasEnabled, Focusable, HasName, HasValue<T> {

    HTMLElement asElement(Form.State state);

    void clearValue();

    /**
     * @return if this form item has no value.
     */
    boolean isEmpty();

    void setId(String id);

    String getId(Form.State state);

    void assignDefaultValue(T defaultValue);

    void mask();

    void unmask();

    void registerSuggestHandler(SuggestHandler suggestHandler);

    void addValidationHandler(FormItemValidation<T> validationHandler);

    boolean validate();

    void clearError();

    void showError(String message);

    boolean isExpressionValue();

    void setExpressionValue(String expressionValue);

    String getExpressionValue();

    boolean supportsExpressions();

    void addResolveExpressionHandler(ResolveExpressionHandler handler);

    String getLabel();

    void setLabel(String label);

    boolean isRequired();

    void setRequired(boolean required);

    boolean isModified();

    void setModified(boolean modified);

    boolean isUndefined();

    void setUndefined(boolean undefined);

    boolean isExpressionAllowed();

    void setExpressionAllowed(boolean expressionAllowed);

    boolean isRestricted();

    void setRestricted(boolean restricted);

    boolean isDeprecated();

    void setDeprecated(Deprecation deprecation);
}
