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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.runtime.Server;
import org.jboss.hal.client.runtime.domain.TopologyFunctions;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.HasVerticalNavigation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE_DEPTH;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
public class LoggingPresenter extends MbuiPresenter<LoggingPresenter.MyView, LoggingPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.LOGGING)
    @Requires({ROOT_LOGGER_ADDRESS, LOGGER_ADDRESS,
            ASYNC_HANDLER_ADDRESS, CONSOLE_HANDLER_ADDRESS, CUSTOM_HANDLER_ADDRESS, FILE_HANDLER_ADDRESS,
            PERIODIC_ROTATING_FILE_HANDLER_ADDRESS, PERIODIC_SIZE_ROTATING_FILE_HANDLER_ADDRESS,
            SIZE_ROTATING_FILE_HANDLER_ADDRESS, SYSLOG_HANDLER_ADDRESS,
            CUSTOM_FORMATTER_ADDRESS, PATTERN_FORMATTER_ADDRESS})
    public interface MyProxy extends ProxyPlace<LoggingPresenter> {}

    public interface MyView extends MbuiView<LoggingPresenter>, HasVerticalNavigation {
        void updateRootLogger(ModelNode modelNode);
        void updateLogger(List<NamedNode> items);

        void updateAsyncHandler(List<NamedNode> items);
        void updateConsoleHandler(List<NamedNode> items);
        void updateCustomHandler(List<NamedNode> items);
        void updateFileHandler(List<NamedNode> items);
        void updatePeriodicHandler(List<NamedNode> items);
        void updatePeriodicSizeHandler(List<NamedNode> items);
        void updateSizeHandlerHandler(List<NamedNode> items);
        void updateSyslogHandler(List<NamedNode> items);

        void updateCustomFormatter(List<NamedNode> items);
        void updatePatternFormatter(List<NamedNode> items);
    }
    // @formatter:on


    private static final Logger logger = LoggerFactory.getLogger(LoggingPresenter.class);

    private final Environment environment;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final Provider<Progress> progress;
    private Operation pathOperation;

    @Inject
    public LoggingPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final Environment environment,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            @Footer final Provider<Progress> progress) {
        super(eventBus, view, proxy, finder);
        this.environment = environment;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.progress = progress;
        this.pathOperation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, "path").build();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected FinderPath finderPath() {
        return FinderPath
                .subsystemPath(statementContext.selectedProfile(), LOGGING_SUBSYSTEM_TEMPLATE.lastValue());
    }

    @Override
    protected void reload() {
        Operation operation = new Operation.Builder(READ_RESOURCE_OPERATION,
                LOGGING_SUBSYSTEM_TEMPLATE.resolve(statementContext))
                .param(RECURSIVE_DEPTH, 2)
                .build();
        dispatcher.execute(operation, result -> {
            // @formatter:off
            getView().updateRootLogger(result.get(ROOT_LOGGER_TEMPLATE.lastKey()).get(ROOT_LOGGER_TEMPLATE.lastValue()));
            getView().updateLogger(asNamedNodes(failSafePropertyList(result, LOGGER_TEMPLATE.lastKey())));

            getView().updateAsyncHandler(asNamedNodes(failSafePropertyList(result, ASYNC_HANDLER_TEMPLATE.lastKey())));
            getView().updateConsoleHandler(asNamedNodes(failSafePropertyList(result, CONSOLE_HANDLER_TEMPLATE.lastKey())));
            getView().updateCustomHandler(asNamedNodes(failSafePropertyList(result, CUSTOM_HANDLER_TEMPLATE.lastKey())));
            getView().updateFileHandler(asNamedNodes(failSafePropertyList(result, FILE_HANDLER_TEMPLATE.lastKey())));
            getView().updatePeriodicHandler(asNamedNodes(failSafePropertyList(result, PERIODIC_ROTATING_FILE_HANDLER_TEMPLATE.lastKey())));
            getView().updatePeriodicSizeHandler(asNamedNodes(failSafePropertyList(result, PERIODIC_SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastKey())));
            getView().updateSizeHandlerHandler(asNamedNodes(failSafePropertyList(result, SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastKey())));
            getView().updateSyslogHandler(asNamedNodes(failSafePropertyList(result, SYSLOG_HANDLER_TEMPLATE.lastKey())));

            getView().updateCustomFormatter(asNamedNodes(failSafePropertyList(result, CUSTOM_FORMATTER_TEMPLATE.lastKey())));
            getView().updatePatternFormatter(asNamedNodes(failSafePropertyList(result, PATTERN_FORMATTER_TEMPLATE.lastKey())));
            // @formatter:on
        });

        if (!environment.isStandalone()) {
            new Async<FunctionContext>(progress.get()).single(new FunctionContext(),
                    new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            //noinspection HardCodedStringLiteral
                            logger.error("Unable to create suggestion handler for 'relative-to' form field: " +
                                    "Error reading running servers: {}", context.getErrorMessage());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<Server> servers = context.get(TopologyFunctions.RUNNING_SERVERS);
                            if (!servers.isEmpty() && servers.get(0).isStarted()) {
                                pathOperation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION,
                                        servers.get(0).getServerAddress()).param(CHILD_TYPE, "path").build();
                            }
                        }
                    },
                    new TopologyFunctions.RunningServersOfProfile(environment, dispatcher,
                            statementContext.selectedProfile()));
        }
    }

    Operation pathOperation() {
        return pathOperation;
    }
}
