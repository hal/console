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

/**
 * DMR parsing events.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public enum ModelEvent {
    /**
     * Parsing cursor points to DMR <CODE>boolean</CODE>.
     */
    BOOLEAN,
    /**
     * Parsing cursor points to DMR <CODE>bytes</CODE>.
     */
    BYTES,
    /**
     * Parsing cursor points to DMR <CODE>expression</CODE>.
     */
    EXPRESSION,
    /**
     * Parsing cursor points to DMR <CODE>list end</CODE> token.
     */
    LIST_END,
    /**
     * Parsing cursor points to DMR <CODE>list start</CODE> token.
     */
    LIST_START,
    /**
     * Parsing cursor points to DMR <CODE>int</CODE>.
     */
    INT,
    /**
     * Parsing cursor points to DMR <CODE>long</CODE>.
     */
    LONG,
    /**
     * Parsing cursor points to DMR <CODE>double</CODE>.
     */
    DOUBLE,
    /**
     * Parsing cursor points to DMR <CODE>big integer</CODE>.
     */
    BIG_INTEGER,
    /**
     * Parsing cursor points to DMR <CODE>big decimal</CODE>.
     */
    BIG_DECIMAL,
    /**
     * Parsing cursor points to DMR <CODE>object start</CODE> token.
     */
    OBJECT_START,
    /**
     * Parsing cursor points to DMR <CODE>object end</CODE> token.
     */
    OBJECT_END,
    /**
     * Parsing cursor points to DMR <CODE>property end</CODE> token.
     */
    PROPERTY_END,
    /**
     * Parsing cursor points to DMR <CODE>property start</CODE> token.
     */
    PROPERTY_START,
    /**
     * Parsing cursor points to DMR <CODE>string</CODE>.
     */
    STRING,
    /**
     * Parsing cursor points to DMR <CODE>type</CODE>.
     */
    TYPE,
    /**
     * Parsing cursor points to DMR <CODE>undefined</CODE> token.
     */
    UNDEFINED,
}
