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

public class AddOnlyStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new AddOnlyStateMachine();
    }

    @Test
    public void initialState() {
        assertEquals(EDITING, stateMachine.current());
    }

    @Test
    public void supports() {
        assertFalse(stateMachine.supports(EMPTY));
        assertFalse(stateMachine.supports(READONLY));
        assertTrue(stateMachine.supports(EDITING));

        assertFalse(stateMachine.supports(VIEW));
        assertFalse(stateMachine.supports(CLEAR));
        assertFalse(stateMachine.supports(RESET));
        assertTrue(stateMachine.supports(EDIT));
        assertTrue(stateMachine.supports(SAVE));
        assertTrue(stateMachine.supports(CANCEL));
        assertFalse(stateMachine.supports(REMOVE));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void view() {
        stateMachine.execute(VIEW);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void clear() {
        stateMachine.execute(CLEAR);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void reset() {
        stateMachine.execute(RESET);
    }

    @Test
    public void edit() {
        stateMachine.execute(EDIT);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test
    public void save() {
        stateMachine.execute(SAVE);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test
    public void cancel() {
        stateMachine.execute(CANCEL);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        stateMachine.execute(REMOVE);
    }
}