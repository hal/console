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
package org.jboss.hal.core;

import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.junit.Before;
import org.junit.Test;

import static java.util.stream.StreamSupport.stream;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINE_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "OptionalGetWithoutIsPresent",
        "SameParameterValue"})
public class OperationFactoryTest {

    private Metadata metadata;
    private ResourceAddress address;
    private OperationFactory operationFactory;

    @Before
    public void setUp() {
        AddressTemplate template = AddressTemplate.of(
                "/{selected.profile}/subsystem=resource-adapters/resource-adapter=*/connection-definitions=*");
        ModelNode rrd = ExternalModelNode.read(
                OperationFactoryTest.class.getResourceAsStream("connection-definition.dmr"));
        metadata = new Metadata(template, () -> SecurityContext.RWX, new ResourceDescription(rrd),
                new Capabilities(null));
        address = ResourceAddress.root();
        operationFactory = new OperationFactory();
    }

    @Test
    public void empty() {
        Composite composite = operationFactory.fromChangeSet(address, Collections.emptyMap(), metadata);
        assertTrue(composite.isEmpty());
    }

    @Test
    public void notInMetadata() {
        Composite composite = operationFactory.fromChangeSet(address, ImmutableMap.of("foo", "bar"), metadata);

        assertEquals(1, composite.size());
        assertWrite(composite, "foo", "bar");
    }

    @Test
    public void write() {
        Composite composite = operationFactory.fromChangeSet(address, ImmutableMap.of("allocation-retry", 23L),
                metadata);

        assertEquals(1, composite.size());
        assertWrite(composite, "allocation-retry", 23L);
    }

    @Test
    public void undefine() {
        Composite composite = operationFactory.fromChangeSet(address, ImmutableMap.of("class-name", ""), metadata);

        assertEquals(1, composite.size());
        assertUndefine(composite, "class-name");
    }

    @Test
    public void defaultValue() {
        Composite composite = operationFactory.fromChangeSet(address, ImmutableMap.of("enlistment", true), metadata);

        assertEquals(1, composite.size());
        assertUndefine(composite, "enlistment");
    }

    @Test
    public void expression() {
        Composite composite = operationFactory.fromChangeSet(address, ImmutableMap.of("expression", "${foo:bar}"),
                metadata);

        assertEquals(1, composite.size());
        assertWrite(composite, "expression", "${foo:bar}");
    }

    @Test
    public void mixed() {
        Composite composite = operationFactory.fromChangeSet(address,
                ImmutableMap.of("class-name", "", "jndi-name", "java:/foo"), metadata);

        assertEquals(2, composite.size());
        assertUndefine(composite, "class-name");
        assertWrite(composite, "jndi-name", "java:/foo");
    }

    @Test
    public void alternativesNoConflicts() {
        Composite composite = operationFactory.fromChangeSet(address,
                ImmutableMap.of("authentication-context", "foo"), metadata);

        assertEquals(2, composite.size());
        assertUndefine(composite, "authentication-context-and-application");
        assertWrite(composite, "authentication-context", "foo");
    }

    @Test
    public void alternativesNoConflictsWithRequires() {
        Composite composite = operationFactory.fromChangeSet(address,
                ImmutableMap.of("security-domain", "foo"), metadata);

        assertEquals(6, composite.size());
        assertUndefine(composite, "security-application");
        assertUndefine(composite, "security-domain-and-application");
        assertUndefine(composite, "elytron-enabled");
        assertUndefine(composite, "authentication-context");
        assertUndefine(composite, "authentication-context-and-application");
        assertWrite(composite, "security-domain", "foo");
    }

    @Test
    public void alternativesWithConflicts1() {
        // Turn elytron 'on'
        Composite composite = operationFactory.fromChangeSet(address,
                ImmutableMap.of("elytron-enabled", true, "authentication-context", "foo"), metadata);

        assertEquals(6, composite.size());
        assertUndefine(composite, "security-application");
        assertUndefine(composite, "security-domain");
        assertUndefine(composite, "security-domain-and-application");
        assertUndefine(composite, "authentication-context-and-application");
        assertWrite(composite, "elytron-enabled", true);
        assertWrite(composite, "authentication-context", "foo");
    }

    @Test
    public void alternativesWithConflicts2() {
        // Turn elytron 'off'
        Composite composite = operationFactory.fromChangeSet(address,
                ImmutableMap.of("elytron-enabled", false, "security-domain", "foo"), metadata);

        assertEquals(6, composite.size());
        assertUndefine(composite, "security-application");
        assertUndefine(composite, "security-domain-and-application");
        assertUndefine(composite, "elytron-enabled");
        assertUndefine(composite, "authentication-context");
        assertUndefine(composite, "authentication-context-and-application");
        assertWrite(composite, "security-domain", "foo");
    }

    @Test
    public void reset() {
        Composite composite = operationFactory.resetResource(address,
                Sets.newHashSet("authentication-context", // string w/ alternative
                        "capacity-decrementer-class", // string
                        "capacity-incrementer-properties", // object
                        "class-name", // string(required)
                        "connectable", // boolean(false)
                        "initial-pool-size", // int
                        "max-pool-size", // int(20)
                        "xa-resource-timeout"), // is required by wrap-xa-resource
                metadata);
        assertUndefine(composite, "capacity-decrementer-class");
        assertUndefine(composite, "capacity-incrementer-properties");
        assertUndefine(composite, "connectable");
        assertUndefine(composite, "max-pool-size");
        assertUndefineNotPresent(composite, "xa-resource-timeout");
    }

    private void assertUndefine(Composite composite, String name) {
        assertTrue(stream(composite.spliterator(), false)
                .anyMatch(operation -> UNDEFINE_ATTRIBUTE_OPERATION.equals(operation.getName()) &&
                        operation.hasDefined(NAME) &&
                        operation.get(NAME).asString().equals(name)));
    }

    private void assertUndefineNotPresent(Composite composite, String name) {
        assertTrue(stream(composite.spliterator(), false)
                .noneMatch(operation -> UNDEFINE_ATTRIBUTE_OPERATION.equals(operation.getName()) &&
                        operation.hasDefined(NAME) &&
                        operation.get(NAME).asString().equals(name)));
    }

    private void assertWrite(Composite composite, String name, boolean value) {
        Optional<Operation> operation = writeOperation(composite, name);
        assertTrue(operation.isPresent());
        assertEquals(value, operation.get().get(VALUE).asBoolean());
    }

    private void assertWrite(Composite composite, String name, long value) {
        Optional<Operation> operation = writeOperation(composite, name);
        assertTrue(operation.isPresent());
        assertEquals(value, operation.get().get(VALUE).asLong());
    }

    private void assertWrite(Composite composite, String name, String value) {
        Optional<Operation> operation = writeOperation(composite, name);
        assertTrue(operation.isPresent());
        assertEquals(value, operation.get().get(VALUE).asString());
    }

    private Optional<Operation> writeOperation(Composite composite, String name) {
        return stream(composite.spliterator(), false)
                .filter(operation -> WRITE_ATTRIBUTE_OPERATION.equals(operation.getName()) &&
                        operation.hasDefined(NAME) &&
                        operation.get(NAME).asString().equals(name))
                .findAny();
    }
}