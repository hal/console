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
import java.util.function.Consumer;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.PropertiesOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.FilteringStatementContext.Filter;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.webservice.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HANDLER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WEBSERVICES;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

public class WebservicePresenter
        extends ApplicationFinderPresenter<WebservicePresenter.MyView, WebservicePresenter.MyProxy>
        implements SupportsExpertMode {

    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final CrudOperations crud;
    private final PropertiesOperations po;
    private final Resources resources;

    private Config configType;
    private NamedNode config;
    private HandlerChain handlerChainType;
    private NamedNode handlerChain;

    @Inject
    public WebservicePresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            CrudOperations crud,
            PropertiesOperations po,
            Resources resources) {

        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = new FilteringStatementContext(statementContext, new Filter() {
            @Override
            public String filter(String resource) {
                switch (resource) {
                    case CONFIG_TYPE:
                        return configType.resource;
                    case CONFIG_NAME:
                        return config != null ? config.getName() : null;
                    case HANDLER_CHAIN_TYPE:
                        return handlerChainType.resource;
                    case HANDLER_CHAIN_NAME:
                        return handlerChain != null ? handlerChain.getName() : null;
                    default:
                        break;
                }
                return null;
            }

            @Override
            public String[] filterTuple(String placeholder) {
                return null;
            }
        });
        this.crud = crud;
        this.po = po;
        this.resources = resources;
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
        return finderPathFactory.configurationSubsystemPath(WEBSERVICES);
    }


    // ------------------------------------------------------ webservices subsystem

    @Override
    protected void reload() {
        reload(modelNode -> getView().update(modelNode));
    }

    private void reload(Consumer<ModelNode> payload) {
        crud.readRecursive(WEBSERVICES_TEMPLATE, payload::accept);
    }

    void saveWebservicesConfiguration(Map<String, Object> changedValues) {
        crud.saveSingleton(Names.WEBSERVICES_CONFIGURATION, WEBSERVICES_TEMPLATE, changedValues, this::reload);
    }

    void resetWebservicesConfiguration(Form<ModelNode> form, Metadata metadata) {
        crud.resetSingleton(Names.WEBSERVICES_CONFIGURATION, WEBSERVICES_TEMPLATE, form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }


    // ------------------------------------------------------ client and endpoint config

    void selectConfig(Config configType) {
        this.configType = configType;
    }

    String configSegment() {
        return configType != null && config != null && config.isDefined()
                ? configType.type + ": " + config.getName()
                : Names.NOT_AVAILABLE;
    }

    void addConfig() {
        Metadata metadata = metadataRegistry.lookup(configType.template);
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(configType.baseId, Ids.ADD),
                resources.messages().addResourceTitle(configType.type), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_CONFIG_TEMPLATE.resolve(statementContext, name);
                    crud.add(configType.type, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void saveConfig(Form<NamedNode> form, Map<String, Object> changedValues, String property) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_CONFIG_TEMPLATE.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(configType.template);
        po.saveWithProperties(configType.type, name, address, changedValues, metadata, property,
                form.<Map<String, String>>getFormItem(property).getValue(), this::reload);
    }

    void resetConfig(Form<NamedNode> form) {
        String name = form.getModel().getName();
        ResourceAddress address = SELECTED_CONFIG_TEMPLATE.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(configType.template);
        crud.reset(configType.type, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reload();
            }
        });
    }

    void removeConfig(String name) {
        ResourceAddress address = SELECTED_CONFIG_TEMPLATE.resolve(statementContext, name);
        crud.remove(configType.type, name, address, this::reload);
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
        Metadata metadata = metadataRegistry.lookup(HANDLER_CHAIN_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.WEBSERVICES_HANDLER_CHAIN_ADD,
                resources.messages().addResourceTitle(handlerChainType.type), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HANDLER_CHAIN_TEMPLATE.resolve(statementContext, name);
                    crud.add(handlerChainType.type, name, address, model, (n, a) -> reloadHandlerChains());
                });
        dialog.show();
    }

    void saveHandlerChain(String name, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HANDLER_CHAIN_TEMPLATE.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(HANDLER_CHAIN_TEMPLATE);
        crud.save(handlerChainType.type, name, address, changedValues, metadata, this::reloadHandlerChains);
    }

    void resetHandlerChain(String name, Form<NamedNode> form) {
        ResourceAddress address = SELECTED_HANDLER_CHAIN_TEMPLATE.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(HANDLER_CHAIN_TEMPLATE);
        crud.reset(handlerChainType.type, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reloadHandlerChains();
            }
        });
    }

    void removeHandlerChain(String name) {
        ResourceAddress address = SELECTED_HANDLER_CHAIN_TEMPLATE.resolve(statementContext, name);
        crud.remove(handlerChainType.type, name, address, this::reloadHandlerChains);
    }

    private void reloadHandlerChains() {
        reload(modelNode -> {
            getView().update(modelNode);
            updateHandlerChains(modelNode);
        });
    }

    private void updateHandlerChains(ModelNode modelNode) {
        config = new NamedNode(config.getName(),
                failSafeGet(modelNode, configType.resource + "/" + config.getName()));
        List<NamedNode> handlerChains = asNamedNodes(failSafePropertyList(config, handlerChainType.resource));
        getView().updateHandlerChains(configType, handlerChainType, handlerChains);
    }


    // ------------------------------------------------------ handler

    void showHandlers(NamedNode handlerChain) {
        this.handlerChain = handlerChain;

        List<NamedNode> handlers = asNamedNodes(failSafePropertyList(handlerChain, HANDLER));
        getView().updateHandlers(configType, handlerChainType, handlers);
    }

    void addHandler() {
        Metadata metadata = metadataRegistry.lookup(HANDLER_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.WEBSERVICES_HANDLER_ADD,
                resources.messages().addResourceTitle(Names.HANDLER), metadata,
                (name, model) -> {
                    ResourceAddress address = SELECTED_HANDLER_TEMPLATE.resolve(statementContext, name);
                    crud.add(Names.HANDLER, name, address, model, (n, a) -> reloadHandlers());
                });
        dialog.show();
    }

    void saveHandler(String name, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HANDLER_TEMPLATE.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(HANDLER_TEMPLATE);
        crud.save(Names.HANDLER, name, address, changedValues, metadata, this::reloadHandlers);
    }

    void resetHandler(String name, Form<NamedNode> form) {
        ResourceAddress address = SELECTED_HANDLER_TEMPLATE.resolve(statementContext, name);
        Metadata metadata = metadataRegistry.lookup(HANDLER_TEMPLATE);
        crud.reset(Names.HANDLER, name, address, form, metadata, new FinishReset<NamedNode>(form) {
            @Override
            public void afterReset(Form<NamedNode> form) {
                reloadHandlers();
            }
        });
    }

    void removeHandler(String name) {
        ResourceAddress address = SELECTED_HANDLER_TEMPLATE.resolve(statementContext, name);
        crud.remove(Names.HANDLER, name, address, this::reloadHandlers);
    }

    private void reloadHandlers() {
        reload(modelNode -> {
            getView().update(modelNode);
            updateHandlerChains(modelNode);
            updateHandlers(modelNode);
        });
    }

    private void updateHandlers(ModelNode modelNode) {
        handlerChain = new NamedNode(handlerChain.getName(), failSafeGet(modelNode,
                String.join("/",
                        configType.resource, config.getName(),
                        handlerChainType.resource, handlerChain.getName())));

        List<NamedNode> handlers = asNamedNodes(failSafePropertyList(handlerChain, HANDLER));
        getView().updateHandlers(configType, handlerChainType, handlers);
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.WEBSERVICES)
    @Requires(AddressTemplates.WEBSERVICES_ADDRESS)
    public interface MyProxy extends ProxyPlace<WebservicePresenter> {
    }

    public interface MyView extends HalView, HasPresenter<WebservicePresenter> {
        void update(ModelNode payload);
        void updateHandlerChains(Config configType, HandlerChain handlerChainType, List<NamedNode> handlerChains);
        void updateHandlers(Config configType, HandlerChain handlerChainType, List<NamedNode> handlers);
    }
    // @formatter:on
}
