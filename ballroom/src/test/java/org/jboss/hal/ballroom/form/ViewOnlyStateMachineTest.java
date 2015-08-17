package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.*;
import static org.junit.Assert.*;

/**
 * @author Harald Pehl
 */
public class ViewOnlyStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new ViewOnlyStateMachine();
    }

    @Test
    public void initialState() {
        assertNull(stateMachine.current());
    }

    @Test
    public void supports() {
        assertTrue(stateMachine.supports(VIEW));
        assertFalse(stateMachine.supports(EDIT));
        assertFalse(stateMachine.supports(CANCEL));
        assertFalse(stateMachine.supports(SAVE));
        assertFalse(stateMachine.supports(RESET));
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

    @Test(expected = UnsupportedOperationException.class)
    public void edit() {
        stateMachine.execute(EDIT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void cancel() {
        stateMachine.execute(CANCEL);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void save() {
        stateMachine.execute(SAVE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void reset() {
        stateMachine.execute(RESET);
    }
}
