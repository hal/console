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
package org.jboss.hal.client.configuration.subsystem.distributableweb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mbui.dialog.AddResourceDialog;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static org.jboss.hal.client.configuration.subsystem.distributableweb.AddressTemplates.DISTRIBUTABLE_WEB_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafePropertyList;

public class DistributableWebPresenter
        extends MbuiPresenter<DistributableWebPresenter.MyView, DistributableWebPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final Dispatcher dispatcher;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;
    private ManagementAffinity mgmtAffinity;
    private Routing routing;

    @Inject
    public DistributableWebPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            Dispatcher dispatcher,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.dispatcher = dispatcher;
        this.finderPathFactory = finderPathFactory;
        this.metadataRegistry = metadataRegistry;
        this.statementContext = statementContext;
        this.resources = resources;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return DISTRIBUTABLE_WEB_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(DISTRIBUTABLE_WEB);
    }

    @Override
    protected void reload() {
        crud.read(DISTRIBUTABLE_WEB_TEMPLATE, 2, result -> {
            getView().updateConfiguration(result);
            List<NamedNode> routings = asNamedNodes(failSafePropertyList(result, ROUTING));
            if (!routings.isEmpty()) {
                Routing routing = Routing.fromResource(routings.get(0).getName());
                if (routing != null) {
                    getView().updateRouting(routing, routings.get(0).asModelNode());
                }
            }
            getView().updateHotRodSessionManagement(
                    asNamedNodes(failSafePropertyList(result, "hotrod-session-management")));
            getView().updateHotRodSSOManagement(
                    asNamedNodes(failSafePropertyList(result, "hotrod-single-sign-on-management")));
            getView().updateInfinispanSessionManagement(
                    asNamedNodes(failSafePropertyList(result, "infinispan-session-management")));
            getView().updateInfinispanSSOManagement(
                    asNamedNodes(failSafePropertyList(result, "infinispan-single-sign-on-management")));
        });
    }

    public void saveRouting(Routing routing, Map<String, Object> changedValues) {
        crud.saveSingleton(routing.type, routing.template(), changedValues, this::reload);
    }

    public void resetRouting(Routing routing, Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(routing.template());
        crud.resetSingleton(routing.type, routing.template(), form, metadata, this::reload);
    }

    public void switchRouting(Routing newRouting) {
        if (newRouting != null && newRouting != this.routing) {
            if (newRouting.addWithDialog) {
                Metadata metadata = metadataRegistry.lookup(newRouting.template());
                String id = Ids.build(newRouting.baseId, Ids.ADD);
                Form<ModelNode> form = new ModelNodeForm.Builder<>(id, metadata) // custom form w/o unbound name item
                        .fromRequestProperties()
                        .requiredOnly()
                        .build();
                AddResourceDialog dialog = new AddResourceDialog(resources.messages().addResourceTitle(newRouting.type),
                        form, (name, model) -> addRouting(newRouting, model));
                dialog.show();
            } else {
                addRouting(newRouting, null);
            }
        }
    }

    private void addRouting(Routing newRouting, ModelNode model) {
        Operation.Builder builder = new Operation.Builder(resourceAddress().add(ROUTING, newRouting.resource), ADD);
        if (model != null) {
            builder.payload(model);
        }
        dispatcher.execute(builder.build(), result -> {
            MessageEvent.fire(getEventBus(),
                    Message.success(resources.messages().addSingleResourceSuccess(newRouting.type)));
            this.routing = newRouting;
            reload();
        });
    }

    // ------------------------------------------------------ affinity

    void saveAffinity(Affinity affinity, Map<String, Object> changedValues) {
        if (affinity != Affinity.UNDEFINED) {
            Metadata metadata = metadataRegistry.lookup(affinityTemplate(mgmtAffinity));
            crud.save(affinity.type, AFFINITY, affinityAddress(mgmtAffinity), changedValues, metadata, () -> reload());
        }
    }

    void resetAffinity(Affinity affinity, Form<ModelNode> form) {
        if (affinity != Affinity.UNDEFINED) {
            Metadata metadata = metadataRegistry.lookup(affinityTemplate(mgmtAffinity));
            crud.reset(affinity.type, AFFINITY, affinityAddress(mgmtAffinity), form, metadata,
                    new Form.FinishReset<ModelNode>(form) {
                        @Override
                        public void afterReset(Form<ModelNode> f) {
                            reload();
                        }
                    });
        }
    }

    void switchAffinity(SessionManagement sessionManagement, String mgmtName, Affinity newAffinity) {
        ManagementAffinity newManagementAffinity = new ManagementAffinity(sessionManagement, mgmtName, newAffinity);
        if (this.mgmtAffinity == null || !mgmtName.equals(mgmtAffinity.getMgmtName())) {
            this.mgmtAffinity = newManagementAffinity;
        }
        if (!this.mgmtAffinity.equals(newManagementAffinity)) {
            List<Operation> operations = new ArrayList<>();
            if (this.mgmtAffinity.getAffinity() != Affinity.UNDEFINED) { // switching from pre-existing affinity type
                operations.add(new Operation.Builder(affinityAddress(this.mgmtAffinity), REMOVE).build());
            }
            if (newAffinity != Affinity.UNDEFINED) {
                operations.add(new Operation.Builder(affinityAddress(newManagementAffinity), ADD).build());
            }
            Composite composite = new Composite(operations)
                    .addHeader(ALLOW_RESOURCE_SERVICE_RESTART, true);
            dispatcher.execute(composite, (CompositeResult result) -> {
                MessageEvent.fire(getEventBus(),
                        Message.success(resources.messages().addSingleResourceSuccess(newAffinity.type)));
                this.mgmtAffinity = newManagementAffinity;
                reload();
            });
        }
    }

    private AddressTemplate affinityTemplate(ManagementAffinity mgmtAffinity) {
        return mgmtAffinity.getMgmt().template.append(AddressTemplates.AFFINITY_TEMPLATE.replaceWildcards(mgmtAffinity.getAffinity().resource));
    }

    private ResourceAddress affinityAddress(ManagementAffinity mgmtAffinity) {
        return affinityTemplate(mgmtAffinity).resolve(statementContext, mgmtAffinity.getMgmtName());
    }


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DISTRIBUTABLE_WEB)
    @Requires(AddressTemplates.DISTRIBUTABLE_WEB_ADDRESS)
    public interface MyProxy extends ProxyPlace<DistributableWebPresenter> {
    }

    public interface MyView extends MbuiView<DistributableWebPresenter> {
        void updateConfiguration(ModelNode node);
        void updateRouting(Routing routing, ModelNode node);
        void updateHotRodSessionManagement(List<NamedNode> nodes);
        void updateHotRodSSOManagement(List<NamedNode> nodes);
        void updateInfinispanSessionManagement(List<NamedNode> nodes);
        void updateInfinispanSSOManagement(List<NamedNode> nodes);
    }
    // @formatter:on

    // helper class to keep track of session management and affinity
    private class ManagementAffinity {
        private SessionManagement mgmt;
        private String mgmtName;
        private Affinity affinity;

        public ManagementAffinity(SessionManagement mgmt, String mgmtName, Affinity affinity) {
            this.mgmt = mgmt;
            this.mgmtName = mgmtName;
            this.affinity = affinity;
        }

        public SessionManagement getMgmt() {
            return mgmt;
        }

        public String getMgmtName() {
            return mgmtName;
        }

        public Affinity getAffinity() {
            return affinity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            ManagementAffinity otherManagementAffinity = (ManagementAffinity) o;

            return (mgmt == otherManagementAffinity.getMgmt() &&
                    mgmtName.equals(otherManagementAffinity.getMgmtName()) &&
                    affinity == otherManagementAffinity.getAffinity());
        }

        @Override
        public int hashCode() {
            return Objects.hash(mgmt, mgmtName, affinity);
        }
    }
}
