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

import org.jboss.hal.dmr.stream.ModelException;
import org.jboss.hal.dmr.stream.ModelWriter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class ExpressionValue extends ModelValue {

    /** JSON Key used to identify ExpressionValue. */
    private static final String TYPE_KEY = "EXPRESSION_VALUE";

    private final ValueExpression valueExpression;

    ExpressionValue(String expressionString) {
        this(new ValueExpression(expressionString));
    }

    ExpressionValue(ValueExpression valueExpression) {
        super(ModelType.EXPRESSION);
        this.valueExpression = valueExpression;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeByte(ModelType.EXPRESSION.typeChar);
        out.writeUTF(valueExpression.getExpressionString());
    }

    @Override
    String asString() {
        return valueExpression.getExpressionString();
    }

    @Override
    void format(StringBuilder builder, int indent, boolean multiLine) {
        builder.append("expression ");
        builder.append(quote(valueExpression.getExpressionString()));
    }

    @Override
    void formatAsJSON(StringBuilder builder, int indent, boolean multiLine) {
        builder.append('{');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        } else {
            builder.append(' ');
        }
        builder.append(jsonEscape(TYPE_KEY));
        builder.append(" : ");
        builder.append(jsonEscape(asString()));
        if (multiLine) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExpressionValue && equals((ExpressionValue) other);
    }

    public boolean equals(ExpressionValue other) {
        return this == other || other != null && valueExpression.equals(other.valueExpression);
    }

    @Override
    public int hashCode() {
        return valueExpression.hashCode();
    }

    @Override
    void write(ModelWriter writer) throws ModelException {
        writer.writeExpression(valueExpression.getExpressionString());
    }
}
