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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.jboss.hal.dmr.dmr2.stream.ModelEvent;
import org.jboss.hal.dmr.dmr2.stream.ModelException;
import org.jboss.hal.dmr.dmr2.stream.ModelReader;
import org.jboss.hal.dmr.dmr2.stream.ModelStreamFactory;

import static org.jboss.hal.dmr.dmr2.stream.ModelEvent.*;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class ModelNodeFactory {

    static final ModelNodeFactory INSTANCE = new ModelNodeFactory();

    private ModelNodeFactory() {
        // forbidden instantiation
    }

    ModelNode readFrom(Reader input, boolean jsonCompatible) throws IOException, ModelException {
        ModelReader reader = ModelStreamFactory.getInstance(jsonCompatible).newModelReader(input);
        return readFrom(reader);
    }

    ModelNode readFrom(String input, boolean jsonCompatible) throws IOException, ModelException {
        Reader reader = new StringReader(input);
        return readFrom(reader, jsonCompatible);
    }

    ModelNode readFrom(InputStream input, boolean jsonCompatible) throws IOException, ModelException {
        ModelReader reader = ModelStreamFactory.getInstance(jsonCompatible).newModelReader(input);
        return readFrom(reader);
    }

    ModelNode readFrom(InputStream input, Charset charset, boolean jsonCompatible) throws IOException, ModelException {
        ModelReader reader = ModelStreamFactory.getInstance(jsonCompatible).newModelReader(input, charset);
        return readFrom(reader);
    }

    private ModelNode readFrom(ModelReader modelReader) throws IOException, ModelException {
        ModelEvent event = modelReader.next();
        if (event == STRING) {
            return readStringFrom(modelReader);
        } else if (event == INT) {
            return readIntFrom(modelReader);
        } else if (event == LONG) {
            return readLongFrom(modelReader);
        } else if (event == DOUBLE) {
            return readDoubleFrom(modelReader);
        } else if (event == BIG_INTEGER) {
            return readBigIntegerFrom(modelReader);
        } else if (event == BIG_DECIMAL) {
            return readBigDecimalFrom(modelReader);
        } else if (event == BYTES) {
            return readBytesFrom(modelReader);
        } else if (event == EXPRESSION) {
            return readExpressionFrom(modelReader);
        } else if (event == TYPE) {
            return readTypeFrom(modelReader);
        } else if (event == BOOLEAN) {
            return readBooleanFrom(modelReader);
        } else if (event == UNDEFINED) {
            return readUndefinedFrom(modelReader);
        } else if (event == OBJECT_START) {
            return readObjectFrom(modelReader);
        } else if (event == LIST_START) {
            return readListFrom(modelReader);
        } else if (event == PROPERTY_START) {
            return readPropertyFrom(modelReader);
        } else {
            throw new IllegalStateException(); // never happens
        }
    }

    private ModelNode readStringFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getString());
    }

    private ModelNode readIntFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getInt());
    }

    private ModelNode readLongFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getLong());
    }

    private ModelNode readDoubleFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getDouble());
    }

    private ModelNode readBigIntegerFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getBigInteger());
    }

    private ModelNode readBigDecimalFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getBigDecimal());
    }

    private ModelNode readBytesFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getBytes());
    }

    private ModelNode readExpressionFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().setExpression(modelReader.getExpression());
    }

    private ModelNode readTypeFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(ModelType.valueOf(modelReader.getType().toString()));
    }

    private ModelNode readBooleanFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode().set(modelReader.getBoolean());
    }

    private ModelNode readUndefinedFrom(ModelReader modelReader) throws IOException, ModelException {
        return new ModelNode();
    }

    private ModelNode readListFrom(ModelReader modelReader) throws IOException, ModelException {
        ModelNode listNode = new ModelNode();
        listNode.setEmptyList();
        ModelEvent event = modelReader.next();
        ModelNode value;
        while (event != LIST_END) {
            if (event == STRING) {
                value = readStringFrom(modelReader);
            } else if (event == INT) {
                value = readIntFrom(modelReader);
            } else if (event == LONG) {
                value = readLongFrom(modelReader);
            } else if (event == DOUBLE) {
                value = readDoubleFrom(modelReader);
            } else if (event == BIG_INTEGER) {
                value = readBigIntegerFrom(modelReader);
            } else if (event == BIG_DECIMAL) {
                value = readBigDecimalFrom(modelReader);
            } else if (event == BYTES) {
                value = readBytesFrom(modelReader);
            } else if (event == EXPRESSION) {
                value = readExpressionFrom(modelReader);
            } else if (event == TYPE) {
                value = readTypeFrom(modelReader);
            } else if (event == BOOLEAN) {
                value = readBooleanFrom(modelReader);
            } else if (event == UNDEFINED) {
                value = readUndefinedFrom(modelReader);
            } else if (event == OBJECT_START) {
                value = readObjectFrom(modelReader);
            } else if (event == LIST_START) {
                value = readListFrom(modelReader);
            } else if (event == PROPERTY_START) {
                value = readPropertyFrom(modelReader);
            } else {
                throw new IllegalStateException(); // never happens
            }
            listNode.addNoCopy(value);
            event = modelReader.next();
        }
        return listNode;
    }

    private ModelNode readObjectFrom(ModelReader modelReader) throws IOException, ModelException {
        ModelNode objectNode = new ModelNode();
        objectNode.setEmptyObject();
        ModelEvent event = modelReader.next();
        String key;
        ModelNode value;
        while (event != OBJECT_END) {
            key = modelReader.getString();
            event = modelReader.next();
            if (event == STRING) {
                value = readStringFrom(modelReader);
            } else if (event == INT) {
                value = readIntFrom(modelReader);
            } else if (event == LONG) {
                value = readLongFrom(modelReader);
            } else if (event == DOUBLE) {
                value = readDoubleFrom(modelReader);
            } else if (event == BIG_INTEGER) {
                value = readBigIntegerFrom(modelReader);
            } else if (event == BIG_DECIMAL) {
                value = readBigDecimalFrom(modelReader);
            } else if (event == BYTES) {
                value = readBytesFrom(modelReader);
            } else if (event == EXPRESSION) {
                value = readExpressionFrom(modelReader);
            } else if (event == TYPE) {
                value = readTypeFrom(modelReader);
            } else if (event == BOOLEAN) {
                value = readBooleanFrom(modelReader);
            } else if (event == UNDEFINED) {
                value = readUndefinedFrom(modelReader);
            } else if (event == OBJECT_START) {
                value = readObjectFrom(modelReader);
            } else if (event == LIST_START) {
                value = readListFrom(modelReader);
            } else if (event == PROPERTY_START) {
                value = readPropertyFrom(modelReader);
            } else {
                throw new IllegalStateException(); // never happens
            }
            objectNode.get(key).setNoCopy(value);
            event = modelReader.next();
        }
        return objectNode;
    }

    private ModelNode readPropertyFrom(ModelReader modelReader) throws IOException, ModelException {
        ModelNode propertyNode = new ModelNode();
        ModelEvent event = modelReader.next();
        String key;
        ModelNode value;
        while (event != PROPERTY_END) {
            key = modelReader.getString();
            event = modelReader.next();
            if (event == STRING) {
                value = readStringFrom(modelReader);
            } else if (event == INT) {
                value = readIntFrom(modelReader);
            } else if (event == LONG) {
                value = readLongFrom(modelReader);
            } else if (event == DOUBLE) {
                value = readDoubleFrom(modelReader);
            } else if (event == BIG_INTEGER) {
                value = readBigIntegerFrom(modelReader);
            } else if (event == BIG_DECIMAL) {
                value = readBigDecimalFrom(modelReader);
            } else if (event == BYTES) {
                value = readBytesFrom(modelReader);
            } else if (event == EXPRESSION) {
                value = readExpressionFrom(modelReader);
            } else if (event == TYPE) {
                value = readTypeFrom(modelReader);
            } else if (event == BOOLEAN) {
                value = readBooleanFrom(modelReader);
            } else if (event == UNDEFINED) {
                value = readUndefinedFrom(modelReader);
            } else if (event == OBJECT_START) {
                value = readObjectFrom(modelReader);
            } else if (event == LIST_START) {
                value = readListFrom(modelReader);
            } else if (event == PROPERTY_START) {
                value = readPropertyFrom(modelReader);
            } else {
                throw new IllegalStateException(); // never happens
            }
            propertyNode.setNoCopy(key, value);
            event = modelReader.next();
        }
        return propertyNode;
    }

}
