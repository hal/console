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
import java.util.Arrays;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class BytesModelValue extends ModelValue {

    /** JSON Key used to identify BytesModelValue. */
    private static final String TYPE_KEY = "BYTES_VALUE";

    private final byte[] bytes;

    BytesModelValue(byte[] bytes) {
        super(ModelType.BYTES);
        this.bytes = bytes;
    }

    BytesModelValue(DataInput in) {
        super(ModelType.BYTES);
        byte[] b = new byte[in.readInt()];
        in.readFully(b);
        this.bytes = b;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    @Override
    long asLong() {
        byte[] bytes = this.bytes;
        int length = bytes.length;
        int cnt = Math.min(8, length);
        long v = 0L;
        for (int i = 0; i < cnt; i++) {
            v <<= 8;
            v |= bytes[length - cnt + i] & 0xff;
        }
        return v;
    }

    @Override
    long asLong(long defVal) {
        return asLong();
    }

    @Override
    int asInt() {
        byte[] bytes = this.bytes;
        int length = bytes.length;
        int cnt = Math.min(4, length);
        int v = 0;
        for (int i = 0; i < cnt; i++) {
            v <<= 8;
            v |= bytes[length - cnt + i] & 0xff;
        }
        return v;
    }

    @Override
    int asInt(int defVal) {
        return asInt();
    }

    @Override
    double asDouble() {
        throw new IllegalArgumentException();
        // return Double.longBitsToDouble(asLong());
    }

    @Override
    double asDouble(double defVal) {
        throw new IllegalArgumentException();
        // return Double.longBitsToDouble(asLong());
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
        StringBuilder builder = new StringBuilder(bytes.length * 4 + 4);
        format(builder, 0, false);
        return builder.toString();
    }

    @Override
    public String toJSONString(boolean compact) {
        StringBuilder builder = new StringBuilder(bytes.length * 4 + 4);
        formatAsJSON(builder, 0, !compact);
        return builder.toString();
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
        // builder.append(jsonEscape(Base64.encodeBytes(bytes)));
        if (multiLine) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    void format(StringBuilder builder, int indent, boolean multiLine) {
        builder.append("bytes {");
        if (multiLine) {
            builder.append('\n');
            indent(builder, indent + 1);
        } else {
            builder.append(' ');
        }
        for (int i = 0, length = bytes.length; i < length; i++) {
            byte b = bytes[i];
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

    void formatMultiLine(StringBuilder target, int indent) {
        int length = bytes.length;
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
    public boolean equals(Object other) {
        return other instanceof BytesModelValue && equals((BytesModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(BytesModelValue other) {
        return this == other || other != null && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes) + 71;
    }
}
