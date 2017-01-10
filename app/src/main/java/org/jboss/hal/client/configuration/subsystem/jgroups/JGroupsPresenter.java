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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.FilteringStatementContext;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.JGROUPS_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.STACK_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;
import static org.jboss.hal.resources.Ids.JGROUPS_REMOTE_SITE;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class JGroupsPresenter extends ApplicationFinderPresenter<JGroupsPresenter.MyView, JGroupsPresenter.MyProxy>
        implements SupportsExpertMode {

    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.JGROUPS)
    @Requires(AddressTemplates.JGROUPS_ADDRESS)
    public interface MyProxy extends ProxyPlace<JGroupsPresenter> {}

    public interface MyView extends HalView, HasPresenter<JGroupsPresenter> {
        void update(ModelNode payload);

        // stack resources
        void updateProtocols(List<NamedNode> model);
        void updateTransports(List<NamedNode> model);
        void updateRelays(List<NamedNode> model);
        void updateRemoteSite(List<NamedNode> model);
        void showStackInnerPage(String id);

        // channel resources
        void showChannelInnerPage(String id);
        void updateForks(List<NamedNode> model);
        void updateChannelProtocols(List<NamedNode> model);
    }
    // @formatter:on


    private Resources resources;
    private MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final CrudOperations crud;
    private String currentStack;
    private String currentChannel;
    private String currentFork;
    StatementContext filterStatementContext;

    @Inject
    public JGroupsPresenter(final EventBus eventBus,
            final MyView view, final Resources resources,
            final MyProxy myProxy, MetadataRegistry metadataRegistry,
            final Finder finder,
            final FinderPathFactory finderPathFactory,
            final StatementContext statementContext,
            final CrudOperations crud) {

        super(eventBus, view, myProxy, finder);
        this.resources = resources;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.crud = crud;
        this.filterStatementContext = new FilteringStatementContext(statementContext, new FilteringStatementContext.Filter() {
            @Override
            public String filter(final String filterKey) {
                switch (filterKey) {
                    case "selected.channel":
                        return currentChannel;
                    case "selected.fork":
                        return currentFork;
                    case "selected.stack":
                        return currentStack;
                }
                return null;
            }

            @Override
            public String[] filterTuple(final String placeholder) {
                return null;
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return JGROUPS_TEMPLATE.resolve(filterStatementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.subsystemPath(JGROUPS);
    }


    // ------------------------------------------------------ jgroups subsystem

    @Override
    protected void reload() {
        reload(modelNode -> {
            // update the UI with the resulting model

            // root configuration, stacks, channel
            getView().update(modelNode);

            // stack / relays
            List<NamedNode> relayNode = asNamedNodes(failSafePropertyList(modelNode, String.join("/", STACK, currentStack, RELAY)));
            getView().updateRelays(relayNode);

            // stack / relay / remote-site
            List<NamedNode> remoteSite = asNamedNodes(failSafePropertyList(modelNode, String.join("/", STACK, currentStack, RELAY, "relay.RELAY2", JGROUPS_REMOTE_SITE)));
            getView().updateRemoteSite(remoteSite);

            // stack / protocols
            List<NamedNode> protocol = asNamedNodes(failSafePropertyList(modelNode, String.join("/", STACK, currentStack, PROTOCOL)));
            getView().updateProtocols(protocol);

            // stack / transport
            List<NamedNode> transport = asNamedNodes(failSafePropertyList(modelNode, String.join("/", STACK, currentStack, TRANSPORT)));
            getView().updateTransports(transport);

            // channel / fork
            List<NamedNode> forks = asNamedNodes(failSafePropertyList(modelNode, String.join("/", CHANNEL, currentChannel, FORK)));
            getView().updateForks(forks);

            // channel / fork / protocol
            List<NamedNode> channelProtocols = asNamedNodes(failSafePropertyList(modelNode, String.join("/", CHANNEL, currentChannel, FORK, currentFork, PROTOCOL)));
            getView().updateChannelProtocols(channelProtocols);

        });
    }

    private void reload(Consumer<ModelNode> payload) {
        crud.readRecursive(JGROUPS_TEMPLATE, payload::accept);
    }

    void saveSingleton(final AddressTemplate template, final Map<String, Object> changedValues,
            final SafeHtml successMessage) {
        crud.saveSingleton(template.resolve(filterStatementContext), changedValues, successMessage, this::reload);
    }

    void saveResource(final AddressTemplate template, final String resourceName, final Map<String, Object> changedValues,
            final SafeHtml successMessage) {
        crud.save(template.resolve(filterStatementContext, resourceName), changedValues, successMessage, this::reload);
    }

    void removeResource(final AddressTemplate template, String name, String displayName) {
        ResourceAddress address = template.resolve(filterStatementContext, name);
        crud.remove(displayName, name, address, this::reload);
    }

    void addResourceDialog(AddressTemplate template, String resourceid, String displayName) {
        AddressTemplate addressTemplate = AddressTemplate.of(template.resolve(filterStatementContext));
        crud.add(resourceid, displayName, addressTemplate, (n, a) -> reload());
    }

    // stack resources

    void addStack() {
        Metadata metadata = metadataRegistry.lookup(STACK_TEMPLATE);
        AddResourceDialog dialog = new AddResourceDialog(Ids.build(Ids.JGROUPS_STACK_CONFIG, Ids.ADD_SUFFIX),
                resources.messages().addResourceTitle(Names.STACK), metadata,
                (name, model) -> {
                    ResourceAddress address = STACK_TEMPLATE.resolve(filterStatementContext, name);
                    // add operation requires a transport parameter
                    model.get(TRANSPORT).setEmptyObject();
                    model.get(TRANSPORT).get(TYPE).set(DEFAULT);
                    crud.add(Names.STACK, name, address, model, (n, a) -> reload());
                });
        dialog.show();
    }

    void showStackInnerPage(String id) {
        getView().showStackInnerPage(id);
    }

    // relay resources

    void showRelays(NamedNode selectedStack) {
        currentStack = selectedStack.getName();
        List<NamedNode> relayNode = asNamedNodes(failSafePropertyList(selectedStack.asModelNode(), RELAY));
        getView().updateRelays(relayNode);
    }

    public void addRelay() {
        AddressTemplate addressTemplate = AddressTemplate.of(SELECTED_RELAY_TEMPLATE.resolve(filterStatementContext));
        crud.addSingleton(Ids.build(RELAY, Ids.ADD_SUFFIX, Ids.FORM_SUFFIX), Names.RELAY,
                addressTemplate, (n, a) -> reload());
    }

    // remote sites

    public void showRemoteSites(final NamedNode row) {
        List<NamedNode> model = asNamedNodes(failSafePropertyList(row.asModelNode(), JGROUPS_REMOTE_SITE));
        getView().updateRemoteSite(model);
    }

    // protocol resources

    void showProtocols(NamedNode selectedStack) {
        currentStack = selectedStack.getName();
        getView().updateProtocols(asNamedNodes(failSafePropertyList(selectedStack, PROTOCOL)));
    }

    // transport resources

    void showTransports(NamedNode selectedStack) {
        currentStack = selectedStack.getName();
        List<NamedNode> model = asNamedNodes(failSafePropertyList(selectedStack.asModelNode(), TRANSPORT));
        getView().updateTransports(model);
    }

    void showForks(NamedNode selectedChannel) {
        currentChannel = selectedChannel.getName();
        List<NamedNode> model = asNamedNodes(failSafePropertyList(selectedChannel.asModelNode(), FORK));
        getView().updateForks(model);
    }

    void showChannelInnerPage(String id) {
        getView().showChannelInnerPage(id);
    }

    void showChannelProtocol(NamedNode selectedFork) {
        currentFork = selectedFork.getName();
        List<NamedNode> model = asNamedNodes(failSafePropertyList(selectedFork.asModelNode(), PROTOCOL));
        getView().updateChannelProtocols(model);
    }

    public String getCurrentFork() {
        return currentFork;
    }

}
