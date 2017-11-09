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

import com.google.common.base.Charsets;
import elemental2.core.Array;

class DataOutput {

    private final Array<Byte> bytes;

    DataOutput() {
        bytes = new Array<>();
    }

    @Override
    public String toString() {
        byte[] array = new byte[bytes.getLength()];
        for (int i = 0; i < bytes.getLength(); i++) {
            array[i] = bytes.getAt(i);
        }
        return new String(array, Charsets.ISO_8859_1);
    }

    // ------------------------------------------------------ write a-z

    void write(byte[] values) {
        for (byte value : values) {
            bytes.push(value);
        }
    }

    private void write(Array<Byte> values) {
        for (int i = 0; i < values.getLength(); i++) {
            bytes.push(values.getAt(i));
        }
    }

    void write(int value) {
        writeInt(value);
    }

    void writeBoolean(boolean value) {
        bytes.push((byte) (value ? 1 : 0));
    }

    void writeByte(int value) {
        bytes.push((byte) value);
    }

    void writeDouble(double value) {
        Array<Integer> array = IEEE754.fromDoubleClosure(value);
        for (int i = 0; i < array.getLength(); i++) {
            bytes.push(array.getAt(i).byteValue());
        }
    }

    void writeInt(int value) {
        bytes.push((byte) (value >>> 24));
        bytes.push((byte) ((value >>> 16) & 0xFF));
        bytes.push((byte) ((value >>> 8) & 0xFF));
        bytes.push((byte) (value & 0xFF));
    }

    void writeLong(long value) {
        bytes.push((byte) (value >>> 56));
        bytes.push((byte) ((value >>> 48) & 0xFF));
        bytes.push((byte) ((value >>> 40) & 0xFF));
        bytes.push((byte) ((value >>> 32) & 0xFF));
        bytes.push((byte) ((value >>> 24) & 0xFF));
        bytes.push((byte) ((value >>> 16) & 0xFF));
        bytes.push((byte) ((value >>> 8) & 0xFF));
        bytes.push((byte) (value & 0xFF));
    }

    private void writeShort(int value) {
        bytes.push((byte) (value >>> 8));
        bytes.push((byte) (value & 0xFF));
    }

    void writeUTF(String value) {
        int length = value.length();
        Array<Byte> array = new Array<>();
        char c;
        for (int i = 0; i < length; i++) {
            c = value.charAt(i);
            if (c > 0 && c <= 0x7f) {
                array.push((byte) c);
            } else if (c <= 0x07ff) {
                array.push((byte) (0xc0 | 0x1f & c >> 6));
                array.push((byte) (0x80 | 0x3f & c));
            } else {
                array.push((byte) (0xe0 | 0x0f & c >> 12));
                array.push((byte) (0x80 | 0x3f & c >> 6));
                array.push((byte) (0x80 | 0x3f & c));
            }
        }
        writeShort(array.getLength());
        write(array);
    }
}
