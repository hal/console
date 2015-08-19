package org.jboss.hal.core.mbui;

import org.jboss.hal.ballroom.form.DefaultStateMachine;
import org.jboss.hal.ballroom.form.EditOnlyStateMachine;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.ballroom.form.ViewOnlyStateMachine;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Harald Pehl
 */
public class ModelNodeFormBuilderTest {

    @Test(expected = IllegalStateException.class)
    public void viewAndEdit() {
        new ModelNodeForm.Builder("viewAndEdit", new ResourceDescriptionBuilder().empty())
                .viewOnly()
                .editOnly()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noAttributes() {
        new ModelNodeForm.Builder("noAttributes", new ResourceDescriptionBuilder().empty()).build();
    }

    @Test(expected = IllegalStateException.class)
    public void noRequestProperties() {
        new ModelNodeForm.Builder("noRequestProperties", new ResourceDescriptionBuilder().empty())
                .createResource()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void viewAndCreateResource() {
        new ModelNodeForm.Builder("viewAndCreateResource", new ResourceDescriptionBuilder().empty())
                .viewOnly()
                .createResource()
                .build();
    }

    @Test
    public void createResourceStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("createResourceStateMachine",
                new ResourceDescriptionBuilder().requestProperties()).createResource().stateMachine();
        Assert.assertTrue(stateMachine instanceof EditOnlyStateMachine);
    }

    @Test
    public void editOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("editOnlyStateMachine",
                new ResourceDescriptionBuilder().attributes()).editOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof EditOnlyStateMachine);
    }

    @Test
    public void viewOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("viewOnlyStateMachine",
                new ResourceDescriptionBuilder().attributes()).viewOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof ViewOnlyStateMachine);
    }

    @Test
    public void defaultStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("defaultStateMachine",
                new ResourceDescriptionBuilder().attributes()).stateMachine();
        Assert.assertTrue(stateMachine instanceof DefaultStateMachine);
    }
}
