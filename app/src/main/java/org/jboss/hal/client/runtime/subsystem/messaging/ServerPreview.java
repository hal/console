/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.messaging;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ACTIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STARTED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VERSION;

class ServerPreview extends PreviewContent<NamedNode> {

    private final Alert started;
    private final Alert stopped;

    ServerPreview(NamedNode server, StatementContext statementContext, Resources resources) {
        super(server.getName());

        started = new Alert(Icons.OK, resources.messages().messageServerStarted(server.getName()));
        stopped = new Alert(Icons.INFO,
                resources.messages().messageServerStopped(server.getName(), statementContext.selectedServer()));
        previewBuilder().addAll(started.element(), stopped.element());
        previewBuilder().addAll(new PreviewAttributes<>(server, asList(ACTIVE, STARTED, VERSION)));
        update(server);
    }

    @Override
    public void update(NamedNode server) {
        boolean started = server.get(STARTED).asBoolean(false);
        Elements.setVisible(this.started.element(), started);
        Elements.setVisible(this.stopped.element(), !started);
    }
}
