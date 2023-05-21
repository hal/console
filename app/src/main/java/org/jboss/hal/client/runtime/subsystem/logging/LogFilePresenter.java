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
package org.jboss.hal.client.runtime.subsystem.logging;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static java.util.stream.Collectors.joining;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.setInterval;
import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOG_FILE_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOG_FILE_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.PROFILE_LOG_FILE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LINES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING_PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_LOG_FILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TAIL;
import static org.jboss.hal.meta.token.NameTokens.LOG_FILE;

public class LogFilePresenter extends ApplicationFinderPresenter<LogFilePresenter.MyView, LogFilePresenter.MyProxy> {

    private static final int REFRESH_INTERVAL = 1000;

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String logFileName;
    private String loggingProfile;
    private LogFile logFile;
    private double intervalHandle;

    @Inject
    public LogFilePresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        this.logFileName = null;
        this.loggingProfile = null;
        this.logFile = null;
        this.intervalHandle = -1;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        logFileName = request.getParameter(NAME, null);
        loggingProfile = request.getParameter(LOGGING_PROFILE, null);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.RUNTIME_SUBSYSTEM, LOGGING,
                        resources.constants().monitor(), resources.constants().logFiles())
                .append(Ids.LOG_FILE, Ids.asId(logFileName), resources.constants().logFile(), logFileName);
    }

    @Override
    protected void reload() {
        if (logFileName != null) {
            double handle = setTimeout((o) -> getView().loading(), UIConstants.MEDIUM_TIMEOUT);
            ResourceAddress address;
            if (loggingProfile == null) {
                address = LOG_FILE_TEMPLATE.resolve(statementContext, logFileName);
            } else {
                address = PROFILE_LOG_FILE_TEMPLATE.resolve(statementContext, loggingProfile, logFileName);
            }
            Operation logFileOp = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            // noinspection HardCodedStringLiteral
            Operation contentOp = new Operation.Builder(address, "read-log-file")
                    .param("lines", LogFiles.LINES)
                    .param("tail", true)
                    .build();
            dispatcher.execute(new Composite(logFileOp, contentOp),
                    (CompositeResult result) -> {
                        clearTimeout(handle);
                        if (loggingProfile == null) {
                            logFile = new LogFile(logFileName, result.step(0).get(RESULT));
                        } else {
                            logFile = new LogFile(logFileName, loggingProfile, result.step(0).get(RESULT));
                        }
                        List<ModelNode> linesRead = result.step(1).get(RESULT).asList();
                        String content = linesRead.stream().map(ModelNode::asString)
                                .collect(joining("\n"));
                        getView().show(logFile, linesRead.size(), content);
                    },
                    (operation, failure) -> {
                        clearTimeout(handle);
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().logFileError(logFileName), failure));
                    });
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noLogFile()));
        }
    }

    void reloadFile() {
        if (logFile != null) {
            int linesToRead = inTailMode() ? getView().visibleLines() : LogFiles.LINES;
            double handle = setTimeout((o) -> getView().loading(), UIConstants.MEDIUM_TIMEOUT);
            ResourceAddress address;
            if (loggingProfile == null) {
                address = LOG_FILE_TEMPLATE.resolve(statementContext, logFileName);
            } else {
                address = PROFILE_LOG_FILE_TEMPLATE.resolve(statementContext, loggingProfile, logFileName);
            }
            // noinspection HardCodedStringLiteral
            Operation operation = new Operation.Builder(address, READ_LOG_FILE)
                    .param(LINES, linesToRead)
                    .param(TAIL, true)
                    .build();
            dispatcher.execute(operation, result -> {
                clearTimeout(handle);
                List<ModelNode> linesRead = result.asList();
                String content = linesRead.stream().map(ModelNode::asString).collect(joining("\n"));
                getView().refresh(linesRead.size(), content);
            }, (op, failure) -> {
                clearTimeout(handle);
                MessageEvent.fire(getEventBus(),
                        Message.error(resources.messages().logFileError(logFileName), failure));
            });
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noLogFile()));
        }
    }

    void toggleTailMode(boolean on) {
        if (logFile != null) {
            if (on) {
                if (!inTailMode()) {
                    intervalHandle = setInterval((o) -> reloadFile(), REFRESH_INTERVAL);
                }
            } else {
                clearInterval(intervalHandle);
                intervalHandle = -1;
                reloadFile();
            }
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noLogFile()));
        }
    }

    private boolean inTailMode() {
        return intervalHandle != -1;
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(LOG_FILE)
    @Requires(LOG_FILE_ADDRESS)
    public interface MyProxy extends ProxyPlace<LogFilePresenter> {
    }

    public interface MyView extends HalView, HasPresenter<LogFilePresenter> {
        void loading();

        void show(LogFile logFile, int lines, String content);

        void refresh(int lines, String content);

        int visibleLines();
    }
    // @formatter:on
}
