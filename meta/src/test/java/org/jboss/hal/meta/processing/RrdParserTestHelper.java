/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.processing;

import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("CheckStyle")
class RrdParserTestHelper {

    static void assertResourceDescriptions(RrdResult rrdResult, int size, String... addresses) {
        assertEquals(size, rrdResult.resourceDescriptions.size());

        for (String a : addresses) {
            ResourceAddress address = AddressTemplate.of(a).resolve(StatementContext.NOOP);
            assertTrue("RrdResult does not contain resource description for " + a, // NON-NLS
                    rrdResult.containsResourceDescription(address));
        }
    }

    static void assertSecurityContexts(RrdResult rrdResult, int size, String... addresses) {
        assertEquals(size, rrdResult.securityContexts.size());

        for (String a : addresses) {
            ResourceAddress address = AddressTemplate.of(a).resolve(StatementContext.NOOP);
            assertTrue("RrdResult does not contain security context for " + a, // NON-NLS
                    rrdResult.containsSecurityContext(address));
        }
    }
}
