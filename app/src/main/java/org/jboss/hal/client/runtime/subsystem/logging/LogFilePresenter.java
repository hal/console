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
package org.jboss.hal.client.runtime.subsystem.logging;

import java.util.List;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.client.runtime.subsystem.logging.AddressTemplates.LOG_FILE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.meta.token.NameTokens.LOG_FILE;

/**
 * @author Harald Pehl
 */
public class LogFilePresenter extends ApplicationFinderPresenter<LogFilePresenter.MyView, LogFilePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(LOG_FILE)
    @Requires(LOG_FILE_ADDRESS)
    public interface MyProxy extends ProxyPlace<LogFilePresenter> {}

    public interface MyView extends HalView, HasPresenter<LogFilePresenter> {
        void loading();
        void show(LogFile logFile, int lines, String content);
        void refresh(int lines, String content);
        int visibleLines();
    }
    // @formatter:on


    private static final int REFRESH_INTERVAL = 1000;

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Resources resources;
    private String logFileName;
    private LogFile logFile;
    private int intervalHandle;

    @Inject
    public LogFilePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.resources = resources;

        this.logFileName = null;
        this.logFile = null;
        this.intervalHandle = -1;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        logFileName = request.getParameter(NAME, null);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeServerPath()
                .append(Ids.SERVER_MONITOR, Ids.asId(resources.constants().logFiles()),
                        resources.constants().monitor(), resources.constants().logFiles())
                .append(Ids.LOG_FILE, Ids.asId(logFileName), resources.constants().logFile(), logFileName);
    }

    @Override
    protected void reload() {
        if (logFileName != null) {
            int handle = Browser.getWindow().setTimeout(() -> getView().loading(), UIConstants.MEDIUM_TIMEOUT);
            ResourceAddress address = AddressTemplates.LOG_FILE_TEMPLATE.resolve(statementContext, logFileName);
            Operation logFileOp = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            //noinspection HardCodedStringLiteral
            Operation contentOp = new Operation.Builder("read-log-file", address)
                    .param("lines", LogFiles.LINES)
                    .param("tail", true)
                    .build();
            dispatcher.execute(new Composite(logFileOp, contentOp),
                    (CompositeResult result) -> {
                        Browser.getWindow().clearTimeout(handle);
                        logFile = new LogFile(logFileName, result.step(0).get(RESULT));
                        List<ModelNode> linesRead = result.step(1).get(RESULT).asList();
                        String content = linesRead.stream().map(ModelNode::asString)
                                .collect(joining("\n"));
                        getView().show(logFile, linesRead.size(), content);
                    },
                    (operation, failure) -> {
                        Browser.getWindow().clearTimeout(handle);
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().logFileError(logFileName), failure));
                    },
                    (operation, exception) -> {
                        Browser.getWindow().clearTimeout(handle);
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().logFileError(logFileName), exception.getMessage()));
                    });
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noLogFile()));
        }
    }

    void reloadFile() {
        if (logFile != null) {
            int linesToRead = inTailMode() ? getView().visibleLines() : LogFiles.LINES;
            int handle = Browser.getWindow().setTimeout(() -> getView().loading(), UIConstants.MEDIUM_TIMEOUT);
            ResourceAddress address = AddressTemplates.LOG_FILE_TEMPLATE.resolve(statementContext, logFileName);
            //noinspection HardCodedStringLiteral
            Operation operation = new Operation.Builder("read-log-file", address)
                    .param("lines", linesToRead)
                    .param("tail", true)
                    .build();
            dispatcher.execute(operation, result -> {
                        Browser.getWindow().clearTimeout(handle);
                        List<ModelNode> linesRead = result.asList();
                        String content = linesRead.stream().map(ModelNode::asString).collect(joining("\n"));
                        getView().refresh(linesRead.size(), content);
                    },
                    (op, failure) -> {
                        Browser.getWindow().clearTimeout(handle);
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().logFileError(logFileName), failure));
                    },
                    (op, exception) -> {
                        Browser.getWindow().clearTimeout(handle);
                        MessageEvent.fire(getEventBus(),
                                Message.error(resources.messages().logFileError(logFileName), exception.getMessage()));
                    });
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noLogFile()));
        }
    }

    void toggleTailMode(final boolean on) {
        if (logFile != null) {
            if (on) {
                if (!inTailMode()) {
                    intervalHandle = Browser.getWindow().setInterval(this::reloadFile, REFRESH_INTERVAL);
                }
            } else {
                Browser.getWindow().clearInterval(intervalHandle);
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
}
