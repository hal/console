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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.stream.ModelException;
import org.jboss.hal.dmr.stream.ModelWriter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class DoubleModelValue extends ModelValue {

    private final double value;

    DoubleModelValue(double value) {
        super(ModelType.DOUBLE);
        this.value = value;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeByte(ModelType.DOUBLE.typeChar);
        out.writeDouble(value);
    }

    @Override
    long asLong() {
        return (long) value;
    }

    @Override
    long asLong(long defVal) {
        return (long) value;
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
        long value = Double.doubleToLongBits(this.value);
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
        // Use the "pretty" over the "exact"
        return BigDecimal.valueOf(value);
    }

    @Override
    BigInteger asBigInteger() {
        return BigInteger.valueOf((long) value);
    }

    @Override
    String asString() {
        return Double.toString(value);
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
        return other instanceof DoubleModelValue && equals((DoubleModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(DoubleModelValue other) {
        return this == other || other != null && other.value == value;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(value).hashCode();
    }

    @Override
    void write(ModelWriter writer) throws ModelException {
        writer.writeDouble(value);
    }

}
