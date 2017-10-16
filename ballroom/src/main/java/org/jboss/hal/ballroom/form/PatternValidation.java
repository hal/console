package org.jboss.hal.ballroom.form;

import com.google.gwt.core.client.GWT;
import org.jboss.hal.resources.Messages;

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
            super("java:(jboss)?/.*"); //NON-NLS
        }

        @Override
        protected String errorMessage() {
            return MESSAGES.invalidJNDIName();
        }
    }
}
