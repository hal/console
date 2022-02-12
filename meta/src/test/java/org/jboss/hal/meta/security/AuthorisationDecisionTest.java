/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.security;

import java.util.Optional;

import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.meta.security.SecurityContext.READ_ONLY;
import static org.jboss.hal.meta.security.SecurityContext.RWX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral" })
public class AuthorisationDecisionTest {

    private Environment rbac;
    private Environment simple;
    private SecurityContext securityContext;
    private Constraint foo;
    private Constraint bar;

    @Before
    public void setUp() throws Exception {
        rbac = mock(Environment.class);
        when(rbac.getAccessControlProvider()).thenReturn(AccessControlProvider.RBAC);

        simple = mock(Environment.class);
        when(simple.getAccessControlProvider()).thenReturn(AccessControlProvider.SIMPLE);

        foo = Constraint.executable(AddressTemplate.ROOT, "foo");
        bar = Constraint.executable(AddressTemplate.ROOT, "bar");

        securityContext = mock(SecurityContext.class);
        when(securityContext.isExecutable("foo")).thenReturn(true);
        when(securityContext.isExecutable("bar")).thenReturn(false);
    }

    @Test
    public void rbacAllowed() throws Exception {
        assertTrue(AuthorisationDecision.from(rbac, c -> Optional.of(RWX)).isAllowed(foo));
    }

    @Test
    public void rbacForbidden() throws Exception {
        assertFalse(AuthorisationDecision.from(rbac, c -> Optional.of(READ_ONLY)).isAllowed(foo));
    }

    @Test
    public void simpleAllowed() throws Exception {
        assertTrue(AuthorisationDecision.from(simple, c -> Optional.of(RWX)).isAllowed(foo));
    }

    @Test
    public void simpleForbidden() throws Exception {
        assertTrue(AuthorisationDecision.from(simple, c -> Optional.of(READ_ONLY)).isAllowed(foo));
    }

    @Test
    public void emptyReadOnly() throws Exception {
        assertTrue(AuthorisationDecision.from(rbac, c -> Optional.of(READ_ONLY)).isAllowed(Constraints.empty()));
    }

    @Test
    public void emptyRwx() throws Exception {
        assertTrue(AuthorisationDecision.from(rbac, c -> Optional.of(RWX)).isAllowed(Constraints.empty()));
    }

    @Test
    public void singleReadOnly() throws Exception {
        assertFalse(AuthorisationDecision.from(rbac, c -> Optional.of(READ_ONLY))
                .isAllowed(Constraints.single(foo)));
    }

    @Test
    public void singleRwx() throws Exception {
        assertTrue(AuthorisationDecision.from(rbac, c -> Optional.of(RWX)).isAllowed(Constraints.single(foo)));
    }

    @Test
    public void and() throws Exception {
        Constraints constraints = Constraints.and(foo, bar);
        assertFalse(AuthorisationDecision.from(rbac, c -> Optional.of(securityContext)).isAllowed(constraints));
    }

    @Test
    public void or() throws Exception {
        Constraints constraints = Constraints.or(foo, bar);
        assertTrue(AuthorisationDecision.from(rbac, c -> Optional.of(securityContext)).isAllowed(constraints));
    }
}