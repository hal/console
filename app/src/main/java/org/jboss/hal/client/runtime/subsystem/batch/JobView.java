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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.ballroom.listview.ItemRenderer;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.header;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.Format.humanReadableDuration;
import static org.jboss.hal.ballroom.Format.mediumDateTime;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.LayoutBuilder.*;
import static org.jboss.hal.ballroom.LayoutBuilder.stickyLayout;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STARTED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.END_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXECUTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAST_UPDATED_TIME;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.x2;
import static org.jboss.hal.resources.UIConstants.POLLING_INTERVAL;

public class JobView extends HalViewImpl implements JobPresenter.MyView {

    private final HTMLElement header;
    private final HTMLElement lead;
    private final EmptyState empty;
    private final ListView<ExecutionNode> listView;
    private JobPresenter presenter;

    @Inject
    public JobView(Resources resources) {
        empty = new EmptyState.Builder(resources.constants().noExecutions())
                .description(resources.messages().noExecutions())
                .build();

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

        initElement(row()
                .add(column()
                        .add(stickyLayout()
                                .add(stickyHeader()
                                        .add(header()
                                                .add(a().css(clickable, pullRight).on(click, event -> refresh())
                                                        .add(span().css(fontAwesome("refresh"), marginRight5))
                                                        .add(span().textContent(resources.constants().refresh())))
                                                .add(header = h(1).asElement())
                                                .add(lead = p().css(CSS.lead).asElement())))
                                .add(stickyBody()
                                        .addAll(empty, listView)))));
    }

    @Override
    public void setPresenter(JobPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(JobNode job) {
        header.textContent = job.getName();
        lead.textContent = job.getPath();

        boolean hasExecutions = !job.getExecutions().isEmpty();
        Elements.setVisible(empty.asElement(), !hasExecutions);
        Elements.setVisible(listView.asElement(), hasExecutions);
        if (hasExecutions) {
            listView.setItems(job.getExecutions());
            if (job.getRunningExecutions() > 0) {
                setTimeout(o -> presenter.reload(), POLLING_INTERVAL);
            }
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
