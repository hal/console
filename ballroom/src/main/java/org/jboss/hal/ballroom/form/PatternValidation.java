package org.jboss.hal.ballroom.form;

import org.jboss.hal.resources.Messages;

import com.google.gwt.core.client.GWT;

public class PatternValidation implements FormItemValidation<Object> {

    private final static Messages MESSAGES = GWT.create(Messages.class);

    private String pattern;

    public PatternValidation(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public ValidationResult validate(Object value) {
        return value.toString().matches(pattern) ? ValidationResult.OK : ValidationResult.invalid(errorMessage());
    }

    protected String errorMessage() {
        return MESSAGES.invalidFormat();
    }

    public static class JNDINameValidation extends PatternValidation {

        public JNDINameValidation() {
            super("java:(jboss)?/.*");
        }

        @Override
        protected String errorMessage() {
            return MESSAGES.invalidJNDIName();
        }
    }
}
