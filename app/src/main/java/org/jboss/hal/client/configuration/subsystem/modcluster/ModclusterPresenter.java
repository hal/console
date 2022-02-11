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
package org.jboss.hal.client.configuration.subsystem.modcluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mbui.MbuiPresenter;
import org.jboss.hal.core.mbui.MbuiView;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.client.configuration.subsystem.modcluster.AddressTemplates.*;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asNamedNodes;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.Ids.ADD;
import static org.jboss.hal.resources.Ids.CUSTOM_LOAD_METRIC;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.LOAD_METRIC;

public class ModclusterPresenter
        extends MbuiPresenter<ModclusterPresenter.MyView, ModclusterPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private MetadataRegistry metadataRegistry;
    private final FinderPathFactory finderPathFactory;
    private final StatementContext statementContext;
    private String proxyName;

    @Inject
    public ModclusterPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            MetadataRegistry metadataRegistry,
            FinderPathFactory finderPathFactory,
            StatementContext statementContext) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
        this.metadataRegistry = metadataRegistry;
        this.finderPathFactory = finderPathFactory;
        this.statementContext = statementContext;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        proxyName = request.getParameter(NAME, null);
    }

    @Override
    public ResourceAddress resourceAddress() {
        return PROXY_TEMPLATE.resolve(statementContext, proxyName);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(ModelDescriptionConstants.MODCLUSTER)
                .append(Ids.MODCLUSTER_PROXY, Ids.modclusterProxy(proxyName), Names.PROXY, proxyName);
    }

    @Override
    protected void reload() {
        crud.read(PROXY_TEMPLATE.replaceWildcards(proxyName), 2, result -> {
            getView().updateConfiguration(result);
            ModelNode loadProvDynamicModel = failSafeGet(result, "load-provider/dynamic");
            if (loadProvDynamicModel.isDefined()) {
                getView().updateLoadProviderDynamic(loadProvDynamicModel);

                // update custom load metric
                ModelNode customLoadMetricModel = failSafeGet(result, "load-provider/dynamic/custom-load-metric");
                List<NamedNode> customLoadMetricNamedNodes = Collections.emptyList();
                if (customLoadMetricModel.isDefined()) {
                    customLoadMetricNamedNodes = asNamedNodes(customLoadMetricModel.asPropertyList());
                }
                getView().updateCustomLoadMetrics(customLoadMetricNamedNodes);

                // update load metric
                ModelNode loadMetricModel = failSafeGet(result, "load-provider/dynamic/load-metric");
                List<NamedNode> loadMetricNamedNodes = Collections.emptyList();
                if (loadMetricModel.isDefined()) {
                    loadMetricNamedNodes = asNamedNodes(loadMetricModel.asPropertyList());
                }
                getView().updateLoadMetrics(loadMetricNamedNodes);
            } else {
                getView().updateLoadProviderDynamic(loadProvDynamicModel);
                getView().updateCustomLoadMetrics(Collections.emptyList());
                getView().updateLoadMetrics(Collections.emptyList());
            }
        });
    }

    public String getProxyName() {
        return proxyName;
    }

    // =================== load-provider=dynamic

    public Operation loadProviderDynamicOperation() {
        AddressTemplate template = LOAD_PROVIDER_DYNAMIC_TEMPLATE.replaceWildcards(proxyName);
        return new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION).build();
    }

    public void addLoadProviderDynamic() {
        String id = Ids.build("load-provider-dynamic", FORM, ADD);
        AddressTemplate template = LOAD_PROVIDER_DYNAMIC_TEMPLATE.replaceWildcards(proxyName);
        crud.addSingleton(id, Names.LOAD_PROVIDER_DYNAMIC, template, address -> reload());
    }

    public void removeLoadProviderDynamic() {
        AddressTemplate template = LOAD_PROVIDER_DYNAMIC_TEMPLATE.replaceWildcards(proxyName);
        crud.removeSingleton(Names.LOAD_PROVIDER_DYNAMIC, template, this::reload);
    }

    // =================== load-provider=simple

    public Operation loadProviderSimpleOperation() {
        AddressTemplate template = LOAD_PROVIDER_SIMPLE_TEMPLATE.replaceWildcards(proxyName);
        return new Operation.Builder(template.resolve(statementContext), READ_RESOURCE_OPERATION).build();
    }

    public void addLoadProviderSimple() {
        String id = Ids.build("load-provider-simple", FORM, ADD);
        AddressTemplate template = LOAD_PROVIDER_SIMPLE_TEMPLATE.replaceWildcards(proxyName);
        crud.addSingleton(id, Names.LOAD_PROVIDER_SIMPLE, template, address -> reload());
    }

    public void removeLoadProviderSimple() {
        AddressTemplate template = LOAD_PROVIDER_SIMPLE_TEMPLATE.replaceWildcards(proxyName);
        crud.removeSingleton(Names.LOAD_PROVIDER_SIMPLE, template, this::reload);
    }

    // =================== custom load metric

    public void addCustomLoadMetric() {
        String id = Ids.build(CUSTOM_LOAD_METRIC, FORM, ADD);
        AddressTemplate template = CUSTOM_LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        crud.add(id, Names.CUSTOM_LOAD_METRIC, template, (name, address) -> reload());
    }

    public void removeCustomLoadMetric(Table<NamedNode> table) {
        AddressTemplate template = CUSTOM_LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        crud.remove(Names.CUSTOM_LOAD_METRIC, table.selectedRow().getName(), template, this::reload);
    }

    public void saveCustomLoadMetric(Form<NamedNode> form, Map<String, Object> changedValues) {
        AddressTemplate template = CUSTOM_LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        crud.save(Names.CUSTOM_LOAD_METRIC, form.getModel().getName(), template, changedValues, this::reload);
    }

    public void prepareResetCustomLoadMetric(Form<NamedNode> form) {
        AddressTemplate template = CUSTOM_LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        Metadata metadata = metadataRegistry.lookup(CUSTOM_LOAD_METRIC_TEMPLATE);
        crud.reset(Names.CUSTOM_LOAD_METRIC, form.getModel().getName(), template, form, metadata, this::reload);
    }

    // =================== load metric
    public void addLoadMetric() {
        String id = Ids.build(LOAD_METRIC, FORM, ADD);
        AddressTemplate template = LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        crud.add(id, Names.LOAD_METRIC, template, (name, address) -> reload());
    }

    public void removeLoadMetric(Table<NamedNode> table) {
        AddressTemplate template = LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        crud.remove(Names.LOAD_METRIC, table.selectedRow().getName(), template, this::reload);
    }

    public void saveLoadMetric(Form<NamedNode> form, Map<String, Object> changedValues) {
        AddressTemplate template = LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        crud.save(Names.LOAD_METRIC, form.getModel().getName(), template, changedValues, this::reload);
    }

    public void prepareResetLoadMetric(Form<NamedNode> form) {
        AddressTemplate template = LOAD_METRIC_TEMPLATE.replaceWildcards(proxyName);
        Metadata metadata = metadataRegistry.lookup(LOAD_METRIC_TEMPLATE);
        crud.reset(Names.LOAD_METRIC, form.getModel().getName(), template, form, metadata, this::reload);
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires(PROXY_ADDRESS)
    @NameToken(NameTokens.MODCLUSTER)
    public interface MyProxy extends ProxyPlace<ModclusterPresenter> {
    }

    public interface MyView extends MbuiView<ModclusterPresenter> {
        void updateConfiguration(ModelNode payload);

        void updateLoadProviderDynamic(ModelNode payload);

        void updateCustomLoadMetrics(List<NamedNode> nodes);

        void updateLoadMetrics(List<NamedNode> nodes);
    }
    // @formatter:on
}
