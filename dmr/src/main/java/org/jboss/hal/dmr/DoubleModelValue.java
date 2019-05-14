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
package org.jboss.hal.dmr;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class DoubleModelValue extends ModelValue {

    private final double value;

    DoubleModelValue(double value) {
        super(ModelType.DOUBLE);
        this.value = value;
    }

    @Override
    void writeExternal(DataOutput out) {
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
        throw new IllegalArgumentException();
    }

    @Override
    BigDecimal asBigDecimal() {
        return new BigDecimal(value);
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
}
