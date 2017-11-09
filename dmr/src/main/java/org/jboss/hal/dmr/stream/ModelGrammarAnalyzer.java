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
final class ModelGrammarAnalyzer {

    private static final byte LIST_START = 1;
    private static final byte OBJECT_START = 2;
    private static final byte PROPERTY_START = 4;
    private static final byte STRING = 8;
    private static final byte ARROW = 16;
    private boolean canWriteComma;
    private boolean canWriteArrow;
    private boolean expectedPropertyEnd;
    private byte[] stack = new byte[8];
    private int index;
    private ModelEvent currentEvent;
    boolean finished;

    ModelGrammarAnalyzer() {
    }

    boolean isArrowExpected() {
        return canWriteArrow;
    }

    boolean isCommaExpected() {
        return canWriteComma;
    }

    void putObjectEnd() throws ModelException {
        // preconditions
        if (finished || index == 0 || stack[index - 1] != OBJECT_START) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.OBJECT_END;
        index--;
        if (index > 0) {
            if (stack[index - 1] == ARROW) {
                index -= 2;
                canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
                expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
            } else if (stack[index - 1] == LIST_START) {
                canWriteComma = true;
            }
        }
        if (index == 0) {
            finished = true;
        }
    }

    void putListEnd() throws ModelException {
        // preconditions
        if (finished || index == 0 || stack[index - 1] != LIST_START) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.LIST_END;
        index--;
        if (index > 0) {
            if (stack[index - 1] == ARROW) {
                index -= 2;
                canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
                expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
            } else if (stack[index - 1] == LIST_START) {
                canWriteComma = true;
            }
        } else {
            finished = true;
        }
    }

    void putPropertyEnd() throws ModelException {
        // preconditions
        if (finished || index == 0 || stack[index - 1] != PROPERTY_START || !expectedPropertyEnd) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.PROPERTY_END;
        expectedPropertyEnd = false;
        index--;
        if (index > 0) {
            if (stack[index - 1] == ARROW) {
                index -= 2;
                canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
                expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
            } else if (stack[index - 1] == LIST_START) {
                canWriteComma = true;
            }
        } else {
            finished = true;
        }
    }

    void putExpression() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.EXPRESSION;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putNumber(ModelEvent numberEvent) throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = numberEvent;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putBoolean() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.BOOLEAN;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putBytes() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.BYTES;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putUndefined() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.UNDEFINED;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putType() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.TYPE;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putString() throws ModelException {
        // preconditions
        if (finished || canWriteComma || expectedPropertyEnd || index != 0 && (stack[index - 1] & (OBJECT_START | LIST_START | PROPERTY_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.STRING;
        if (index == 0) {
            finished = true;
            return;
        }
        if (stack[index - 1] == OBJECT_START) {
            if (index == stack.length) {
                doubleStack();
            }
            stack[index++] = STRING;
            canWriteArrow = true;
            return;
        }
        if (stack[index - 1] == PROPERTY_START) {
            if (index == stack.length) {
                doubleStack();
            }
            stack[index++] = STRING;
            canWriteArrow = true;
            return;
        }
        if (stack[index - 1] == ARROW) {
            index -= 2;
        }
        canWriteComma = (stack[index - 1] & (OBJECT_START | LIST_START)) != 0;
        expectedPropertyEnd = stack[index - 1] == PROPERTY_START;
    }

    void putObjectStart() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.OBJECT_START;
        if (index == stack.length) {
            doubleStack();
        }
        stack[index++] = OBJECT_START;
    }

    void putListStart() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.LIST_START;
        if (index == stack.length) {
            doubleStack();
        }
        stack[index++] = LIST_START;
    }

    void putPropertyStart() throws ModelException {
        // preconditions
        if (finished || canWriteComma || index != 0 && (stack[index - 1] & (LIST_START | ARROW)) == 0) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = ModelEvent.PROPERTY_START;
        if (index == stack.length) {
            doubleStack();
        }
        stack[index++] = PROPERTY_START;
    }

    void putArrow() throws ModelException {
        // preconditions
        if (finished || index == 0 || stack[index - 1] != STRING) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = null;
        if (index == stack.length) {
            doubleStack();
        }
        stack[index++] = ARROW;
        canWriteArrow = false;
    }

    void putComma() throws ModelException {
        // preconditions
        if (finished || !canWriteComma) {
            throw newModelException(getExpectingTokensMessage());
        }
        // implementation
        currentEvent = null;
        canWriteComma = false;
    }

    private String getExpectingTokensMessage() {
        if (index == 0) {
            if (!finished) {
                return "Expecting OBJECT_START or LIST_START or PROPERTY_START or STRING or EXPRESSION or BYTES or NUMBER or BOOLEAN or TYPE or UNDEFINED";
            } else {
                return "Expecting EOF";
            }
        }
        if (stack[index - 1] == OBJECT_START) {
            if (!canWriteComma) {
                return "Expecting OBJECT_END or STRING";
            } else {
                return "Expecting ',' or OBJECT_END";
            }
        }
        if (stack[index - 1] == PROPERTY_START) {
            if (!expectedPropertyEnd) {
                return "Expecting STRING";
            } else {
                return "Expecting PROPERTY_END";
            }
        }
        if (stack[index - 1] == LIST_START) {
            if (!canWriteComma) {
                if (currentEvent != null) {
                    return "Expecting LIST_END or OBJECT_START or LIST_START or PROPERTY_START or STRING or EXPRESSION or BYTES or NUMBER or BOOLEAN or TYPE or UNDEFINED";
                } else {
                    return "Expecting OBJECT_START or LIST_START or PROPERTY_START or STRING or EXPRESSION or BYTES or NUMBER or BOOLEAN or TYPE or UNDEFINED";
                }
            } else {
                return "Expecting ',' or LIST_END";
            }
        }
        if (stack[index - 1] == ARROW) {
            return "Expecting OBJECT_START or LIST_START or PROPERTY_START or STRING or EXPRESSION or BYTES or NUMBER or BOOLEAN or TYPE or UNDEFINED";
        }
        if (stack[index - 1] == STRING) {
            return "Expecting '=>'";
        }
        throw new IllegalStateException();
    }

    private void doubleStack() {
        byte[] oldData = stack;
        stack = new byte[oldData.length * 2];
        System.arraycopy(oldData, 0, stack, 0, oldData.length);
    }

    ModelException newModelException(String s) {
        finished = true;
        currentEvent = null;
        return new ModelException(s);
    }
}
