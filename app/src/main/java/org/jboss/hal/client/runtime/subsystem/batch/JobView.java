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
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.hal.ballroom.Format.humanReadableDuration;
import static org.jboss.hal.ballroom.Format.shortDateTime;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.FAILED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.END_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXECUTION;
import static org.jboss.hal.resources.CSS.*;

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

        listView = new ListView<>(Ids.JOB_LIST, item -> new ItemDisplay<ExecutionNode>() {
            @Override
            public String getId() {
                return Ids.build(EXECUTION, String.valueOf(item.getExecutionId()));
            }

            @Override
            public boolean stacked() {
                return true;
            }

            @Override
            public HTMLElement status() {
                HtmlContentBuilder<HTMLElement> builder = span().css(listViewPfIconMd);
                switch (item.getBatchStatus()) {
                    case STARTED:
                        builder.css(pfIcon("spinner"));
                        break;
                    case STOPPED:
                        builder.css(fontAwesome(stopCircleO), grey);
                        break;
                    case COMPLETED:
                        builder.css(pfIcon(ok));
                        break;
                    case FAILED:
                    case ABANDONED:
                        builder.css(pfIcon(errorCircleO));
                        break;
                    case UNKNOWN:
                        builder.css(fontAwesome(questionsCircleO));
                        break;
                }
                return builder.title(item.getBatchStatus().name()).asElement();
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
            public HasElements getAdditionalInfo() {
                ElementsBuilder elements = elements();
                elements.add(div()
                        .add(p().style("width: 15em")
                                .css(textRight).innerHtml(new SafeHtmlBuilder()
                                .appendEscaped(resources.constants().start() + ": ")
                                .appendEscaped(shortDateTime(item.getCreateTime()))
                                .appendHtmlConstant("<br/>")
                                .appendEscaped(resources.constants().finished() + ": ")
                                .appendEscaped(shortDateTime(item.getEndTime()))
                                .toSafeHtml())));
                elements.add(div()
                        .add(span().css(fontAwesome("clock-o")).title(resources.constants().duration()))
                        .add(strong().textContent(failsSafeEnd(item, itm ->
                                humanReadableDuration(itm.getEndTime().getTime() - item.getCreateTime().getTime())))));
                return elements;
            }

            @Override
            public List<ItemAction<ExecutionNode>> actions() {
                List<ItemAction<ExecutionNode>> actions = new ArrayList<>();
                if (item.getBatchStatus() == FAILED) {
                    actions.add(new ItemAction<>(Ids.JOP_EXECUTION_RESTART,
                            resources.constants().restart(), execution -> presenter.restartExecution(execution)));
                } else {
                    actions.add(ItemAction.placeholder(resources.constants().restart()));
                }
                return actions;
            }
        });

        initElement(row()
                .add(column()
                        .add(header = h(1).asElement())
                        .add(lead = p().css(CSS.lead).asElement())
                        .add(listView)));
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
            List<ExecutionNode> byInstanceIdAndLastUpdatedTime = job.getExecutions().stream()
                    .sorted(comparing(ExecutionNode::getInstanceId)
                            .thenComparing(ExecutionNode::getLastUpdatedTime))
                    .collect(toList());
            listView.setItems(byInstanceIdAndLastUpdatedTime);
        }
    }

    private String failsSafeEnd(ExecutionNode execution, Function<ExecutionNode, String> fn) {
        if (execution.hasDefined(END_TIME)) {
            return fn.apply(execution);
        }
        return Names.NOT_AVAILABLE;
    }
}
