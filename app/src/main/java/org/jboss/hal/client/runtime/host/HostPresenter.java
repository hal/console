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
package org.jboss.hal.client.runtime.host;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;

public class HostPresenter
        extends MbuiPresenter<HostPresenter.MyView, HostPresenter.MyProxy>
        implements SupportsExpertMode {

    static final String HOST_ADDRESS = "/{selected.host}";
    static final String INTERFACE_ADDRESS = HOST_ADDRESS + "/interface=*";
    static final String JVM_ADDRESS = HOST_ADDRESS + "/jvm=*";
    static final String PATH_ADDRESS = HOST_ADDRESS + "/path=*";
    static final String SOCKET_BINDING_GROUP_ADDRESS = HOST_ADDRESS + "/socket-binding-group=*";
    static final String SYSTEM_PROPERTY_ADDRESS = HOST_ADDRESS + "/system-property=*";


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.HOST_CONFIGURATION)
    @Requires(value = {HOST_ADDRESS, INTERFACE_ADDRESS, JVM_ADDRESS, PATH_ADDRESS, SOCKET_BINDING_GROUP_ADDRESS,
            SYSTEM_PROPERTY_ADDRESS}, recursive = false)
    public interface MyProxy extends ProxyPlace<HostPresenter> {}

    public interface MyView extends MbuiView<HostPresenter> {
        void updateHost(Host host);
        void updateInterfaces(List<NamedNode> interfaces);
        void updateJvms(List<NamedNode> interfaces);
        void updatePaths(List<NamedNode> paths);
        void updateSocketBindingGroups(List<NamedNode> groups);
        void updateSystemProperties(List<NamedNode> properties);
    }
    // @formatter:on


    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final Resources resources;

    @Inject
    public HostPresenter(final EventBus eventBus,
            final HostPresenter.MyView view,
            final HostPresenter.MyProxy proxy,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final CrudOperations crud,
            final Resources resources) {
        super(eventBus, view, proxy, finder);
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return new ResourceAddress().add(HOST, statementContext.selectedHost());
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.runtimeHostPath();
    }

    @Override
    protected void reload() {
        ResourceAddress hostAddress = new ResourceAddress().add(HOST, statementContext.selectedHost());
        Operation hostOp = new Operation.Builder(hostAddress, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation interfacesOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, INTERFACE)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation jvmsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, JVM)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation pathsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, PATH)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation socketBindingGroupsOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, SOCKET_BINDING_GROUP)
                .param(INCLUDE_RUNTIME, true)
                .build();
        Operation systemPropertiesOp = new Operation.Builder(hostAddress, READ_CHILDREN_RESOURCES_OPERATION)
                .param(CHILD_TYPE, SYSTEM_PROPERTY)
                .param(INCLUDE_RUNTIME, true)
                .build();

        dispatcher.execute(
                new Composite(hostOp, interfacesOp, jvmsOp, pathsOp, socketBindingGroupsOp, systemPropertiesOp),
                (CompositeResult result) -> {
                    getView().updateHost(new Host(result.step(0).get(RESULT)));
                    getView().updateInterfaces(asNamedNodes(result.step(1).get(RESULT).asPropertyList()));
                    getView().updateJvms(asNamedNodes(result.step(2).get(RESULT).asPropertyList()));
                    getView().updatePaths(asNamedNodes(result.step(3).get(RESULT).asPropertyList()));
                    getView().updateSocketBindingGroups(asNamedNodes(result.step(4).get(RESULT).asPropertyList()));
                    getView().updateSystemProperties(asNamedNodes(result.step(5).get(RESULT).asPropertyList()));
                });
    }

    void saveHost(Form<Host> form, Map<String, Object> changedValues) {
        boolean hostNameChanged = changedValues.containsKey(NAME);
        crud.save(Names.HOST, form.getModel().getName(), AddressTemplate.of(HOST_ADDRESS), changedValues, () -> {
            reload();
            if (hostNameChanged) {
                DialogFactory.showConfirmation(resources.constants().hostNameChanged(),
                        resources.messages().hostNameChanged(),
                        () -> window.location.reload());
            }
        });
    }
}
