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
package org.jboss.hal.core.expression;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.resources.Resources;

/** A special form item which is used to resolve expressions. */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class ExpressionItem extends TextBoxItem {

    ExpressionItem(final Resources resources) {
        super("expression", new LabelBuilder().label("expression"), null);

        setExpressionAllowed(false);
        addValidationHandler(value -> {
            try {
                Expression.of(value);
                return ValidationResult.OK;
            } catch (IllegalArgumentException e) {
                return ValidationResult.invalid(resources.constants().invalidExpression());
            }
        });
    }

    @Override
    protected void modifyExpressionValue(final String newExpressionValue) {
        // called from the on change handler, because the value is an expression,
        // but we have to treat it as normal value
        modifyValue(newExpressionValue); // 'redirect' to modifyValue
    }
}
