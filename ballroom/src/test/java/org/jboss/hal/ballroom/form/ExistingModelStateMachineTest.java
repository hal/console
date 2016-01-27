package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class ExistingModelStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new ExistingModelStateMachine();
    }

    @Test
    public void initialState() {
        assertNull(stateMachine.current());
    }

    @Test
    public void supports() {
        assertTrue(stateMachine.supports(VIEW));
        assertTrue(stateMachine.supports(EDIT));
        assertTrue(stateMachine.supports(CANCEL));
        assertTrue(stateMachine.supports(SAVE));
        assertTrue(stateMachine.supports(RESET));
    }

    @Test
    public void initialView() {
        stateMachine.execute(VIEW);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void repeatedView() {
        stateMachine.execute(VIEW);
        stateMachine.execute(VIEW);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void initialEdit() {
        stateMachine.execute(EDIT);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test
    public void repeatedEdit() {
        stateMachine.execute(EDIT);
        stateMachine.execute(EDIT);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialCancel() {
        stateMachine.execute(CANCEL);
    }

    @Test
    public void cancel() {
        stateMachine.execute(EDIT);
        stateMachine.execute(CANCEL);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialSave() {
        stateMachine.execute(SAVE);
    }

    @Test
    public void save() {
        stateMachine.execute(EDIT);
        stateMachine.execute(SAVE);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialReset() {
        stateMachine.execute(RESET);
    }

    @Test
    public void reset() {
        stateMachine.execute(VIEW);
        stateMachine.execute(RESET);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void viewEditSave() {
        stateMachine.execute(VIEW);
        stateMachine.execute(EDIT);
        stateMachine.execute(SAVE);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void viewEditCancel() {
        stateMachine.execute(VIEW);
        stateMachine.execute(EDIT);
        stateMachine.execute(CANCEL);
        assertEquals(READONLY, stateMachine.current());
    }
}