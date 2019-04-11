/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class PropertyModelValue extends ModelValue {

    /** JSON Key used to identify PropertyModelValue. */
    private static final String TYPE_KEY = "PROPERTY_VALUE";
    private final Property property;

    PropertyModelValue(String name, ModelNode value) {
        this(new Property(name, value));
    }

    private PropertyModelValue(Property property) {
        super(ModelType.PROPERTY);
        if (property == null) {
            throw new IllegalArgumentException("property is null");
        }
        this.property = property;
    }

    PropertyModelValue(DataInput in) {
        super(ModelType.PROPERTY);
        ModelNode node = new ModelNode();
        String name = in.readUTF();
        node.readExternal(in);
        property = new Property(name, node);
    }

    @Override
    void writeExternal(DataOutput out) {
        out.writeUTF(property.getName());
        property.getValue().writeExternal(out);
    }

    @Override
    ModelValue protect() {
        property.getValue().protect();
        return this;
    }

    @Override
    String asString() {
        return "(" + quote(property.getName()) + " => " + property.getValue() + ")";
    }

    @Override
    Property asProperty() {
        return property;
    }

    @Override
    List<Property> asPropertyList() {
        return Collections.singletonList(property);
    }

    @Override
    ModelNode asObject() {
        ModelNode node = new ModelNode();
        node.get(property.getName()).set(property.getValue());
        return node;
    }

    @Override
    Set<String> getKeys() {
        return Collections.singleton(property.getName());
    }

    @Override
    List<ModelNode> asList() {
        return Collections.singletonList(new ModelNode(this));
    }

    @Override
    ModelNode getChild(String name) {
        return property.getName().equals(name) ? property.getValue() : super.getChild(name);
    }

    @Override
    ModelNode getChild(int index) {
        return index == 0 ? property.getValue() : super.getChild(index);
    }

    @Override
    ModelValue copy() {
        return new PropertyModelValue(property.getName(), property.getValue());
    }

    @Override
    ModelValue resolve() {
        return new PropertyModelValue(property.getName(), property.getValue().resolve());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PropertyModelValue && equals((PropertyModelValue) other);
    }

    public boolean equals(PropertyModelValue other) {
        return this == other || other != null && other.property.getName().equals(property.getName()) && other.property
                .getValue().equals(property.getValue());
    }

    @Override
    public int hashCode() {
        return property.getName().hashCode() * 31 + property.getValue().hashCode();
    }

    @Override
    boolean has(String key) {
        return key.equals(property.getName());
    }

    @Override
    ModelNode requireChild(String name) throws NoSuchElementException {
        return property.getName().equals(name) ? property.getValue() : super.requireChild(name);
    }

    void formatAsJSON(StringBuilder builder, int indent, boolean multiLineRequested) {
        builder.append('{');
        if (multiLineRequested) {
            indent(builder.append('\n'), indent + 1);
        } else {
            builder.append(' ');
        }
        builder.append(jsonEscape(TYPE_KEY));
        builder.append(" : ");
        formatPropertyAsJSON(builder, indent + 1, multiLineRequested);
        if (multiLineRequested) {
            indent(builder.append('\n'), indent);
        } else {
            builder.append(' ');
        }
        builder.append('}');
    }

    private void formatPropertyAsJSON(StringBuilder writer, int indent, boolean multiLineRequested) {
        writer.append('{');
        if (multiLineRequested) {
            indent(writer.append('\n'), indent + 1);
        } else {
            writer.append(' ');
        }
        writer.append(jsonEscape(property.getName()));
        writer.append(" : ");
        property.getValue().formatAsJSON(writer, indent + 1, multiLineRequested);
        if (multiLineRequested) {
            indent(writer.append('\n'), indent);
        } else {
            writer.append(' ');
        }
        writer.append('}');
    }
}
