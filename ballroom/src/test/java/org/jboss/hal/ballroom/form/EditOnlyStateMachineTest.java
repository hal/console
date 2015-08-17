package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class EditOnlyStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new EditOnlyStateMachine();
    }

    @Test
    public void initialState() {
        assertNull(stateMachine.current());
    }

    @Test
    public void supports() {
        assertFalse(stateMachine.supports(VIEW));
        assertTrue(stateMachine.supports(EDIT));
        assertFalse(stateMachine.supports(CANCEL));
        assertFalse(stateMachine.supports(SAVE));
        assertFalse(stateMachine.supports(RESET));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void view() {
        stateMachine.execute(VIEW);
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
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialSave() {
        stateMachine.execute(SAVE);
    }

    @Test
    public void save() {
        stateMachine.execute(EDIT);
        stateMachine.execute(SAVE);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void reset() {
        stateMachine.execute(RESET);
    }
}