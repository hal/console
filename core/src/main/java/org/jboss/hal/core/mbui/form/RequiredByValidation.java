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
package org.jboss.hal.core.mbui.form;

import java.util.Collection;
import java.util.List;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.form.FormItemValidation.ValidationRule.ALWAYS;

public class RequiredByValidation<T> implements FormItemValidation<T> {

    private final FormItem<T> formItem;
    private final Collection<String> requiredBy;
    private final ModelNodeForm form;
    private final Constants constants;
    private final Messages messages;
    private final LabelBuilder labelBuilder;

    public RequiredByValidation(final FormItem<T> formItem, final Collection<String> requiredBy, final ModelNodeForm form,
            final Constants constants, final Messages messages) {
        this.formItem = formItem;
        this.requiredBy = requiredBy;
        this.form = form;
        this.constants = constants;
        this.messages = messages;
        this.labelBuilder = new LabelBuilder();
    }

    @Override
    public ValidationRule validateIf() {
        return ALWAYS;
    }

    @Override
    public ValidationResult validate(final T value) {
        List<String> nonEmptyRequiredBy = requiredBy.stream()
                .filter(name -> {
                    FormItem formItem = form.getFormItem(name);
                    return formItem != null && !this.form.isEmptyOrDefault(formItem);
                })
                .map(labelBuilder::label)
                .collect(toList());

        if (nonEmptyRequiredBy.isEmpty()) {
            // if all required-by fields are empty, everything is fine
            return ValidationResult.OK;
        } else {
            // there is a special case for SwitchItem of Boolean type, the SwitchItem.isEmpty() tests if the value is
            // null, but for this validation case we must ensure the value is false
            boolean switchItemFalse = false;
            if (formItem instanceof SwitchItem) {
                switchItemFalse = !((SwitchItem) formItem).getValue();
            }
            // but as soon as there's one non-empty required-by field, this form item must be non-empty as well!
            if (formItem.isEmpty() || switchItemFalse) {
                return ValidationResult.invalid(
                        messages.nonEmptyRequires(labelBuilder.enumeration(nonEmptyRequiredBy, constants.or())));
            } else {
                return ValidationResult.OK;
            }
        }
    }
}
