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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.hal.dmr.stream.ModelException;
import org.jboss.hal.dmr.stream.ModelWriter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class ObjectModelValue extends ModelValue {

    private final Map<String, ModelNode> map;

    ObjectModelValue() {
        super(ModelType.OBJECT);
        map = new LinkedHashMap<>();
    }

    private ObjectModelValue(Map<String, ModelNode> map) {
        super(ModelType.OBJECT);
        this.map = map;
    }

    ObjectModelValue(DataInput in) {
        super(ModelType.OBJECT);
        int count = in.readInt();
        LinkedHashMap<String, ModelNode> map = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            String key = in.readUTF();
            ModelNode value = new ModelNode();
            value.readExternal(in);
            map.put(key, value);
        }
        this.map = map;
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeByte(ModelType.OBJECT.typeChar);
        Map<String, ModelNode> map = this.map;
        int size = map.size();
        out.writeInt(size);
        for (Map.Entry<String, ModelNode> entry : map.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().writeExternal(out);
        }
    }

    @Override
    ModelValue protect() {
        Map<String, ModelNode> map = this.map;
        for (ModelNode node : map.values()) {
            node.protect();
        }
        return map.getClass() == LinkedHashMap.class ? new ObjectModelValue(Collections.unmodifiableMap(map)) : this;
    }

    @Override
    ModelNode asObject() {
        return new ModelNode(copy());
    }

    @Override
    ModelNode getChild(String name) {
        if (name == null) {
            return null;
        }
        ModelNode node = map.get(name);
        if (node != null) {
            return node;
        }
        ModelNode newNode = new ModelNode();
        map.put(name, newNode);
        return newNode;
    }

    @Override
    ModelNode removeChild(String name) {
        if (name == null) {
            return null;
        }
        return map.remove(name);
    }

    @Override
    int asInt() {
        return map.size();
    }

    @Override
    int asInt(int defVal) {
        return asInt();
    }

    @Override
    long asLong() {
        return asInt();
    }

    @Override
    long asLong(long defVal) {
        return asInt();
    }

    @Override
    boolean asBoolean() {
        return !map.isEmpty();
    }

    @Override
    boolean asBoolean(boolean defVal) {
        return !map.isEmpty();
    }

    @Override
    Property asProperty() {
        if (map.size() == 1) {
            Map.Entry<String, ModelNode> entry = map.entrySet().iterator().next();
            return new Property(entry.getKey(), entry.getValue());
        }
        return super.asProperty();
    }

    @Override
    List<Property> asPropertyList() {
        List<Property> propertyList = new ArrayList<>();
        for (Map.Entry<String, ModelNode> entry : map.entrySet()) {
            propertyList.add(new Property(entry.getKey(), entry.getValue()));
        }
        return propertyList;
    }

    @Override
    ModelValue copy() {
        LinkedHashMap<String, ModelNode> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, ModelNode> entry : map.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().clone());
        }
        return new ObjectModelValue(newMap);
    }

    @Override
    List<ModelNode> asList() {
        ArrayList<ModelNode> nodes = new ArrayList<>();
        for (Map.Entry<String, ModelNode> entry : map.entrySet()) {
            ModelNode node = new ModelNode();
            node.set(entry.getKey(), entry.getValue());
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    Set<String> getKeys() {
        return map.keySet();
    }

    @Override
    String asString() {
        StringBuilder builder = new StringBuilder();
        format(builder, 0, false);
        return builder.toString();
    }

    @Override
    void format(StringBuilder builder, int indent, boolean multiLineRequested) {
        builder.append('{');
        boolean multiLine = multiLineRequested && map.size() > 1;
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        }
        Iterator<Map.Entry<String, ModelNode>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ModelNode> entry = iterator.next();
            builder.append(quote(entry.getKey()));
            ModelNode value = entry.getValue();
            builder.append(" => ");
            value.format(builder, multiLine ? indent + 1 : indent, multiLineRequested);
            if (iterator.hasNext()) {
                if (multiLine) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(',');
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        }
        builder.append('}');
    }

    @Override
    void formatAsJSON(StringBuilder builder, int indent, boolean multiLineRequested) {
        builder.append('{');
        boolean multiLine = multiLineRequested && map.size() > 1;
        if (multiLine) {
            indent(builder.append('\n'), indent + 1);
        }
        Iterator<Map.Entry<String, ModelNode>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ModelNode> entry = iterator.next();
            builder.append(quote(entry.getKey()));
            builder.append(" : ");
            ModelNode value = entry.getValue();
            value.formatAsJSON(builder, multiLine ? indent + 1 : indent, multiLineRequested);
            if (iterator.hasNext()) {
                if (multiLine) {
                    indent(builder.append(",\n"), indent + 1);
                } else {
                    builder.append(", ");
                }
            }
        }
        if (multiLine) {
            indent(builder.append('\n'), indent);
        }
        builder.append('}');
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof ObjectModelValue && equals((ObjectModelValue) other);
    }

    /**
     * Determine whether this object is equal to another.
     *
     * @param other the other object
     *
     * @return {@code true} if they are equal, {@code false} otherwise
     */
    public boolean equals(ObjectModelValue other) {
        return this == other || other != null && other.map.equals(map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    boolean has(String key) {
        return map.containsKey(key);
    }

    @Override
    ModelNode requireChild(String name) throws NoSuchElementException {
        ModelNode node = map.get(name);
        if (node != null) {
            return node;
        }
        return super.requireChild(name);
    }

    @Override
    void write(ModelWriter writer) throws ModelException {
        writer.writeObjectStart();
        Iterator<Map.Entry<String, ModelNode>> iterator = map.entrySet().iterator();
        Map.Entry<String, ModelNode> entry;
        while (iterator.hasNext()) {
            entry = iterator.next();
            writer.writeString(entry.getKey());
            entry.getValue().write(writer);
        }
        writer.writeObjectEnd();
    }

}
