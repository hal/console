package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.*;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class ReadOnlyStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new ReadOnlyStateMachine();
    }

    @Test
    public void initialState() {
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void supports() {
        assertFalse(stateMachine.supports(EMPTY));
        assertTrue(stateMachine.supports(READONLY));
        assertFalse(stateMachine.supports(EDITING));

        assertTrue(stateMachine.supports(VIEW));
        assertTrue(stateMachine.supports(CLEAR));
        assertFalse(stateMachine.supports(RESET));
        assertFalse(stateMachine.supports(EDIT));
        assertFalse(stateMachine.supports(SAVE));
        assertFalse(stateMachine.supports(CANCEL));
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

    @Test(expected = UnsupportedOperationException.class)
    public void reset() {
        stateMachine.execute(RESET);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void edit() {
        stateMachine.execute(EDIT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void save() {
        stateMachine.execute(SAVE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cancel() {
        stateMachine.execute(CANCEL);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        stateMachine.execute(REMOVE);
    }
}
