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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.dmr2.ModelType;

import static org.jboss.hal.dmr.dmr2.stream.ModelConstants.*;
import static org.jboss.hal.dmr.dmr2.stream.Utils.HEX_TABLE;
import static org.jboss.hal.dmr.dmr2.stream.Utils.isHexNumberChar;
import static org.jboss.hal.dmr.dmr2.stream.Utils.isNumberChar;
import static org.jboss.hal.dmr.dmr2.stream.Utils.isWhitespace;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class ModelReaderImpl implements ModelReader {

    private static final char[] BIG = ModelConstants.BIG.toCharArray();
    private static final char[] BYTES = ModelConstants.BYTES.toCharArray();
    private static final char[] DECIMAL = ModelConstants.DECIMAL.toCharArray();
    private static final char[] EXPRESSION = ModelConstants.EXPRESSION.toCharArray();
    private static final char[] FALSE = ModelConstants.FALSE.toCharArray();
    private static final char[] INFINITY = ModelConstants.INFINITY.toCharArray();
    private static final char[] INTEGER = ModelConstants.INTEGER.toCharArray();
    private static final char[] NAN = ModelConstants.NAN.toCharArray();
    private static final char[] TRUE = ModelConstants.TRUE.toCharArray();
    private static final char[] TYPE_BIG_DECIMAL = ModelType.BIG_DECIMAL.toString().toCharArray();
    private static final char[] TYPE_BIG_INTEGER = ModelType.BIG_INTEGER.toString().toCharArray();
    private static final char[] TYPE_BOOLEAN = ModelType.BOOLEAN.toString().toCharArray();
    private static final char[] TYPE_BYTES = ModelType.BYTES.toString().toCharArray();
    private static final char[] TYPE_DOUBLE = ModelType.DOUBLE.toString().toCharArray();
    private static final char[] TYPE_INT = ModelType.INT.toString().toCharArray();
    private static final char[] TYPE_LIST = ModelType.LIST.toString().toCharArray();
    private static final char[] TYPE_LONG = ModelType.LONG.toString().toCharArray();
    private static final char[] TYPE_EXPRESSION = ModelType.EXPRESSION.toString().toCharArray();
    private static final char[] TYPE_OBJECT = ModelType.OBJECT.toString().toCharArray();
    private static final char[] TYPE_PROPERTY = ModelType.PROPERTY.toString().toCharArray();
    private static final char[] TYPE_STRING = ModelType.STRING.toString().toCharArray();
    private static final char[] TYPE_TYPE = ModelType.TYPE.toString().toCharArray();
    private static final char[] TYPE_UNDEFINED = ModelType.UNDEFINED.toString().toCharArray();
    private static final char[] UNDEFINED = ModelConstants.UNDEFINED.toCharArray();
    private static final String UNEXPECTED_CHARACTER = "Unexpected character '";
    private static final String UNEXPECTED_SECOND_CHARACTER = "Unexpected second character '";
    private final Reader in;
    private final ModelGrammarAnalyzer analyzer;
    private char[] buffer = new char[1024];
    private int position;
    private int limit;
    private int numberOffset;
    private int numberLength;
    private int stringOffset;
    private int stringLength;
    private byte[] bytesValue;
    private ModelType typeValue;
    private int intValue;
    private long longValue;
    private double doubleValue;
    private BigInteger bigIntegerValue;
    private BigDecimal bigDecimalValue;
    private boolean booleanValue;
    private String stringValue;
    private boolean closed;

    ModelReaderImpl(Reader in) {
        this.in = in;
        analyzer = new ModelGrammarAnalyzer();
    }

    @Override
    public void close() throws ModelException {
        if (closed) {
            return; // idempotent
        }
        closed = true;
        if (!analyzer.finished) {
            throw analyzer.newModelException("Uncomplete DMR stream have been read");
        }
    }

    @Override
    public String getString() {
        if (!isCurrentEvent(ModelEvent.STRING)) {
            throw new IllegalStateException("Current event isn't string");
        }
        return stringValue;
    }

    @Override
    public int getInt() {
        if (!isCurrentEvent(ModelEvent.INT)) {
            throw new IllegalStateException("Current event isn't int");
        }
        return intValue;
    }

    @Override
    public long getLong() {
        if (!isCurrentEvent(ModelEvent.LONG)) {
            throw new IllegalStateException("Current event isn't long");
        }
        return longValue;
    }

    @Override
    public double getDouble() {
        if (!isCurrentEvent(ModelEvent.DOUBLE)) {
            throw new IllegalStateException("Current event isn't double");
        }
        return doubleValue;
    }

    @Override
    public BigInteger getBigInteger() {
        if (!isCurrentEvent(ModelEvent.BIG_INTEGER)) {
            throw new IllegalStateException("Current event isn't big integer");
        }
        return bigIntegerValue;
    }

    @Override
    public BigDecimal getBigDecimal() {
        if (!isCurrentEvent(ModelEvent.BIG_DECIMAL)) {
            throw new IllegalStateException("Current event isn't big decimal");
        }
        return bigDecimalValue;
    }

    @Override
    public byte[] getBytes() {
        if (!isCurrentEvent(ModelEvent.BYTES)) {
            throw new IllegalStateException("Current event isn't bytes");
        }
        return bytesValue;
    }

    @Override
    public String getExpression() {
        if (!isCurrentEvent(ModelEvent.EXPRESSION)) {
            throw new IllegalStateException("Current event isn't expression");
        }
        return stringValue;
    }

    @Override
    public ModelType getType() {
        if (!isCurrentEvent(ModelEvent.TYPE)) {
            throw new IllegalStateException("Current event isn't type");
        }
        return typeValue;
    }

    @Override
    public boolean getBoolean() {
        if (!isCurrentEvent(ModelEvent.BOOLEAN)) {
            throw new IllegalStateException("Current event isn't boolean");
        }
        return booleanValue;
    }

    @Override
    public boolean isListEnd() {
        return isCurrentEvent(ModelEvent.LIST_END);
    }

    @Override
    public boolean isListStart() {
        return isCurrentEvent(ModelEvent.LIST_START);
    }

    @Override
    public boolean isObjectEnd() {
        return isCurrentEvent(ModelEvent.OBJECT_END);
    }

    @Override
    public boolean isObjectStart() {
        return isCurrentEvent(ModelEvent.OBJECT_START);
    }

    @Override
    public boolean isPropertyEnd() {
        return isCurrentEvent(ModelEvent.PROPERTY_END);
    }

    @Override
    public boolean isPropertyStart() {
        return isCurrentEvent(ModelEvent.PROPERTY_START);
    }

    @Override
    public boolean isString() {
        return isCurrentEvent(ModelEvent.STRING);
    }

    @Override
    public boolean isInt() {
        return isCurrentEvent(ModelEvent.INT);
    }

    @Override
    public boolean isLong() {
        return isCurrentEvent(ModelEvent.LONG);
    }

    @Override
    public boolean isDouble() {
        return isCurrentEvent(ModelEvent.DOUBLE);
    }

    @Override
    public boolean isBigInteger() {
        return isCurrentEvent(ModelEvent.BIG_INTEGER);
    }

    @Override
    public boolean isBigDecimal() {
        return isCurrentEvent(ModelEvent.BIG_DECIMAL);
    }

    @Override
    public boolean isBytes() {
        return isCurrentEvent(ModelEvent.BYTES);
    }

    @Override
    public boolean isExpression() {
        return isCurrentEvent(ModelEvent.EXPRESSION);
    }

    @Override
    public boolean isType() {
        return isCurrentEvent(ModelEvent.TYPE);
    }

    @Override
    public boolean isBoolean() {
        return isCurrentEvent(ModelEvent.BOOLEAN);
    }

    @Override
    public boolean isUndefined() {
        return isCurrentEvent(ModelEvent.UNDEFINED);
    }

    private boolean isCurrentEvent(ModelEvent event) {
        ensureOpen();
        return analyzer.currentEvent == event;
    }

    @Override
    public boolean hasNext() {
        ensureOpen();
        return !analyzer.finished;
    }

    @Override
    public ModelEvent next() throws IOException, ModelException {
        ensureOpen();
        if (analyzer.finished) {
            throw new IllegalStateException("No more DMR tokens available");
        }
        boolean assertEmptyStream = true;
        try {
            int currentChar;
            boolean hexNumber;
            while (true) {
                ensureBufferAccess(1);
                currentChar = buffer[position++];
                switch (currentChar) {
                    case QUOTE: {
                        analyzer.putString();
                        readString();
                        stringValue = new String(buffer, stringOffset, stringLength);
                        return analyzer.currentEvent;
                    }
                    case EQUAL: {
                        ensureBufferAccess(1);
                        currentChar = buffer[position++];
                        if (currentChar != GREATER_THAN) {
                            throw newModelException(
                                    UNEXPECTED_CHARACTER + (char) currentChar + "' while reading DMR arrow");
                        }
                        analyzer.putArrow();
                    }
                    break;
                    case COMMA: {
                        analyzer.putComma();
                    }
                    break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case MINUS:
                    case PLUS: {
                        hexNumber = false;
                        if (currentChar == PLUS || currentChar == MINUS) {
                            position--;
                            ensureBufferAccess(2);
                            position++;
                            if (buffer[position] == 'I') {
                                readString(INFINITY);
                                analyzer.putNumber(ModelEvent.DOUBLE);
                                doubleValue = currentChar == PLUS ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                                return analyzer.currentEvent;
                            } else if (buffer[position] == 'N') {
                                readString(NAN);
                                analyzer.putNumber(ModelEvent.DOUBLE);
                                doubleValue = Double.NaN;
                                return analyzer.currentEvent;
                            } else if (!isNumberChar(buffer[position])) {
                                throw newModelException("Unexpected first character '" + buffer[position]
                                        + "' while reading DMR Infinity or NaN or number token");
                            }
                        }
                        if (currentChar == '0') {
                            position--;
                            if (ensureBufferAccessNoFail(2)) {
                                if (buffer[position + 1] == 'x') {
                                    hexNumber = true;
                                    position++;
                                }
                            }
                            position++;
                        }
                        if (!hexNumber) {
                            position--;
                        }
                        readNumber(hexNumber);
                        currentChar = position < limit ? buffer[position++] : read();
                        if (currentChar == 'L') {
                            try {
                                analyzer.putNumber(ModelEvent.LONG);
                                if (hexNumber) {
                                    longValue = parseHexLong();
                                } else {
                                    longValue = Long.parseLong(new String(buffer, numberOffset, numberLength), 10);
                                }
                            } catch (NumberFormatException nfe) {
                                throw newModelException("Incorrect long value", nfe);
                            }
                        } else {
                            if (currentChar != -1) {
                                position--;
                            }
                            if (isDecimalString()) {
                                try {
                                    analyzer.putNumber(ModelEvent.DOUBLE);
                                    doubleValue = Double.parseDouble(new String(buffer, numberOffset, numberLength));
                                } catch (NumberFormatException nfe) {
                                    throw newModelException("Incorrect double value", nfe);
                                }
                            } else {
                                try {
                                    analyzer.putNumber(ModelEvent.INT);
                                    if (hexNumber) {
                                        intValue = parseHexInt();
                                    } else {
                                        intValue = Integer.parseInt(new String(buffer, numberOffset, numberLength), 10);
                                    }
                                } catch (NumberFormatException nfe) {
                                    throw newModelException("Incorrect integer value", nfe);
                                }
                            }
                        }
                        return analyzer.currentEvent;
                    }
                    case 'b': {
                        position--;
                        ensureBufferAccess(2);
                        if (buffer[position + 1] == 'i') {
                            readString(BIG);
                            processWhitespaces();
                            ensureBufferAccess(1);
                            if (buffer[position] == 'd') {
                                readString(DECIMAL);
                                processWhitespaces();
                                readNumber(false);
                                try {
                                    analyzer.putNumber(ModelEvent.BIG_DECIMAL);
                                    bigDecimalValue = new BigDecimal(new String(buffer, numberOffset, numberLength));
                                } catch (NumberFormatException nfe) {
                                    throw newModelException("Incorrect big decimal value", nfe);
                                }
                            } else if (buffer[position] == 'i') {
                                readString(INTEGER);
                                processWhitespaces();
                                readNumber(false);
                                try {
                                    analyzer.putNumber(ModelEvent.BIG_INTEGER);
                                    bigIntegerValue = new BigInteger(new String(buffer, numberOffset, numberLength));
                                } catch (NumberFormatException nfe) {
                                    throw newModelException("Incorrect big integer value", nfe);
                                }
                            } else {
                                throw newModelException("Unexpected first character '" + buffer[position]
                                        + "' while reading DMR decimal or integer token");
                            }
                        } else if (buffer[position + 1] == 'y') {
                            analyzer.putBytes();
                            readString(BYTES);
                            processWhitespaces();
                            readBytes();
                        } else {
                            throw newModelException(UNEXPECTED_SECOND_CHARACTER + buffer[position + 1]
                                    + "' while reading DMR big or bytes token");
                        }
                        return analyzer.currentEvent;
                    }
                    case 'D': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_DOUBLE);
                        typeValue = ModelType.DOUBLE;
                        return analyzer.currentEvent;
                    }
                    case 'e': {
                        analyzer.putExpression();
                        position--;
                        readString(EXPRESSION);
                        processWhitespaces();
                        ensureBufferAccess(1);
                        currentChar = buffer[position++];
                        if (currentChar != QUOTE) {
                            throw newModelException(
                                    UNEXPECTED_CHARACTER + (char) currentChar + "' while reading DMR expression value");
                        }
                        readString();
                        stringValue = new String(buffer, stringOffset, stringLength);
                        return analyzer.currentEvent;
                    }
                    case 'E': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_EXPRESSION);
                        typeValue = ModelType.EXPRESSION;
                        return analyzer.currentEvent;
                    }
                    case 'O': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_OBJECT);
                        typeValue = ModelType.OBJECT;
                        return analyzer.currentEvent;
                    }
                    case 'P': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_PROPERTY);
                        typeValue = ModelType.PROPERTY;
                        return analyzer.currentEvent;
                    }
                    case 'S': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_STRING);
                        typeValue = ModelType.STRING;
                        return analyzer.currentEvent;
                    }
                    case 'T': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_TYPE);
                        typeValue = ModelType.TYPE;
                        return analyzer.currentEvent;
                    }
                    case 'U': {
                        analyzer.putType();
                        position--;
                        readString(TYPE_UNDEFINED);
                        typeValue = ModelType.UNDEFINED;
                        return analyzer.currentEvent;
                    }
                    case 'L': {
                        position--;
                        ensureBufferAccess(2);
                        analyzer.putType();
                        if (buffer[position + 1] == 'I') {
                            readString(TYPE_LIST);
                            typeValue = ModelType.LIST;
                        } else if (buffer[position + 1] == 'O') {
                            readString(TYPE_LONG);
                            typeValue = ModelType.LONG;
                        } else {
                            throw newModelException(UNEXPECTED_SECOND_CHARACTER + buffer[position + 1]
                                    + "' while reading DMR LIST or LONG token");
                        }
                        return analyzer.currentEvent;
                    }
                    case 'B': {
                        position--;
                        ensureBufferAccess(2);
                        analyzer.putType();
                        if (buffer[position + 1] == 'I') {
                            ensureBufferAccess(5);
                            if (buffer[position + 4] == 'D') {
                                readString(TYPE_BIG_DECIMAL);
                                typeValue = ModelType.BIG_DECIMAL;
                            } else if (buffer[position + 4] == 'I') {
                                readString(TYPE_BIG_INTEGER);
                                typeValue = ModelType.BIG_INTEGER;
                            } else {
                                throw newModelException("Unexpected fifth character '" + buffer[position + 4]
                                        + "' while reading DMR BIG_DECIMAL or BIG_INTEGER token");
                            }
                        } else if (buffer[position + 1] == 'O') {
                            readString(TYPE_BOOLEAN);
                            typeValue = ModelType.BOOLEAN;
                        } else if (buffer[position + 1] == 'Y') {
                            readString(TYPE_BYTES);
                            typeValue = ModelType.BYTES;
                        } else {
                            throw newModelException(UNEXPECTED_SECOND_CHARACTER + buffer[position + 1]
                                    + "' while reading DMR BIG_DECIMAL or BIG_INTEGER or BOOLEAN or BYTES token");
                        }
                        return analyzer.currentEvent;
                    }
                    case 'I': {
                        position--;
                        ensureBufferAccess(2);
                        if (buffer[position + 1] == 'N') {
                            analyzer.putType();
                            readString(TYPE_INT);
                            typeValue = ModelType.INT;
                        } else if (buffer[position + 1] == 'n') {
                            readString(INFINITY);
                            analyzer.putNumber(ModelEvent.DOUBLE);
                            doubleValue = Double.POSITIVE_INFINITY;
                        } else {
                            throw newModelException(UNEXPECTED_SECOND_CHARACTER + buffer[position + 1]
                                    + "' while reading DMR INT or Infinity token");
                        }
                        return analyzer.currentEvent;
                    }
                    case 'N': {
                        position--;
                        readString(NAN);
                        analyzer.putNumber(ModelEvent.DOUBLE);
                        doubleValue = Double.NaN;
                        return analyzer.currentEvent;
                    }
                    case 'f':
                    case 't': {
                        analyzer.putBoolean();
                        position--;
                        booleanValue = currentChar == 't';
                        readString(booleanValue ? TRUE : FALSE);
                        return analyzer.currentEvent;
                    }
                    case 'u': {
                        analyzer.putUndefined();
                        position--;
                        readString(UNDEFINED);
                        return analyzer.currentEvent;
                    }
                    case OBJECT_START: {
                        analyzer.putObjectStart();
                        return analyzer.currentEvent;
                    }
                    case LIST_START: {
                        analyzer.putListStart();
                        return analyzer.currentEvent;
                    }
                    case PROPERTY_START: {
                        analyzer.putPropertyStart();
                        return analyzer.currentEvent;
                    }
                    case OBJECT_END: {
                        analyzer.putObjectEnd();
                        return analyzer.currentEvent;
                    }
                    case LIST_END: {
                        analyzer.putListEnd();
                        return analyzer.currentEvent;
                    }
                    case PROPERTY_END: {
                        analyzer.putPropertyEnd();
                        return analyzer.currentEvent;
                    }
                    default: {
                        if (isWhitespace(currentChar)) {
                            processWhitespaces();
                        } else {
                            throw newModelException(
                                    UNEXPECTED_CHARACTER + (char) currentChar + "' while reading DMR stream");
                        }
                    }
                }
            }
        } catch (Throwable t) {
            assertEmptyStream = false;
            throw t;
        } finally {
            if (analyzer.finished && assertEmptyStream) {
                processWhitespaces();
                if (read() != -1) {
                    throw new ModelException("Unexpected content following the DMR stream");
                }
            }
        }
    }

    private void processWhitespaces() throws IOException {
        int currentChar;
        do {
            if (position == limit) {
                limit = 0;
                position = 0;
                fillBuffer();
                if (position == limit) {
                    return;
                }
            } else if (position == limit - 1) {
                buffer[0] = buffer[position];
                limit = 1;
                position = 0;
                fillBuffer();
            }
            currentChar = buffer[position++];
        } while (isWhitespace(currentChar));
        position--;
    }

    private void ensureData() throws IOException {
        if (position == limit) {
            if (limit == buffer.length) {
                limit = 0;
                position = 0;
            }
            fillBuffer();
        }
    }

    private void ensureBufferAccess(int charsCount) throws IOException, ModelException {
        if (position + charsCount <= limit) {
            return;
        }
        if (position <= limit) {
            System.arraycopy(buffer, position, buffer, 0, limit - position);
            limit -= position;
            position = 0;
        }
        fillBuffer();
        if (position + charsCount > limit) {
            throw newModelException("Unexpected EOF while reading DMR stream");
        }
    }

    private boolean ensureBufferAccessNoFail(int charsCount) throws IOException, ModelException {
        if (position + charsCount <= limit) {
            return true;
        }
        if (position <= limit) {
            System.arraycopy(buffer, position, buffer, 0, limit - position);
            limit -= position;
            position = 0;
        }
        fillBuffer();
        return position + charsCount <= limit;
    }

    private void fillBuffer() throws IOException {
        int read;
        do {
            read = in.read(buffer, limit, buffer.length - limit);
            if (read == -1) {
                return;
            }
            limit += read;
        } while (limit != buffer.length);
    }

    private int read() throws IOException {
        ensureData();
        return position < limit ? buffer[position++] : -1;
    }

    private void readString() throws IOException, ModelException {
        boolean escaped = false;
        char currentChar;
        stringLength = 0;
        boolean copy = false;
        while (true) {
            if (stringLength == 0) {
                stringOffset = position;
            }
            while (position != limit) {
                currentChar = buffer[position++];
                if (escaped) {
                    copy = true;
                    if (currentChar == 'b') {
                        buffer[stringOffset + stringLength++] = BACKSPACE;
                    } else if (currentChar == 'f') {
                        buffer[stringOffset + stringLength++] = FORMFEED;
                    } else if (currentChar == 'n') {
                        buffer[stringOffset + stringLength++] = NL;
                    } else if (currentChar == 'r') {
                        buffer[stringOffset + stringLength++] = CR;
                    } else {
                        buffer[stringOffset + stringLength++] = currentChar;
                    }
                    escaped = false;
                } else {
                    if (currentChar == QUOTE) {
                        return;
                    }
                    if (currentChar == BACKSLASH) {
                        escaped = true;
                        continue;
                    }
                    if (copy) {
                        buffer[stringOffset + stringLength] = currentChar;
                    }
                    stringLength++;
                }
            }
            if (stringOffset != 0 && stringLength > 0) {
                System.arraycopy(buffer, stringOffset, buffer, 0, stringLength);
                position = stringLength;
                limit = stringLength;
                stringOffset = 0;
            } else if (stringOffset == 0 && limit == buffer.length) {
                doubleBuffer();
            }
            ensureData();
            if (position == limit) {
                throw newModelException("Unexpected EOF while reading DMR stream");
            }
        }
    }

    private void readString(char[] expected) throws IOException, ModelException {
        int i = 0;
        if (position < limit - expected.length + 1) {
            // fast path
            for (; i < expected.length; i++) {
                if (buffer[position++] != expected[i]) {
                    throw newModelException(UNEXPECTED_CHARACTER + buffer[position - 1]
                            + "' while reading DMR " + new String(expected) + " token");
                }
            }
        } else {
            // slow path
            while (true) {
                while (position < limit && i != expected.length) {
                    if (buffer[position++] != expected[i++]) {
                        throw newModelException(UNEXPECTED_CHARACTER + buffer[position - 1]
                                + "' while reading DMR " + new String(expected) + " token");
                    }
                }
                if (i == expected.length) {
                    return;
                }
                ensureData();
                if (position == limit) {
                    throw newModelException("Unexpected EOF while reading DMR stream");
                }
            }
        }
    }

    private void readNumber(boolean hexed) throws IOException, ModelException {
        numberOffset = position;
        while (true) {
            while (position < limit) {
                if (hexed ? isHexNumberChar(buffer[position++]) : isNumberChar(buffer[position++])) {
                    continue;
                }
                position--;
                break;
            }
            numberLength = position - numberOffset;
            if (position < limit) {
                break;
            }
            if (numberOffset != 0) {
                System.arraycopy(buffer, numberOffset, buffer, 0, numberLength);
                position = numberLength;
                limit = numberLength;
                numberOffset = 0;
            } else if (limit == buffer.length) {
                doubleBuffer();
            }
            ensureData();
            if (position == limit) {
                break;
            }
        }
    }

    private void readBytes() throws IOException, ModelException {
        ensureBufferAccess(1);
        if (buffer[position++] != BYTES_START) {
            throw newModelException("Incorrect bytes value. It must start with: '{'");
        }
        processWhitespaces();
        boolean expectingComma = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ensureBufferAccess(1);
        while (buffer[position] != BYTES_END) {
            if (!expectingComma && isNumberChar(buffer[position])) {
                ensureBufferAccess(2);
                try {
                    if (buffer[position + 1] == 'x') {
                        if (buffer[position] != '0') {
                            throw newModelException("Expected integer or hexed integer value inside bytes section");
                        }
                        position += 2;
                        readNumber(true);
                        baos.write(parseHexInt());
                        expectingComma = true;
                    } else {
                        readNumber(false);
                        baos.write(Integer.parseInt(new String(buffer, numberOffset, numberLength)));
                        expectingComma = true;
                    }
                } catch (NumberFormatException nfe) {
                    throw newModelException("Expected integer or hexed integer value inside bytes section", nfe);
                }
            } else if (expectingComma && buffer[position] == COMMA) {
                expectingComma = false;
                position++;
            } else {
                if (!expectingComma) {
                    throw newModelException("Expected integer or hexed integer value inside bytes section");
                } else {
                    throw newModelException("Missing ',' separator inside bytes section");
                }
            }
            processWhitespaces();
            ensureBufferAccess(1);
        }
        position++;
        processWhitespaces();
        bytesValue = baos.toByteArray();
    }

    private boolean isDecimalString() {
        int numberLimit = numberOffset + numberLength;
        for (int i = numberOffset; i < numberLimit; i++) {
            if (buffer[i] == '.') {
                return true;
            }
        }
        return false;
    }

    private void doubleBuffer() {
        char[] oldData = buffer;
        buffer = new char[oldData.length * 2];
        System.arraycopy(oldData, 0, buffer, 0, oldData.length);
    }

    private ModelException newModelException(String message) throws ModelException {
        throw analyzer.newModelException(message);
    }

    private ModelException newModelException(String message, Throwable t) throws ModelException {
        throw analyzer.newModelException(message, t);
    }

    private int parseHexInt() throws NumberFormatException {
        if (numberLength == 0 || numberLength > 8) {
            throw new NumberFormatException("Not an integer value: " + new String(buffer, numberOffset, numberLength));
        }
        if (numberLength == 1) {
            return HEX_TABLE[buffer[numberOffset]];
        }
        int result = 0;
        for (int i = 0; i < numberLength - 1; i++) {
            result |= HEX_TABLE[buffer[numberOffset + i]];
            result <<= 4;
        }
        result |= HEX_TABLE[buffer[numberOffset + numberLength - 1]];
        return result;
    }

    private long parseHexLong() throws NumberFormatException {
        if (numberLength == 0 || numberLength > 16) {
            throw new NumberFormatException("Not a long value: " + new String(buffer, numberOffset, numberLength));
        }
        if (numberLength == 1) {
            return HEX_TABLE[buffer[numberOffset]];
        }
        long result = 0;
        for (int i = 0; i < numberLength - 1; i++) {
            result |= HEX_TABLE[buffer[numberOffset + i]];
            result <<= 4;
        }
        result |= HEX_TABLE[buffer[numberOffset + numberLength - 1]];
        return result;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("DMR reader have been closed");
        }
    }

}
