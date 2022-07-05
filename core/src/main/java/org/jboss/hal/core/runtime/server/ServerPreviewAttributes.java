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
package org.jboss.hal.core.runtime.server;

import org.jboss.hal.core.finder.PreviewAttributes;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;

public final class ServerPreviewAttributes {

    public static void refresh(Server server, PreviewAttributes<Server> serverAttributes) {
        serverAttributes.refresh(server);
        serverAttributes.setVisible(PROFILE, !server.hasOperationFailure() && server.isStarted());
        serverAttributes.setVisible(URL, !server.hasOperationFailure() && server.isStarted());
        serverAttributes.setVisible(RUNNING_MODE, !server.hasOperationFailure() && server.isStarted());
        serverAttributes.setVisible(SERVER_STATE, !server.hasOperationFailure() && server.isStarted());
        serverAttributes.setVisible(SUSPEND_STATE, !server.hasOperationFailure() && server.isStarted());
    }

    private ServerPreviewAttributes() {
    }
}
