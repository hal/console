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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class LongModelValue extends ModelValue {

    private final long value;

    LongModelValue(long value) {
        super(ModelType.LONG);
        this.value = value;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeLong(value);
    }

    @Override
    long asLong() {
        return value;
    }

    @Override
    long asLong(long defVal) {
        return value;
    }

    @Override
    int asInt() {
        return (int) value;
    }

    @Override
    int asInt(int defVal) {
        return (int) value;
    }

    @Override
    boolean asBoolean() {
        return value != 0;
    }

    @Override
    boolean asBoolean(boolean defVal) {
        return value != 0;
    }

    @Override
    double asDouble() {
        return value;
    }

    @Override
    double asDouble(double defVal) {
        return value;
    }

    @Override
    byte[] asBytes() {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (value >>> 56);
        bytes[1] = (byte) (value >>> 48);
        bytes[2] = (byte) (value >>> 40);
        bytes[3] = (byte) (value >>> 32);
        bytes[4] = (byte) (value >>> 24);
        bytes[5] = (byte) (value >>> 16);
        bytes[6] = (byte) (value >>> 8);
        bytes[7] = (byte) (value);
        return bytes;
    }

    @Override
    BigDecimal asBigDecimal() {
        return new BigDecimal(value);
    }

    @Override
    BigInteger asBigInteger() {
        return BigInteger.valueOf(value);
    }

    @Override
    String asString() {
        return Long.toString(value);
    }

    @Override
    void format(StringBuilder builder, int indent, boolean multiLine) {
        builder.append(value).append('L');
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
        return other instanceof LongModelValue && equals((LongModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(LongModelValue other) {
        return this == other || other != null && other.value == value;
    }

    @Override
    public int hashCode() {
        return (int) value;
    }
}
