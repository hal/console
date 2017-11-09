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
import jsinterop.annotations.JsMethod;

/**
 * Encodes and decodes to and from Base64 notation.
 *
 * @author Robert Harder
 * @author rob@iharder.net
 * @version 2.1
 */
public class Base64 {

    /** The equals sign (=) as a byte. */
    private static final byte EQUALS_SIGN = (byte) '=';

    /** Preferred encoding. */
    private static final String PREFERRED_ENCODING = "UTF-8";

    /** The 64 valid Base64 values. */
    private static final byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(
            Charsets.UTF_8);


    // ------------------------------------------------------ encode

    @JsMethod(namespace = "Window", name = "btoa")
    public static native String encode(String decoded);

    public static String encodeBytes(byte[] source) {
        int len43 = source.length * 4 / 3;
        byte[] outBuff = new byte[(len43)   // Main 4:3
                + ((source.length % 3) > 0 ? 4 : 0)]; // Account for padding
        int d = 0;
        int e = 0;
        int len2 = source.length - 2;
        for (; d < len2; d += 3, e += 4) {
            encode3to4(source, d, 3, outBuff, e);
        }

        if (d < source.length) {
            encode3to4(source, d, source.length - d, outBuff, e);
            e += 4;
        }

        // Return value according to relevant encoding.
        try {
            return new String(outBuff, 0, e, PREFERRED_ENCODING);
        } catch (java.io.UnsupportedEncodingException ignored) {
            return new String(outBuff, 0, e);
        }
    }

    /**
     * Encodes up to three bytes of the array <var>source</var> and writes the resulting four Base64 bytes to
     * <var>destination</var>. The source and destination arrays can be manipulated anywhere along their length by
     * specifying
     * <var>srcOffset</var> and <var>destOffset</var>. This method does not check to make sure your arrays are large
     * enough to
     * accomodate <var>srcOffset</var> + 3 for the <var>source</var> array or <var>destOffset</var> + 4 for the
     * <var>destination</var> array. The actual number of significant bytes in your array is given by
     * <var>numSigBytes</var>.
     *
     * @param source      the array to convert
     * @param srcOffset   the index where conversion begins
     * @param numSigBytes the number of significant bytes in your array
     * @param destination the array to hold the conversion
     * @param destOffset  the index where output will be put
     *
     * @return the <var>destination</var> array
     *
     * @since 1.3
     */
    private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes,
            byte[] destination,
            int destOffset) {
        // 1 2 3
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------| || || || | Six bit groups to index ALPHABET
        // >>18 >>12 >> 6 >> 0 Right shift necessary
        // 0x3f 0x3f 0x3f Additional AND

        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
                | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
                | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

        switch (numSigBytes) {
            case 3:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
                return destination;

            case 2:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;

            case 1:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = EQUALS_SIGN;
                destination[destOffset + 3] = EQUALS_SIGN;
                return destination;

            default:
                return destination;
        }
    }


    // ------------------------------------------------------ decode

    @JsMethod(namespace = "Window", name = "atob")
    public static native String decode(String encoded);

    /** Defeats instantiation. */
    private Base64() {
    }
}
