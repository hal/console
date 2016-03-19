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

import static org.jboss.hal.ballroom.form.Form.Operation.ADD;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;

/**
 * A state machine for transient models. Supports only the {@link Form.Operation#ADD}, {@link Form.Operation#CANCEL}
 * and {@link Form.Operation#SAVE} operations.
 *
 * @author Harald Pehl
 */
public class AddOnlyStateMachine extends AbstractStateMachine implements StateMachine {

    public AddOnlyStateMachine() {
        super(EnumSet.of(ADD));
        this.current = null;
    }

    @Override
    public void execute(final Form.Operation operation) {
        switch (operation) {

            case ADD:
                if (current != null) {
                    assertState(EDITING);
                }
                transitionTo(EDITING);
                break;

            case SAVE:
                assertState(EDITING);
                break;

            case CANCEL:
                assertState(EDITING);
                break;

            default:
                unsupported(operation);
        }
    }
}
