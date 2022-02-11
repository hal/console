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
package org.jboss.hal.client.configuration.subsystem.deploymentscanner;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.hal.client.configuration.subsystem.deploymentscanner.AddressTemplates.DEPLOYMENTSCANNER_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.deploymentscanner.AddressTemplates.DEPLOYMENTSCANNER_SUBSYSTEM_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.deploymentscanner.AddressTemplates.DEPLOYMENTSCANNER_SUBSYSTEM_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.deploymentscanner.AddressTemplates.DEPLOYMENTSCANNER_TEMPLATE;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

public class DeploymentScannerPresenter
        extends MbuiPresenter<DeploymentScannerPresenter.MyView, DeploymentScannerPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;

    @Inject
    public DeploymentScannerPresenter(EventBus eventBus,
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
        return DEPLOYMENTSCANNER_SUBSYSTEM_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(ModelDescriptionConstants.DEPLOYMENT_SCANNER);
    }

    @Override
    protected void reload() {
        crud.read(DEPLOYMENTSCANNER_SUBSYSTEM_TEMPLATE, 2, result -> getView().updateScanners(
                asNamedNodes(failSafePropertyList(result, DEPLOYMENTSCANNER_TEMPLATE.lastName()))));
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DEPLOYMENT_SCANNERS)
    @Requires({ DEPLOYMENTSCANNER_SUBSYSTEM_ADDRESS, DEPLOYMENTSCANNER_ADDRESS })
    public interface MyProxy extends ProxyPlace<DeploymentScannerPresenter> {
    }

    public interface MyView extends MbuiView<DeploymentScannerPresenter> {
        void updateScanners(List<NamedNode> items);
    }
    // @formatter:on
}
