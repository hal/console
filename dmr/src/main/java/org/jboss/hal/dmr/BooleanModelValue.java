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
final class BooleanModelValue extends ModelValue {

    static final BooleanModelValue TRUE = new BooleanModelValue(true);
    static final BooleanModelValue FALSE = new BooleanModelValue(false);

    static BooleanModelValue valueOf(final boolean value) {
        return value ? TRUE : FALSE;
    }
    private final boolean value;

    private BooleanModelValue(final boolean value) {
        super(ModelType.BOOLEAN);
        this.value = value;
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        out.writeBoolean(value);
    }

    @Override
    long asLong() {
        return value ? 1 : 0;
    }

    @Override
    long asLong(final long defVal) {
        return value ? 1 : 0;
    }

    @Override
    int asInt() {
        return value ? 1 : 0;
    }

    @Override
    int asInt(final int defVal) {
        return value ? 1 : 0;
    }

    @Override
    boolean asBoolean() {
        return value;
    }

    @Override
    boolean asBoolean(final boolean defVal) {
        return value;
    }

    @Override
    double asDouble() {
        return value ? 1.0 : 0.0;
    }

    @Override
    double asDouble(final double defVal) {
        return value ? 1.0 : 0.0;
    }

    @Override
    byte[] asBytes() {
        return value ? new byte[]{1} : new byte[]{0};
    }

    @Override
    BigDecimal asBigDecimal() {
        return value ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    @Override
    BigInteger asBigInteger() {
        return value ? BigInteger.ONE : BigInteger.ZERO;
    }

    @Override
    String asString() {
        return Boolean.toString(value);
    }

    @Override
    public boolean equals(final Object other) {
        return other == this;
    }

    @Override
    public int hashCode() {
        return Boolean.valueOf(value).hashCode();
    }
}
