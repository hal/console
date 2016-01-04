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

            case CANCEL:
                assertState(EDITING);
                break;

            case SAVE:
                assertState(EDITING);
                break;

            default:
                unsupported(operation);
        }
    }
}
