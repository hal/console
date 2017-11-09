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
package org.jboss.hal.dmr.dmr2;

import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.hal.dmr.dmr2.stream.ModelException;
import org.jboss.hal.dmr.dmr2.stream.ModelWriter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
abstract class ModelValue implements Cloneable {

    private final ModelType type;

    protected ModelValue(ModelType type) {
        this.type = type;
    }

    ModelType getType() {
        return type;
    }

    long asLong() {
        throw new IllegalArgumentException();
    }

    long asLong(long defVal) {
        throw new IllegalArgumentException();
    }

    int asInt() {
        throw new IllegalArgumentException();
    }

    int asInt(int defVal) {
        throw new IllegalArgumentException();
    }

    boolean asBoolean() {
        throw new IllegalArgumentException();
    }

    boolean asBoolean(boolean defVal) {
        throw new IllegalArgumentException();
    }

    double asDouble() {
        throw new IllegalArgumentException();
    }

    double asDouble(double defVal) {
        throw new IllegalArgumentException();
    }

    byte[] asBytes() {
        throw new IllegalArgumentException();
    }

    BigDecimal asBigDecimal() {
        throw new IllegalArgumentException();
    }

    BigInteger asBigInteger() {
        throw new IllegalArgumentException();
    }

    abstract String asString();

    Property asProperty() {
        throw new IllegalArgumentException();
    }

    List<Property> asPropertyList() {
        throw new IllegalArgumentException();
    }

    ValueExpression asExpression() {
        throw new IllegalArgumentException();
    }

    ModelNode asObject() {
        throw new IllegalArgumentException();
    }

    ModelNode getChild(String name) {
        throw new IllegalArgumentException();
    }

    ModelNode removeChild(String name) {
        throw new IllegalArgumentException();
    }

    ModelNode removeChild(int index) {
        throw new IllegalArgumentException();
    }

    ModelNode getChild(int index) {
        throw new IllegalArgumentException();
    }

    ModelNode addChild() {
        throw new IllegalArgumentException();
    }

    ModelNode insertChild(int index) {
        throw new IllegalArgumentException();
    }

    Set<String> getKeys() {
        throw new IllegalArgumentException();
    }

    List<ModelNode> asList() {
        throw new IllegalArgumentException();
    }

    ModelType asType() {
        throw new IllegalArgumentException();
    }

    ModelValue protect() {
        return this;
    }

