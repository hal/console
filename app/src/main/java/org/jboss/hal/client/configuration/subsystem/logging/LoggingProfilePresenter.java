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
import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
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
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.logging.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FORMATTER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.Ids.ADD;
import static org.jboss.hal.resources.Ids.asId;

public class LoggingProfilePresenter
        extends MbuiPresenter<LoggingProfilePresenter.MyView, LoggingProfilePresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final ComplexAttributeOperations ca;
    private String loggingProfile;

    @Inject
    public LoggingProfilePresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            ComplexAttributeOperations ca) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> loggingProfile);
        this.ca = ca;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        loggingProfile = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_LOGGING_PROFILE_TEMPLATE.resolve(statementContext);
    }

    String getLoggingProfile() {
        return loggingProfile;
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(LOGGING)
                .append(Ids.LOGGING_CONFIG_AND_PROFILES, Ids.asId(Names.LOGGING_PROFILES),
                        Names.LOGGING, Names.LOGGING_PROFILES)
                .append(Ids.LOGGING_PROFILE, Ids.loggingProfile(loggingProfile),
                        Names.LOGGING_PROFILE, loggingProfile);
    }

    @Override
    protected void reload() {
        ResourceAddress address = SELECTED_LOGGING_PROFILE_TEMPLATE.resolve(statementContext);
        crud.read(address, 2, result -> {
            // @formatter:off
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
            getView().updateSizeHandler(asNamedNodes(failSafePropertyList(result, SIZE_ROTATING_FILE_HANDLER_TEMPLATE.lastName())));
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
    @NameToken(NameTokens.LOGGING_PROFILE)
    @Requires(LOGGING_PROFILE_ADDRESS)
    public interface MyProxy extends ProxyPlace<LoggingProfilePresenter> {
    }

    public interface MyView extends MbuiView<LoggingProfilePresenter> {
        void updateRootLogger(ModelNode modelNode);
        void noRootLogger();
        void updateLogger(List<NamedNode> items);

        void updateAsyncHandler(List<NamedNode> items);
        void updateConsoleHandler(List<NamedNode> items);
        void updateCustomHandler(List<NamedNode> items);
        void updateFileHandler(List<NamedNode> items);
        void updatePeriodicHandler(List<NamedNode> items);
        void updatePeriodicSizeHandler(List<NamedNode> items);
        void updateSizeHandler(List<NamedNode> items);
        void updateSocketHandler(List<NamedNode> items);
        void updateSyslogHandler(List<NamedNode> items);

        void updateCustomFormatter(List<NamedNode> items);
        void updatePatternFormatter(List<NamedNode> items);
        void updateJsonFormatter(List<NamedNode> items);
        void updateXmlFormatter(List<NamedNode> items);    }
    // @formatter:on
}
