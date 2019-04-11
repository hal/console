/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import static org.jboss.hal.ballroom.form.Form.Operation.CANCEL;
import static org.jboss.hal.ballroom.form.Form.Operation.EDIT;
import static org.jboss.hal.ballroom.form.Form.Operation.SAVE;
import static org.jboss.hal.ballroom.form.Form.State.EDITING;

/**
 * A state machine for transient models. Supports only the {@link Form.State#EDITING} state and the {@link
 * Operation#CANCEL} and {@link Operation#SAVE} operations.
 * <p>
 * Most often used in add dialogs.
 */
public class AddOnlyStateMachine extends AbstractStateMachine implements StateMachine {

    public AddOnlyStateMachine() {
        super(EnumSet.of(EDITING), EnumSet.of(EDIT, SAVE, CANCEL));
        this.current = initial();
    }

    @Override
    protected Form.State initial() {
        return EDITING;
    }

    @Override
    protected <C> void safeExecute(final Operation operation, final C context) {
        switch (operation) {

            case EDIT:
                assertState(EDITING);
                break;

            case SAVE:
                assertState(EDITING);
                break;

            case CANCEL:
                assertState(EDITING);
                break;

            default:
                break;
        }
    }

    @Override
    protected String name() {
        return "AddOnlyStateMachine";
    }
}
