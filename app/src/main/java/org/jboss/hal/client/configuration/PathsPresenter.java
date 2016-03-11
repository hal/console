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
package org.jboss.hal.client.configuration;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.mvp.ApplicationPresenter;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.spi.Requires;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.meta.token.NameTokens.PATH;

/**
 * @author Harald Pehl
 */
public class PathsPresenter extends
        ApplicationPresenter<PathsPresenter.MyView, PathsPresenter.MyProxy> {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(PATH)
    @Requires(ROOT_ADDRESS)
    public interface MyProxy extends ProxyPlace<PathsPresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<PathsPresenter> {
        void update(List<NamedNode> paths);
    }
    // @formatter:on


    static final String ROOT_ADDRESS = "/path=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);

    private final Dispatcher dispatcher;

    @Inject
    public PathsPresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher) {
        super(eventBus, view, proxy);
        this.dispatcher = dispatcher;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadPaths();
    }

    void loadPaths() {
        Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, "path")
                .build();
        dispatcher.execute(operation, result -> getView().update(asNamedNodes(result.asPropertyList())));
    }

    public void savePath(final String name, final Map<String, Object> changedValues) {
        loadPaths();
    }
}
