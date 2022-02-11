/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
