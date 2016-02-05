/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration.subsystem.jca;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.client.NameTokens;
import org.jboss.hal.core.ProfileSelectionEvent;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.core.mvp.PatternFlyPresenter;
import org.jboss.hal.core.mvp.PatternFlyView;
import org.jboss.hal.core.mvp.Slots;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.ChangeSetAdapter;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.spi.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_RESOURCES_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asNodesWithNames;
import static org.jboss.hal.resources.Names.PROFILE;

/**
 * @author Harald Pehl
 */
public class DataSourcePresenter extends
        PatternFlyPresenter<DataSourcePresenter.MyView, DataSourcePresenter.MyProxy> {

    static final String ROOT_ADDRESS = "/{any.profile}/subsystem=datasources/data-source=*";
    static final AddressTemplate ROOT_TEMPLATE = AddressTemplate.of(ROOT_ADDRESS);
    static final AddressTemplate DATA_SOURCE_SUBSYSTEM = AddressTemplate
            .of("/{selected.profile}/subsystem=datasources");

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);


    // @formatter:off
    @ProxyCodeSplit
    @NameToken(NameTokens.DATASOURCE)
    @Requires(ROOT_ADDRESS)
    public interface MyProxy extends ProxyPlace<DataSourcePresenter> {}

    public interface MyView extends PatternFlyView, HasPresenter<DataSourcePresenter> {
        void update(List<ModelNode> datasources);
    }
    // @formatter:on


    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final ChangeSetAdapter changeSetAdapter;

    @Inject
    public DataSourcePresenter(final EventBus eventBus,
            final MyView view,
            final MyProxy proxy,
            final Dispatcher dispatcher,
            final StatementContext statementContext) {
        super(eventBus, view, proxy, Slots.MAIN);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.changeSetAdapter = new ChangeSetAdapter();
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        String profile = request.getParameter(PROFILE, null);
        if (profile != null) {
            getEventBus().fireEvent(new ProfileSelectionEvent(profile));
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        loadDataSources();
    }

    private void loadDataSources() {
        Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                DATA_SOURCE_SUBSYSTEM.resolve(statementContext))
                .param(CHILD_TYPE, "data-source")
                .build();
        dispatcher.execute(operation, result -> getView().update(asNodesWithNames(result.asPropertyList())));
    }

    void saveDataSource(final String dataSource, final Map<String, Object> changedValues) {
        logger.debug("About to save changes for {}: {}", dataSource, changedValues); //NON-NLS

        AddressTemplate template = DATA_SOURCE_SUBSYSTEM.append("data-source=" + dataSource);
        ResourceAddress resourceAddress = template.resolve(statementContext);
        Composite composite = changeSetAdapter.fromChangeSet(resourceAddress, changedValues);

        dispatcher.execute(composite, (CompositeResult result) -> {
            logger.debug("Datasource {} successfully modified", dataSource); //NON-NLS
            loadDataSources();
        });
    }
}
