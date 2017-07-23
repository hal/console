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

import java.util.List;
import javax.inject.Inject;

import org.jboss.hal.ballroom.listview.DataProvider;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.ballroom.listview.Toolbar;
import org.jboss.hal.ballroom.listview.Toolbar.Column;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.jboss.gwt.elemento.core.Elements.elements;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.vh;
import static org.jboss.hal.resources.UIConstants.POLLING_INTERVAL;

public class JobView extends HalViewImpl implements JobPresenter.MyView {

    private final DataProvider<ExecutionNode> dataProvider;
    private final Toolbar<ExecutionNode> toolbar;
    private final ListView<ExecutionNode> listView;
    private JobPresenter presenter;

    @Inject
    public JobView(Resources resources) {
        dataProvider = new DataProvider<>(NamedNode::getName);

        List<Column<ExecutionNode>> column = asList(
                new Column<>(NAME, Names.EXECUTION_ID,
                        (node, filter) -> node.getName().equals(filter),
                        comparing(ExecutionNode::getExecutionId)),
                new Column<>(INSTANCE_ID, Names.INSTANCE_ID,
                        (node, filter) -> String.valueOf(node.getInstanceId()).equals(filter),
                        comparing(ExecutionNode::getInstanceId)),
                new Column<>(BATCH_STATUS, Names.BATCH_STATUS,
                        (node, filter) -> node.getBatchStatus().name().toLowerCase().contains(filter.toLowerCase()),
                        comparing(ExecutionNode::getBatchStatus)),
                new Column<>(START_TIME, resources.constants().start(), null, comparing(ExecutionNode::getStartTime)),
                new Column<>(END_TIME, resources.constants().finished(), null, comparing(ExecutionNode::getEndTime)),
                new Column<>(LAST_UPDATED_TIME, resources.constants().lastModified(), null,
                        comparing(ExecutionNode::getLastUpdatedTime)),
                new Column<>("duration", resources.constants().duration(), null,
                        comparing(ExecutionNode::getDuration)));

        toolbar = new Toolbar.Builder<>(dataProvider, column)
                .action(Ids.JOP_EXECUTION_REFRESH, resources.constants().refresh(), this::refresh)
                .build();
        registerAttachable(toolbar);

        listView = new ListView.Builder<ExecutionNode>(Ids.JOB_LIST, item ->
                new ExecutionNodeDisplay(item, presenter, resources))
                .contentWidths("50%", "50%")
                .stacked(true)
                .multiselect(false)
                .build();

        initElements(elements()
                .add(toolbar)
                .add(row()
                        .add(column()
                                .add(listView))));
    }

    @Override
    public void attach() {
        super.attach();
        int toolbarHeight = (int) (toolbar.asElement().offsetHeight);
        listView.asElement().style.height = vh(applicationOffset() + toolbarHeight + 1);
        listView.asElement().style.overflow = "scroll"; //NON-NLS

        dataProvider.addDisplay(listView);
        dataProvider.addDisplay(toolbar);
    }

    @Override
    public void setPresenter(JobPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(JobNode job) {
        dataProvider.setItems(job.getExecutions());
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
