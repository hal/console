/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.mbui.form;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.FormValidation;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.toList;

public class RequireAtLeastOneAttributeValidation<T extends ModelNode> implements FormValidation<T> {

    private final List<String> requiresAtLeast;
    private final Resources resources;


    public RequireAtLeastOneAttributeValidation(List<String> requiresAtLeast, Resources resources) {
        this.requiresAtLeast = requiresAtLeast;
        this.resources = resources;
    }

    @Override
    public ValidationResult validate(Form<T> form) {
        LabelBuilder labelBuilder = new LabelBuilder();
        List<String> nonEmptyItems = requiresAtLeast.stream()
                .map(form::getFormItem)
                .filter(formItem -> formItem != null && !formItem.isEmpty())
                .map(FormItem::getName)
                .collect(toList());

        if (nonEmptyItems.isEmpty()) {

            // retrieve the label item, instead the attribute name, for the cases when a complex attribute is in use
            List<String> attributesLabels = new ArrayList<>();
            requiresAtLeast.forEach(requiredAttribute -> {
                FormItem<Object> formItem = form.getFormItem(requiredAttribute);
                attributesLabels.add(formItem.getLabel());
            });

            // show an error on each related form item
            requiresAtLeast.forEach(requiredAttribute -> {
                FormItem<Object> formItem = form.getFormItem(requiredAttribute);
                if (formItem.isEmpty()) {
                    formItem.showError(resources.messages().exactlyOneAlternativeError(
                            labelBuilder.enumeration(attributesLabels, resources.constants().or())));
                }
            });
            // return overall result
            return ValidationResult.invalid(
                    resources.messages()
                            .atLeastOneIsRequired(
                                    labelBuilder.enumeration(attributesLabels, resources.constants().or())));
        } else {
            return ValidationResult.OK;
        }
    }
}
