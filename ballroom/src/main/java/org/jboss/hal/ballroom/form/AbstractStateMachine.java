/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.form;

import com.google.common.base.Joiner;
import org.jboss.hal.ballroom.form.Form.Operation;
import org.jboss.hal.ballroom.form.Form.State;

import java.util.EnumSet;

/**
 * @author Harald Pehl
 */
public abstract class AbstractStateMachine implements StateMachine {

    private final EnumSet<Operation> supportedOperations;
    protected State current;

    public AbstractStateMachine(EnumSet<Operation> supportedOperations) {
        this.supportedOperations = supportedOperations;
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

    protected void transitionTo(State next) {
        current = next;
    }

    protected void assertNoState() {
        if (current != null) {
            throw new IllegalStateException("Illegal state: Expected no state, but got " + current);
        }
    }

    protected void assertState(State... state) {
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

    protected void unsupported(Operation operation) {
        throw new UnsupportedOperationException(getClass().getName() + ": Unknown operation " + operation);
    }
}
