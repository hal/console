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

public class ExistingStateMachineTest {

    private StateMachine stateMachineWithReset;
    private StateMachine stateMachineWithoutReset;

    @Before
    public void setUp() {
        stateMachineWithReset = new ExistingStateMachine(true);
        stateMachineWithoutReset = new ExistingStateMachine(false);
    }

    @Test
    public void initialState() {
        assertEquals(READONLY, stateMachineWithReset.current());
    }

    @Test
    public void supports() {
        assertFalse(stateMachineWithReset.supports(EMPTY));
        assertTrue(stateMachineWithReset.supports(READONLY));
        assertTrue(stateMachineWithReset.supports(EDITING));

        assertTrue(stateMachineWithReset.supports(VIEW));
        assertTrue(stateMachineWithReset.supports(CLEAR));
        assertTrue(stateMachineWithReset.supports(RESET));
        assertTrue(stateMachineWithReset.supports(EDIT));
        assertTrue(stateMachineWithReset.supports(SAVE));
        assertTrue(stateMachineWithReset.supports(CANCEL));
        assertFalse(stateMachineWithReset.supports(REMOVE));

        assertFalse(stateMachineWithoutReset.supports(EMPTY));
        assertTrue(stateMachineWithoutReset.supports(READONLY));
        assertTrue(stateMachineWithoutReset.supports(EDITING));

        assertTrue(stateMachineWithoutReset.supports(VIEW));
        assertTrue(stateMachineWithoutReset.supports(CLEAR));
        assertFalse(stateMachineWithoutReset.supports(RESET));
        assertTrue(stateMachineWithoutReset.supports(EDIT));
        assertTrue(stateMachineWithoutReset.supports(SAVE));
        assertTrue(stateMachineWithoutReset.supports(CANCEL));
        assertFalse(stateMachineWithoutReset.supports(REMOVE));
    }

    @Test
    public void view() {
        stateMachineWithReset.execute(VIEW);
        assertEquals(READONLY, stateMachineWithReset.current());
    }

    @Test
    public void clear() {
        stateMachineWithReset.execute(CLEAR);
        assertEquals(READONLY, stateMachineWithReset.current());
    }

    @Test
    public void reset() {
        stateMachineWithReset.execute(RESET);
        assertEquals(READONLY, stateMachineWithReset.current());
    }

    @Test
    public void edit() {
        stateMachineWithReset.execute(EDIT);
        assertEquals(EDITING, stateMachineWithReset.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialSave() {
        stateMachineWithReset.execute(SAVE);
    }

    @Test
    public void save() {
        stateMachineWithReset.execute(EDIT);
        stateMachineWithReset.execute(SAVE);
        assertEquals(READONLY, stateMachineWithReset.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialCancel() {
        stateMachineWithReset.execute(CANCEL);
    }

    @Test
    public void cancel() {
        stateMachineWithReset.execute(EDIT);
        stateMachineWithReset.execute(CANCEL);
        assertEquals(READONLY, stateMachineWithReset.current());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        stateMachineWithReset.execute(REMOVE);
    }
}