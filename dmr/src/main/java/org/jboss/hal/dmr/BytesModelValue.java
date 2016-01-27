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
import java.util.Arrays;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class BytesModelValue extends ModelValue {

    /**
     * JSON Key used to identify BytesModelValue.
     */
    public static final String TYPE_KEY = "BYTES_VALUE";

    private final byte[] bytes;

    BytesModelValue(final byte[] bytes) {
        super(ModelType.BYTES);
        this.bytes = bytes;
    }

    @Override
    void writeExternal(final DataOutput out) throws IOException {
        out.write(bytes);
    }

    @Override
    long asLong() {
        final byte[] bytes = this.bytes;
        final int length = bytes.length;
        final int cnt = Math.min(8, length);
        long v = 0L;
        for (int i = 0; i < cnt; i++) {
            v <<= 8;
            v |= bytes[length - cnt + i] & 0xff;
        }
        return v;
    }

    @Override
    long asLong(final long defVal) {
        return asLong();
    }

    @Override
    int asInt() {
        final byte[] bytes = this.bytes;
        final int length = bytes.length;
        final int cnt = Math.min(4, length);
        int v = 0;
        for (int i = 0; i < cnt; i++) {
            v <<= 8;
            v |= bytes[length - cnt + i] & 0xff;
        }
        return v;
    }

    @Override
    int asInt(final int defVal) {
        return asInt();
    }

    @Override
    double asDouble() {
        throw new IllegalArgumentException();
        //return Double.longBitsToDouble(asLong());
    }

    @Override
    double asDouble(final double defVal) {
        throw new IllegalArgumentException();
        //return Double.longBitsToDouble(asLong());
    }

    @Override
    BigDecimal asBigDecimal() {
        return new BigDecimal(new BigInteger(bytes));
    }

    @Override
    BigInteger asBigInteger() {
        return new BigInteger(bytes);
    }

    @Override
    byte[] asBytes() {
        byte[] clone = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            clone[i] = bytes[i];
        }
        return clone;
    }

    @Override
    String asString() {
        final StringBuilder builder = new StringBuilder(bytes.length * 4 + 4);
        format(builder, 0, false);
        return builder.toString();
    }

    @Override
    public String toJSONString(final boolean compact) {
        final StringBuilder builder = new StringBuilder(bytes.length * 4 + 4);
        formatAsJSON(builder, 0, !compact);
        return builder.toString();
    }

    @Override
    void formatAsJSON(final StringBuilder builder, final int indent, final boolean multiLine) {
        builder.append('{');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        } else {
            builder.append(' ');
        }
        builder.append(jsonEscape(TYPE_KEY));
        builder.append(" : ");
        //builder.append(jsonEscape(Base64.encodeBytes(bytes)));
        if (multiLine) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    void format(final StringBuilder builder, final int indent, final boolean multiLine) {
        builder.append("bytes {");
        if (multiLine) {
            builder.append('\n');
            indent(builder, indent + 1);
        } else {
            builder.append(' ');
        }
        for (int i = 0, length = bytes.length; i < length; i++) {
            final byte b = bytes[i];
            if (b >= 0 && b < 0x10) {
                builder.append("0x0").append(Integer.toHexString(b & 0xff));
            } else {
                builder.append("0x").append(Integer.toHexString(b & 0xff));
            }
            if (i != length - 1) {
                if (multiLine && (i & 7) == 7) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(", ");
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    void formatMultiLine(final StringBuilder target, final int indent) {
        final int length = bytes.length;
        format(target, indent, length > 8);
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
        return other instanceof BytesModelValue && equals((BytesModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(final BytesModelValue other) {
        return this == other || other != null && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes) + 71;
    }
}
