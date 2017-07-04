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

import java.util.List;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormItemValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.form.FormItemValidation.ValidationRule.ALWAYS;

class RequiresValidation<T> implements FormItemValidation<T> {

    private final List<String> requires;
    private final ModelNodeForm form;
    private final Constants constants;
    private final Messages messages;
    private final LabelBuilder labelBuilder;

    RequiresValidation(final List<String> requires, final ModelNodeForm form,
            final Constants constants, final Messages messages) {
        this.requires = requires;
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
        List<String> nonEmptyRequires = requires.stream()
                .filter(name -> {
                    FormItem formItem = form.getFormItem(name);
                    return formItem != null && !this.form.isEmptyOrDefault(formItem);
                })
                .map(labelBuilder::label)
                .collect(toList());
        if (nonEmptyRequires.isEmpty()) {
            return ValidationResult.OK;
        } else {
            return ValidationResult.invalid(
                    messages.nonEmptyRequires(labelBuilder.enumeration(nonEmptyRequires, constants.or())));
        }
    }
}
