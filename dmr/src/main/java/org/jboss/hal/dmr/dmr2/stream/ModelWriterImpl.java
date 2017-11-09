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
package org.jboss.hal.dmr.dmr2.stream;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.dmr2.ModelType;

import static java.lang.Math.min;
import static java.lang.String.valueOf;
import static org.jboss.hal.dmr.dmr2.stream.ModelConstants.*;
import static org.jboss.hal.dmr.dmr2.stream.Utils.ONES;
import static org.jboss.hal.dmr.dmr2.stream.Utils.TENS;
import static org.jboss.hal.dmr.dmr2.stream.Utils.stringSizeOf;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class ModelWriterImpl implements ModelWriter {

    private static final String BIG_DECIMAL_PREFIX = BIG + SPACE + DECIMAL + SPACE;
    private static final String BIG_INTEGER_PREFIX = BIG + SPACE + INTEGER + SPACE;
    private static final String BYTES_PREFIX = BYTES + SPACE + BYTES_START;
    private static final String BYTES_SUFFIX = "" + BYTES_END;
    private static final String EXPRESSION_PREFIX = EXPRESSION + SPACE;
    private final ModelGrammarAnalyzer analyzer;
    private final Writer out;
    private final char[] buffer = new char[1024];
    private int limit;
    private boolean closed;

    ModelWriterImpl(Writer out) {
        this.out = out;
        analyzer = new ModelGrammarAnalyzer();
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
    public ModelWriterImpl writeObjectStart() throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putObjectStart();
        write(OBJECT_START);
        return this;
    }

    @Override
    public ModelWriterImpl writeObjectEnd() throws IOException, ModelException {
        ensureOpen();
        analyzer.putObjectEnd();
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriterImpl writePropertyStart() throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putPropertyStart();
        write(PROPERTY_START);
        return this;
    }

    @Override
    public ModelWriterImpl writePropertyEnd() throws IOException, ModelException {
        ensureOpen();
        analyzer.putPropertyEnd();
        write(PROPERTY_END);
        return this;
    }

    @Override
    public ModelWriterImpl writeListStart() throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putListStart();
        write(LIST_START);
        return this;
    }

    @Override
    public ModelWriterImpl writeListEnd() throws IOException, ModelException {
        ensureOpen();
        analyzer.putListEnd();
        write(LIST_END);
        return this;
    }

    @Override
    public ModelWriterImpl writeExpression(String data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putExpression();
        write(EXPRESSION_PREFIX);
        encode(data);
        return this;
    }

    @Override
    public ModelWriterImpl writeString(String data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putString();
        encode(data);
        return this;
    }

    public ModelWriterImpl writeBytes(byte[] data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putBytes();
        write(BYTES_PREFIX);
        encode(data);
        write(BYTES_SUFFIX);
        return this;
    }

    @Override
    public ModelWriterImpl writeUndefined() throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putUndefined();
        write(UNDEFINED);
        return this;
    }

    @Override
    public ModelWriterImpl writeBoolean(boolean data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putBoolean();
        if (data) {
            write(TRUE);
        } else {
            write(FALSE);
        }
        return this;
    }

    @Override
    public ModelWriterImpl writeInt(int data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.INT);
        encode(data);
        return this;
    }

    @Override
    public ModelWriterImpl writeLong(long data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.LONG);
        encode(data);
        return this;
    }

    @Override
    public ModelWriterImpl writeDouble(double data) throws IOException, ModelException {
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.DOUBLE);
        write(valueOf(data));
        return this;
    }

    @Override
    public ModelWriterImpl writeBigInteger(BigInteger data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.BIG_INTEGER);
        write(BIG_INTEGER_PREFIX);
        write(valueOf(data));
        return this;
    }

    @Override
    public ModelWriterImpl writeBigDecimal(BigDecimal data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.BIG_DECIMAL);
        write(BIG_DECIMAL_PREFIX);
        write(valueOf(data));
        return this;
    }

    @Override
    public ModelWriterImpl writeType(ModelType data) throws IOException, ModelException {
        assertNotNullParameter(data);
        ensureOpen();
        writeOptionalArrowOrComma();
        analyzer.putType();
        write(data.toString());
        return this;
    }

    private void writeOptionalArrowOrComma() throws IOException, ModelException {
        if (analyzer.isArrowExpected()) {
            analyzer.putArrow();
            write(ARROW);
        } else if (analyzer.isCommaExpected()) {
            analyzer.putComma();
            write(COMMA);
        }
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

    private void write(String data) throws IOException {
        write(data, 0, data.length());
    }

    private void encode(byte[] data) throws IOException {
        for (int i = 0, length = data.length; i < length; i++) {
            byte b = data[i];
            if (b >= 0 && b < 0x10) {
                write("0x0");
                write(Integer.toHexString(b & 0xff));
            } else {
                write("0x");
                write(Integer.toHexString(b & 0xff));
            }
            if (i != length - 1) {
                write(COMMA);
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
            while (c != BACKSLASH && c != QUOTE) {
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
            write(c);
        }
        write(QUOTE);
    }

    private void encode(long l) throws IOException {
        // cannot write all possible long values if less than 21 chars is remaining
        if (buffer.length - limit < 20) {
            out.write(buffer, 0, limit);
            limit = 0;
        }

        // compute bounds
        long longQuotient;
        int remainder;
        int writeIndex = limit + stringSizeOf(l) + 1;
        limit = writeIndex;

        // always convert to negative number
        boolean negative = l < 0;
        if (!negative) {
            l = -l;
        }

        // longs are always ending with 'L'
        buffer[--writeIndex] = 'L';

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
