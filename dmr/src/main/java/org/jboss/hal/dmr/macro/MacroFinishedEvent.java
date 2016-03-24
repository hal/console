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

/**
 * Event to signal that the recording for the active macro has been finished and the macro is now sealed.
 *
 * @author Harald Pehl
 */
public class MacroFinishedEvent extends GwtEvent<MacroFinishedEvent.MacroFinishedHandler> {

    public interface MacroFinishedHandler extends EventHandler {

        void onMacroFinished(MacroFinishedEvent event);
    }


    private static final Type<MacroFinishedHandler> TYPE = new Type<>();

    public static Type<MacroFinishedHandler> getType() {
        return TYPE;
    }

    private final Macro macro;
    private final MacroOptions options;

    public MacroFinishedEvent(final Macro macro, MacroOptions options) {
        this.macro = macro;
        this.options = options;
    }

    public Macro getMacro() {
        return macro;
    }

    public MacroOptions getOptions() {
        return options;
    }

    @Override
    protected void dispatch(MacroFinishedHandler handler) {
        handler.onMacroFinished(this);
    }

    @Override
    public Type<MacroFinishedHandler> getAssociatedType() {
        return TYPE;
    }
}
