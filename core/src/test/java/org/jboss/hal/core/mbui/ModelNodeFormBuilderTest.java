package org.jboss.hal.core.mbui;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceDescription;
import org.junit.Test;

/**
 * @author Harald Pehl
 */
public class ModelNodeFormBuilderTest {

    @Test(expected = IllegalStateException.class)
    public void noAttributes() {
        new ModelNodeForm.Builder("noAttributes", new ResourceDescription(new ModelNode())).build();
    }
}
