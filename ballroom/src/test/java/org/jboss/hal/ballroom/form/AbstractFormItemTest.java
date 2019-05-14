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
package org.jboss.hal.ballroom.form;

import java.util.Collections;
import java.util.List;

import org.jboss.hal.ballroom.form.AbstractFormItem.ExpressionContext;
import org.jboss.hal.dmr.Deprecation;
import org.jboss.hal.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REASON;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SINCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "unchecked"})
public class AbstractFormItemTest {

    static class TestableFormItemWithoutExpressionSupport extends AbstractFormItem<String> {

        TestableFormItemWithoutExpressionSupport(String name, String label, String hint,
                Appearance<String> readOnly, Appearance<String> editing) {
            super(name, label, hint);
            addAppearance(Form.State.READONLY, readOnly);
            addAppearance(Form.State.EDITING, editing);
        }

        @Override
        List<FormItemValidation<String>> defaultValidationHandlers() {
            return Collections.emptyList(); // don't use RequiredValidation which relies on GWT.create(Constants.class)
        }

        @Override
        public boolean supportsExpressions() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        boolean isModal() {
            return false;
        }
    }


    static class TestableFormItemWithExpressionSupport extends AbstractFormItem<String> {

        TestableFormItemWithExpressionSupport(String name, String label, String hint,
                Appearance<String> readOnly, Appearance<String> editing) {
            super(name, label, hint);
            addAppearance(Form.State.READONLY, readOnly);
            addAppearance(Form.State.EDITING, editing);
        }

        @Override
        List<FormItemValidation<String>> defaultValidationHandlers() {
            return Collections.emptyList(); // don't use RequiredValidation which relies on GWT.create(Constants.class)
        }

        @Override
        public boolean supportsExpressions() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        boolean isModal() {
            return false;
        }
    }


