/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.logging;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static elemental2.dom.DomGlobal.window;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOG_FILE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.PROFILE_LOG_FILE_TEMPLATE;
import static org.jboss.hal.core.mvp.Places.EXTERNAL_PARAM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/** Common code used by the finder column and the presenter. */
public class LogFiles {

    /**
     * If log files are bigger than this threshold a warning is displayed.
     */
    static final int LOG_FILE_SIZE_THRESHOLD = 15000000; // bytes

    /**
     * The number of lines in the log file viewer
     */
    static final int LINES = 2000;

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final StatementContext statementContext;
    private final Places places;

    @Inject
    public LogFiles(final Dispatcher dispatcher,
            final Environment environment,
            final StatementContext statementContext,
            final Places places) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.statementContext = statementContext;
        this.places = places;
    }

    public void download(String logFile) {
        window.open(downloadUrl(logFile, null), "", "");
    }

    public void download(String logFile, String loggingProfile) {
        window.open(downloadUrl(logFile, loggingProfile), "", "");
    }

    String downloadUrl(String name, String loggingProfile) {
        ResourceAddress address;
        if (loggingProfile == null) {
            address = LOG_FILE_TEMPLATE.resolve(statementContext, name);
        } else {
            address = PROFILE_LOG_FILE_TEMPLATE.resolve(statementContext, loggingProfile, name);
        }
        Operation operation = new Operation.Builder(address, READ_ATTRIBUTE_OPERATION)
                .param(NAME, STREAM)
                .build();
        return dispatcher.downloadUrl(operation);
    }

    String externalUrl(String name, String loggingProfile) {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(NameTokens.LOG_FILE)
                .with(HOST, statementContext.selectedHost())
                .with(SERVER, statementContext.selectedServer())
                .with(NAME, name)
                .with(EXTERNAL_PARAM, String.valueOf(true));
        if (loggingProfile != null) {
            builder.with(LOGGING_PROFILE, loggingProfile);
        }
        PlaceRequest request = builder.build();
        return places.historyToken(request);
    }

    public String target(String name) {
        return environment.isStandalone() ? Ids.asId(name)
                : Ids
                        .build(statementContext.selectedHost(), statementContext.selectedServer(), Ids.asId(name));
    }

    public void tail(String name, String loggingProfile, int lines, AsyncCallback<String> callback) {
        ResourceAddress address;
        if (loggingProfile == null) {
            address = LOG_FILE_TEMPLATE.resolve(statementContext, name);
        } else {
            address = PROFILE_LOG_FILE_TEMPLATE.resolve(statementContext, loggingProfile, name);
        }
        Operation operation = new Operation.Builder(address, READ_LOG_FILE)
                .param(ModelDescriptionConstants.LINES, lines)
                .param(TAIL, true)
                .build();
        dispatcher.execute(operation, result -> callback.onSuccess(result.asList()
                .stream()
                .map(ModelNode::asString)
                .collect(joining("\n"))));
    }
}
