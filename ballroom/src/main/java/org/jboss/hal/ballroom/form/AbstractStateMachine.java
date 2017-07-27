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

import com.google.common.base.Joiner;
import org.jboss.hal.ballroom.form.Form.Operation;
import org.jboss.hal.ballroom.form.Form.State;
import org.jetbrains.annotations.NonNls;

public abstract class AbstractStateMachine implements StateMachine {

    private final EnumSet<State> supportedStates;
    private final EnumSet<Operation> supportedOperations;
    State current;

    AbstractStateMachine(EnumSet<State> supportedStates, EnumSet<Operation> supportedOperations) {
        this.supportedStates = supportedStates;
        this.supportedOperations = supportedOperations;
    }

    @Override
    public void reset() {
        current = initial();
    }

    protected abstract State initial();

    public State current() {
        return current;
    }

    @Override
    public boolean supports(final State state) {
        return supportedStates.contains(state);
    }

    public boolean supports(final Operation operation) {
        return supportedOperations.contains(operation);
    }

    void transitionTo(State next) {
        if (!supportedStates.contains(next)) {
            throw new IllegalStateException(name() + ": Unsupported state " + next);
        }
        current = next;
    }

    void assertState(State... state) {
        for (State st : state) {
            if (current == st) {
                return;
            }
        }
        if (state.length == 1) {
            throw new IllegalStateException(name() + ": Illegal state: Expected " + state[0] + ", but got " + current);
        } else {
            throw new IllegalStateException(name() + ": Illegal state: Expected one of [" + Joiner.on(", ")
                    .join(state) + "], but got " + current);
        }
    }

    @Override
    public <C> void execute(final Operation operation, C context) {
        if (!supportedOperations.contains(operation)) {
            throw new UnsupportedOperationException(name() + ": Unsupported operation " + operation);
        } else {
            safeExecute(operation, context);
        }
    }

    protected abstract <C> void safeExecute(final Operation operation, C context);

    @NonNls protected abstract String name();
}
