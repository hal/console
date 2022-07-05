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
package org.jboss.hal.client.runtime.server;

import java.util.function.Consumer;

import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;

/**
 * Function used to evaluate the server(-config) status. Implemented in a central place to ensure the right order of the
 * {@code if-then-else} statements.
 * <p>
 * The order when comparing the server(-config) status should be:
 * <ol>
 * <li>pending</li>
 * <li>boot errors</li>
 * <li>failed</li>
 * <li>admin mode</li>
 * <li>starting</li>
 * <li>suspended</li>
 * <li>needs reload</li>
 * <li>needs restart</li>
 * <li>running</li>
 * <li>stopped</li>
 * <li>unknown</li>
 * </ol>
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
public abstract class ServerStatusSwitch implements Consumer<Server> {

    private final ServerActions serverActions;

    public ServerStatusSwitch(final ServerActions serverActions) {
        this.serverActions = serverActions;
    }

    @Override
    public final void accept(final Server server) {
        if (serverActions.isPending(server)) {
            onPending(server);
        } else if (server.hasOperationFailure()) {
            onFailed(server);
        } else if (server.hasBootErrors()) {
            onBootErrors(server);
        } else if (server.isFailed()) {
            onFailed(server);
        } else if (server.isAdminMode()) {
            onAdminMode(server);
        } else if (server.isStarting()) {
            onStarting(server);
        } else if (server.isSuspended()) {
            onSuspended(server);
        } else if (server.needsReload()) {
            onNeedsReload(server);
        } else if (server.needsRestart()) {
            onNeedsRestart(server);
        } else if (server.isRunning()) {
            onRunning(server);
        } else if (server.isStopped()) {
            onStopped(server);
        } else {
            onUnknown(server);
        }
    }

    protected abstract void onPending(Server server);

    protected abstract void onBootErrors(Server server);

    protected abstract void onFailed(Server server);

    protected abstract void onAdminMode(Server server);

    protected abstract void onStarting(Server server);

    protected abstract void onSuspended(Server server);

    protected abstract void onNeedsReload(Server server);

    protected abstract void onNeedsRestart(Server server);

    protected abstract void onRunning(Server server);

    protected abstract void onStopped(Server server);

    protected abstract void onUnknown(Server server);
}
