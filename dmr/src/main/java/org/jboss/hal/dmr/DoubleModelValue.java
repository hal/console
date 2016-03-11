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

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.hal.dmr;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class DoubleModelValue extends ModelValue {

    private final double value;

    DoubleModelValue(final double value) {
        super(ModelType.DOUBLE);
        this.value = value;
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        out.writeDouble(value);
    }

    @Override
    long asLong() {
        return (long) value;
    }

    @Override
    long asLong(final long defVal) {
        return (long) value;
    }

    @Override
    int asInt() {
        return (int) value;
    }

    @Override
    int asInt(final int defVal) {
        return (int) value;
    }

    @Override
    boolean asBoolean() {
        return value != 0;
    }

    @Override
    boolean asBoolean(final boolean defVal) {
        return value != 0;
    }

    @Override
    double asDouble() {
        return value;
    }

    @Override
    double asDouble(final double defVal) {
        return value;
    }

    @Override
    byte[] asBytes() {
        throw new IllegalArgumentException();
        //        final long value = Double.doubleToLongBits(this.value);
        //        final byte[] bytes = new byte[8];
        //        bytes[0] = (byte) (value >>> 56);
        //        bytes[1] = (byte) (value >>> 48);
        //        bytes[2] = (byte) (value >>> 40);
        //        bytes[3] = (byte) (value >>> 32);
        //        bytes[4] = (byte) (value >>> 24);
        //        bytes[5] = (byte) (value >>> 16);
        //        bytes[6] = (byte) (value >>> 8);
        //        bytes[7] = (byte) (value);
        //        return bytes;
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
    public boolean equals(final Object other) {
        return other instanceof DoubleModelValue && equals((DoubleModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(final DoubleModelValue other) {
        return this == other || other != null && other.value == value;
    }

    @Override
    public int hashCode() {
        return Double.valueOf(value).hashCode();
    }
}
