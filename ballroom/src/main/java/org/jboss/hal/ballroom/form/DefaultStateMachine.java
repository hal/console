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

import org.jboss.hal.ballroom.form.Form.Operation;

import java.util.EnumSet;

import static org.jboss.hal.ballroom.form.Form.State.EDITING;
import static org.jboss.hal.ballroom.form.Form.State.READONLY;

/**
 * A state machine for existing resources. Supports all {@linkplain Operation operations}.
 * <pre>
 *                             (O)
 *                              |
 *             +--------+       |
 *             |        |     view()
 *             |        |       |
 *          reset()     |       |
 *             |     +--v-------v--+
 *             |     |             |
 *             +-----+  READONLY   <-----+
 *                   |             |     |
 *                   +--+-------^--+     |
 *                      |       |        |
 *                   edit()  cancel()  save()
 *                      |       |        |
 *                   +--v-------+--+     |
 *                   |             |     |
 *  (O)----edit()---->   EDITING   +-----+
 *                   |             |
 *                   +-------------+
 * </pre>
 * (0) Initial states
 *
 * @author Harald Pehl
 */
public class DefaultStateMachine extends AbstractStateMachine implements StateMachine {

    public DefaultStateMachine() {
        super(EnumSet.allOf(Operation.class));
        this.current = null;
    }

    @Override
    public void execute(final Operation operation) {
        switch (operation) {

            case VIEW:
                if (current != null) {
                    assertState(READONLY);
                }
                transitionTo(READONLY);
                break;

            case EDIT:
                if (current != null) {
                    assertState(EDITING);
                }
                transitionTo(EDITING);
                break;

            case CANCEL:
                assertState(EDITING);
                transitionTo(READONLY);
                break;

            case SAVE:
                assertState(EDITING);
                transitionTo(READONLY);
                break;

            case RESET:
                assertState(READONLY);
                transitionTo(READONLY);
                break;

            default:
                unsupported(operation);
                break;
        }
    }
}
