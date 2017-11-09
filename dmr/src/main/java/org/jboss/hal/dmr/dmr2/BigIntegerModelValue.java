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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.dmr2.stream.ModelException;
import org.jboss.hal.dmr.dmr2.stream.ModelWriter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class BigIntegerModelValue extends ModelValue {

    private final BigInteger value;

    BigIntegerModelValue(BigInteger value) {
        super(ModelType.BIG_INTEGER);
        this.value = value;
    }

    BigIntegerModelValue(DataInput in) throws IOException {
        super(ModelType.BIG_INTEGER);
        byte[] b = new byte[in.readInt()];
        in.readFully(b);
        this.value = new BigInteger(b);
    }

    @Override
    void writeExternal(DataOutput out) throws IOException {
        out.write(ModelType.BIG_INTEGER.typeChar);
        byte[] b = value.toByteArray();
        out.writeInt(b.length);
        out.write(b);
    }

    @Override
    long asLong() {
        return value.longValue();
    }

    @Override
    long asLong(long defVal) {
        return value.longValue();
    }

    @Override
    int asInt() {
        return value.intValue();
    }

    @Override
    int asInt(int defVal) {
        return value.intValue();
    }

    @Override
    boolean asBoolean() {
        return !value.equals(BigInteger.ZERO);
    }

    @Override
    boolean asBoolean(boolean defVal) {
        return !value.equals(BigInteger.ZERO);
    }

    @Override
    double asDouble() {
        return value.doubleValue();
    }

    @Override
    double asDouble(double defVal) {
        return value.doubleValue();
    }

    @Override
    BigDecimal asBigDecimal() {
        return new BigDecimal(value);
    }

    @Override
    BigInteger asBigInteger() {
        return value;
    }

    @Override
    String asString() {
        return value.toString();
    }

    @Override
    ValueExpression asExpression() {
        return new ValueExpression(asString());
    }

    @Override
    void format(PrintWriter writer, int indent, boolean ignored) {
        writer.append("big integer ");
        writer.append(asString());
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
        return other instanceof BigIntegerModelValue && equals((BigIntegerModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(BigIntegerModelValue other) {
        return this == other || other != null && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    void write(ModelWriter writer) throws IOException, ModelException {
        writer.writeBigInteger(value);
    }

}
