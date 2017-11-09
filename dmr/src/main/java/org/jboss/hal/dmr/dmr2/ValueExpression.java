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
package org.jboss.hal.dmr.dmr2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A value expression.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ValueExpression implements Externalizable {

    private static final long serialVersionUID = -277358532170444708L;
    private static final Field expressionStringField;

    private final String expressionString;

    static {
        expressionStringField = AccessController.doPrivileged((PrivilegedAction<Field>) () -> {
            Field field;
            try {
                field = ValueExpression.class.getDeclaredField("expressionString");
            } catch (NoSuchFieldException e) {
                throw new NoSuchFieldError(e.getMessage());
            }
            field.setAccessible(true);
            return field;
        });
    }

    /**
     * Quote a string so that it can be used in an expression as a literal string, instead of being expanded.
     *
     * @param string the string to quote
     *
     * @return the quoted string
     */
    public static String quote(String string) {
        return string.replace("$", "$$");
    }

    /**
     * Construct a new instance.
     *
     * @param expressionString the expression string
     */
    public ValueExpression(String expressionString) {
        if (expressionString == null) {
            throw new IllegalArgumentException("expressionString is null");
        }
        this.expressionString = expressionString;
    }

    /**
     * Serialize this instance.
     *
     * @param out the target stream
     *
     * @throws IOException if a serialization error occurs
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(expressionString);
    }

    /**
     * Deserialize this instance.
     *
     * @param in the source stream
     *
     * @throws IOException if a serialization error occurs
     */
    public void readExternal(ObjectInput in) throws IOException {
        String str = in.readUTF();
        try {
            expressionStringField.set(this, str);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the raw expression string.
     *
     * @return the raw expression string (will not be {@code null})
     */
    public String getExpressionString() {
        return expressionString;
    }

    /**
     * Get the hash code of the expression string.
     *
     * @return the hash code
     */
    public int hashCode() {
        return expressionString.hashCode();
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(Object other) {
        return other instanceof ValueExpression && equals((ValueExpression) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(ValueExpression other) {
        return this == other || other != null && expressionString.equals(other.expressionString);
    }

    /**
     * Resolve this expression to a string value.
     *
     * @return the resolved value
     */
    public String resolveString() {
        return resolveString(ValueExpressionResolver.DEFAULT_RESOLVER);
    }

    /**
     * Resolve this expression to a string value.
     *
     * @param resolver the resolver to use
     *
     * @return the resolved value
     */
    public String resolveString(ValueExpressionResolver resolver) {
        return resolver.resolve(this);
    }

    /**
     * Resolve this expression to a {@code boolean} value.
     *
     * @return the resolved value
     */
    public boolean resolveBoolean() {
        String value = resolveString();
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Resolve this expression to a {@code boolean} value.
     *
     * @param resolver the resolver to use
     *
     * @return the resolved value
     */
    public boolean resolveBoolean(ValueExpressionResolver resolver) {
        String value = resolveString(resolver);
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Resolve this expression to an {@code int} value.
     *
     * @return the resolved value
     */
    public int resolveInt() {
        return Integer.parseInt(resolveString());
    }

    /**
     * Resolve this expression to an {@code int} value.
     *
     * @param resolver the resolver to use
     *
     * @return the resolved value
     */
    public int resolveInt(ValueExpressionResolver resolver) {
        return Integer.parseInt(resolveString(resolver));
    }

    /**
     * Resolve this expression to a {@code long} value.
     *
     * @return the resolved value
     */
    public long resolveLong() {
        return Long.parseLong(resolveString());
    }

    /**
     * Resolve this expression to a {@code long} value.
     *
     * @param resolver the resolver to use
     *
     * @return the resolved value
     */
    public long resolveLong(ValueExpressionResolver resolver) {
        return Long.parseLong(resolveString(resolver));
    }

    /**
     * Resolve this expression to a large integer value.
     *
     * @return the resolved value
     */
    public BigInteger resolveBigInteger() {
        return new BigInteger(resolveString());
    }

    /**
     * Resolve this expression to a large integer value.
     *
     * @param resolver the resolver to use
     *
     * @return the resolved value
     */
    public BigInteger resolveBigInteger(ValueExpressionResolver resolver) {
        return new BigInteger(resolveString(resolver));
    }

    /**
     * Resolve this expression to a decimal value.
     *
     * @return the resolved value
     */
    public BigDecimal resolveBigDecimal() {
        return new BigDecimal(resolveString());
    }

    /**
     * Resolve this expression to a decimal value.
     *
     * @param resolver the resolver to use
     *
     * @return the resolved value
     */
    public BigDecimal resolveBigDecimal(ValueExpressionResolver resolver) {
        return new BigDecimal(resolveString(resolver));
    }

    /**
     * Get a printable string representation of this object.
     *
     * @return the string
     */
    public String toString() {
        return "Expression \"" + expressionString + "\"";
    }
}
