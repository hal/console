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
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.jboss.hal.dmr;

import com.google.gwt.core.client.JsArrayInteger;

public class IEEE754 {

    public static native JsArrayInteger fromFloat(float v)/*-{
        var ebits = 8;
        var fbits = 23;
        var bias = (1 << (ebits - 1)) - 1;

        // Compute sign, exponent, fraction
        var s, e, f;
        if (isNaN(v)) {
            e = (1 << bias) - 1;
            f = 1;
            s = 0;
        }
        else if (v === Infinity || v === -Infinity) {
            e = (1 << bias) - 1;
            f = 0;
            s = (v < 0) ? 1 : 0;
        }
        else if (v === 0) {
            e = 0;
            f = 0;
            s = (1 / v === -Infinity) ? 1 : 0;
        }
        else {
            s = v < 0;
            v = Math.abs(v);

            if (v >= Math.pow(2, 1 - bias)) {
                var ln = Math.min(Math.floor(Math.log(v) / Math.LN2), bias);
                e = ln + bias;
                f = v * Math.pow(2, fbits - ln) - Math.pow(2, fbits);
            }
            else {
                e = 0;
                f = v / Math.pow(2, 1 - bias - fbits);
            }
        }

        // Pack sign, exponent, fraction
        var i, bits = [];
        for (i = fbits; i; i -= 1) {
            bits.push(f % 2 ? 1 : 0);
            f = Math.floor(f / 2);
        }
        for (i = ebits; i; i -= 1) {
            bits.push(e % 2 ? 1 : 0);
            e = Math.floor(e / 2);
        }
        bits.push(s ? 1 : 0);
        bits.reverse();
        var str = bits.join('');

        // Bits to bytes
        var bytes = [];
        while (str.length) {
            bytes.push(parseInt(str.substring(0, 8), 2));
            str = str.substring(8);
        }
        return bytes;
    }-*/;

    public static native float toFloat(byte b1, byte b2, byte b3, byte b4)/*-{
        var ebits = 8;
        var fbits = 23;
        var bytes = arguments;

        // Bytes to bits
        var bits = [];
        for (var i = bytes.length; i; i -= 1) {
            var byteA = bytes[i - 1];
            for (var j = 8; j; j -= 1) {
                bits.push(byteA % 2 ? 1 : 0);
                byteA = byteA >> 1;
            }
        }
        bits.reverse();
        var str = bits.join('');

        // Unpack sign, exponent, fraction
        var bias = (1 << (ebits - 1)) - 1;
        var s = parseInt(str.substring(0, 1), 2) ? -1 : 1;
        var e = parseInt(str.substring(1, 1 + ebits), 2);
        var f = parseInt(str.substring(1 + ebits), 2);

        // Produce number
        if (e === (1 << ebits) - 1) {
            return f !== 0 ? NaN : s * Infinity;
        }
        else if (e > 0) {
            return s * Math.pow(2, e - bias) * (1 + f / Math.pow(2, fbits));
        }
        else if (f !== 0) {
            return s * Math.pow(2, -(bias - 1)) * (f / Math.pow(2, fbits));
        }
        else {
            return s * 0;
        }
    }-*/;


    public static native JsArrayInteger fromDoubleClosure(double a) /*-{
        var f = 11; // ebits
        var c = 52; // fbits
        var b = (1 << f - 1) - 1, d, e;
        if (isNaN(a))
            e = (1 << b) - 1, b = 1, d = 0;
        else if (Infinity === a || -Infinity === a)
            e = (1 << b) - 1, b = 0, d = 0 > a ? 1 : 0;
        else if (0 === a)
            b = e = 0, d = -Infinity === 1 / a ? 1 : 0;
        else if (d = 0 > a, a = Math.abs(a), a >= Math.pow(2, 1 - b)) {
            var g = Math.min(Math.floor(Math.log(a) / Math.LN2), b);
            e = g + b;
            b = a * Math.pow(2, c - g) - Math.pow(2, c)
        }
        else
            e = 0, b = a / Math.pow(2, 1 - b - c);
        for (a = []; c; c -= 1)
            a.push(b % 2 ? 1 : 0), b = Math.floor(b / 2);
        for (c = f; c; c -= 1)
            a.push(e % 2 ? 1 : 0), e = Math.floor(e / 2);
        a.push(d ? 1 : 0);
        a.reverse();
        f = a.join("");
        for (d = []; f.length;)
            d.push(parseInt(f.substring(0, 8), 2)), f = f.substring(8);
        return d;
    }-*/;


