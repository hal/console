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

    public DataOutput() {
        bytes = new Array<>();
    }

    @Override
    public String toString() {
        int length = bytes.getLength();
        byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = bytes.getAt(i);
        }
        return new String(array, Charsets.ISO_8859_1);
    }


    // ------------------------------------------------------ write a-z

    void write(byte[] bits) {
        for (int i = 0; i < bits.length; i++) {
            bytes.push(bits[i]);
        }
    }

    private void write(byte[] b, int off, int len) {
        for (int i = 0; i < len; i++) {
            bytes.push(b[off + i]);
        }
    }

    void writeBoolean(boolean v) {
        bytes.push(v ? (byte) 1 : (byte) 0);
    }

    void writeByte(int v) {
        bytes.push((byte) v);
    }

    void writeChar(int v) {
        bytes.push((byte) (v >>> 8));
        bytes.push((byte) (v & 0xFF));
    }

    void writeDouble(double v) {
        Array<Integer> array = IEEE754.fromDoubleClosure(v);
        for (int i = 0; i < 8; i++) {
            bytes.push(array.getAt(i).byteValue());
        }
    }

    void writeInt(int v) {
        bytes.push((byte) (v >>> 24));
        bytes.push((byte) ((v >>> 16) & 0xFF));
        bytes.push((byte) ((v >>> 8) & 0xFF));
        bytes.push((byte) (v & 0xFF));
    }

    void writeLong(long v) {
        bytes.push((byte) (v >>> 56));
        bytes.push((byte) ((v >>> 48) & 0xFF));
        bytes.push((byte) ((v >>> 40) & 0xFF));
        bytes.push((byte) ((v >>> 32) & 0xFF));
        bytes.push((byte) ((v >>> 24) & 0xFF));
        bytes.push((byte) ((v >>> 16) & 0xFF));
        bytes.push((byte) ((v >>> 8) & 0xFF));
        bytes.push((byte) (v & 0xFF));
    }

    private void writeShort(int v) {
        bytes.push((byte) (v >>> 8));
        bytes.push((byte) (v & 0xFF));
    }

    void writeUTF(String s) {
        int length = s.length();
        byte[] bytes = new byte[length * 3];
        int bl = 0;
        char c;
        for (int i = 0; i < length; i++) {
            c = s.charAt(i);
            if (c > 0 && c <= 0x7f) {
                bytes[bl++] = (byte) c;
            } else if (c <= 0x07ff) {
                bytes[bl++] = (byte) (0xc0 | 0x1f & c >> 6);
                bytes[bl++] = (byte) (0x80 | 0x3f & c);
            } else {
                bytes[bl++] = (byte) (0xe0 | 0x0f & c >> 12);
                bytes[bl++] = (byte) (0x80 | 0x3f & c >> 6);
                bytes[bl++] = (byte) (0x80 | 0x3f & c);
            }
        }
        writeShort(bl);
        write(bytes, 0, bl);
    }
}
