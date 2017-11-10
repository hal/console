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

class DataInput {

    private final byte[] bytes;
    private int pos = 0;

    DataInput(byte[] bytes) {
        this.bytes = bytes;
    }


    // ------------------------------------------------------ read a-z

    private int read() {
        if (pos >= bytes.length) {
            return -1;
        }
        return bytes[pos++] & 0xFF;
    }

    boolean readBoolean() {
        return readByte() != 0;
    }

    byte readByte() {
        int i = read();
        if (i == -1) {
            throw new RuntimeException("EOF");
        }
        return (byte) i;
    }

    char readChar() {
        int a = readUnsignedByte();
        int b = readUnsignedByte();
        return (char) ((a << 8) | b);
    }

    double readDouble() {
        // See  https://issues.jboss.org/browse/AS7-4126
        //return IEEE754.toDouble(bytes[pos++], bytes[pos++], bytes[pos++], bytes[pos++], bytes[pos++], bytes[pos++], bytes[pos++], bytes[pos++]);
        byte[] doubleBytes = new byte[8];
        readFully(doubleBytes);

        return IEEE754.toDouble(
                doubleBytes[0],
                doubleBytes[1],
                doubleBytes[2],
                doubleBytes[3],
                doubleBytes[4],
                doubleBytes[5],
                doubleBytes[6],
                doubleBytes[7]);
    }

    void readFully(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            b[i] = bytes[pos++];
        }
    }

    int readInt() {
        int a = readUnsignedByte();
        int b = readUnsignedByte();
        int c = readUnsignedByte();
        int d = readUnsignedByte();
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    long readLong() {
        byte[] longBytes = new byte[8];
        readFully(longBytes);

        return (((long) longBytes[0] << 56) +
                ((long) (longBytes[1] & 255) << 48) +
                ((long) (longBytes[2] & 255) << 40) +
                ((long) (longBytes[3] & 255) << 32) +
                ((long) (longBytes[4] & 255) << 24) +
                ((longBytes[5] & 255) << 16) +
                ((longBytes[6] & 255) << 8) +
                ((longBytes[7] & 255) << 0));

    }

    short readShort() {
        int a = readUnsignedByte();
        int b = readUnsignedByte();
        return (short) ((a << 8) | b);
    }

    private int readUnsignedByte() {
        int i = read();
        if (i == -1) {
            throw new RuntimeException("EOF");
        }
        return i;
    }

    private int readUnsignedShort() {
        int a = readUnsignedByte();
        int b = readUnsignedByte();
        return ((a << 8) | b);
    }

    String readUTF() {
        int bytes = readUnsignedShort();
        StringBuilder sb = new StringBuilder();

        while (bytes > 0) {
            bytes -= readUTFChar(sb);
        }

        return sb.toString();
    }

    private int readUTFChar(StringBuilder sb) {
        int a = readUnsignedByte();
        if (a < 0x80) {
            sb.append((char) a);
            return 1;
        } else if (a < 0xc0) {
            sb.append('?');
            return 1;
        } else if (a < 0xe0) {
            int b = readUnsignedByte();
            if ((b & 0xc0) != 0x80) {
                sb.append('?');
                // probably a US-ASCII char after a Latin-1 char
                sb.append((char) b);
            } else {
                sb.append((char) ((a & 0x1F) << 6 | b & 0x3F));
            }
            return 2;
        } else if (a < 0xf0) {
            int b = readUnsignedByte();
            if ((b & 0xc0) != 0x80) {
                sb.append('?');
                sb.append((char) b);
                return 2;
            }
            int c = readUnsignedByte();
            if ((c & 0xc0) != 0x80) {
                // probably a US-ASCII char after two Latin-1 chars?
                sb.append('?').append('?');
                sb.append((char) c);
            } else {
                sb.append((char) ((a & 0x0F) << 12 | (b & 0x3F) << 6 | c & 0x3F));
            }
            return 3;
        } else {
            sb.append('?');
            return 1;
        }
    }
}
