/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class ExpressionValue extends ModelValue {

    /** JSON Key used to identify ExpressionValue. */
    private static final String TYPE_KEY = "EXPRESSION_VALUE";
    private static final int INITIAL = 0;
    private static final int GOT_DOLLAR = 1;
    private static final int GOT_OPEN_BRACE = 2;
    private static final int RESOLVED = 3;
    private static final int DEFAULT = 4;

    /**
     * Replace properties of the form:
     * <code>${<i>&lt;name&gt;[</i>,<i>&lt;name2&gt;[</i>,<i>&lt;name3&gt;...]][</i>:<i>&lt;default&gt;]</i>}</code>
     *
     * @param value
     *
     * @return
     */
    private static String replaceProperties(String value) {
        StringBuilder builder = new StringBuilder();
        int len = value.length();
        int state = 0;
        int start = -1;
        int nameStart = -1;
        for (int i = 0; i < len; i = value.offsetByCodePoints(i, 1)) {
            char ch = value.charAt(i);
            switch (state) {
                case INITIAL: {
                    switch (ch) {
                        case '$': {
                            state = GOT_DOLLAR;
                            continue;
                        }
                        default: {
                            builder.append(ch);
                            continue;
                        }
                    }
                    // not reachable
                }
                case GOT_DOLLAR: {
                    switch (ch) {
                        case '$': {
                            builder.append(ch);
                            state = INITIAL;
                            continue;
                        }
                        case '{': {
                            start = i + 1;
                            nameStart = start;
                            state = GOT_OPEN_BRACE;
                            continue;
                        }
                        default: {
                            // invalid; emit and resume
                            builder.append('$').append(ch);
                            state = INITIAL;
                            continue;
                        }
                    }
                    // not reachable
                }
                case GOT_OPEN_BRACE: {
                    switch (ch) {
                        case ':':
                        case '}':
                        case ',': {
                            String name = value.substring(nameStart, i).trim();
                            if ("/".equals(name)) {
                                builder.append('/');
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            } else if (":".equals(name)) {
                                builder.append('/');
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            }
                            String val = null;// System.getProperty(name);
                            if (val != null) {
                                builder.append(val);
                                state = ch == '}' ? INITIAL : RESOLVED;
                                continue;
                            } else if (ch == ',') {
                                nameStart = i + 1;
                                continue;
                            } else if (ch == ':') {
                                start = i + 1;
                                state = DEFAULT;
                                continue;
                            } else {
                                builder.append(value.substring(start - 2, i + 1));
                                state = INITIAL;
                                continue;
                            }
                        }
                        default: {
                            continue;
                        }
                    }
                    // not reachable
                }
                case RESOLVED: {
                    if (ch == '}') {
                        state = INITIAL;
                    }
                    continue;
                }
                case DEFAULT: {
                    if (ch == '}') {
                        state = INITIAL;
                        builder.append(value.substring(start, i));
                    }
                    continue;
                }
                default:
                    throw new IllegalStateException();
            }
        }
        switch (state) {
            case GOT_DOLLAR: {
                builder.append('$');
                break;
            }
            case DEFAULT:
            case GOT_OPEN_BRACE: {
                builder.append(value.substring(start - 2));
                break;
            }
            default:
                break;
        }
        return builder.toString();
    }

    private final String expressionString;

    ExpressionValue(String expressionString) {
        super(ModelType.EXPRESSION);
        if (expressionString == null) {
            throw new IllegalArgumentException("expressionString is null");
        }
        this.expressionString = expressionString;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeUTF(expressionString);
    }

    @Override
    String asString() {
        return expressionString;
    }

    @Override
    void format(StringBuilder builder, int indent, boolean multiLine) {
        builder.append("expression ").append(quote(expressionString));
    }

    @Override
    void formatAsJSON(StringBuilder builder, int indent, boolean multiLine) {
        builder.append('{');
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        } else {
            builder.append(' ');
        }
        builder.append(jsonEscape(TYPE_KEY));
        builder.append(" : ");
        builder.append(jsonEscape(asString()));
        if (multiLine) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ExpressionValue && equals((ExpressionValue) other);
    }

    public boolean equals(ExpressionValue other) {
        return this == other || other != null && expressionString.equals(other.expressionString);
    }

    @Override
    public int hashCode() {
        return expressionString.hashCode();
    }

    @Override
    ModelValue resolve() {
        return new StringModelValue(replaceProperties(expressionString));
    }

    // ------------------------------------------------------ code not in jboss-dmr

    @Override
    boolean asBoolean(boolean defVal) {
        return defVal;
    }
}
