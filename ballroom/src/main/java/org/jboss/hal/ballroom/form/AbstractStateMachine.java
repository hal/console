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

/**
 * @author Harald Pehl
 */
public abstract class AbstractStateMachine implements StateMachine {

    private final EnumSet<Operation> supportedOperations;
    State current;

    AbstractStateMachine(EnumSet<Operation> supportedOperations) {
        this.supportedOperations = supportedOperations;
    }

    @Override
    public void reset() {
        current = null;
    }

    public State current() {
        return current;
    }

    public boolean supports(final Operation operation) {
        return supportedOperations.contains(operation);
    }

    public boolean supportsAny(final Operation first, final Operation... rest) {
        boolean support = supports(first);
        if (!support && rest != null) {
            for (Operation op : rest) {
                support = supports(op);
                if (support) {
                    break;
                }
            }
        }
        return support;
    }

    void transitionTo(State next) {
        current = next;
    }

    protected void assertNoState() {
        if (current != null) {
            throw new IllegalStateException("Illegal state: Expected no state, but got " + current);
        }
    }

    void assertState(State... state) {
        for (State st : state) {
            if (current == st) {
                return;
            }
        }
        if (state.length == 1) {
            throw new IllegalStateException("Illegal state: Expected " + state[0] + ", but got " + current);
        } else {
            throw new IllegalStateException(
                    "Illegal state: Expected one of [" + Joiner.on(", ").join(state) + "], but got " + current);
        }
    }

    void unsupported(Operation operation) {
        throw new UnsupportedOperationException(getClass().getName() + ": Unknown operation " + operation);
    }
}
