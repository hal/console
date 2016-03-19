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
package org.jboss.hal.dmr.macro;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.jboss.hal.dmr.model.Operation;

/**
 * Event to signal that an operation was recorded to the active macro.
 *
 * @author Harald Pehl
 */
public class MacroOperationEvent extends GwtEvent<MacroOperationEvent.MacroOperationHandler> {

    public interface MacroOperationHandler extends EventHandler {

        void onMacroStep(MacroOperationEvent event);
    }


    private static final Type<MacroOperationHandler> TYPE = new Type<>();

    public static Type<MacroOperationHandler> getType() {
        return TYPE;
    }

    private final Macro macro;
    private final Operation operation;

    public MacroOperationEvent(final Macro macro, final Operation operation) {
        this.macro = macro;
        this.operation = operation;
    }

    public Macro getMacro() {
        return macro;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    protected void dispatch(MacroOperationHandler handler) {
        handler.onMacroStep(this);
    }

    @Override
    public Type<MacroOperationHandler> getAssociatedType() {
        return TYPE;
    }
}
