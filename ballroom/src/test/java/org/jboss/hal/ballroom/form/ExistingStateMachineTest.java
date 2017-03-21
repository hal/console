package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class ExistingStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new ExistingStateMachine();
    }

    @Test
    public void initialState() {
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void supports() {
        assertFalse(stateMachine.supports(EMPTY));
        assertTrue(stateMachine.supports(READONLY));
        assertTrue(stateMachine.supports(EDITING));

        assertTrue(stateMachine.supports(VIEW));
        assertTrue(stateMachine.supports(CLEAR));
        assertTrue(stateMachine.supports(RESET));
        assertTrue(stateMachine.supports(EDIT));
        assertTrue(stateMachine.supports(SAVE));
        assertTrue(stateMachine.supports(CANCEL));
        assertFalse(stateMachine.supports(REMOVE));
    }

    @Test
    public void view() {
        stateMachine.execute(VIEW);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void clear() {
        stateMachine.execute(CLEAR);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void reset() {
        stateMachine.execute(RESET);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void edit() {
        stateMachine.execute(EDIT);
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
        assertEquals(READONLY, stateMachine.current());
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

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        stateMachine.execute(REMOVE);
    }
}