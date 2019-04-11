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
package org.jboss.hal.meta.description;

import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.TestableStatementContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@SuppressWarnings("DuplicateStringLiteralInspection")
public class ResourceDescriptionStatementContextTest {

    private StatementContext statementContext;

    @Before
    public void setUp() throws Exception {
        Environment environment = mock(Environment.class);
        statementContext = new ResourceDescriptionStatementContext(new TestableStatementContext(), environment);
    }

    @Test
    public void domainController() throws Exception {
        ResourceAddress address = AddressTemplate.of("/{domain.controller}/foo=bar").resolve(statementContext);
        assertEquals("/host=*/foo=bar", address.toString());
    }

    @Test
    public void selectedProfile() throws Exception {
        ResourceAddress address = AddressTemplate.of("/{selected.profile}/foo=bar").resolve(statementContext);
        assertEquals("/profile=*/foo=bar", address.toString());
    }

    @Test
    public void selectedGroup() throws Exception {
        ResourceAddress address = AddressTemplate.of("/{selected.group}/foo=bar").resolve(statementContext);
        assertEquals("/server-group=*/foo=bar", address.toString());
    }

    @Test
    public void selectedHost() throws Exception {
        ResourceAddress address = AddressTemplate.of("/{selected.host}/foo=bar").resolve(statementContext);
        assertEquals("/host=*/foo=bar", address.toString());
    }

    @Test
    public void selectedServerConfig() throws Exception {
        ResourceAddress address = AddressTemplate.of("/host=master/{selected.server-config}/foo=bar")
                .resolve(statementContext);
        assertEquals("/host=master/server-config=*/foo=bar", address.toString());
    }

    @Test
    public void selectedServer() throws Exception {
        ResourceAddress address = AddressTemplate.of("/host=master/{selected.server}/foo=bar")
                .resolve(statementContext);
        assertEquals("/host=master/server=*/foo=bar", address.toString());
    }

    @Test
    public void selection() throws Exception {
        ResourceAddress address = AddressTemplate.of("/foo=bar/qux={selection}").resolve(statementContext);
        assertEquals("/foo=bar/qux=*", address.toString());
    }

    @Test
    public void regular() throws Exception {
        ResourceAddress address = AddressTemplate.of("/foo=bar").resolve(statementContext);
        assertEquals("/foo=bar", address.toString());
    }
}