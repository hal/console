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
package org.jboss.hal.client.runtime.logging;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.client.Browser;
import org.jboss.hal.client.runtime.server.ServerColumn;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.client.runtime.logging.AddressTemplates.LOG_FILE_ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.meta.token.NameTokens.LOG_FILE;

/**
 * @author Harald Pehl
 */
public class LogFilePresenter extends ApplicationPresenter<LogFilePresenter.MyView, LogFilePresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(LOG_FILE)
    @Requires(LOG_FILE_ADDRESS)
    public interface MyProxy extends ProxyPlace<LogFilePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<LogFilePresenter> {
        void loading();
        void show(LogFile logFile, String content);
    }
    // @formatter:on


    static final String LOG_FILE_PARAM = "log-file";
    private static final int LINES = 2000;

    private final Dispatcher dispatcher;
    private final LogFiles logFiles;
    private final StatementContext statementContext;
    private final Resources resources;
    private String logFileName;

    @Inject
    public LogFilePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final LogFiles logFiles,
            final StatementContext statementContext,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.logFiles = logFiles;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        logFileName = request.getParameter(LOG_FILE_PARAM, null);
    }

    @Override
    protected FinderPath finderPath() {
        FinderPath path;
        if (ServerColumn.browseByServerGroups(finder.getContext())) {
            path = FinderPath
                    .runtimeServerGroupPath(statementContext.selectedServerGroup(), statementContext.selectedServer());
        } else {
            path = FinderPath.runtimeHostPath(statementContext.selectedHost(), statementContext.selectedServer());
        }
        path.append(Ids.SERVER_MONITOR_COLUMN, IdBuilder.asId(resources.constants().logFiles()),
                resources.constants().monitor(), resources.constants().logFiles())
                .append(Ids.LOG_FILE_COLUMN, IdBuilder.asId(logFileName), resources.constants().logFile(), logFileName);
        return path;
    }

    @Override
    protected void onReset() {
        super.onReset();
        load();
    }

    void load() {
        if (logFileName != null) {
            int handle = Browser.getWindow().setTimeout(() -> getView().loading(), UIConstants.PROGRESS_TIMEOUT);
            ResourceAddress address = AddressTemplates.LOG_FILE_TEMPLATE.resolve(statementContext, logFileName);
            Operation logFileOp = new Operation.Builder(READ_RESOURCE_OPERATION, address)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            //noinspection HardCodedStringLiteral
            Operation contentOp = new Operation.Builder("read-log-file", address)
                    .param("lines", LINES)
                    .param("tail", true)
                    .build();
            dispatcher.execute(new Composite(logFileOp, contentOp),
                    (CompositeResult result) -> {
                        Browser.getWindow().clearTimeout(handle);
                        LogFile logFile = new LogFile(logFileName, result.step(0).get(RESULT));
                        String content = result.step(1).get(RESULT).asList().stream().map(ModelNode::asString)
                                .collect(joining("\n"));
                        getView().show(logFile, content);
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

    void download() {
        if (logFileName != null) {
            logFiles.download(logFileName);
        } else {
            MessageEvent.fire(getEventBus(), Message.error(resources.messages().noLogFile()));
        }
    }
}