    @Override
    protected final ModelValue clone() {
        try {
            return (ModelValue) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String quote(String orig) {
        int length = orig.length();
        StringBuilder builder = new StringBuilder(length + 32);
        builder.append('"');
        for (int i = 0; i < length; i = orig.offsetByCodePoints(i, 1)) {
            int cp = orig.codePointAt(i);
            if (cp == '"' || cp == '\\') {
                builder.append('\\').appendCodePoint(cp);
            } else {
                builder.appendCodePoint(cp);
            }
        }
        builder.append('"');
        return builder.toString();
    }

    /**
     * Escapes the original string for inclusion in a JSON string.
     *
     * @param orig A string to be included in a JSON string.
     *
     * @return The string appropriately escaped to produce valid JSON.
     */
    protected static String jsonEscape(String orig) {
        int length = orig.length();
        StringBuilder builder = new StringBuilder(length + 32);
        builder.append('"');
        for (int i = 0; i < length; i = orig.offsetByCodePoints(i, 1)) {
            int cp = orig.codePointAt(i);
            switch (cp) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    // Only escape control characters 0x00 through 0x1F (space is 0x20)
                    // Reference: http://www.ietf.org/rfc/rfc4627.txt
                    if (cp < 0x20) {
                        String hexString = Integer.toHexString(cp);
                        builder.append("\\u");
                        for (int k = 0; k < 4 - hexString.length(); k++) {
                            builder.append('0');
                        }
                        builder.append(hexString.toUpperCase());
                    } else {
                        builder.appendCodePoint(cp);
                    }
            }
        }
        builder.append('"');
        return builder.toString();
    }

    ModelValue copy() {
        return this;
    }

    static final ModelValue UNDEFINED = new ModelValue(ModelType.UNDEFINED) {

        @Override
        String asString() {
            return "undefined";
        }

        @Override
        long asLong(long defVal) {
            return defVal;
        }

        @Override
        int asInt(int defVal) {
            return defVal;
        }

        @Override
        boolean asBoolean(boolean defVal) {
            return defVal;
        }

        @Override
        double asDouble(double defVal) {
            return defVal;
        }

        @Override
        public boolean equals(Object other) {
            return other == this;
        }

        @Override
        void formatAsJSON(PrintWriter writer, int indent, boolean multiLine) {
            writer.append("null");
        }

        @Override
        void writeExternal(DataOutput out) throws IOException {
            out.write(ModelType.UNDEFINED.typeChar);
        }

        @Override
        void write(ModelWriter out) throws IOException, ModelException {
            out.writeUndefined();
        }

        @Override
        public int hashCode() {
            return 7113;
        }
    };

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

    /**
     * Adds the number of indentations (4 spaces each) specified to the writer's output.
     *
     * @param writer The PrintWriter instance containing the current output.
     * @param count  The number of indentations to be written.
     */
    protected static void indent(PrintWriter writer, int count) {
        for (int i = 0; i < count; i++) {
            writer.append("    ");
        }
    }

    /**
     * Formats the current value object as part of a DMR string.
     *
     * @param writer    A PrintWriter instance containing the generated DMR string representation.
     * @param indent    The number of tabs to indent the current generated string.
     * @param multiLine Flag indicating whether or not the string should begin on a new line.
     */
    void format(PrintWriter writer, int indent, boolean multiLine) {
        writer.append(asString());
    }

    /**
     * Formats the current value object as part of a JSON string.
     *
     * @param writer    A PrintWriter instance containing the JSON string.
     * @param indent    The number of tabs to indent the current generated string.
     * @param multiLine Flag that indicates whether or not the string should
     *                  begin on a new line.
     */
    void formatAsJSON(PrintWriter writer, int indent, boolean multiLine) {
        writer.append(asString());
    }

    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter, true);
        writeString(writer, false);
        return stringWriter.toString();
    }

    /**
     * Outputs the DMR representation of this value to the supplied PrintWriter instance.
     *
     * @param writer  A PrintWriter instance use to output the DMR string.
     * @param compact Flag indicating whether or not to include new lines in the generated string representation.
     */
    public void writeString(PrintWriter writer, boolean compact) {
        format(writer, 0, !compact);
    }

    /**
     * Converts this value to a JSON string representation.
     *
     * @param compact Flag indicating whether or not to include new lines in the generated string representation.
     *
     * @return The JSON formatted string representation of this value.
     */
    public String toJSONString(boolean compact) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter, true);
        writeJSONString(writer, compact);
        return stringWriter.toString();
    }

    /**
     * Outputs this value as a JSON string representation to the supplied PrintWriter instance.
     *
     * @param writer  A PrintWriter instance use to output the JSON string.
     * @param compact Flag indicating whether or not to include new lines in the generated string representation.
     */
    public void writeJSONString(PrintWriter writer, boolean compact) {
        formatAsJSON(writer, 0, !compact);
    }

    abstract void write(ModelWriter writer) throws IOException, ModelException;

    ModelValue resolve() {
        return copy();
    }

    abstract void writeExternal(DataOutput out) throws IOException;

    boolean has(int index) {
        return false;
    }

    boolean has(String key) {
        return false;
    }

    ModelNode requireChild(String name) throws NoSuchElementException {
        throw new NoSuchElementException("No child '" + name + "' exists");
    }

    ModelNode requireChild(int index) throws NoSuchElementException {
        throw new NoSuchElementException("No child exists at index [" + index + "]");
    }
}
