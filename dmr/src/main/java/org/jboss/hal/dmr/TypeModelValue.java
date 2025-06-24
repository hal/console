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
package org.jboss.hal.dmr;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class TypeModelValue extends ModelValue {

    /** JSON Key used to identify TypeModelValue. */
    private static final String TYPE_KEY = "TYPE_MODEL_VALUE";
    private static final TypeModelValue BIG_DECIMAL = new TypeModelValue(ModelType.BIG_DECIMAL);
    private static final TypeModelValue BIG_INTEGER = new TypeModelValue(ModelType.BIG_INTEGER);
    private static final TypeModelValue BOOLEAN = new TypeModelValue(ModelType.BOOLEAN);
    private static final TypeModelValue BYTES = new TypeModelValue(ModelType.BYTES);
    private static final TypeModelValue DOUBLE = new TypeModelValue(ModelType.DOUBLE);
    private static final TypeModelValue EXPRESSION = new TypeModelValue(ModelType.EXPRESSION);
    private static final TypeModelValue INT = new TypeModelValue(ModelType.INT);
    private static final TypeModelValue LONG = new TypeModelValue(ModelType.LONG);
    private static final TypeModelValue LIST = new TypeModelValue(ModelType.LIST);
    private static final TypeModelValue OBJECT = new TypeModelValue(ModelType.OBJECT);
    private static final TypeModelValue PROPERTY = new TypeModelValue(ModelType.PROPERTY);
    private static final TypeModelValue STRING = new TypeModelValue(ModelType.STRING);
    private static final TypeModelValue TYPE = new TypeModelValue(ModelType.TYPE);
    private static final TypeModelValue UNDEFINED = new TypeModelValue(ModelType.UNDEFINED);

    static TypeModelValue of(final ModelType type) {
        switch (type) {
            case BIG_DECIMAL:
                return BIG_DECIMAL;
            case BIG_INTEGER:
                return BIG_INTEGER;
            case BOOLEAN:
                return BOOLEAN;
            case BYTES:
                return BYTES;
            case DOUBLE:
                return DOUBLE;
            case EXPRESSION:
                return EXPRESSION;
            case INT:
                return INT;
            case LIST:
                return LIST;
            case LONG:
                return LONG;
            case OBJECT:
                return OBJECT;
            case PROPERTY:
                return PROPERTY;
            case STRING:
                return STRING;
            case TYPE:
                return TYPE;
            default:
                return UNDEFINED;
        }
    }

    private final ModelType value;

    private TypeModelValue(ModelType value) {
        super(ModelType.TYPE);
        this.value = value;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeByte(value.getTypeChar());
    }

    @Override
    boolean asBoolean() {
        return value != ModelType.UNDEFINED;
    }

    @Override
    boolean asBoolean(boolean defVal) {
        return value != ModelType.UNDEFINED;
    }

    @Override
    String asString() {
        return value.toString();
    }

    @Override
    ModelType asType() {
        return value;
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

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof TypeModelValue && equals((TypeModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(TypeModelValue other) {
        return this == other || other != null && other.value == value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
