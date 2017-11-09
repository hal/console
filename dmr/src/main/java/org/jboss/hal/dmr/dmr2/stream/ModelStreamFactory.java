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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * DMR streams factory. Defines an abstract implementation of a factory for getting DMR readers and
 * writers. All readers and writers returned by this factory are not thread safe.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @see ModelWriter
 */
public final class ModelStreamFactory {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final ModelStreamFactory DMR_FACTORY = new ModelStreamFactory(false);
    private static final ModelStreamFactory JSON_FACTORY = new ModelStreamFactory(true);
    private final boolean jsonCompatible;

    /**
     * Forbidden instantiation.
     */
    private ModelStreamFactory(boolean jsonCompatible) {
        this.jsonCompatible = jsonCompatible;
    }

    /**
     * Returns DMR stream factory instance.
     *
     * @param jsonCompatible whether stream factories should read/write JSON
     *
     * @return DMR stream factory instance
     */
    public static ModelStreamFactory getInstance(boolean jsonCompatible) {
        return jsonCompatible ? JSON_FACTORY : DMR_FACTORY;
    }

    /**
     * Creates new DMR writer.
     *
     * @param writer output
     *
     * @return DMR writer instance
     */
    public ModelWriter newModelWriter(Writer writer) {
        assertNotNullParameter(writer);
        return jsonCompatible ? new JsonWriterImpl(writer) : new ModelWriterImpl(writer);
    }

    /**
     * Creates new DMR writer with <code>UTF-8</code> character set.
     *
     * @param stream output
     *
     * @return DMR writer instance
     */
    public ModelWriter newModelWriter(OutputStream stream) {
        return newModelWriter(stream, DEFAULT_CHARSET);
    }

    /**
     * Creates new DMR writer with specified character set.
     *
     * @param stream  output
     * @param charset character set
     *
     * @return DMR writer instance
     */
    public ModelWriter newModelWriter(OutputStream stream, Charset charset) {
        assertNotNullParameter(stream);
        assertNotNullParameter(charset);
        return newModelWriter(new OutputStreamWriter(stream, charset));
    }

    private static void assertNotNullParameter(Object o) {
        if (o == null) {
            throw new NullPointerException("Parameter cannot be null");
        }
    }

}