    public static native JsArrayInteger fromDouble(double v)/*-{
        var ebits = 11;
        var fbits = 52;
        var bias = (1 << (ebits - 1)) - 1;

        // Compute sign, exponent, fraction
        var s, e, f;
        if (isNaN(v)) {
            e = (1 << bias) - 1;
            f = 1;
            s = 0;
        }
        else if (v === Infinity || v === -Infinity) {
            e = (1 << bias) - 1;
            f = 0;
            s = (v < 0) ? 1 : 0;
        }
        else if (v === 0) {
            e = 0;
            f = 0;
            s = (1 / v === -Infinity) ? 1 : 0;
        }
        else {
            s = v < 0;
            v = Math.abs(v);

            if (v >= Math.pow(2, 1 - bias)) {
                var ln = Math.min(Math.floor(Math.log(v) / Math.LN2), bias);
                e = ln + bias;
                f = v * Math.pow(2, fbits - ln) - Math.pow(2, fbits);
            }
            else {
                e = 0;
                f = v / Math.pow(2, 1 - bias - fbits);
            }
        }

        // Pack sign, exponent, fraction
        var i, bits = [];
        for (i = fbits; i; i -= 1) {
            bits.push(f % 2 ? 1 : 0);
            f = Math.floor(f / 2);
        }
        for (i = ebits; i; i -= 1) {
            bits.push(e % 2 ? 1 : 0);
            e = Math.floor(e / 2);
        }
        bits.push(s ? 1 : 0);
        bits.reverse();
        var str = bits.join('');

        // Bits to bytes
        var bytes = [];
        while (str.length) {
            bytes.push(parseInt(str.substring(0, 8), 2));
            str = str.substring(8);
        }
        return bytes;
    }-*/;

    public static native double toDouble(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) /*-{
        var ebits = 11;
        var fbits = 52;
        var bytes = arguments;

        // Bytes to bits
        var bits = [];
        for (var i = bytes.length; i; i -= 1) {
            var byteA = bytes[i - 1];
            for (var j = 8; j; j -= 1) {
                bits.push(byteA % 2 ? 1 : 0);
                byteA = byteA >> 1;
            }
        }
        bits.reverse();
        var str = bits.join('');

        // Unpack sign, exponent, fraction
        var bias = (1 << (ebits - 1)) - 1;
        var s = parseInt(str.substring(0, 1), 2) ? -1 : 1;
        var e = parseInt(str.substring(1, 1 + ebits), 2);
        var f = parseInt(str.substring(1 + ebits), 2);

        // Produce number
        if (e === (1 << ebits) - 1) {
            return f !== 0 ? NaN : s * Infinity;
        }
        else if (e > 0) {
            return s * Math.pow(2, e - bias) * (1 + f / Math.pow(2, fbits));
        }
        else if (f !== 0) {
            return s * Math.pow(2, -(bias - 1)) * (f / Math.pow(2, fbits));
        }
        else {
            return s * 0;
        }
    }-*/;


    public static native double toDouble(byte[] bytes) /*-{
        var ebits = 11;
        var fbits = 52;

        // Bytes to bits
        var bits = [];
        for (var i = bytes.length; i; i -= 1) {
            var byteA = bytes[i - 1];
            for (var j = 8; j; j -= 1) {
                bits.push(byteA % 2 ? 1 : 0);
                byteA = byteA >> 1;
            }
        }
        bits.reverse();
        var str = bits.join('');

        // Unpack sign, exponent, fraction
        var bias = (1 << (ebits - 1)) - 1;
        var s = parseInt(str.substring(0, 1), 2) ? -1 : 1;
        var e = parseInt(str.substring(1, 1 + ebits), 2);
        var f = parseInt(str.substring(1 + ebits), 2);

        // Produce number
        if (e === (1 << ebits) - 1) {
            return f !== 0 ? NaN : s * Infinity;
        }
        else if (e > 0) {
            return s * Math.pow(2, e - bias) * (1 + f / Math.pow(2, fbits));
        }
        else if (f !== 0) {
            return s * Math.pow(2, -(bias - 1)) * (f / Math.pow(2, fbits));
        }
        else {
            return s * 0;
        }
    }-*/;


    //    function fromIEEE754Double(b) { return fromIEEE754(b, 11, 52); }
    //    function   toIEEE754Double(v) { return   toIEEE754(v, 11, 52); }
    //    function fromIEEE754Single(b) { return fromIEEE754(b,  8, 23); }
    //    function   toIEEE754Single(v) { return   toIEEE754(v,  8, 23); }

    private IEEE754() {
    }
}