    private Appearance<String> readOnlyAppearance;
    private Appearance<String> editingAppearance;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        readOnlyAppearance = mock(Appearance.class);
        editingAppearance = mock(Appearance.class);
        when(readOnlyAppearance.asString(anyString())).thenCallRealMethod();
        when(editingAppearance.asString(anyString())).thenCallRealMethod();
    }


    // ------------------------------------------------------ test methods

    @Test
    public void hint() {
        formItem(false, "Hint");
        verify(readOnlyAppearance).apply(HINT, "Hint");
        verify(editingAppearance).apply(HINT, "Hint");
    }

    @Test
    public void attach() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.attach();
        verify(readOnlyAppearance).attach();
        verify(editingAppearance).attach();
    }

    @Test
    public void detach() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.detach();
        verify(readOnlyAppearance).detach();
        verify(editingAppearance).detach();
    }

    @Test
    public void getId() {
        when(readOnlyAppearance.getId()).thenReturn("test-read-only");
        when(editingAppearance.getId()).thenReturn("test-editing");
        AbstractFormItem<String> formItem = formItem(false);
        assertEquals("test-read-only", formItem.getId(Form.State.READONLY));
        assertEquals("test-editing", formItem.getId(Form.State.EDITING));
    }

    @Test
    public void setId() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.setId("test");
        verify(readOnlyAppearance).setId("test");
        verify(editingAppearance).setId("test");
    }

    @Test
    public void setNullValue() {
        AbstractFormItem<String> noExpression = formItem(false);
        noExpression.setValue(null);
        assertNull(noExpression.getValue());
        assertNull(noExpression.getExpressionValue());
        verify(readOnlyAppearance).showValue(null);
        verify(editingAppearance).showValue(null);

        reset(readOnlyAppearance, editingAppearance);

        AbstractFormItem<String> expression = formItem(true);
        expression.setValue(null);
        assertNull(expression.getValue());
        assertNull(expression.getExpressionValue());
        verify(readOnlyAppearance).showValue(null);
        verify(readOnlyAppearance).unapply(EXPRESSION);
        verify(editingAppearance).showValue(null);
        verify(editingAppearance).unapply(EXPRESSION);
    }

    @Test
    public void setEmptyValue() {
        AbstractFormItem<String> noExpression = formItem(false);
        noExpression.setValue("");
        assertEquals("", noExpression.getValue());
        assertNull(noExpression.getExpressionValue());
        verify(readOnlyAppearance).showValue("");
        verify(editingAppearance).showValue("");

        reset(readOnlyAppearance, editingAppearance);

        AbstractFormItem<String> expression = formItem(true);
        expression.setValue("");
        assertEquals("", expression.getValue());
        assertNull(expression.getExpressionValue());
        verify(readOnlyAppearance).showValue("");
        verify(readOnlyAppearance).unapply(EXPRESSION);
        verify(editingAppearance).showValue("");
        verify(editingAppearance).unapply(EXPRESSION);
    }

    @Test
    public void setValue() {
        AbstractFormItem<String> noExpression = formItem(false);
        boolean modified = noExpression.isModified();
        boolean undefined = noExpression.isUndefined();
        noExpression.setValue("foo");

        assertEquals("foo", noExpression.getValue());
        assertNull(noExpression.getExpressionValue());
        assertEquals(modified, noExpression.isModified());
        assertEquals(undefined, noExpression.isUndefined());
        verify(readOnlyAppearance).showValue("foo");
        verify(editingAppearance).showValue("foo");

        reset(readOnlyAppearance, editingAppearance);

        AbstractFormItem<String> expression = formItem(true);
        modified = expression.isModified();
        undefined = expression.isUndefined();
        expression.setValue("foo");

        assertEquals("foo", expression.getValue());
        assertNull(expression.getExpressionValue());
        assertEquals(modified, expression.isModified());
        assertEquals(undefined, expression.isUndefined());
        verify(readOnlyAppearance).showValue("foo");
        verify(editingAppearance).showValue("foo");
        verify(readOnlyAppearance).unapply(EXPRESSION);
        verify(editingAppearance).unapply(EXPRESSION);
    }

    @Test
    public void clearValue() {
        AbstractFormItem<String> noExpression = formItem(false);
        boolean modified = noExpression.isModified();
        boolean undefined = noExpression.isUndefined();

        noExpression.clearValue();
        assertNull(noExpression.getValue());
        assertNull(noExpression.getExpressionValue());
        assertEquals(modified, noExpression.isModified());
        assertEquals(undefined, noExpression.isUndefined());
        verify(readOnlyAppearance).clearValue();
        verify(readOnlyAppearance).unapply(INVALID);
        verify(editingAppearance).clearValue();
        verify(editingAppearance).unapply(INVALID);

        reset(readOnlyAppearance, editingAppearance);

        AbstractFormItem<String> expression = formItem(true);
        modified = expression.isModified();
        undefined = expression.isUndefined();
        expression.clearValue();

        assertNull(expression.getValue());
        assertNull(expression.getExpressionValue());
        assertEquals(modified, expression.isModified());
        assertEquals(undefined, expression.isUndefined());
        verify(readOnlyAppearance).clearValue();
        verify(readOnlyAppearance).unapply(INVALID);
        verify(readOnlyAppearance).unapply(EXPRESSION);
        verify(editingAppearance).clearValue();
        verify(editingAppearance).unapply(INVALID);
        verify(editingAppearance).unapply(EXPRESSION);
    }

    @Test
    public void modifyValue() {
        AbstractFormItem<String> noExpression = formItem(false);
        noExpression.setModified(false);
        noExpression.setUndefined(true);
        noExpression.modifyValue("foo");

        assertEquals("foo", noExpression.getValue());
        assertNull(noExpression.getExpressionValue());
        assertTrue(noExpression.isModified());
        assertFalse(noExpression.isUndefined());
        verify(readOnlyAppearance, never()).showValue("foo");
        verify(editingAppearance, never()).showValue("foo");

        reset(readOnlyAppearance, editingAppearance);

        AbstractFormItem<String> expression = formItem(true);
        expression.setModified(false);
        expression.setUndefined(true);
        expression.modifyValue("foo");

        assertEquals("foo", expression.getValue());
        assertNull(expression.getExpressionValue());
        assertTrue(expression.isModified());
        assertFalse(expression.isUndefined());
        verify(readOnlyAppearance, never()).showValue("foo");
        verify(editingAppearance, never()).showValue("foo");
        verify(readOnlyAppearance, never()).unapply(EXPRESSION);
        verify(editingAppearance, never()).unapply(EXPRESSION);
    }

    @Test
    public void defaultValue() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.assignDefaultValue("foo");
        formItem.clearValue();

        assertNull(formItem.getValue());
        assertNull(formItem.getExpressionValue());
        verify(readOnlyAppearance).unapply(INVALID);
        verify(editingAppearance).unapply(INVALID);
        verify(readOnlyAppearance).apply(DEFAULT, "foo");
        verify(editingAppearance).apply(DEFAULT, "foo");
        verify(readOnlyAppearance).clearValue();
        verify(editingAppearance).clearValue();
    }

    @Test
    public void setName() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.setName("foo");
        verify(readOnlyAppearance).setName("foo");
        verify(editingAppearance).setName("foo");
    }

    @Test
    public void requiresValidation() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.setUndefined(false);
        formItem.setRequired(true);
        assertTrue(formItem.requiresValidation());

        formItem = formItem(false);
        formItem.setUndefined(true);
        assertFalse(formItem.requiresValidation());

        formItem = formItem(false);
        formItem.setUndefined(false);
        formItem.setModified(true);
        assertFalse(formItem.requiresValidation());

        formItem = formItem(false);
        formItem.setUndefined(false);
        formItem.setModified(true);
        formItem.addValidationHandler(value -> ValidationResult.OK);
        assertTrue(formItem.requiresValidation());
    }

    @Test
    public void valid() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.addValidationHandler(value -> ValidationResult.OK);
        formItem.setModified(true);

        assertTrue(formItem.validate());
        verify(readOnlyAppearance).unapply(INVALID);
        verify(editingAppearance).unapply(INVALID);
    }

    @Test
    public void invalid() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.addValidationHandler(value -> ValidationResult.invalid("error"));
        formItem.setUndefined(false);
        formItem.setModified(true);

        assertFalse(formItem.validate());
        verify(readOnlyAppearance).apply(INVALID, "error");
        verify(editingAppearance).apply(INVALID, "error");
    }

    @Test
    public void isExpressionValue() {
        AbstractFormItem<String> noExpression = formItem(false);
        assertFalse(noExpression.isExpressionValue());

        noExpression.setValue("${foo:bar}");
        assertFalse(noExpression.isExpressionValue());

        noExpression.setExpressionValue("${foo:bar}");
        assertFalse(noExpression.isExpressionValue());

        AbstractFormItem<String> expression = formItem(true);
        assertFalse(expression.isExpressionValue());

        expression.setValue("${foo:bar}");
        assertFalse(expression.isExpressionValue());

        expression.setExpressionValue("${foo:bar}");
        assertTrue(expression.isExpressionValue());
    }

    @Test
    public void setExpressionValue() {
        AbstractFormItem<String> formItem = formItem(true);
        boolean modified = formItem.isModified();
        boolean undefined = formItem.isUndefined();
        formItem.setExpressionValue("${foo:bar}");

        assertNull(formItem.getValue());
        assertEquals("${foo:bar}", formItem.getExpressionValue());
        assertEquals(modified, formItem.isModified());
        assertEquals(undefined, formItem.isUndefined());

        ArgumentMatcher<ExpressionContext> eqExpr = ec -> "${foo:bar}".equals(ec.expression);
        verify(readOnlyAppearance).showExpression("${foo:bar}");
        verify(editingAppearance).showExpression("${foo:bar}");
        verify(readOnlyAppearance).apply(eq(EXPRESSION), argThat(eqExpr));
        verify(editingAppearance).apply(eq(EXPRESSION), argThat(eqExpr));
    }

    @Test
    public void modifyExpressionValue() {
        AbstractFormItem<String> formItem = formItem(true);
        formItem.setModified(false);
        formItem.setUndefined(true);
        formItem.modifyExpressionValue("${foo:bar}");

        assertNull(formItem.getValue());
        assertEquals("${foo:bar}", formItem.getExpressionValue());
        assertTrue(formItem.isModified());
        assertFalse(formItem.isUndefined());
        ArgumentMatcher<ExpressionContext> eqExpr = ec -> "${foo:bar}".equals(ec.expression);
        verify(readOnlyAppearance, never()).apply(eq(EXPRESSION), argThat(eqExpr));
        verify(editingAppearance, never()).apply(eq(EXPRESSION), argThat(eqExpr));
    }

    @Test
    public void registerSuggestHandler() {
        AbstractFormItem<String> formItem = formItem(false);
        SuggestHandler suggestHandler = new SuggestHandler() {
            @Override
            public void setFormItem(FormItem formItem) {

            }

            @Override
            public void showAll() {

            }

            @Override
            public void close() {

            }
        };
        formItem.registerSuggestHandler(suggestHandler);
        verify(readOnlyAppearance).apply(SUGGESTIONS, suggestHandler);
        verify(editingAppearance).apply(SUGGESTIONS, suggestHandler);
    }

    @Test
    public void setRestricted() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.setRestricted(true);
        verify(readOnlyAppearance).apply(RESTRICTED, null);
        verify(editingAppearance).apply(RESTRICTED, null);

        reset(readOnlyAppearance, editingAppearance);

        formItem.setRestricted(false);
        verify(readOnlyAppearance).unapply(RESTRICTED);
        verify(editingAppearance).unapply(RESTRICTED);
    }

    @Test
    public void setEnabled() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.setEnabled(false);
        verify(readOnlyAppearance).unapply(ENABLED);
        verify(editingAppearance).unapply(ENABLED);

        reset(readOnlyAppearance, editingAppearance);

        formItem.setEnabled(true);
        verify(readOnlyAppearance).apply(ENABLED, null);
        verify(editingAppearance).apply(ENABLED, null);
    }

    @Test
    public void setRequired() {
        AbstractFormItem<String> formItem = formItem(false);
        formItem.setRequired(true);
        verify(readOnlyAppearance).apply(REQUIRED, null);
        verify(editingAppearance).apply(REQUIRED, null);

        reset(readOnlyAppearance, editingAppearance);

        formItem.setRequired(false);
        verify(readOnlyAppearance).unapply(REQUIRED);
        verify(editingAppearance).unapply(REQUIRED);
    }

    @Test
    public void setDeprecated() {
        ModelNode modelNode = new ModelNode();
        modelNode.get(SINCE).set("1.2.3");
        modelNode.get(REASON).set("why not");
        Deprecation deprecation = new Deprecation(modelNode);

        AbstractFormItem<String> formItem = formItem(false);
        assertFalse(formItem.isDeprecated());

        formItem.setDeprecated(deprecation);
        assertTrue(formItem.isDeprecated());
        verify(readOnlyAppearance).apply(DEPRECATED, deprecation);
        verify(editingAppearance).apply(DEPRECATED, deprecation);

        formItem.setDeprecated(null);
        assertFalse(formItem.isDeprecated());
        verify(readOnlyAppearance).unapply(DEPRECATED);
        verify(editingAppearance).unapply(DEPRECATED);
    }

    // ------------------------------------------------------ helper methods

    private AbstractFormItem<String> formItem(boolean expression) {
        return formItem(expression, null);
    }

    private AbstractFormItem<String> formItem(boolean expression, String hint) {
        return expression
                ? new TestableFormItemWithExpressionSupport("test", "Test", hint, readOnlyAppearance, editingAppearance)
                : new TestableFormItemWithoutExpressionSupport("test", "Test", hint, readOnlyAppearance,
                editingAppearance);
    }
}