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
package org.jboss.hal.ballroom.form;

import org.jboss.hal.resources.Messages;

import com.google.gwt.core.client.GWT;

public class PatternValidation implements FormItemValidation<Object> {

    private static final Messages MESSAGES = GWT.create(Messages.class);

    private String pattern;

    PatternValidation(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public ValidationResult validate(Object value) {
        return value.toString().matches(pattern) ? ValidationResult.OK : ValidationResult.invalid(errorMessage());
    }

    protected String errorMessage() {
        return MESSAGES.invalidFormat();
    }

    public static class JndiNameValidation extends PatternValidation {

        public JndiNameValidation() {
            super("java:(jboss)?/.*"); // NON-NLS
        }

        @Override
        protected String errorMessage() {
            return MESSAGES.invalidJNDIName();
        }
    }
}
