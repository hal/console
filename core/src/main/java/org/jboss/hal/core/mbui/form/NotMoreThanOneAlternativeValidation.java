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

import com.google.common.collect.Iterables;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.stream.Collectors.toList;

/** Validates that only one of the alternatives is defined. */
public class NotMoreThanOneAlternativeValidation<T extends ModelNode> implements FormValidation<T> {

    private final SortedSet<String> alternatives;
    private final Constants constants;
    private final Messages messages;
    private final ModelNodeForm<T> form;

    public NotMoreThanOneAlternativeValidation(Iterable<String> alternatives, ModelNodeForm<T> form, Constants constants,
            Messages messages) {
        this.alternatives = new TreeSet<>();
        Iterables.addAll(this.alternatives, alternatives);
        this.form = form;
        this.constants = constants;
        this.messages = messages;
    }

    @Override
    public ValidationResult validate(Form<T> form) {
        LabelBuilder labelBuilder = new LabelBuilder();
        List<String> nonEmptyItems = alternatives.stream()
                .map(form::getFormItem)
                .filter(formItem -> formItem != null && !this.form.isEmptyOrDefault(formItem))
                .map(FormItem::getName)
                .collect(toList());

        if (nonEmptyItems.size() > 1) {
            // show an error on each related form item
            alternatives.forEach(alternative -> {
                TreeSet<String> otherAlternatives = new TreeSet<>(nonEmptyItems);
                otherAlternatives.remove(alternative);

                FormItem<Object> formItem = form.getFormItem(alternative);
                if (!this.form.isEmptyOrDefault(formItem)) {
                    formItem.showError(
                            messages.notMoreThanOneAlternativeError(
                                    labelBuilder.enumeration(otherAlternatives, constants.and())));
                }
            });
            // return overall result
            return ValidationResult.invalid(
                    messages.notMoreThanOneAlternativesError(labelBuilder.enumeration(alternatives, constants.or())));
        } else {
            return ValidationResult.OK;
        }
    }
}
