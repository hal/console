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

import org.jboss.hal.dmr.Base64;
import org.jboss.hal.dmr.ModelType;

import static java.lang.String.valueOf;
import static org.jboss.hal.dmr.stream.ModelConstants.*;
import static org.jboss.hal.dmr.stream.Utils.isControl;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class JsonWriterImpl implements ModelWriter {

    private final StringBuilder builder;
    private final JsonGrammarAnalyzer analyzer;

    JsonWriterImpl(StringBuilder builder) {
        this.builder = builder;
        this.analyzer = new JsonGrammarAnalyzer();
    }

    @Override
    public ModelWriter writeObjectStart() throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putObjectStart();
        write(OBJECT_START);
        return this;
    }

    @Override
    public ModelWriter writeObjectEnd() throws ModelException {
        analyzer.putObjectEnd();
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeListStart() throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putListStart();
        write(LIST_START);
        return this;
    }

    @Override
    public ModelWriter writeListEnd() throws ModelException {
        analyzer.putListEnd();
        write(LIST_END);
        return this;
    }

    @Override
    public ModelWriter writePropertyStart() throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putPropertyStart();
        write(OBJECT_START);
        return this;
    }

    @Override
    public ModelWriter writePropertyEnd() throws ModelException {
        analyzer.putPropertyEnd();
        write(OBJECT_END);
        return this;
    }

    @Override
    public ModelWriter writeExpression(String data) throws ModelException {
        assertNotNullParameter(data);
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
    public ModelWriter writeBytes(byte[] data) throws ModelException {
        assertNotNullParameter(data);
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
    public ModelWriter writeType(ModelType data) throws ModelException {
        assertNotNullParameter(data);
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
    public ModelWriter writeString(String data) throws ModelException {
        assertNotNullParameter(data);
        writeOptionalColonOrComma();
        analyzer.putString();
        encode(data);
        return this;
    }

    @Override
    public ModelWriter writeUndefined() throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putUndefined();
        write(NULL, 0, NULL.length());
        return this;
    }

    @Override
    public ModelWriter writeBoolean(boolean data) throws ModelException {
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
    public ModelWriter writeInt(int data) throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putNumber(ModelEvent.INT);
        encode(data);
        return this;
    }

    @Override
    public ModelWriter writeLong(long data) throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putNumber(ModelEvent.LONG);
        encode(data);
        return this;
    }

    @Override
    public ModelWriter writeDouble(double data) throws ModelException {
        return writeNumber(valueOf(data), ModelEvent.DOUBLE);
    }

    @Override
    public ModelWriter writeBigInteger(BigInteger data) throws ModelException {
        assertNotNullParameter(data);
        return writeNumber(valueOf(data), ModelEvent.BIG_INTEGER);
    }

    @Override
    public ModelWriter writeBigDecimal(BigDecimal data) throws ModelException {
        assertNotNullParameter(data);
        return writeNumber(valueOf(data), ModelEvent.BIG_DECIMAL);
    }

    private ModelWriter writeNumber(String data, ModelEvent numberEvent) throws ModelException {
        writeOptionalColonOrComma();
        analyzer.putNumber(numberEvent);
        write(data, 0, data.length());
        return this;
    }

    private void writeOptionalColonOrComma() throws ModelException {
        if (analyzer.isColonExpected()) {
            analyzer.putColon();
            write(COLON);
        } else if (analyzer.isCommaExpected()) {
            analyzer.putComma();
            write(COMMA);
        }
    }

    private void write(String data) {
        write(data, 0, data.length());
    }

    private void write(char c) {
        builder.append(c);
    }

    private void write(String data, int dataBegin, int dataEnd) {
        builder.append(data.substring(dataBegin, dataEnd));
    }

    private void encode(String s) {
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

    private void base64Encode(byte[] data) {
        write(QUOTE);
        write(Base64.encodeBytes(data));
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
