/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom.form;

import org.junit.Before;
import org.junit.Test;

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Harald Pehl
 */
public class SingletonStateMachineTest {

    private StateMachine stateMachine;

    @Before
    public void setUp() {
        stateMachine = new SingletonStateMachine();
    }

    @Test
    public void initialState() {
        assertEquals(EMPTY, stateMachine.current());
    }

    @Test
    public void supports() {
        assertTrue(stateMachine.supports(EMPTY));
        assertTrue(stateMachine.supports(READONLY));
        assertTrue(stateMachine.supports(EDITING));

        assertTrue(stateMachine.supports(VIEW));
        assertTrue(stateMachine.supports(CLEAR));
        assertTrue(stateMachine.supports(RESET));
        assertTrue(stateMachine.supports(EDIT));
        assertTrue(stateMachine.supports(SAVE));
        assertTrue(stateMachine.supports(CANCEL));
        assertTrue(stateMachine.supports(REMOVE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void viewNoState() {
        stateMachine.execute(VIEW);
    }

    @Test
    public void viewEmpty() {
        stateMachine.execute(VIEW, EMPTY);
        assertEquals(EMPTY, stateMachine.current());
    }

    @Test
    public void viewReadOnly() {
        stateMachine.execute(VIEW, READONLY);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test
    public void clear() {
        stateMachine.execute(CLEAR);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialReset() {
        stateMachine.execute(RESET);
    }

    @Test
    public void reset() {
        stateMachine.execute(VIEW, READONLY);
        stateMachine.execute(RESET);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialEdit() {
        stateMachine.execute(EDIT);
    }

    @Test
    public void edit() {
        stateMachine.execute(VIEW, READONLY);
        stateMachine.execute(EDIT);
        assertEquals(EDITING, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialSave() {
        stateMachine.execute(SAVE);
    }

    @Test
    public void save() {
        stateMachine.execute(VIEW, READONLY);
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
        stateMachine.execute(VIEW, READONLY);
        stateMachine.execute(EDIT);
        stateMachine.execute(CANCEL);
        assertEquals(READONLY, stateMachine.current());
    }

    @Test(expected = IllegalStateException.class)
    public void initialRemove() {
        stateMachine.execute(REMOVE);
    }

    @Test
    public void remove() {
        stateMachine.execute(VIEW, READONLY);
        stateMachine.execute(REMOVE);
        assertEquals(EMPTY, stateMachine.current());
    }
}