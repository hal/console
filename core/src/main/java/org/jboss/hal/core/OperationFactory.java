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
package org.jboss.hal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 * @author Harald Pehl
 */
public class OperationFactory {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(OperationFactory.class);

    /**
     * Turns a change-set into a composite operation containing {@linkplain org.jboss.hal.dmr.ModelDescriptionConstants#WRITE_ATTRIBUTE_OPERATION write-attribute}
     * and {@link org.jboss.hal.dmr.ModelDescriptionConstants#UNDEFINE_ATTRIBUTE_OPERATION undefine-attribute} operations.
     * <p>
     * This method does not take any metadata into account when assembling the composite operations. In particular
     * alternative attributes won't be turned into {@link org.jboss.hal.dmr.ModelDescriptionConstants#UNDEFINE_ATTRIBUTE_OPERATION}
     * operations. Please use this signature for simple change-sets only or if you're sure that the attributes in the
     * change-set don't have alternative attributes.
     */
    public Composite fromChangeSet(final ResourceAddress address, final Map<String, Object> changeSet) {
        return fromChangeSet(address, changeSet, null);
    }

    /**
     * Turns a change-set into a composite operation containing {@link org.jboss.hal.dmr.ModelDescriptionConstants#WRITE_ATTRIBUTE_OPERATION}
     * and {@link org.jboss.hal.dmr.ModelDescriptionConstants#UNDEFINE_ATTRIBUTE_OPERATION} operations.
     * <p>
     * The composite operation will contain {@link org.jboss.hal.dmr.ModelDescriptionConstants#UNDEFINE_ATTRIBUTE_OPERATION}
     * operations which reflect the alternative attributes as defined in the specified metadata.
     */
    public Composite fromChangeSet(final ResourceAddress address, final Map<String, Object> changeSet,
            final Metadata metadata) {

        List<Operation> operations = new ArrayList<>();
        for (String name : changeSet.keySet()) {
            Object value = changeSet.get(name);

            if (value == null
                    || (value instanceof String && (Strings.isNullOrEmpty((String) value)))
                    || (value instanceof List && ((List) value).isEmpty())
                    || (value instanceof Map && ((Map) value).isEmpty())) {
                operations.add(new Operation.Builder(UNDEFINE_ATTRIBUTE_OPERATION, address)
                        .param(NAME, name)
                        .build());

            } else {
                ModelNode valueNode = asValueNode(value);
                if (valueNode != null) {
                    operations.add(new Operation.Builder(WRITE_ATTRIBUTE_OPERATION, address)
                            .param(NAME, name)
                            .param(VALUE, valueNode)
                            .build());
                } else {
                    logger.error("Unsupported type {} when building composite operation for {} from changeset {}",
                            value.getClass(), address, changeSet);
                }
            }
        }

        return new Composite(operations);
    }

    private ModelNode asValueNode(final Object value) {
        Class type = value.getClass();
        ModelNode valueNode = new ModelNode();

        if (String.class == type) {
            String stringValue = (String) value;
            if (stringValue.startsWith("$")) {
                valueNode.setExpression(stringValue);
            } else {
                valueNode.set(stringValue);
            }
        } else if (Boolean.class == type) {
            valueNode.set((Boolean) value);
        } else if (Integer.class == type) {
            valueNode.set((Integer) value);
        } else if (Double.class == type) {
            valueNode.set((Double) value);
        } else if (Long.class == type) {
            valueNode.set((Long) value);
        } else if (Float.class == type) {
            valueNode.set((Float) value);
        } else if (ArrayList.class == type) {
            valueNode.clear();
            List l = (List) value;
            for (Object o : l) { valueNode.add(String.valueOf(o)); }
        } else if (HashMap.class == type) {
            valueNode.clear();
            Map map = (Map) value;
            for (Object k : map.keySet()) {
                valueNode.get(String.valueOf(k)).set(String.valueOf(map.get(k)));
            }
        } else if (ModelNode.class == type) {
            valueNode.set((ModelNode) value);
        } else {
            valueNode = null;
        }

        return valueNode;
    }
}
