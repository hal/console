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

import java.io.Flushable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.hal.dmr.ModelType;

/**
 * DMR writer. Instances of this interface are not thread safe.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @see ModelStreamFactory
 */
public interface ModelWriter extends Flushable, AutoCloseable {

    /**
     * Writes DMR <code>object start</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeObjectStart() throws IOException, ModelException;

    /**
     * Writes DMR <code>object end</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeObjectEnd() throws IOException, ModelException;

    /**
     * Writes DMR <code>property start</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writePropertyStart() throws IOException, ModelException;

    /**
     * Writes DMR <code>property end</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writePropertyEnd() throws IOException, ModelException;

    /**
     * Writes DMR <code>list start</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeListStart() throws IOException, ModelException;

    /**
     * Writes DMR <code>list end</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeListEnd() throws IOException, ModelException;

    /**
     * Writes DMR <code>undefined</code> token.
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeUndefined() throws IOException, ModelException;

    /**
     * Writes DMR <code>string</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeString(String data) throws IOException, ModelException;

    /**
     * Writes DMR <code>expression</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeExpression(String data) throws IOException, ModelException;

    /**
     * Writes DMR <code>bytes</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeBytes(byte[] data) throws IOException, ModelException;

    /**
     * Writes DMR <code>true</code> or <code>false</code> token.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeBoolean(boolean data) throws IOException, ModelException;

    /**
     * Writes DMR <code>number</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeInt(int data) throws IOException, ModelException;

    /**
     * Writes DMR <code>number</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeLong(long data) throws IOException, ModelException;

    /**
     * Writes DMR <code>number</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeBigInteger(BigInteger data) throws IOException, ModelException;

    /**
     * Writes DMR <code>number</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeBigDecimal(BigDecimal data) throws IOException, ModelException;

    /**
     * Writes DMR <code>number</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeDouble(double data) throws IOException, ModelException;

    /**
     * Writes DMR <code>type</code>.
     *
     * @param data to encode
     *
     * @return this writer instance
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    ModelWriter writeType(ModelType data) throws IOException, ModelException;

    /**
     * Writes all cached data.
     *
     * @throws IOException if some I/O error occurs
     */
    @Override
    void flush() throws IOException;

    /**
     * Free resources associated with this writer. Never closes underlying input stream or writer.
     *
     * @throws IOException    if some I/O error occurs
     * @throws ModelException if invalid DMR write attempt is detected
     */
    @Override
    void close() throws IOException, ModelException;

}
