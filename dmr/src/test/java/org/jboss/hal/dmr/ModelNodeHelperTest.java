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
package org.jboss.hal.dmr;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ModelNodeHelperTest {

    private ModelNode modelNode;
    private ModelNode foo;
    private ModelNode bar;

    /**
     * Creates the model node
     *
     * <pre>
     *     {"foo" : {"bar" : 42}}
     * </pre>
     */
    @Before
    public void setUp() {
        modelNode = new ModelNode();
        bar = new ModelNode().set(42);
        foo = new ModelNode();
        foo.get("bar").set(bar);
        modelNode.get("foo").set(foo);
    }

    @Test
    public void nullPath() {
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, null).isDefined());
    }

    @Test
    public void emptyPath() {
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "").isDefined());
    }

    @Test
    public void invalidPath() {
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "/").isDefined());
    }

    @Test
    public void wrongPath() {
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "a").isDefined());
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "a/b").isDefined());
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "foo/bar/baz").isDefined());
    }

    @Test
    public void simplePath() {
        ModelNode node = ModelNodeHelper.failSafeGet(modelNode, "foo");
        assertTrue(node.isDefined());
        assertEquals(foo, node);
    }

    @Test
    public void nestedPath() {
        ModelNode node = ModelNodeHelper.failSafeGet(modelNode, "foo/bar");
        assertTrue(node.isDefined());
        assertEquals(bar, node);
    }

    @Test
    public void flatToNestedNull() {
        assertNull(ModelNodeHelper.flatToNested(null));
    }

    @Test
    public void flatToNestedUndefined() {
        assertFalse(ModelNodeHelper.flatToNested(new ModelNode()).isDefined());
    }

    @Test
    public void flatToNestedFlat() {
        assertEquals(modelNode, ModelNodeHelper.flatToNested(modelNode));
    }

    @Test
    public void flatToNested() {
        ModelNode flat = new ModelNode();
        flat.get("foo.bar").set(42);
        assertEquals(modelNode, ModelNodeHelper.flatToNested(flat));
    }

    @Test
    public void flatToNestedWithOverlay() {
        ModelNode flat = new ModelNode();
        flat.get("a.b").set("ab");
        flat.get("a.b.c").set("abc");

        ModelNode nested = new ModelNode();
        nested.get("a").get("b").get("c").set("abc");

        assertEquals(nested, ModelNodeHelper.flatToNested(flat));
    }
}
