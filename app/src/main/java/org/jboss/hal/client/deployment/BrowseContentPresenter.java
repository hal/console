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
package org.jboss.hal.client.deployment;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.token.NameTokens;

import static org.jboss.hal.dmr.ModelDescriptionConstants.BROWSE_CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;

/**
 * @author Harald Pehl
 */
public class BrowseContentPresenter
        extends ApplicationFinderPresenter<BrowseContentPresenter.MyView, BrowseContentPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.BROWSE_CONTENT)
    public interface MyProxy extends ProxyPlace<BrowseContentPresenter> {}

    public interface MyView extends HalView, HasPresenter<BrowseContentPresenter> {
        void setContent(String content, ModelNode browseContentResult);
    }
    // @formatter:on

    private final FinderPathFactory finderPathFactory;
    private final Dispatcher dispatcher;
    private String content;

    @Inject
    public BrowseContentPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        content = request.getParameter(CONTENT, null);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadContent();
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.content(content);
    }

    void loadContent() {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content);
        Operation operation = new Operation.Builder(BROWSE_CONTENT, address).build();
        dispatcher.execute(operation, result -> getView().setContent(content, result));
    }
}
