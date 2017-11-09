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
package org.jboss.hal.dmr;

/**
 * A value expression.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class ValueExpression {

    private String expressionString;

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
    ValueExpression(String expressionString) {
        if (expressionString == null) {
            throw new IllegalArgumentException("expressionString is null");
        }
        this.expressionString = expressionString;
    }

    /**
     * Get the raw expression string.
     *
     * @return the raw expression string (will not be {@code null})
     */
    String getExpressionString() {
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
     * Get a printable string representation of this object.
     *
     * @return the string
     */
    public String toString() {
        return "Expression \"" + expressionString + "\"";
    }
}
