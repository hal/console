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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.Form.FinishRemove;
import org.jboss.hal.ballroom.form.Form.FinishReset;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.PropertiesOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.SelectionAwareStatementContext;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static java.util.Collections.emptyMap;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVLET_CONTAINER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SERVLET_CONTAINER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public class ServletContainerPresenter
        extends ApplicationFinderPresenter<ServletContainerPresenter.MyView, ServletContainerPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final PropertiesOperations po;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Resources resources;
    private String servletContainerName;

    @Inject
    public ServletContainerPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            PropertiesOperations po,
            MetadataRegistry metadataRegistry,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.po = po;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new SelectionAwareStatementContext(statementContext, () -> servletContainerName);
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        servletContainerName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.SERVLET_CONTAINER),
                        resources.constants().settings(), Names.SERVLET_CONTAINER)
                .append(Ids.UNDERTOW_SERVLET_CONTAINER, Ids.undertowServletContainer(servletContainerName),
                        Names.SERVLET_CONTAINER, servletContainerName);
    }

    @Override
    protected void reload() {
        crud.readRecursive(SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext),
                result -> getView().update(result));
    }

    void saveServletContainer(Map<String, Object> changedValues) {
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        crud.save(Names.SERVLET_CONTAINER, servletContainerName,
                SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext), changedValues, metadata, this::reload);
    }

    void resetServletContainer(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        crud.reset(Names.SERVLET_CONTAINER, servletContainerName,
                SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext), form, metadata,
                new FinishReset<ModelNode>(form) {
                    @Override
                    public void afterReset(Form<ModelNode> form) {
                        reload();
                    }
                });
    }

    void saveMimeMapping(Map<String, String> properties) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        po.saveSingletonWithProperties(Names.MIME_MAPPING, address, emptyMap(), metadata, MIME_MAPPING, properties,
                this::reload);
    }

    void resetMimeMapping(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        crud.resetSingleton(Names.MIME_MAPPING, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void saveWelcomeFile(Map<String, String> properties) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        po.saveSingletonWithProperties(Names.WELCOME_FILE, address, emptyMap(), metadata, WELCOME_FILE, properties,
                this::reload);
    }

    void resetWelcomeFile(Form<ModelNode> form) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE);
        crud.resetSingleton(Names.WELCOME_FILE, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void saveSettings(ServletContainerSetting settingType, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix())
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix()));
        crud.saveSingleton(settingType.type, address, changedValues, metadata, this::reload);
    }

    void resetSettings(ServletContainerSetting settingType, Form<ModelNode> form) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix())
                .resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix()));
        crud.resetSingleton(settingType.type, address, form, metadata, new FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeSettings(ServletContainerSetting settingType, Form<ModelNode> form) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix())
                .resolve(statementContext);
        crud.removeSingleton(settingType.type, address, new FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(Form<ModelNode> form) {
                reload();
            }
        });
    }

    Operation pingSettings(ServletContainerSetting settingType) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix())
                .resolve(statementContext);
        return new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
    }

    void addSettingsSingleton(ServletContainerSetting settingType) {
        ResourceAddress address = SELECTED_SERVLET_CONTAINER_TEMPLATE.append(settingType.templateSuffix())
                .resolve(statementContext);
        crud.addSingleton(settingType.type, address, null, a -> reload());
    }

    // ------------------------------------------------------ getter

    StatementContext getStatementContext() {
        return statementContext;
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(SERVLET_CONTAINER_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_SERVLET_CONTAINER)
    public interface MyProxy extends ProxyPlace<ServletContainerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<ServletContainerPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on
}
