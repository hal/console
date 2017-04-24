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
package org.jboss.hal.client.runtime.server;

import java.util.function.Function;

import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

/**
 * @author Harald Pehl
 */
class ServerTooltip implements Function<Server, String> {

    private final ServerActions serverActions;
    private final Resources resources;

    ServerTooltip(final ServerActions serverActions, final Resources resources) {
        this.serverActions = serverActions;
        this.resources = resources;
    }

    @Override
    public String apply(final Server server) {
        final String[] tooltip = new String[1];
        ServerStatusSwitch sss = new ServerStatusSwitch(serverActions) {
            @Override
            protected void onPending(final Server server) {
                tooltip[0] = resources.constants().pending();
            }

            @Override
            protected void onBootErrors(final Server server) {
                tooltip[0] = Names.BOOT_ERRORS;
            }

            @Override
            protected void onFailed(final Server server) {
                tooltip[0] = resources.constants().failed();
            }

            @Override
            protected void onAdminMode(final Server server) {
                tooltip[0] = resources.constants().adminOnly();
            }

            @Override
            protected void onStarting(final Server server) {
                tooltip[0] = resources.constants().starting();
            }

            @Override
            protected void onSuspended(final Server server) {
                tooltip[0] = resources.constants().suspended();
            }

            @Override
            protected void onNeedsReload(final Server server) {
                tooltip[0] = resources.constants().needsReload();
            }

            @Override
            protected void onNeedsRestart(final Server server) {
                tooltip[0] = resources.constants().needsRestart();
            }

            @Override
            protected void onRunning(final Server server) {
                tooltip[0] = resources.constants().running();
            }

            @Override
            protected void onStopped(final Server server) {
                tooltip[0] = resources.constants().stopped();
            }

            @Override
            protected void onUnknown(final Server server) {
                tooltip[0] = resources.constants().unknownState();
            }
        };
        sss.accept(server);
        return tooltip[0];
    }
}
