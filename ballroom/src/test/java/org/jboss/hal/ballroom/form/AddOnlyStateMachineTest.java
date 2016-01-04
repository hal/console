package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class AddOnlyStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new AddOnlyStateMachine();
    }

    @Test
    public void initialState() {
        assertNull(stateMachine.current());
    }

    @Test
    public void supports() {
        assertFalse(stateMachine.supports(VIEW));
        assertTrue(stateMachine.supports(ADD));
        assertFalse(stateMachine.supports(EDIT));
        assertFalse(stateMachine.supports(CANCEL));
        assertFalse(stateMachine.supports(SAVE));
        assertFalse(stateMachine.supports(RESET));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void view() {
        stateMachine.execute(VIEW);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void edit() {
        stateMachine.execute(EDIT);
    }

    @Test
    public void initialAdd() {
        stateMachine.execute(ADD);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test
    public void repeatedAdd() {
        stateMachine.execute(ADD);
        stateMachine.execute(ADD);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialCancel() {
        stateMachine.execute(CANCEL);
    }

    @Test
    public void cancel() {
        stateMachine.execute(ADD);
        stateMachine.execute(CANCEL);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialSave() {
        stateMachine.execute(SAVE);
    }

    @Test
    public void save() {
        stateMachine.execute(ADD);
        stateMachine.execute(SAVE);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void reset() {
        stateMachine.execute(RESET);
    }
}