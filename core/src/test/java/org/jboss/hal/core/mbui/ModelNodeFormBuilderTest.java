package org.jboss.hal.core.mbui;

import org.jboss.hal.ballroom.form.DefaultStateMachine;
import org.jboss.hal.ballroom.form.EditOnlyStateMachine;
import org.jboss.hal.ballroom.form.StateMachine;
import org.jboss.hal.ballroom.form.ViewOnlyStateMachine;
import org.jboss.hal.security.SecurityContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Harald Pehl
 */
public class ModelNodeFormBuilderTest {

    @Test(expected = IllegalStateException.class)
    public void viewAndEdit() {
        new ModelNodeForm.Builder("viewAndEdit", SecurityContext.RWX, new ResourceDescriptionBuilder().empty())
                .viewOnly()
                .editOnly()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noAttributes() {
        new ModelNodeForm.Builder("noAttributes", SecurityContext.RWX, new ResourceDescriptionBuilder().empty())
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void noRequestProperties() {
        new ModelNodeForm.Builder("noRequestProperties", SecurityContext.RWX, new ResourceDescriptionBuilder().empty())
                .createResource()
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void viewAndCreateResource() {
        new ModelNodeForm.Builder("viewAndCreateResource", SecurityContext.RWX,
                new ResourceDescriptionBuilder().empty())
                .viewOnly()
                .createResource()
                .build();
    }

    @Test
    public void createResourceStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("createResourceStateMachine",
                SecurityContext.RWX,
                new ResourceDescriptionBuilder().requestProperties()).createResource().stateMachine();
        Assert.assertTrue(stateMachine instanceof EditOnlyStateMachine);
    }

    @Test
    public void editOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("editOnlyStateMachine",
                SecurityContext.RWX,
                new ResourceDescriptionBuilder().attributes()).editOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof EditOnlyStateMachine);
    }

    @Test
    public void viewOnlyStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("viewOnlyStateMachine",
                SecurityContext.RWX,
                new ResourceDescriptionBuilder().attributes()).viewOnly().stateMachine();
        Assert.assertTrue(stateMachine instanceof ViewOnlyStateMachine);
    }

    @Test
    public void defaultStateMachine() {
        StateMachine stateMachine = new ModelNodeForm.Builder("defaultStateMachine",
                SecurityContext.RWX,
                new ResourceDescriptionBuilder().attributes()).stateMachine();
        Assert.assertTrue(stateMachine instanceof DefaultStateMachine);
    }
}
