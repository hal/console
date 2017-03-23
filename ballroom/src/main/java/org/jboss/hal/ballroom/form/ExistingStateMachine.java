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

import static org.jboss.hal.ballroom.form.Form.Operation.*;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;

/**
 * A state machine for existing models. Supports the {@link Form.State#READONLY} and {@link Form.State#EDITING} state
 * and all {@linkplain Operation operations} except {@link Operation#REMOVE}.
 * <p>
 * <pre>
 *           +--------+      (0)
 *           |        |       |
 *        clear()     |     view()
 *        reset()     |       |
 *           |     +--v-------v--+
 *           |     |             |
 *           +-----+  READONLY   <-----+------+
 *                 |             |     |      |
 *                 +--+-------^--+     |      |
 *                    |       |        |      |
 *                 edit()  cancel()  save()   |
 *                    |       |        |      |
 *                 +--v-------+--+     |      |
 *                 |             |     |      |
 *  (0)---edit()--->   EDITING   +-----+    clear()
 *                 |             |            |
 *                 +------+------+            |
 *                        |                   |
 *                        +-------------------+
 * </pre>
 * (0) Initial states
 *
 * @author Harald Pehl
 */
public class ExistingStateMachine extends AbstractStateMachine implements StateMachine {

    public ExistingStateMachine() {
        super(EnumSet.of(READONLY, EDITING), EnumSet.of(VIEW, CLEAR, RESET, EDIT, SAVE, CANCEL));
        this.current = initial();
    }

    @Override
    protected Form.State initial() {
        return READONLY;
    }

    @Override
    protected <C> void safeExecute(final Operation operation, final C context) {
        switch (operation) {

            case VIEW:
                assertState(READONLY);
                transitionTo(READONLY);
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
        }
    }

    @Override
    protected String name() {
        return "ExistingStateMachine";
    }
}
