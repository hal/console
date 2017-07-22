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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.listview.DataProvider;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.ballroom.listview.ItemRenderer;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.ballroom.listview.Toolbar;
import org.jboss.hal.ballroom.listview.Toolbar.Column;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.elements;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.ballroom.Format.humanReadableDuration;
import static org.jboss.hal.ballroom.Format.mediumDateTime;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STARTED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.x2;
import static org.jboss.hal.resources.UIConstants.POLLING_INTERVAL;

public class JobView extends HalViewImpl implements JobPresenter.MyView {

    private final DataProvider<ExecutionNode> dataProvider;
    private final Toolbar<ExecutionNode> toolbar;
    private final ListView<ExecutionNode> listView;
    private JobPresenter presenter;

    @Inject
    public JobView(Resources resources) {
        dataProvider = new DataProvider<>(NamedNode::getName);

        toolbar = new Toolbar.Builder<>(dataProvider, asList(
                new Column<>(NAME, Names.EXECUTION_ID,
                        (node, filter) -> node.getName().equals(filter),
                        comparing(NamedNode::getName)),
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
                        comparing(ExecutionNode::getDuration))))
                .action(Ids.JOP_EXECUTION_REFRESH, resources.constants().refresh(), this::refresh)
                .build();
        registerAttachable(toolbar);

        ItemRenderer<ExecutionNode> itemRenderer = item -> new ItemDisplay<ExecutionNode>() {
            @Override
            public String getId() {
                return Ids.build(EXECUTION, String.valueOf(item.getExecutionId()));
            }

            @Override
            public HTMLElement getStatusElement() {
                HtmlContentBuilder<HTMLElement> builder = span()
                        .css(listHalIconBig)
                        .title(item.getBatchStatus().name());
                switch (item.getBatchStatus()) {
                    case STARTED:
                        builder.css(pfIcon("spinner"), faSpin, listHalIconProgress);
                        break;
                    case STOPPED:
                        builder.css(fontAwesome(stopCircleO), listHalIconInfo);
                        break;
                    case COMPLETED:
                        builder.css(pfIcon(ok), listHalIconSuccess);
                        break;
                    case FAILED:
                    case ABANDONED:
                        builder.css(pfIcon(errorCircleO), listHalIconError);
                        break;
                    case UNKNOWN:
                        builder.css(fontAwesome(questionsCircleO));
                        break;
                }
                return builder.asElement();
            }

            @Override
            public String getTitle() {
                return Names.EXECUTION_ID + ": " + item.getExecutionId();
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public SafeHtml getDescriptionHtml() {
                SafeHtmlBuilder html = new SafeHtmlBuilder();
                html.appendEscaped(Names.INSTANCE_ID + ": " + item.getInstanceId())
                        .appendHtmlConstant("<br/>")
                        .appendEscaped(Names.BATCH_STATUS + ": " + item.getBatchStatus());
                if (item.getExitError() != null) {
                    html.appendHtmlConstant("<br/>").appendEscaped(item.getExitError());
                }
                return html.toSafeHtml();
            }

            @Override
            @SuppressWarnings("HardCodedStringLiteral")
            public HasElements getAdditionalInfoElements() {
                ElementsBuilder elements = elements();
                elements.add(div().css(halExecutionTime)
                        .add(p().css(textRight).innerHtml(new SafeHtmlBuilder()
                                .appendEscaped(resources.constants().start() + ": ")
                                .appendEscaped(mediumDateTime(item.getCreateTime()))
                                .appendHtmlConstant("<br/>")
                                .appendEscaped(resources.constants().finished() + ": ")
                                .appendEscaped(failsSafeTime(item, END_TIME,
                                        itm -> mediumDateTime(item.getEndTime())))
                                .appendHtmlConstant("<br/>")
                                .appendEscaped(resources.constants().lastModified() + ": ")
                                .appendEscaped(failsSafeTime(item, LAST_UPDATED_TIME,
                                        itm -> mediumDateTime(item.getLastUpdatedTime())))
                                .toSafeHtml())));
                elements.add(div().css(halExecutionDuration)
                        .add(span()
                                .css(fontAwesome("clock-o", x2), marginRight5)
                                .title(resources.constants().duration()))
                        .add(p().css(CSS.lead).textContent(failsSafeTime(item, END_TIME, itm ->
                                humanReadableDuration(
                                        itm.getEndTime().getTime() - item.getCreateTime().getTime())))));
                return elements;
            }

            @Override
            public List<ItemAction<ExecutionNode>> actions() {
                List<ItemAction<ExecutionNode>> actions = new ArrayList<>();
                if (item.getBatchStatus() == STARTED) {
                    actions.add(new ItemAction<>(Ids.JOP_EXECUTION_STOP,
                            resources.constants().stop(), execution -> presenter.stopExecution(execution)));
                } else if (item.getBatchStatus() == STOPPED) {
                    actions.add(new ItemAction<>(Ids.JOP_EXECUTION_RESTART,
                            resources.constants().restart(),
                            execution -> presenter.restartExecution(execution)));
                }
                return actions;
            }
        };
        listView = new ListView.Builder<>(Ids.JOB_LIST, itemRenderer)
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

    private String failsSafeTime(ExecutionNode execution, String attribute, Function<ExecutionNode, String> fn) {
        if (execution.hasDefined(attribute)) {
            return fn.apply(execution);
        }
        return Names.NOT_AVAILABLE;
    }
}
