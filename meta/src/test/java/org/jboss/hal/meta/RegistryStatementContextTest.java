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
package org.jboss.hal.meta;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class RegistryStatementContextTest {

    private StatementContext statementContext;

    @Before
    public void setUp() throws Exception {
        Environment environment = mock(Environment.class);
        statementContext = new RegistryStatementContext(StatementContext.NOOP, environment);
    }

    @Test
    public void selectedProfile() throws Exception {
        ResourceAddress address = AddressTemplate.of("/{selected.profile}/subsystem=io").resolve(statementContext);
        assertEquals("/profile=*/subsystem=io", address.toString());
    }

    @Test
    public void selectedGroup() throws Exception {
        ResourceAddress address = AddressTemplate.of("/{selected.group}/jvm=*").resolve(statementContext);
        assertEquals("/server-group=*/jvm=*", address.toString());
    }

    @Test
    public void selection() throws Exception {
        ResourceAddress address = AddressTemplate.of("/subsystem=resource-adapters/resource-adapter={selection}")
                .resolve(statementContext);
        assertEquals("/subsystem=resource-adapters/resource-adapter=*", address.toString());
    }

    @Test
    public void regular() throws Exception {
        ResourceAddress address = AddressTemplate.of("/subsystem=datasources").resolve(statementContext);
        assertEquals("/subsystem=datasources", address.toString());
    }
}