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
package org.jboss.hal.client.runtime.subsystem.batch;

import javax.inject.Inject;

import org.jboss.hal.ballroom.DataProvider;
import org.jboss.hal.ballroom.Toolbar.Action;
import org.jboss.hal.ballroom.Toolbar.Attribute;
import org.jboss.hal.core.mbui.listview.ModelNodeListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Comparator.comparing;
import static org.jboss.hal.client.runtime.subsystem.batch.AddressTemplates.EXECUTION_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.UIConstants.POLLING_INTERVAL;

public class JobView extends HalViewImpl implements JobPresenter.MyView {

    private final DataProvider<ExecutionNode> dataProvider;
    private JobPresenter presenter;

    @Inject
    public JobView(MetadataRegistry metadataRegistry, Resources resources) {
        dataProvider = new DataProvider<>(NamedNode::getName);

        Metadata metadata = metadataRegistry.lookup(EXECUTION_TEMPLATE);
        ModelNodeListView<ExecutionNode> listView = new ModelNodeListView.Builder<>(Ids.JOB_LIST, metadata,
                dataProvider, item -> new ExecutionNodeDisplay(item, presenter, resources))
                .toolbarAttribute(new Attribute<>(NAME, Names.EXECUTION_ID,
                        (node, filter) -> node.getName().equals(filter),
                        comparing(ExecutionNode::getExecutionId)))
                .toolbarAttribute(new Attribute<>(INSTANCE_ID, Names.INSTANCE_ID,
                        (node, filter) -> String.valueOf(node.getInstanceId()).equals(filter),
                        comparing(ExecutionNode::getInstanceId)))
                .toolbarAttribute(new Attribute<>(BATCH_STATUS, Names.BATCH_STATUS,
                        (node, filter) -> node.getBatchStatus().name().toLowerCase().contains(filter.toLowerCase()),
                        comparing(ExecutionNode::getBatchStatus)))
                .toolbarAttribute(new Attribute<>(START_TIME, resources.constants().start(), null,
                        comparing(ExecutionNode::getStartTime)))
                .toolbarAttribute(new Attribute<>(END_TIME, resources.constants().finished(), null,
                        comparing(ExecutionNode::getEndTime)))
                .toolbarAttribute(new Attribute<>(LAST_UPDATED_TIME, resources.constants().lastModified(), null,
                        comparing(ExecutionNode::getLastUpdatedTime)))
                .toolbarAttribute(new Attribute<>("duration", resources.constants().duration(), null,
                        comparing(ExecutionNode::getDuration)))
                .toolbarAction(new Action(Ids.JOP_EXECUTION_REFRESH, resources.constants().refresh(), this::refresh))
                .noItems(resources.constants().noExecutions(), resources.messages().noExecutions())
                .build();

        registerAttachable(listView);
        initElements(listView);
    }

    @Override
    public void setPresenter(JobPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(JobNode job) {
        dataProvider.update(job.getExecutions());
        if (job.getRunningExecutions() > 0) {
            setTimeout(o -> presenter.reload(), POLLING_INTERVAL);
        }
    }

    private void refresh() {
        if (presenter != null) {
            presenter.reload();
        }
    }
}
