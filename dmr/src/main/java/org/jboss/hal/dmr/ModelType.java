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
package org.jboss.hal.dmr;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public enum ModelType {
    BIG_DECIMAL('d'),
    BIG_INTEGER('i'),
    BOOLEAN('Z'),
    BYTES('b'),
    DOUBLE('D'),
    EXPRESSION('e'),
    INT('I'),
    LIST('l'),
    LONG('J'),
    OBJECT('o'),
    PROPERTY('p'),
    STRING('s'),
    TYPE('t'),
    UNDEFINED('u');

    static ModelType forChar(char c) {
        switch (c) {
            case 'J':
                return LONG;
            case 'I':
                return INT;
            case 'Z':
                return BOOLEAN;
            case 's':
                return STRING;
            case 'D':
                return DOUBLE;
            case 'd':
                return BIG_DECIMAL;
            case 'i':
                return BIG_INTEGER;
            case 'b':
                return BYTES;
            case 'l':
                return LIST;
            case 't':
                return TYPE;
            case 'o':
                return OBJECT;
            case 'p':
                return PROPERTY;
            case 'e':
                return EXPRESSION;
            case 'u':
                return UNDEFINED;
            default:
                throw new IllegalArgumentException("Invalid type character '" + c + "'");
        }
    }

    final char typeChar;

    ModelType(char typeChar) {
        this.typeChar = typeChar;
    }

    char getTypeChar() {
        return typeChar;
    }
}
