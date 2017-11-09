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
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.dmr2.ModelType;

/**
 * DMR reader. Instances of this interface are not thread safe.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @see ModelStreamFactory
 */
public interface ModelReader extends AutoCloseable {

    /**
     * Detects if there is next DMR parsing event available.
     * Users should call this method before calling {@link #next()} method.
     *
     * @return <code>true</code> if there are more DMR parsing events, <code>false</code> otherwise
     */
    boolean hasNext();

    /**
     * Returns next DMR parsing event.
     * Users should call {@link #hasNext()} before calling this method.
     *
     * @return ModelEvent next event
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if wrong DMR is detected
     */
    ModelEvent next() throws IOException, ModelException;

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>object start</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR object start token, false otherwise
     */
    boolean isObjectStart();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>object end</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR object end token, false otherwise
     */
    boolean isObjectEnd();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>property start</code> token,
     * <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR property start token, false otherwise
     */
    boolean isPropertyStart();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>property end</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR property end token, false otherwise
     */
    boolean isPropertyEnd();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>list start</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR list start token, false otherwise
     */
    boolean isListStart();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>list end</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR list end token, false otherwise
     */
    boolean isListEnd();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>undefined</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR undefined token, false otherwise
     */
    boolean isUndefined();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>type</code>, <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR type, false otherwise
     */
    boolean isType();

    /**
     * Converts available context data to <code>model type</code>.
     * Users have to call {@link #next()} and should call {@link #isType()} before calling this method.
     *
     * @return type the parsing cursor is pointing to
     *
     * @throws IllegalStateException if cursor isn't pointing to DMR type
     */
    ModelType getType();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>string</code>, <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR string, false otherwise
     */
    boolean isString();

    /**
     * Converts available context data to <code>String</code>.
     * Users have to call {@link #next()} and should call {@link #isString()} before calling this method.
     *
     * @return string the parsing cursor is pointing to
     *
     * @throws IllegalStateException if cursor isn't pointing to DMR String
     */
    String getString();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>expression</code>, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR expression, false otherwise
     */
    boolean isExpression();

    /**
     * Converts available context data to <code>expression</code>.
     * Users have to call {@link #next()} and should call {@link #isExpression()} before calling this method.
     *
     * @return expression the parsing cursor is pointing to
     *
     * @throws IllegalStateException if cursor isn't pointing to DMR expression
     */
    String getExpression();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>bytes</code>, <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor position points to DMR bytes, false otherwise
     */
    boolean isBytes();

    /**
     * Converts available context data to <code>bytes</code>.
     * Users have to call {@link #next()} and should call {@link #isBytes()} before calling this method.
     *
     * @return bytes the parsing cursor is pointing to
     *
     * @throws IllegalStateException if cursor isn't pointing to DMR Bytes
     */
    byte[] getBytes();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>boolean</code> token, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor points to DMR boolean tokens, false otherwise
     */
    boolean isBoolean();

    /**
     * Converts available context data to <code>boolean</code>.
     * Users have to call {@link #next()} and should call {@link #isBoolean()} before calling this method.
     *
     * @return boolean value the parsing cursor is pointing to
     *
     * @throws IllegalStateException if cursor isn't pointing to DMR boolean token
     */
    boolean getBoolean();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>int</code>, <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor points to DMR int, false otherwise
     */
    boolean isInt();

    /**
     * Converts available context data to <code>int</code>.
     * Users have to call {@link #next()} and should call {@link #isInt()} before calling this method.
     *
     * @return int value the parsing cursor is pointing to
     */
    int getInt();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>long</code>, <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor points to DMR int, false otherwise
     */
    boolean isLong();

    /**
     * Converts available context data to <code>long</code>.
     * Users have to call {@link #next()} and should call {@link #isLong()} before calling this method.
     *
     * @return long value the parsing cursor is pointing to
     */
    long getLong();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>double</code>, <code>false</code> otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor points to DMR double, false otherwise
     */
    boolean isDouble();

    /**
     * Converts available context data to <code>double</code>.
     * Users have to call {@link #next()} and should call {@link #isDouble()} before calling this method.
     *
     * @return double value the parsing cursor is pointing to
     */
    double getDouble();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>big integer</code>, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor points to DMR big integer, false otherwise
     */
    boolean isBigInteger();

    /**
     * Converts available context data to <code>big integer</code> instance.
     * Users have to call {@link #next()} and should call {@link #isBigInteger()} before calling this method.
     *
     * @return big integer value the parsing cursor is pointing to
     */
    BigInteger getBigInteger();

    /**
     * Returns <code>true</code> if current DMR parsing event is DMR <code>big decimal</code>, <code>false</code>
     * otherwise.
     * Users have to call {@link #next()} before calling this method.
     *
     * @return true if the parsing cursor points to DMR big decimal, false otherwise
     */
    boolean isBigDecimal();

    /**
     * Converts available context data to <code>big decimal</code> instance.
     * Users have to call {@link #next()} and should call {@link #isBigDecimal()} before calling this method.
     *
     * @return big decimal value the parsing cursor is pointing to
     */
    BigDecimal getBigDecimal();

    /**
     * Free resources associated with this reader. Never closes underlying output stream or reader.
     *
     * @throws ModelException if attempting to close this reader before reaching EOF.
     */
    @Override
    void close() throws ModelException;

}
