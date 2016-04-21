package org.jboss.hal.dmr;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ModelNodeHelperTest {

    private ModelNode modelNode;
    private ModelNode foo;
    private ModelNode bar;

    /**
     * Creates the model node
     * <pre>
     *     ("foo" => ("bar" => 42))
     * </pre>
     */
    @Before
    public void setUp() {
        modelNode = new ModelNode();
        foo = new ModelNode();
        bar = new ModelNode().set(42);
        foo.set("bar", bar);
        modelNode.set("foo", foo);
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
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, ".").isDefined());
    }

    @Test
    public void wrongPath() {
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "a").isDefined());
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "a.b").isDefined());
        assertFalse(ModelNodeHelper.failSafeGet(modelNode, "foo.bar.baz").isDefined());
    }

    @Test
    public void simplePath() {
        ModelNode node = ModelNodeHelper.failSafeGet(modelNode, "foo");
        assertTrue(node.isDefined());
        assertEquals(foo, node);
    }

    @Test
    public void nestedPath() {
        ModelNode node = ModelNodeHelper.failSafeGet(modelNode, "foo.bar");
        assertTrue(node.isDefined());
        assertEquals(bar, node);
    }
}