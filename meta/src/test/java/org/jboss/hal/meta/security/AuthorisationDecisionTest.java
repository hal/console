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
package org.jboss.hal.meta.security;

import java.util.Optional;

import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class AuthorisationDecisionTest {

    private Constraint constraint;
    private Environment rbac;
    private Environment simple;

    @Before
    public void setUp() throws Exception {
        rbac = mock(Environment.class);
        when(rbac.getAccessControlProvider()).thenReturn(AccessControlProvider.RBAC);

        simple = mock(Environment.class);
        when(simple.getAccessControlProvider()).thenReturn(AccessControlProvider.SIMPLE);

        constraint = Constraint.executable(AddressTemplate.of("{selected.profile}/subsystem=datasources/data-source=*"),
                REMOVE);
    }

    @Test
    public void rbacAllowed() throws Exception {
        assertTrue(AuthorisationDecision.from(rbac, c -> Optional.of(SecurityContext.RWX)).isAllowed(constraint));
    }

    @Test
    public void rbacForbidden() throws Exception {
        assertFalse(
                AuthorisationDecision.from(rbac, c -> Optional.of(SecurityContext.READ_ONLY)).isAllowed(constraint));
    }

    @Test
    public void simpleAllowed() throws Exception {
        assertTrue(AuthorisationDecision.from(simple, c -> Optional.of(SecurityContext.RWX)).isAllowed(constraint));
    }

    @Test
    public void simpleForbidden() throws Exception {
        assertTrue(
                AuthorisationDecision.from(simple, c -> Optional.of(SecurityContext.READ_ONLY)).isAllowed(constraint));
    }
}