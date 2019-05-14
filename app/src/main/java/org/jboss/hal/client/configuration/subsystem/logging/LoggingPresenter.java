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
package org.jboss.hal.client.configuration.subsystem.logging;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FORMATTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.Ids.ADD;
import static org.jboss.hal.resources.Ids.asId;

public class LoggingPresenter
        extends MbuiPresenter<LoggingPresenter.MyView, LoggingPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private ComplexAttributeOperations ca;

    @Inject
    public LoggingPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            ComplexAttributeOperations ca) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.ca = ca;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return LOGGING_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(LOGGING)
                .append(Ids.LOGGING_CONFIG_AND_PROFILES, Ids.LOGGING_CONFIGURATION,
                        Names.LOGGING, Names.CONFIGURATION);
    }

    @Override
    protected void reload() {
        crud.read(LOGGING_SUBSYSTEM_TEMPLATE, 2, result -> {
            // @formatter:off
            getView().updateLoggingConfig(result);

            if (result.hasDefined(ROOT_LOGGER_TEMPLATE.lastName())) {
                getView().updateRootLogger(result.get(ROOT_LOGGER_TEMPLATE.lastName()).get(ROOT_LOGGER_TEMPLATE.lastValue()));
            } else {
                getView().noRootLogger();
            }
            getView().updateLogger(asNamedNodes(failSafePropertyList(result, LOGGER_TEMPLATE.lastName())));

            getView().updateAsyncHandler(asNamedNodes(failSafePropertyList(result, ASYNC_HANDLER_TEMPLATE.lastName())));
            getView().updateConsoleHandler(asNamedNodes(failSafePropertyList(result, CONSOLE_HANDLER_TEMPLATE.lastName())));
            getView().updateCustomHandler(asNamedNodes(failSafePropertyList(result, CUSTOM_HANDLER_TEMPLATE.lastName())));
            getView().updateFileHandler(asNamedNodes(failSafePropertyList(result, FILE_HANDLER_TEMPLATE.lastName())));
            getView().updatePeriodicHandler(asNamedNodes(failSafePropertyList(result, PERIODIC_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
            getView().updatePeriodicSizeHandler(asNamedNodes(failSafePropertyList(result, PERIODIC_SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
            getView().updateSizeHandlerHandler(asNamedNodes(failSafePropertyList(result, SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
            getView().updateSocketHandler(asNamedNodes(failSafePropertyList(result, SOCKET_HANDLER_TEMPLATE.lastName())));
            getView().updateSyslogHandler(asNamedNodes(failSafePropertyList(result, SYSLOG_HANDLER_TEMPLATE.lastName())));

            getView().updateCustomFormatter(asNamedNodes(failSafePropertyList(result, CUSTOM_FORMATTER_TEMPLATE.lastName())));
            getView().updatePatternFormatter(asNamedNodes(failSafePropertyList(result, PATTERN_FORMATTER_TEMPLATE.lastName())));
            getView().updateJsonFormatter(asNamedNodes(failSafePropertyList(result, JSON_FORMATTER_TEMPLATE.lastName())));
            getView().updateXmlFormatter(asNamedNodes(failSafePropertyList(result, XML_FORMATTER_TEMPLATE.lastName())));
            // @formatter:on
        });
    }

    void saveComplexObject(String type, String name, String complexAttribute, AddressTemplate template,
            Map<String, Object> changedValues) {
        ca.save(name, complexAttribute, type, template, changedValues, this::reload);
    }

    void resetComplexObject(String type, String name, String complexAttribute, AddressTemplate template,
            Metadata metadata, Form<ModelNode> form) {
        ca.reset(name, complexAttribute, type, template, metadata, form, this::reload);
    }

    void removeComplexObject(String type, String name, String complexAttribute, AddressTemplate template) {
        ca.remove(name, complexAttribute, type, template, this::reload);
    }

    void addComplexObject(String type, String name, String complexAttribute, AddressTemplate template) {
        ca.add(Ids.build(LOGGING, FORMATTER, asId(type), ADD), name, complexAttribute, type, template, this::reload);
    }

    public StatementContext getStatementContext() {
        return statementContext;
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.LOGGING_CONFIGURATION)
    @Requires({LOGGING_SUBSYSTEM_ADDRESS, ROOT_LOGGER_ADDRESS, LOGGER_ADDRESS,
            ASYNC_HANDLER_ADDRESS, CONSOLE_HANDLER_ADDRESS, CUSTOM_HANDLER_ADDRESS, FILE_HANDLER_ADDRESS,
            PERIODIC_ROTATING_FILE_HANDLER_ADDRESS, PERIODIC_SIZE_ROTATING_FILE_HANDLER_ADDRESS,
            SIZE_ROTATING_FILE_HANDLER_ADDRESS, SYSLOG_HANDLER_ADDRESS, SOCKET_HANDLER_ADDRESS,
            CUSTOM_FORMATTER_ADDRESS, PATTERN_FORMATTER_ADDRESS, JSON_FORMATTER_ADDRESS, XML_FORMATTER_ADDRESS})
    public interface MyProxy extends ProxyPlace<LoggingPresenter> {
    }

    public interface MyView extends MbuiView<LoggingPresenter> {
        void updateLoggingConfig(ModelNode modelNode);

        void updateRootLogger(ModelNode modelNode);
        void noRootLogger();
        void updateLogger(List<NamedNode> items);

        void updateAsyncHandler(List<NamedNode> items);
        void updateConsoleHandler(List<NamedNode> items);
        void updateCustomHandler(List<NamedNode> items);
        void updateFileHandler(List<NamedNode> items);
        void updatePeriodicHandler(List<NamedNode> items);
        void updatePeriodicSizeHandler(List<NamedNode> items);
        void updateSizeHandlerHandler(List<NamedNode> items);
        void updateSocketHandler(List<NamedNode> items);
        void updateSyslogHandler(List<NamedNode> items);

        void updateCustomFormatter(List<NamedNode> items);
        void updatePatternFormatter(List<NamedNode> items);
        void updateJsonFormatter(List<NamedNode> items);
        void updateXmlFormatter(List<NamedNode> items);
    }
    // @formatter:on
}
