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

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MissingMetadataException;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import static org.jboss.hal.meta.processing.LookupResult.ALL_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.RESOURCE_DESCRIPTION_PRESENT;
import static org.jboss.hal.meta.processing.LookupResult.SECURITY_CONTEXT_PRESENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LookupResultTest {

    private LookupResult lookupResult;
    private AddressTemplate template;

    @Before
    public void setUp() {
        template = AddressTemplate.of("template");
        lookupResult = new LookupResult(Sets.<AddressTemplate> newHashSet(template));
    }

    @Test
    public void initialState() {
        assertEquals(1, lookupResult.templates().size());
        assertEquals(0, lookupResult.missingMetadata(template));
    }

    @Test(expected = MissingMetadataException.class)
    public void missingMetadata() {
        lookupResult.missingMetadata(AddressTemplate.of("foo"));
    }

    @Test
    public void markResourceDescriptionPresent() {
        lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
        assertEquals(0b10, lookupResult.missingMetadata(template));
    }

    @Test
    public void markSecurityContextPresent() {
        lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
        assertEquals(0b01, lookupResult.missingMetadata(template));
    }

    @Test
    public void markAllPresent() {
        lookupResult.markMetadataPresent(template, RESOURCE_DESCRIPTION_PRESENT);
        lookupResult.markMetadataPresent(template, SECURITY_CONTEXT_PRESENT);
        assertEquals(0b11, lookupResult.missingMetadata(template));
    }

    @Test
    public void allPresent() {
        LookupResult localLookupResult = new LookupResult(Sets.newHashSet(
                AddressTemplate.of("one"),
                AddressTemplate.of("two"),
                AddressTemplate.of("three")));

        localLookupResult.markMetadataPresent(AddressTemplate.of("one"), ALL_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("two"), RESOURCE_DESCRIPTION_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("three"), SECURITY_CONTEXT_PRESENT);
        assertFalse(localLookupResult.allPresent());

        localLookupResult.markMetadataPresent(AddressTemplate.of("one"), ALL_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("two"), ALL_PRESENT);
        localLookupResult.markMetadataPresent(AddressTemplate.of("three"), ALL_PRESENT);
        assertTrue(localLookupResult.allPresent());
    }
}
