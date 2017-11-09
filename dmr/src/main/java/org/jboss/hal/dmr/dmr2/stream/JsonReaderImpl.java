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
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.dmr2.ModelType;

import static org.jboss.hal.dmr.dmr2.stream.ModelConstants.*;
import static org.jboss.hal.dmr.dmr2.stream.Utils.*;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class JsonReaderImpl implements ModelReader {

    private static final char[] INFINITY = ModelConstants.INFINITY.toCharArray();
    private static final char[] NAN = ModelConstants.NAN.toCharArray();
    private static final char[] NULL = ModelConstants.NULL.toCharArray();
    private static final char[] TRUE = ModelConstants.TRUE.toCharArray();
    private static final char[] FALSE = ModelConstants.FALSE.toCharArray();
    private static final String UNEXPECTED_CHARACTER = "Unexpected character '";
    private static final String UNEXPECTED_EOF = "Unexpected EOF while reading DMR stream";
    private final Reader in;
    private final JsonGrammarAnalyzer analyzer;
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
    private boolean stringReadInAdvance;

    JsonReaderImpl(Reader in) {
        this.in = in;
        analyzer = new JsonGrammarAnalyzer();
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
            // we read object keys in advance to detect bytes, types or expression types
            if (stringReadInAdvance) {
                stringReadInAdvance = false;
                analyzer.putString();
                return analyzer.currentEvent;
            }
            BigInteger bigIntegerValue;
            int currentChar;
            int radix;
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
                    case COLON: {
                        analyzer.putColon();
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
                        radix = 10;
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
                            } else if (buffer[position] == '0') {
                                position--;
                                if (ensureBufferAccessNoFail(3)) {
                                    if (buffer[position + 2] == 'x') {
                                        radix = 16;
                                        position += 2;
                                    } else if (isDigit(buffer[position + 2])) {
                                        radix = 8;
                                        position++;
                                    }
                                }
                                position++;
                            } else if (!isNumberChar(buffer[position])) {
                                throw newModelException("Unexpected first character '" + buffer[position]
                                        + "' while reading DMR Infinity or NaN or number token");
                            }
                        }
                        if (currentChar == '0') {
                            position--;
                            if (ensureBufferAccessNoFail(2)) {
                                if (buffer[position + 1] == 'x') {
                                    radix = 16;
                                    position++;
                                } else if (isDigit(buffer[position + 1])) {
                                    radix = 8;
                                }
                            }
                            position++;
                        }
                        if (radix == 10) {
                            position--;
                        }
                        readNumber(radix > 10);
                        if (isDecimalString()) {
                            try {
                                analyzer.putNumber(ModelEvent.BIG_DECIMAL);
                                bigDecimalValue = new BigDecimal(new String(buffer, numberOffset, numberLength));
                            } catch (NumberFormatException nfe) {
                                throw newModelException("Incorrect decimal value", nfe);
                            }
                        } else {
                            try {
                                if (radix == 10) {
                                    bigIntegerValue = new BigInteger(new String(buffer, numberOffset, numberLength),
                                            radix);
                                } else {
                                    bigIntegerValue = new BigInteger(
                                            (currentChar == MINUS ? "-" : "+") + new String(buffer, numberOffset,
                                                    numberLength), radix);
                                }
                                if (bigIntegerValue.bitLength() <= 31) {
                                    analyzer.putNumber(ModelEvent.INT);
                                    intValue = bigIntegerValue.intValue();
                                } else if (bigIntegerValue.bitLength() <= 63) {
                                    analyzer.putNumber(ModelEvent.LONG);
                                    longValue = bigIntegerValue.longValue();
                                } else {
                                    analyzer.putNumber(ModelEvent.BIG_INTEGER);
                                    this.bigIntegerValue = bigIntegerValue;
                                }
                            } catch (NumberFormatException nfe) {
                                throw newModelException("Incorrect integer value", nfe);
                            }
                        }
                        return analyzer.currentEvent;
                    }
                    case 'I': {
                        position--;
                        readString(INFINITY);
                        analyzer.putNumber(ModelEvent.DOUBLE);
                        doubleValue = Double.POSITIVE_INFINITY;
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
                    case 'n': {
                        analyzer.putUndefined();
                        position--;
                        readString(NULL);
                        return analyzer.currentEvent;
                    }
                    case OBJECT_START: {
                        processWhitespaces();
                        currentChar = position < limit ? buffer[position++] : read();
                        if (currentChar == QUOTE) {
                            readString();
                            stringValue = new String(buffer, stringOffset, stringLength);
                            if (TYPE_MODEL_VALUE.equals(stringValue)) {
                                processWhitespaces();
                                readType();
                                analyzer.putType();
                            } else if (BYTES_VALUE.equals(stringValue)) {
                                processWhitespaces();
                                readBytes();
                                analyzer.putBytes();
                            } else if (EXPRESSION_VALUE.equals(stringValue)) {
                                processWhitespaces();
                                readExpression();
                                analyzer.putExpression();
                            } else {
                                stringReadInAdvance = true;
                                analyzer.putObjectStart();
                            }
                        } else {
                            if (currentChar != -1) {
                                position--;
                            }
                            analyzer.putObjectStart();
                        }
                        return analyzer.currentEvent;
                    }
                    case LIST_START: {
                        analyzer.putListStart();
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
            throw newModelException(UNEXPECTED_EOF);
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
                    } else if (currentChar == 't') {
                        buffer[stringOffset + stringLength++] = TAB;
                    } else if (currentChar == 'u') {
                        if (limit - position >= 4) {
                            try {
                                buffer[stringOffset + stringLength++] = (char) Integer.parseInt(
                                        new String(buffer, position, 4), 16);
                            } catch (NumberFormatException e) {
                                throw newModelException(
                                        "Invalid DMR unicode sequence. Expecting 4 hexadecimal digits but got '" + new String(
                                                buffer, position, 4) + "'");
                            }
                            position += 4;
                        } else {
                            if (stringOffset != 0) {
                                if (stringLength > 0) {
                                    System.arraycopy(buffer, stringOffset, buffer, 0, stringLength);
                                }
                                position = stringLength;
                                limit = stringLength;
                                stringOffset = 0;
                            }
                            while (limit + 4 > buffer.length) {
                                doubleBuffer();
                            }
                            fillBuffer();
                            if (limit - position < 4) {
                                throw newModelException(UNEXPECTED_EOF);
                            }
                            try {
                                buffer[stringOffset + stringLength++] = (char) Integer.parseInt(
                                        new String(buffer, position, 4), 16);
                            } catch (NumberFormatException e) {
                                throw newModelException(
                                        "Invalid DMR unicode sequence. Expecting 4 hexadecimal digits but got '" + new String(
                                                buffer, position, 4) + "'");
                            }
                            position += 4;
                        }
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
                    if (isControl(currentChar)) {
                        throw newModelException(
                                "Unexpected control character '" + currentChar + "' while reading DMR string");
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
                throw newModelException(UNEXPECTED_EOF);
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
                    throw newModelException(UNEXPECTED_EOF);
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

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("DMR reader have been closed");
        }
    }

    private void readType() throws IOException, ModelException {
        ensureBufferAccess(1);
        char currentChar = buffer[position++];
        if (currentChar != COLON) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR type value");
        }
        processWhitespaces();
        ensureBufferAccess(1);
        currentChar = buffer[position++];
        if (currentChar != QUOTE) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR type value");
        }
        readString();
        try {
            typeValue = ModelType.valueOf(new String(buffer, stringOffset, stringLength));
        } catch (IllegalArgumentException e) {
            throw newModelException(e.getMessage(), e);
        }
        processWhitespaces();
        ensureBufferAccess(1);
        currentChar = buffer[position++];
        if (currentChar != OBJECT_END) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR type value");
        }
    }

    private void readBytes() throws IOException, ModelException {
        ensureBufferAccess(1);
        char currentChar = buffer[position++];
        if (currentChar != COLON) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR bytes value");
        }
        processWhitespaces();
        ensureBufferAccess(1);
        currentChar = buffer[position++];
        if (currentChar != QUOTE) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR bytes value");
        }
        base64Canonicalize();
        base64Decode();
        processWhitespaces();
        ensureBufferAccess(1);
        currentChar = buffer[position++];
        if (currentChar != OBJECT_END) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR bytes value");
        }
    }

    private void base64Canonicalize() throws IOException, ModelException {
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
                    if (currentChar != 'n' && currentChar != 'r') {
                        throw newModelException(
                                UNEXPECTED_CHARACTER + currentChar + "' after escape character while reading DMR base64 string");
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
                    if (!isBase64Char(currentChar)) {
                        throw newModelException(
                                UNEXPECTED_CHARACTER + currentChar + "' while reading DMR base64 string");
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
                throw newModelException(UNEXPECTED_EOF);
            }
        }
    }

    private void base64Decode() throws IOException, ModelException {
        if (stringLength == 0) {
            bytesValue = EMPTY_BYTES;
            return;
        }
        if (stringLength % 4 != 0) {
            throw newModelException("Encoded base64 value is not dividable by 4");
        }
        int paddingSize = 0;
        for (int i = 1; i <= 2; i++) {
            if (buffer[stringOffset + stringLength - i] == EQUAL) {
                paddingSize = i;
            }
        }
        bytesValue = new byte[(stringLength / 4 * 3) - paddingSize];
        int j = 0;
        int[] b = new int[4];
        for (int i = 0; i < stringLength; i += 4) {
            b[0] = BASE64_DEC_TABLE[buffer[stringOffset + i]];
            b[1] = BASE64_DEC_TABLE[buffer[stringOffset + i + 1]];
            b[2] = BASE64_DEC_TABLE[buffer[stringOffset + i + 2]];
            b[3] = BASE64_DEC_TABLE[buffer[stringOffset + i + 3]];
            bytesValue[j++] = (byte) ((b[0] << 2) | (b[1] >> 4));
            if (b[2] != INCORRECT_DATA) {
                bytesValue[j++] = (byte) ((b[1] << 4) | (b[2] >> 2));
                if (b[3] != INCORRECT_DATA) {
                    bytesValue[j++] = (byte) ((b[2] << 6) | b[3]);
                }
            }
        }
    }

    private void readExpression() throws IOException, ModelException {
        ensureBufferAccess(1);
        char currentChar = buffer[position++];
        if (currentChar != COLON) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR expression value");
        }
        processWhitespaces();
        ensureBufferAccess(1);
        currentChar = buffer[position++];
        if (currentChar != QUOTE) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR expression value");
        }
        readString();
        stringValue = new String(buffer, stringOffset, stringLength);
        processWhitespaces();
        ensureBufferAccess(1);
        currentChar = buffer[position++];
        if (currentChar != OBJECT_END) {
            throw newModelException(UNEXPECTED_CHARACTER + currentChar + "' while reading DMR expression value");
        }
    }

}
