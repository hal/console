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

import com.google.gwt.core.client.JsArrayInteger;

import java.io.IOException;

public class DataOutput {

    private byte[] bytes;
    private int pos;

    public DataOutput() {
        bytes = new byte[50];
    }

    public byte[] getBytes() {
        byte[] array = new byte[pos];
        for (int i = 0; i < pos; i++) { array[i] = bytes[i]; }

        return array;
    }

    private void growToFit(int size) {
        if (pos + size >= bytes.length) {
            byte[] array = new byte[bytes.length + size];
            for (int i = 0; i < bytes.length; i++) {
                array[i] = bytes[i];
            }

            bytes = array;
        }
    }

    public void writeBoolean(boolean v) throws IOException {
        growToFit(1);
        bytes[pos++] = v ? (byte) 1 : (byte) 0;
    }

    public void writeByte(int v) throws IOException {
        growToFit(1);
        bytes[pos++] = (byte) v;

    }

    public void writeShort(int v) throws IOException {
        growToFit(2);
        bytes[pos++] = (byte) (v >>> 8);
        bytes[pos++] = (byte) (v & 0xFF);

    }

    public void writeChar(int v) throws IOException {
        growToFit(2);
        bytes[pos++] = (byte) (v >>> 8);
        bytes[pos++] = (byte) (v & 0xFF);
    }

    public void writeInt(int v) throws IOException {
        growToFit(4);
        bytes[pos++] = (byte) (v >>> 24);
        bytes[pos++] = (byte) ((v >>> 16) & 0xFF);
        bytes[pos++] = (byte) ((v >>> 8) & 0xFF);
        bytes[pos++] = (byte) (v & 0xFF);
    }

    public void writeLong(long v) throws IOException {
        growToFit(8);
        bytes[pos++] = (byte) (v >>> 56);
        bytes[pos++] = (byte) ((v >>> 48) & 0xFF);
        bytes[pos++] = (byte) ((v >>> 40) & 0xFF);
        bytes[pos++] = (byte) ((v >>> 32) & 0xFF);
        bytes[pos++] = (byte) ((v >>> 24) & 0xFF);
        bytes[pos++] = (byte) ((v >>> 16) & 0xFF);
        bytes[pos++] = (byte) ((v >>> 8) & 0xFF);
        bytes[pos++] = (byte) (v & 0xFF);
    }

    public void writeFloat(float v) throws IOException {
        growToFit(4);
        JsArrayInteger bytes = IEEE754.fromFloat(v);
        for (int i = 0; i < 4; i++) {
            this.bytes[pos++] = (byte) bytes.get(i);
        }
    }

    public void writeDouble(double v) throws IOException {
        growToFit(8);
        JsArrayInteger bytes = IEEE754.fromDoubleClosure(v);
        for (int i = 0; i < 8; i++) {
            this.bytes[pos++] = (byte) bytes.get(i);
        }
    }

    public void writeUTF(String s) throws IOException {
        final int length = s.length();
        final byte[] bytes = new byte[length * 3];
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

    public void write(byte[] bits) {
        growToFit(bits.length);
        for (int i = 0; i < bits.length; i++) { bytes[pos++] = bits[i]; }
    }

    public void write(byte[] b, int off, int len) {
        growToFit(len);
        for (int i = 0; i < len; i++) {
            bytes[pos++] = b[off + i];
        }
    }
}
