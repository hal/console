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
package org.jboss.hal.client.configuration.subsystem.microprofile;

import java.util.Map;

import javax.inject.Inject;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.mvp.ApplicationFinderPresenter;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.SupportsExpertMode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.MICRO_PROFILE_METRICS_ADDRESS;
import static org.jboss.hal.client.configuration.subsystem.microprofile.AddressTemplates.MICRO_PROFILE_METRICS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MICROPROFILE_METRICS_SMALLRYE;

public class MicroProfileMetricsPresenter
        extends ApplicationFinderPresenter<MicroProfileMetricsPresenter.MyView, MicroProfileMetricsPresenter.MyProxy>
        implements SupportsExpertMode {

    private final CrudOperations crud;
    private final FinderPathFactory finderPathFactory;
    private final MetadataRegistry metadataRegistry;
    private final StatementContext statementContext;
    private final Resources resources;

    @Inject
    public MicroProfileMetricsPresenter(EventBus eventBus,
            MyView view,
            MyProxy myProxy,
            Finder finder,
            CrudOperations crud,
            FinderPathFactory finderPathFactory,
            MetadataRegistry metadataRegistry,
            StatementContext statementContext,
            Resources resources) {
        super(eventBus, view, myProxy, finder);
        this.crud = crud;
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
        return MICRO_PROFILE_METRICS_TEMPLATE.resolve(statementContext);
    }

    @Override
    public FinderPath finderPath() {
        return finderPathFactory.configurationSubsystemPath(MICROPROFILE_METRICS_SMALLRYE);
    }

    @Override
    protected void reload() {
        crud.read(MICRO_PROFILE_METRICS_TEMPLATE, result -> getView().update(result));
    }

    void reset(Form<ModelNode> form) {
        Metadata metadata = metadataRegistry.lookup(MICRO_PROFILE_METRICS_TEMPLATE);
        crud.resetSingleton(Names.MICROPROFILE_METRICS, MICRO_PROFILE_METRICS_TEMPLATE, form, metadata, this::reload);
    }

    void save(Map<String, Object> changedValues) {
        crud.saveSingleton(Names.MICROPROFILE_METRICS, MICRO_PROFILE_METRICS_TEMPLATE, changedValues, this::reload);
    }

    // @formatter:off
    @ProxyCodeSplit
    @Requires(MICRO_PROFILE_METRICS_ADDRESS)
    @NameToken(NameTokens.MICRO_PROFILE_METRICS)
    public interface MyProxy extends ProxyPlace<MicroProfileMetricsPresenter> {
    }

    public interface MyView extends HalView, HasPresenter<MicroProfileMetricsPresenter> {
        void update(ModelNode payload);
    }
    // @formatter:on
}
