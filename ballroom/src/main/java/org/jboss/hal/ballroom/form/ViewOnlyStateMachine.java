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

import static org.jboss.hal.ballroom.form.Form.Operation.VIEW;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;

/**
 * A read-only state machine. Supports only the {@link Form.Operation#VIEW} operation.
 *
 * @author Harald Pehl
 */
public class ViewOnlyStateMachine extends AbstractStateMachine implements StateMachine {

    public ViewOnlyStateMachine() {
        super(EnumSet.of(VIEW));
        this.current = null;
    }

    @Override
    public void execute(final Form.Operation operation) {
        switch (operation) {

            case CLEAR:
                transitionTo(READONLY);
                break;

            case VIEW:
                if (current != null) {
                    assertState(READONLY);
                }
                transitionTo(READONLY);
                break;

            default:
                unsupported(operation);
        }
    }
}
