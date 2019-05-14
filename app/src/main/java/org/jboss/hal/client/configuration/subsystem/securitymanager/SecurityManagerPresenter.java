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
package org.jboss.hal.client.configuration.subsystem.securitymanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.ComplexAttributeOperations;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.hal.client.configuration.subsystem.securitymanager.AddressTemplates.DEPLOYMENT_PERMISSIONS_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.securitymanager.AddressTemplates.SECURITY_MANAGER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.securitymanager.AddressTemplates.SECURITY_MANAGER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.storeIndex;

public class SecurityManagerPresenter
        extends ApplicationFinderPresenter<SecurityManagerPresenter.MyView, SecurityManagerPresenter.MyProxy>
        implements SupportsExpertMode {

    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final CrudOperations crud;
    private final ComplexAttributeOperations ca;

    @Inject
    public SecurityManagerPresenter(EventBus eventBus,
            SecurityManagerPresenter.MyView view,
            SecurityManagerPresenter.MyProxy myProxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            CrudOperations crud,
            ComplexAttributeOperations ca) {
        super(eventBus, view, myProxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.crud = crud;
        this.ca = ca;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return DEPLOYMENT_PERMISSIONS_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(SECURITY_MANAGER);
    }

    @Override
    protected void reload() {
        crud.readRecursive(SECURITY_MANAGER_TEMPLATE, modelNode -> {
            boolean defined = modelNode.hasDefined(DEPLOYMENT_PERMISSIONS);
            Map<Permission, List<ModelNode>> permissions = new HashMap<>();
            if (defined) {
                for (Permission permission : Permission.values()) {
                    ModelNode node = ModelNodeHelper.failSafeGet(modelNode,
                            String.join("/", DEPLOYMENT_PERMISSIONS, DEFAULT, permission.resource));
                    List<ModelNode> nodes = node.isDefined() ? node.asList() : emptyList();
                    storeIndex(nodes);
                    permissions.put(permission, nodes);
                }
            }
            getView().update(defined, permissions);
        });
    }

    void addDeploymentPermissions() {
        crud.addSingleton(Names.DEPLOYMENT_PERMISSIONS, DEPLOYMENT_PERMISSIONS_TEMPLATE, address -> reload());
    }

    void addPermission(Permission permission) {
        ca.listAdd(Ids.build(permission.baseId, Ids.ADD), null, permission.resource, permission.type,
                DEPLOYMENT_PERMISSIONS_TEMPLATE, asList(CLASS, NAME, ACTIONS, MODULE), this::reload);
    }

    void savePermission(Permission permission, int index, Map<String, Object> changedValues) {
        ResourceAddress address = DEPLOYMENT_PERMISSIONS_TEMPLATE.resolve(statementContext);
        Metadata metadata = metadataRegistry.lookup(DEPLOYMENT_PERMISSIONS_TEMPLATE)
                .forComplexAttribute(permission.resource);
        ca.save(permission.resource, permission.type, index, address, changedValues, metadata, this::reload);
    }

    void removePermission(Permission permission, int index) {
        ResourceAddress address = DEPLOYMENT_PERMISSIONS_TEMPLATE.resolve(statementContext);
        ca.remove(permission.resource, permission.type, index, address, this::reload);
    }


    // @formatter:off
    @ProxyCodeSplit
    @Requires(SECURITY_MANAGER_ADDRESS)
    @NameToken(NameTokens.SECURITY_MANAGER)
    public interface MyProxy extends ProxyPlace<SecurityManagerPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<SecurityManagerPresenter> {
        void update(boolean defined, Map<Permission, List<ModelNode>> permissions);
    }
    // @formatter:on
}
