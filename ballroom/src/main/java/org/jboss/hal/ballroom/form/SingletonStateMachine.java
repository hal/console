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

import java.util.EnumSet;

import org.jboss.hal.ballroom.form.Form.Operation;
import org.jboss.hal.ballroom.form.Form.State;

import static org.jboss.hal.ballroom.form.Form.Operation.VIEW;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.EMPTY;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;

/**
 * A state machine for singleton resources. Supports all {@link State states} and {@linkplain Operation
 * operations}.
 * <p>
 * Please note: The singleton resource must be created outside the scope of this state machine. Once created the {@link
 * Operation#VIEW} operation is used to view the resource.
 * <p>
 * <pre>
 *                   (0)----view()----+   +--------+
 *                                    |   |        |
 *     +---------remove()---------+   |   |      clear()
 *     |                          |   |   |      reset()
 *     |                       +--+---v---+--+     |
 *     |                       |             <-----+
 *     |       +-----view()---->  READONLY   |
 *     |       |               |             <-----+------+
 *  +--v-------+--+            +--+-------^--+     |      |
 *  |             |               |       |        |      |
 *  |    EMPTY    |            edit()  cancel()  save()   |
 *  |             |               |       |        |      |
 *  +-------------+            +--v-------+--+     |      |
 *                             |             |     |      |
 *                             |   EDITING   +-----+    clear()
 *                             |             |            |
 *                             +------+------+            |
 *                                    |                   |
 *                                    +-------------------+
 * </pre>
 * (0) Initial states
 *
 * @author Harald Pehl
 */
public class SingletonStateMachine extends AbstractStateMachine implements StateMachine {

    public SingletonStateMachine() {
        super(EnumSet.allOf(State.class), EnumSet.allOf(Operation.class));
        this.current = initial();
    }

    @Override
    protected State initial() {
        return EMPTY;
    }

    /**
     * The context is used for the {@link Operation#VIEW} operation. It's expected to be the next state.
     */
    @Override
    protected <C> void safeExecute(final Operation operation, final C context) {
        switch (operation) {

            case VIEW:
                assertState(EMPTY, READONLY);
                if (context instanceof State) {
                    transitionTo((State) context);
                } else {
                    throw new IllegalArgumentException(
                            "No context provided for " + name() + " and operation " + VIEW.name());
                }
                break;

            case CLEAR:
                transitionTo(READONLY);
                break;

            case RESET:
                assertState(READONLY);
                transitionTo(READONLY);
                break;

            case EDIT:
                assertState(READONLY);
                transitionTo(EDITING);
                break;

            case SAVE:
                assertState(EDITING);
                transitionTo(READONLY);
                break;

            case CANCEL:
                assertState(EDITING);
                transitionTo(READONLY);
                break;

            case REMOVE:
                assertState(READONLY);
                transitionTo(EMPTY);
                break;
        }
    }

    @Override
    protected String name() {
        return "SingletonStateMachine";
    }
}
