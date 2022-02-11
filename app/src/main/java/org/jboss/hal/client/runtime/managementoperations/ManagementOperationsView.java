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
package org.jboss.hal.client.runtime.managementoperations;

import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Toolbar;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.core.mbui.listview.ModelNodeListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.comparing;
import static org.jboss.hal.client.runtime.managementoperations.ManagementOperationsPresenter.ACTIVE_OPERATIONS_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.ACTIVE_OPERATION;

public class ManagementOperationsView extends HalViewImpl implements ManagementOperationsPresenter.MyView {

    private final DataProvider<ManagementOperations> dataProvider;
    private ManagementOperationsPresenter presenter;
    private ModelNodeListView<ManagementOperations> listView;

    @Inject
    public ManagementOperationsView(MetadataRegistry metadataRegistry, Resources resources) {
        dataProvider = new DataProvider<>(ManagementOperations::getName, false);

        Metadata metadata = metadataRegistry.lookup(ACTIVE_OPERATIONS_TEMPLATE);
        String findDescription = resources.messages().findNonProgressingOperation();
        String cancelDescription = resources.messages().cancelNonProgressingOperation();

        Messages messages = resources.messages();
        EmptyState emptyState = new EmptyState.Builder(Ids.ACTIVE_OPERATION_EMPTY, resources.constants().noItems())
                .description(resources.messages().noItems().asString() + " " + resources.messages()
                        .configurationChangesDescription())
                .icon(Icons.INFO)
                .primaryAction(resources.constants().reload(), () -> presenter.reload())
                .build();

        Constants constants = resources.constants();
        listView = new ModelNodeListView.Builder<>(
                Ids.build(ACTIVE_OPERATION, CANCEL_OPERATION), metadata,
                dataProvider, item -> new ManagementOperationsDisplay(item, presenter, resources))
                        .toolbarAttribute(new Toolbar.Attribute<>(ACCESS_MECHANISM, constants.accessMechanism(),
                                (node, filter) -> node.getAccessMechanism().toLowerCase().equals(filter.toLowerCase()),
                                comparing(ManagementOperations::getAccessMechanism)))
                        .toolbarAttribute(new Toolbar.Attribute<>(ADDRESS, resources.constants().address(),
                                // filter by three address attributes: address, host and server
                                (model, filter) -> model.getAddress().contains(filter)
                                        || (model.getActiveAddressHost() != null
                                                && model.getActiveAddressHost().contains(filter))
                                        || (model.getActiveAddressServer() != null
                                                && model.getActiveAddressServer().contains(filter)),
                                null))
                        .toolbarAttribute(new Toolbar.Attribute<>(EXECUTION_STATUS, resources.constants().executionStatus(),
                                (node, filter) -> node.getExecutionStatus().toLowerCase().contains(filter.toLowerCase()),
                                comparing(ManagementOperations::getExecutionStatus)))
                        .toolbarAttribute(new Toolbar.Attribute<>(OPERATION, resources.constants().operation(),
                                (model, filter) -> model.getOperation().contains(filter), null))
                        .toolbarAction(new Toolbar.Action(Ids.build(ACTIVE_OPERATION, Ids.REFRESH),
                                constants.reload(), findDescription, () -> presenter.reload()))
                        .toolbarAction(new Toolbar.Action(Ids.build(ACTIVE_OPERATION, Ids.CANCEL_NON_PROGRESSING_OPERATION),
                                constants.cancelNonProgressingOperation(), cancelDescription,
                                () -> presenter.cancelNonProgressingOperation()))
                        .noItems(constants.noItems(), messages.noItems())
                        .emptyState(EMPTY, emptyState)
                        .build();
        registerAttachable(listView);
        initElements(listView);
    }

    @Override
    public void setPresenter(ManagementOperationsPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(List<ManagementOperations> activeOperations) {
        dataProvider.update(activeOperations);
        if (activeOperations.isEmpty()) {
            listView.showEmptyState(EMPTY);
        }
    }
}
