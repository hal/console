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
package org.jboss.hal.dmr.stream;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.ModelType;

import static java.lang.Math.min;
import static java.lang.String.valueOf;
import static org.jboss.hal.dmr.stream.ModelConstants.*;
import static org.jboss.hal.dmr.stream.Utils.*;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class JsonWriterImpl implements ModelWriter {

    private final JsonGrammarAnalyzer analyzer;
    private final Writer out;
    private final char[] buffer = new char[1024];
    private int limit;
    private boolean closed;

    JsonWriterImpl(Writer out) {
        this.out = out;
        analyzer = new JsonGrammarAnalyzer();
    }

    @Override
    public void close() throws IOException, ModelException {
        if (closed) {
            return; // idempotent
        }
        closed = true;
        if (limit > 0) {
            out.write(buffer, 0, limit);
            limit = 0;
        }
        if (!analyzer.finished) {
            throw analyzer.newModelException("Uncomplete DMR stream have been written");
        }
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
        if (limit > 0) {
            out.write(buffer, 0, limit);
            limit = 0;
        }
        out.flush();
    }

    @Override
    public ModelWriter writeObjectStart() throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putObjectStart();
        write(OBJECT_START);
        return this;
    }

    @Override
    public ModelWriter writeObjectEnd() throws IOException, ModelException {
        ensureOpen();
        analyzer.putObjectEnd();
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeListStart() throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putListStart();
        write(LIST_START);
        return this;
    }

    @Override
    public ModelWriter writeListEnd() throws IOException, ModelException {
        ensureOpen();
        analyzer.putListEnd();
        write(LIST_END);
        return this;
    }

    @Override
    public ModelWriter writePropertyStart() throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putPropertyStart();
        write(OBJECT_START);
        return this;
    }

    @Override
    public ModelWriter writePropertyEnd() throws IOException, ModelException {
        ensureOpen();
        analyzer.putPropertyEnd();
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeExpression(String data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putExpression();
        write(OBJECT_START);
        encode(EXPRESSION_VALUE);
        write(COLON);
        encode(data);
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeBytes(byte[] data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putBytes();
        write(OBJECT_START);
        encode(BYTES_VALUE);
        write(COLON);
        base64Encode(data);
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeType(ModelType data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putType();
        write(OBJECT_START);
        write(QUOTE);
        write(TYPE_MODEL_VALUE);
        write(QUOTE);
        write(COLON);
        write(QUOTE);
        write(data.toString());
        write(QUOTE);
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeString(String data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putString();
        encode(data);
        return this;
    }

    @Override
    public ModelWriter writeUndefined() throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putUndefined();
        write(NULL, 0, NULL.length());
        return this;
    }

    @Override
    public ModelWriter writeBoolean(boolean data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putBoolean();
        if (data) {
            write(TRUE);
        } else {
            write(FALSE);
        }
        return this;
    }

    @Override
    public ModelWriter writeInt(int data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putNumber(ModelEvent.INT);
        encode(data);
        return this;
    }

    @Override
    public ModelWriter writeLong(long data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putNumber(ModelEvent.LONG);
        encode(data);
        return this;
    }

    @Override
    public ModelWriter writeDouble(double data) throws IOException, ModelException {
        return writeNumber(valueOf(data), ModelEvent.DOUBLE);
    }

    @Override
    public ModelWriter writeBigInteger(BigInteger data) throws IOException, ModelException {
        assertNotNullParameter(data);
        return writeNumber(valueOf(data), ModelEvent.BIG_INTEGER);
    }

    @Override
    public ModelWriter writeBigDecimal(BigDecimal data) throws IOException, ModelException {
        assertNotNullParameter(data);
        return writeNumber(valueOf(data), ModelEvent.BIG_DECIMAL);
    }

    private ModelWriter writeNumber(String data, ModelEvent numberEvent) throws IOException, ModelException {
        ensureOpen();
        writeOptionalColonOrComma();
        analyzer.putNumber(numberEvent);
        write(data, 0, data.length());
        return this;
    }

    private void writeOptionalColonOrComma() throws IOException, ModelException {
        if (analyzer.isColonExpected()) {
            analyzer.putColon();
            write(COLON);
        } else if (analyzer.isCommaExpected()) {
            analyzer.putComma();
            write(COMMA);
        }
    }

    private void write(String data) throws IOException {
        write(data, 0, data.length());
    }

    private void write(char c) throws IOException {
        if (limit == buffer.length) {
            out.write(buffer, 0, limit);
            limit = 0;
        }

        buffer[limit++] = c;
    }

    private void write(String data, int dataBegin, int dataEnd) throws IOException {
        int count;
        while (dataBegin < dataEnd) {
            count = min(dataEnd - dataBegin, buffer.length - limit);
            data.getChars(dataBegin, dataBegin + count, buffer, limit);
            dataBegin += count;
            limit += count;
            if (limit == buffer.length) {
                out.write(buffer, 0, buffer.length);
                limit = 0;
            }
        }
    }

    private void encode(String s) throws IOException {
        char c;
        write(QUOTE);
        int dataBegin = 0;
        for (int dataEnd = 0; dataEnd < s.length(); dataEnd++) {
            c = s.charAt(dataEnd);
            // identify unescaped string sequence
            while (c != BACKSLASH && c != QUOTE && !isControl(c)) {
                if (++dataEnd < s.length()) {
                    c = s.charAt(dataEnd);
                } else {
                    break;
                }
            }
            // write unescaped characters
            if (dataBegin < dataEnd) {
                write(s, dataBegin, dataEnd);
                if (dataEnd == s.length()) {
                    break;
                }
            }
            // escape characters
            dataBegin = dataEnd + 1;
            write(BACKSLASH);
            if (c == BACKSLASH || c == QUOTE) {
                write(c);
            } else if (c == BACKSPACE) {
                write('b');
            } else if (c == FORMFEED) {
                write('f');
            } else if (c == NL) {
                write('n');
            } else if (c == CR) {
                write('r');
            } else if (c == TAB) {
                write('t');
            } else {
                write('u');
                String hexString = Integer.toHexString(c);
                for (int j = 0; j < (4 - hexString.length()); j++) {
                    write('0');
                }
                write(hexString, 0, hexString.length());
            }
        }
        write(QUOTE);
    }

    private void base64Encode(byte[] data) throws IOException {
        write(QUOTE);
        int b;
        for (int i = 0; i < data.length; i += 3) {
            // ensure buffer capacity
            if (buffer.length - limit < 4) {
                out.write(buffer, 0, limit);
                limit = 0;
            }
            if (i > 0 && i % 57 == 0) {
                // write padding
                System.arraycopy(BASE64_NEWLINE, 0, buffer, limit, 4);
                limit += 4;
            } else {
                // base64 encode
                b = (data[i] & 0xFC) >> 2;
                buffer[limit++] = BASE64_ENC_TABLE[b];
                b = (data[i] & 0x03) << 4;
                if (i + 1 < data.length) {
                    b |= (data[i + 1] & 0xF0) >> 4;
                    buffer[limit++] = BASE64_ENC_TABLE[b];
                    b = (data[i + 1] & 0x0F) << 2;
                    if (i + 2 < data.length) {
                        b |= (data[i + 2] & 0xC0) >> 6;
                        buffer[limit++] = BASE64_ENC_TABLE[b];
                        b = data[i + 2] & 0x3F;
                        buffer[limit++] = BASE64_ENC_TABLE[b];
                    } else {
                        buffer[limit++] = BASE64_ENC_TABLE[b];
                        buffer[limit++] = EQUAL;
                    }
                } else {
                    buffer[limit++] = BASE64_ENC_TABLE[b];
                    buffer[limit++] = EQUAL;
                    buffer[limit++] = EQUAL;
                }
            }
        }
        write(QUOTE);
    }

    private void encode(long l) throws IOException {
        // cannot write all possible long values if less than 20 chars is remaining
        if (buffer.length - limit < 20) {
            out.write(buffer, 0, limit);
            limit = 0;
        }

        // compute bounds
        long longQuotient;
        int remainder;
        int writeIndex = limit + stringSizeOf(l);
        limit = writeIndex;

        // always convert to negative number
        boolean negative = l < 0;
        if (!negative) {
            l = -l;
        }

        // processing upper 32 bits (long operations are slower on CPU)
        while (l < Integer.MIN_VALUE) {
            longQuotient = l / 100;
            remainder = (int) ((longQuotient * 100) - l);
            l = longQuotient;
            buffer[--writeIndex] = ONES[remainder];
            buffer[--writeIndex] = TENS[remainder];
        }

        // processing lower 32 bits (int operations are faster on CPU)
        int intQuotient;
        int i = (int) l;
        while (i <= -100) {
            intQuotient = i / 100;
            remainder = (intQuotient * 100) - i;
            i = intQuotient;
            buffer[--writeIndex] = ONES[remainder];
            buffer[--writeIndex] = TENS[remainder];
        }

        // processing remaining digits
        intQuotient = i / 10;
        remainder = (intQuotient * 10) - i;
        buffer[--writeIndex] = (char) ('0' + remainder);

        if (intQuotient < 0) {
            buffer[--writeIndex] = (char) ('0' - intQuotient);
        }

        // processing sign
        if (negative) {
            buffer[--writeIndex] = '-';
        }
    }

    private void encode(int i) throws IOException {
        // cannot write all possible int values if less than 11 chars is remaining
        if (buffer.length - limit < 11) {
            out.write(buffer, 0, limit);
            limit = 0;
        }

        // compute bounds
        int quotient;
        int remainder;
        int writeIndex = limit + stringSizeOf(i);
        limit = writeIndex;

        // always convert to negative number
        boolean negative = i < 0;
        if (!negative) {
            i = -i;
        }

        // processing lower 32 bits (int operations are faster on CPU)
        while (i <= -100) {
            quotient = i / 100;
            remainder = (quotient * 100) - i;
            i = quotient;
            buffer[--writeIndex] = ONES[remainder];
            buffer[--writeIndex] = TENS[remainder];
        }

        // processing remaining digits
        quotient = i / 10;
        remainder = (quotient * 10) - i;
        buffer[--writeIndex] = (char) ('0' + remainder);

        if (quotient < 0) {
            buffer[--writeIndex] = (char) ('0' - quotient);
        }

        // processing sign
        if (negative) {
            buffer[--writeIndex] = '-';
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("DMR writer have been closed");
        }
    }

    private static void assertNotNullParameter(Object o) {
        if (o == null) {
            throw new NullPointerException("Parameter cannot be null");
        }
    }

}
