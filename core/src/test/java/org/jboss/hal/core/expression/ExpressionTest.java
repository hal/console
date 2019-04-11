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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("HardCodedStringLiteral")
public class ExpressionTest {

    @Test
    public void isExpression() throws Exception {
        assertFalse(Expression.isExpression(null));
        assertFalse(Expression.isExpression(""));
        assertFalse(Expression.isExpression("   "));
        assertFalse(Expression.isExpression("foo"));
        assertTrue(Expression.isExpression("${foo}"));
        assertTrue(Expression.isExpression("${foo}/bar"));
        assertTrue(Expression.isExpression("my/${foo}/bar"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nil() {
        Expression.of(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void empty() {
        Expression.of("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void blank() {
        Expression.of("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal() {
        Expression.of("foo");
    }

    @Test
    public void keyOnly() {
        Expression expression = Expression.of("${foo}");
        assertEquals("foo", expression.getKey());
        assertNull(expression.getDefaultValue());
    }

    @Test
    public void keyAndDefault() {
        Expression expression = Expression.of("${foo:bar}");
        assertEquals("foo", expression.getKey());
        assertEquals("bar", expression.getDefaultValue());
    }

    @Test
    public void prefixAndkey() {
        Expression expression = Expression.of("prefix/${foo}");
        assertEquals("foo", expression.getKey());
        assertEquals("prefix/", expression.getPrefix());
    }

    @Test
    public void prefixAndkeyAndDefault() {
        Expression expression = Expression.of("prefix/${foo:bar}");
        assertEquals("foo", expression.getKey());
        assertEquals("bar", expression.getDefaultValue());
        assertEquals("prefix/", expression.getPrefix());
    }

    @Test
    public void keyAndDefaultAndSuffix() {
        Expression expression = Expression.of("${foo:bar}/suffix");
        assertEquals("foo", expression.getKey());
        assertEquals("bar", expression.getDefaultValue());
        assertEquals("/suffix", expression.getSuffix());
    }

    @Test
    public void prefixAndkeyAndDefaultAndSuffix() {
        Expression expression = Expression.of("prefix/${foo:bar}/suffix");
        assertEquals("foo", expression.getKey());
        assertEquals("bar", expression.getDefaultValue());
        assertEquals("prefix/", expression.getPrefix());
        assertEquals("/suffix", expression.getSuffix());
    }
}