/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

// No @GenEvent here due to naming conflicts
public class ProcessStateEvent extends GwtEvent<ProcessStateEvent.ProcessStateHandler> {

    private static final Type<ProcessStateHandler> TYPE = new Type<>();

    public static Type<ProcessStateHandler> getType() {
        return TYPE;
    }

    private final ProcessState processState;

    ProcessStateEvent(final ProcessState processState) {
        this.processState = processState;
    }

    public ProcessState getProcessState() {
        return processState;
    }

    @Override
    protected void dispatch(ProcessStateHandler handler) {
        handler.onProcessState(this);
    }

    @Override
    public Type<ProcessStateHandler> getAssociatedType() {
        return TYPE;
    }

    public interface ProcessStateHandler extends EventHandler {

        void onProcessState(ProcessStateEvent event);
    }
}
