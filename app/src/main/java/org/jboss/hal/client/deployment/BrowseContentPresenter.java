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
package org.jboss.hal.client.deployment;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Content;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;

public class BrowseContentPresenter
        extends ApplicationFinderPresenter<BrowseContentPresenter.MyView, BrowseContentPresenter.MyProxy> {

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private final Environment environment;
    private String content;

    @Inject
    public BrowseContentPresenter(EventBus eventBus,
            MyView view,
            MyProxy proxy,
            Finder finder,
            FinderPathFactory finderPathFactory,
            Dispatcher dispatcher,
            Environment environment) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
        this.environment = environment;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        content = request.getParameter(CONTENT, null);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.content(content);
    }

    @Override
    protected void reload() {
        if (ManagementModel.supportsReadContentFromDeployment(environment.getManagementVersion())) {
            ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content);
            Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                    .param(INCLUDE_RUNTIME, true)
                    .build();
            dispatcher.execute(operation, result -> getView().setContent(new Content(result)));
        } else {
            // TODO Fallback when browse-content is not supported
        }
    }

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.BROWSE_CONTENT)
    @Requires(value = ContentColumn.CONTENT_ADDRESS, recursive = false)
    public interface MyProxy extends ProxyPlace<BrowseContentPresenter> {
    }

    public interface MyView extends HalView {
        void setContent(Content content);
    }
    // @formatter:on
}
