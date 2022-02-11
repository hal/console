/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.coremanagement;

import java.util.List;

import javax.inject.Inject;

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
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static java.util.Arrays.asList;
import static org.jboss.hal.client.configuration.subsystem.coremanagement.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CORE_MANAGEMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVICE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.Ids.ADD;
import static org.jboss.hal.resources.Ids.CONFIGURATION_CHANGES;

public class CoreManagementPresenter
        extends MbuiPresenter<CoreManagementPresenter.MyView, CoreManagementPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;

    @Inject
    public CoreManagementPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, proxy, finder);
        this.crud = crud;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return CORE_MANAGEMENT_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(CORE_MANAGEMENT);
    }

    @Override
    protected void reload() {
        crud.readRecursive(CORE_MANAGEMENT_SUBSYSTEM_TEMPLATE, result -> {
            ModelNode conf = new ModelNode();
            if (result.hasDefined(SERVICE)) {
                conf = result.get(SERVICE).get(CONFIGURATION_CHANGES);
            }
            getView().update(conf);
            getView().updateProcessStateListener(
                    asNamedNodes(failSafePropertyList(result, PROCESS_STATE_LISTENER_TEMPLATE.lastName())));
        });
    }

    void addConfigurationChanges() {
        crud.addSingleton(Ids.build(Ids.CONFIGURATION_CHANGES, ADD), Names.CONFIGURATION_CHANGES,
                CONFIGURATION_CHANGES_TEMPLATE, asList("max-history"), address -> reload());
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.CORE_MANAGEMENT)
    @Requires(value = { CONFIGURATION_CHANGES_ADDRESS, PROCESS_STATE_LISTENER_ADDRESS }, recursive = false)
    public interface MyProxy extends ProxyPlace<CoreManagementPresenter> {
    }

    public interface MyView extends MbuiView<CoreManagementPresenter> {
        void update(ModelNode confChanges);

        void updateProcessStateListener(List<NamedNode> model);
    }
    // @formatter:on
}
