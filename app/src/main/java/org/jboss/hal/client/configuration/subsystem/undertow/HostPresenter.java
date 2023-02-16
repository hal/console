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
package org.jboss.hal.client.configuration.subsystem.undertow;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.CrudOperations;
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
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.HOST_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.undertow.AddressTemplates.SELECTED_HOST_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDERTOW;
import static org.jboss.hal.meta.SelectionAwareStatementContext.SELECTION_KEY;

public class HostPresenter extends ApplicationFinderPresenter<HostPresenter.MyView, HostPresenter.MyProxy>
        implements SupportsExpertMode {

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Provider<Progress> progress;
    private final Environment environment;
    private final Resources resources;

    private String serverName;
    private String hostName;
    private String locationName;

    @Inject
    public HostPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy myProxy,
            final Finder finder,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final MetadataRegistry metadataRegistry,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final @Footer Provider<Progress> progress,
            final Environment environment,
            final Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = new FilteringStatementContext(statementContext,
                new FilteringStatementContext.Filter() {
                    @Override
                    public String filter(String placeholder, AddressTemplate template) {
                        if (SELECTION_KEY.equals(placeholder)) {
                            return serverName;
                        } else if (HOST.equals(placeholder)) {
                            return hostName;
                        }
                        return null;
                    }

                    @Override
                    public String[] filterTuple(String placeholder, AddressTemplate template) {
                        return null;
                    }
                });
        this.progress = progress;
        this.environment = environment;
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
        serverName = request.getParameter(SERVER, null);
        hostName = request.getParameter(HOST, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return SELECTED_HOST_TEMPLATE.resolve(statementContext);
    }

    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(UNDERTOW)
                .append(Ids.UNDERTOW_SETTINGS, Ids.asId(Names.SERVER),
                        resources.constants().settings(), Names.SERVER)
                .append(Ids.UNDERTOW_SERVER, Ids.undertowServer(serverName), Names.SERVER, serverName)
                .append(Ids.UNDERTOW_HOST, Ids.undertowHost(hostName), Names.HOST, hostName);
    }

    @Override
    protected void reload() {
        crud.readRecursive(SELECTED_HOST_TEMPLATE.resolve(statementContext), result -> getView().update(result));
    }

    // ------------------------------------------------------ save & reset

    void saveHost(final Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        crud.save(Names.HOST, hostName, address, changedValues, metadata, this::reload);
    }

    void resetHost(final Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE);
        crud.reset(Names.HOST, hostName, address, form, metadata, new Form.FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    // ------------------------------------------------------ host settings

    Operation hostSettingOperation(HostSetting hostSetting) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        return new Operation.Builder(address, READ_RESOURCE_OPERATION).build();
    }

    void addHostSetting(HostSetting hostSetting) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        crud.addSingleton(hostSetting.type, address, null, a -> reload());
    }

    void saveHostSetting(HostSetting hostSetting, Map<String, Object> changedValues) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        crud.saveSingleton(hostSetting.type, address, changedValues, metadata, this::reload);
    }

    void resetHostSetting(HostSetting hostSetting, Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(HOST_TEMPLATE.append(hostSetting.templateSuffix()));
        crud.resetSingleton(hostSetting.type, address, form, metadata, new Form.FinishReset<ModelNode>(form) {
            @Override
            public void afterReset(Form<ModelNode> form) {
                reload();
            }
        });
    }

    void removeHostSetting(HostSetting hostSetting, Form<ModelNode> form) {
        ResourceAddress address = SELECTED_HOST_TEMPLATE.append(hostSetting.templateSuffix()).resolve(statementContext);
        crud.removeSingleton(hostSetting.type, address, new Form.FinishRemove<ModelNode>(form) {
            @Override
            public void afterRemove(Form<ModelNode> form) {
                reload();
            }
        });
    }

    // ------------------------------------------------------ proxy & view

    // @formatter:off
    @ProxyCodeSplit
    @Requires(HOST_ADDRESS)
    @NameToken(NameTokens.UNDERTOW_HOST)
    public interface MyProxy extends ProxyPlace<HostPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<HostPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on
}
