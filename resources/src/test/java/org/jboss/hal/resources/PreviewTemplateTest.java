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
package org.jboss.hal.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("HardCodedStringLiteral")
public class PreviewTemplateTest {

    private static final String LOREM = "Lorem ipsum dolor sit amet";
    private static final String LOREM_EXPRESSION = "Lorem ${ipsum} dolor sit amet";
    private static final String TEMPLATE = "${build.shortName} also known as ${build.fullName} is installed in ${build.installDir}";
    private static final String COMMUNITY = "WildFly also known as WildFly is installed in WILDFLY_HOME";
    private static final String PRODUCT = "JBoss EAP also known as JBoss Enterprise Application Platform is installed in EAP_HOME";

    private PreviewTemplate community = PreviewTemplate.COMMUNITY;
    private PreviewTemplate product = PreviewTemplate.PRODUCT;

    @Test
    public void nil() throws Exception {
        assertNull(community.evaluate(null));
    }

    @Test
    public void empty() throws Exception {
        assertEquals("", community.evaluate(""));
    }

    @Test
    public void noop() throws Exception {
        assertEquals(LOREM, community.evaluate(LOREM));
        assertEquals(LOREM_EXPRESSION, community.evaluate(LOREM_EXPRESSION));
    }

    @Test
    public void community() throws Exception {
        assertEquals(COMMUNITY, community.evaluate(TEMPLATE));
    }

    @Test
    public void product() throws Exception {
        assertEquals(PRODUCT, product.evaluate(TEMPLATE));
    }
}