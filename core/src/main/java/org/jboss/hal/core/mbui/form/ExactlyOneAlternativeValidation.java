/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.mbui.form;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;

import com.google.common.collect.Iterables;

import static java.util.stream.Collectors.toSet;

class ExactlyOneAlternativeValidation<T extends ModelNode> implements FormValidation<T> {

    private final SortedSet<String> requiredAlternatives;
    private final Constants constants;
    private final Messages messages;

    ExactlyOneAlternativeValidation(final Iterable<String> requiredAlternatives, final Constants constants,
            final Messages messages) {
        this.requiredAlternatives = new TreeSet<>();
        Iterables.addAll(this.requiredAlternatives, requiredAlternatives);
        this.constants = constants;
        this.messages = messages;
    }

    @Override
    public ValidationResult validate(final Form<T> form) {
        LabelBuilder labelBuilder = new LabelBuilder();
        Set<String> emptyItems = requiredAlternatives.stream()
                .filter(name -> {
                    FormItem formItem = form.getFormItem(name);
                    boolean empty = formItem != null && formItem.isEmpty();
                    // there is a special case for SwitchItem of Boolean type, the SwitchItem.isEmpty() tests if the value is
                    // null, but for this validation case we must ensure the value is set to false to allow the validation
                    // to work
                    boolean switchItemFalse = false;
                    if (formItem != null && formItem.getClass().equals(SwitchItem.class)) {
                        Object value = formItem.getValue();
                        switchItemFalse = value != null && !Boolean.parseBoolean(value.toString());
                    }
                    return empty || switchItemFalse;
                })
                .collect(toSet());

        if (requiredAlternatives.size() == emptyItems.size()) {
            // show an error on each related form item
            requiredAlternatives.forEach(alternative -> {
                FormItem<Object> formItem = form.getFormItem(alternative);
                if (formItem.isEmpty()) {
                    formItem.showError(messages.exactlyOneAlternativeError(
                            labelBuilder.enumeration(requiredAlternatives, constants.or())));
                }
            });
            // return overall result
            return ValidationResult.invalid(
                    messages.exactlyOneAlternativesError(
                            labelBuilder.enumeration(requiredAlternatives, constants.or())));

        } else {
            return ValidationResult.OK;
        }
    }
}
