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
package org.jboss.hal.client.configuration.subsystem.webservice;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.FilteringStatementContext.Filter;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WEBSERVICES;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

/**
 * @author Harald Pehl
 */
public class WebservicePresenter
        extends ApplicationFinderPresenter<WebservicePresenter.MyView, WebservicePresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.WEBSERVICES)
    @Requires(AddressTemplates.WEBSERVICES_ADDRESS)
    public interface MyProxy extends ProxyPlace<WebservicePresenter> {}

    public interface MyView extends HalView, HasPresenter<WebservicePresenter> {
        void update(ModelNode payload);
        void updateHandlerChains(Config configType, HandlerChain handlerChainType, List<NamedNode> handlerChains);
        void updateHandlers(Config configType, HandlerChain handlerChainType, List<NamedNode> handlers);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final CrudOperations crud;
    private final StatementContext statementContext;
    private ModelNode payload;
    private Config configType;
    private NamedNode config;
    private HandlerChain handlerChainType;
    private NamedNode handlerChain;

    @Inject
    public WebservicePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final CrudOperations crud,
            final StatementContext statementContext) {

        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.crud = crud;
        this.statementContext = new FilteringStatementContext(statementContext, new Filter() {
            @Override
            public String filter(final String resource) {
                switch (resource) {
                    case CONFIG_TYPE:
                        return configType.resource;
                    case CONFIG_NAME:
                        return config != null ? config.getName() : null;
                    case HANDLER_CHAIN_TYPE:
                        return handlerChainType.resource;
                    case HANDLER_CHAIN_NAME:
                        return handlerChain != null ? handlerChain.getName() : null;
                }
                return null;
            }

            @Override
            public String[] filterTuple(final String placeholder) {
                return null;
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return WEBSERVICES_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(WEBSERVICES);
    }


    // ------------------------------------------------------ webservices subsystem

    @Override
    protected void reload() {
        crud.readRecursive(WEBSERVICES_TEMPLATE, result -> {
            this.payload = result;
            getView().update(payload);
        });
    }

    void saveWebservicesConfiguration(final Map<String, Object> changedValues) {
        crud.saveSingleton(Names.WEBSERVICES_CONFIGURATION, WEBSERVICES_TEMPLATE, changedValues, this::reload);
    }


    // ------------------------------------------------------ client and endpoint config

    void selectConfig(final Config configType) {
        this.configType = configType;
    }

    String configSegment() {
        return configType != null && config != null && config.isDefined()
                ? configType.type + ": " + config.getName()
                : Names.NOT_AVAILABLE;
    }

    void addConfig() {
    }

    void saveConfig(Form<NamedNode> form, Map<String, Object> changedValues, String property) {
    }

    void removeConfig(String name) {
    }


    // ------------------------------------------------------ handler chain

    void showHandlerChains(NamedNode config, HandlerChain handlerChainType) {
        this.config = config;
        this.handlerChainType = handlerChainType;

        List<NamedNode> handlerChains = asNamedNodes(failSafePropertyList(config, handlerChainType.resource));
        getView().updateHandlerChains(configType, handlerChainType, handlerChains);
    }

    String handlerChainTypeSegment() {
        if (handlerChainType != null) {
            return handlerChainType.type;
        } else {
            return Names.NOT_AVAILABLE;
        }
    }

    String handlerChainSegment() {
        if (handlerChainType != null && handlerChain != null && handlerChain.isDefined()) {
            return handlerChainType.type + ": " + handlerChain.getName();
        } else {
            return Names.NOT_AVAILABLE;
        }
    }

    void addHandlerChain() {
    }

    void saveHandlerChain(String name, Map<String, Object> changedValues) {
    }

    void removeHandlerChain(String name) {
    }


    // ------------------------------------------------------ handler

    void showHandlers(NamedNode handlerChain) {
        this.handlerChain = handlerChain;

        List<NamedNode> handlers = asNamedNodes(failSafePropertyList(handlerChain, HANDLER));
        getView().updateHandlers(configType, handlerChainType, handlers);
    }

    void addHandler() {
    }

    void saveHandler(String name, Map<String, Object> changedValues) {
    }

    void removeHandler(String name) {
    }

}
