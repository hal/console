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
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class ModelConstants {

    static final char LIST_END = ']';

    static final char LIST_START = '[';

    static final char BACKSLASH = '\\';

    static final char BACKSPACE = '\b';

    static final char CR = '\r';

    static final char COLON = ':';

    static final char COMMA = ',';

    static final char FORMFEED = '\f';

    static final char NL = '\n';

    static final char OBJECT_END = '}';

    static final char OBJECT_START = '{';

    static final char BYTES_END = '}';

    static final char BYTES_START = '{';

    static final char PROPERTY_END = ')';

    static final char PROPERTY_START = '(';

    static final char QUOTE = '\"';

    static final char SPACE = ' ';

    static final char EQUAL = '=';

    static final char TAB = '\t';

    static final String ARROW = "=>";

    static final String BIG = "big";

    static final String BYTES = "bytes";

    static final String DECIMAL = "decimal";

    static final String EXPRESSION = "expression";

    static final String FALSE = "false";

    static final String INTEGER = "integer";

    static final String NULL = "null";

    static final String TRUE = "true";

    static final String TYPE_MODEL_VALUE = "TYPE_MODEL_VALUE";

    static final String BYTES_VALUE = "BYTES_VALUE";

    static final String EXPRESSION_VALUE = "EXPRESSION_VALUE";

    static final String UNDEFINED = "undefined";

    private ModelConstants() {
        // forbidden instantiation
    }
}
