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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.ModelType;

import static java.lang.String.valueOf;
import static org.jboss.hal.dmr.stream.ModelConstants.*;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class ModelWriterImpl implements ModelWriter {

    private static final String BIG_DECIMAL_PREFIX = BIG + SPACE + DECIMAL + SPACE;
    private static final String BIG_INTEGER_PREFIX = BIG + SPACE + INTEGER + SPACE;
    private static final String BYTES_PREFIX = BYTES + SPACE + BYTES_START;
    private static final String BYTES_SUFFIX = "" + BYTES_END;
    private static final String EXPRESSION_PREFIX = EXPRESSION + SPACE;

    private final StringBuilder builder;
    private final ModelGrammarAnalyzer analyzer;

    ModelWriterImpl(StringBuilder builder) {
        this.builder = builder;
        this.analyzer = new ModelGrammarAnalyzer();
    }

    @Override
    public ModelWriterImpl writeObjectStart() throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putObjectStart();
        write(OBJECT_START);
        return this;
    }

    @Override
    public ModelWriterImpl writeObjectEnd() throws ModelException {
        analyzer.putObjectEnd();
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriterImpl writePropertyStart() throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putPropertyStart();
        write(PROPERTY_START);
        return this;
    }

    @Override
    public ModelWriterImpl writePropertyEnd() throws ModelException {
        analyzer.putPropertyEnd();
        write(PROPERTY_END);
        return this;
    }

    @Override
    public ModelWriterImpl writeListStart() throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putListStart();
        write(LIST_START);
        return this;
    }

    @Override
    public ModelWriterImpl writeListEnd() throws ModelException {
        analyzer.putListEnd();
        write(LIST_END);
        return this;
    }

    @Override
    public ModelWriterImpl writeExpression(String data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalArrowOrComma();
        analyzer.putExpression();
        write(EXPRESSION_PREFIX);
        encode(data);
        return this;
    }

    @Override
    public ModelWriterImpl writeString(String data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalArrowOrComma();
        analyzer.putString();
        encode(data);
        return this;
    }

    public ModelWriterImpl writeBytes(byte[] data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalArrowOrComma();
        analyzer.putBytes();
        write(BYTES_PREFIX);
        encode(data);
        write(BYTES_SUFFIX);
        return this;
    }

    @Override
    public ModelWriterImpl writeUndefined() throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putUndefined();
        write(UNDEFINED);
        return this;
    }

    @Override
    public ModelWriterImpl writeBoolean(boolean data) throws ModelException {
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
    public ModelWriterImpl writeInt(int data) throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.INT);
        encode(data);
        return this;
    }

    @Override
    public ModelWriterImpl writeLong(long data) throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.LONG);
        encode(data);
        return this;
    }

    @Override
    public ModelWriterImpl writeDouble(double data) throws ModelException {
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.DOUBLE);
        write(valueOf(data));
        return this;
    }

    @Override
    public ModelWriterImpl writeBigInteger(BigInteger data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.BIG_INTEGER);
        write(BIG_INTEGER_PREFIX);
        write(valueOf(data));
        return this;
    }

    @Override
    public ModelWriterImpl writeBigDecimal(BigDecimal data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalArrowOrComma();
        analyzer.putNumber(ModelEvent.BIG_DECIMAL);
        write(BIG_DECIMAL_PREFIX);
        write(valueOf(data));
        return this;
    }

    @Override
    public ModelWriterImpl writeType(ModelType data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalArrowOrComma();
        analyzer.putType();
        write(data.toString());
        return this;
    }

    private void writeOptionalArrowOrComma() throws ModelException {
        if (analyzer.isArrowExpected()) {
            analyzer.putArrow();
            write(ARROW);
        } else if (analyzer.isCommaExpected()) {
            analyzer.putComma();
            write(COMMA);
        }
    }

    private void write(char c) {
        builder.append(c);
    }

    private void write(String data) {
        builder.append(data);
    }

    private void encode(byte[] data) {
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

    private void encode(String s) {
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
                write(s.substring(dataBegin, dataEnd));
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

    private void encode(long l) {
        write(String.valueOf(l));
        write('L');
    }

    private void encode(int i) {
        write(String.valueOf(i));
        write('L');
    }

    private static void assertNotNullParameter(Object o) {
        if (o == null) {
            throw new NullPointerException("Parameter cannot be null");
        }
    }

}
