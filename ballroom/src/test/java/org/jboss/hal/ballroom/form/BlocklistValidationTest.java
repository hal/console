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
package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import com.google.gwt.junit.GWTMockUtilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

@SuppressWarnings("HardCodedStringLiteral")
public class BlocklistValidationTest {

    private TestableBlocklistValidation blocklistValidation;

    @Before
    public void setUp() {
        GWTMockUtilities.disarm();
        blocklistValidation = new TestableBlocklistValidation("foo", "bar");
    }

    @Test
    public void validateNull() {
        assertSame(ValidationResult.OK, blocklistValidation.validate(null));
    }

    @Test
    public void validateEmpty() {
        assertSame(ValidationResult.OK, blocklistValidation.validate(""));
    }

    @Test
    public void validateBlank() {
        assertSame(ValidationResult.OK, blocklistValidation.validate("   "));
    }

    @Test
    public void validateOk() {
        assertSame(ValidationResult.OK, blocklistValidation.validate("ok"));
    }

    @Test
    public void invalid() {
        assertFalse(blocklistValidation.validate("foo").isValid());
        assertFalse(blocklistValidation.validate("bar").isValid());
    }
}
