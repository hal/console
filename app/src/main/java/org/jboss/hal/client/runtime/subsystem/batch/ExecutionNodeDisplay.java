/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemDisplay;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.collect;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.ballroom.Format.humanReadableDuration;
import static org.jboss.hal.ballroom.Format.mediumDateTime;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STARTED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STOPPED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.END_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXECUTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LAST_UPDATED_TIME;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.FontAwesomeSize.x2;

class ExecutionNodeDisplay implements ItemDisplay<ExecutionNode> {

    private static final String COLON = ": ";
    private static final String BR = "<br/>";

    private final ExecutionNode item;
    private final JobPresenter presenter;
    private final Resources resources;

    ExecutionNodeDisplay(ExecutionNode item, JobPresenter presenter, Resources resources) {
        this.item = item;
        this.presenter = presenter;
        this.resources = resources;
    }

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
                builder.css(pfIcon("help"));
                break;
            default:
                break;
        }
        return builder.element();
    }

    @Override
    public String getTitle() {
        return Names.EXECUTION_ID + COLON + item.getExecutionId();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public SafeHtml getDescriptionHtml() {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        html.appendEscaped(Names.INSTANCE_ID + COLON + item.getInstanceId())
                .appendHtmlConstant(BR)
                .appendEscaped(Names.BATCH_STATUS + COLON + item.getBatchStatus());
        if (item.getExitError() != null) {
            html.appendHtmlConstant(BR).appendEscaped(item.getExitError());
        }
        return html.toSafeHtml();
    }

    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public Iterable<HTMLElement> getAdditionalInfoElements() {
        return collect()
                .add(div().css(halExecutionTime)
                        .add(p().css(textRight).innerHtml(new SafeHtmlBuilder()
                                .appendEscaped(resources.constants().start() + COLON)
                                .appendEscaped(mediumDateTime(item.getCreateTime()))
                                .appendHtmlConstant(BR)
                                .appendEscaped(resources.constants().finished() + COLON)
                                .appendEscaped(failsSafeTime(item, END_TIME,
                                        itm -> mediumDateTime(item.getEndTime())))
                                .appendHtmlConstant(BR)
                                .appendEscaped(resources.constants().lastModified() + COLON)
                                .appendEscaped(failsSafeTime(item, LAST_UPDATED_TIME,
                                        itm -> mediumDateTime(item.getLastUpdatedTime())))
                                .toSafeHtml())))
                .add(div().css(halExecutionDuration)
                        .add(span()
                                .css(fontAwesome("clock-o", x2), marginRight5)
                                .title(resources.constants().duration()))
                        .add(p().css(lead).textContent(failsSafeTime(item, END_TIME, itm ->
                                humanReadableDuration(
                                        itm.getEndTime().getTime() - item.getCreateTime().getTime()))))).elements();
    }

    @Override
    public List<ItemAction<ExecutionNode>> actions() {
        // TODO add constraints
        List<ItemAction<ExecutionNode>> actions = new ArrayList<>();
        if (item.getBatchStatus() == STARTED) {
            actions.add(new ItemAction<>(Ids.JOP_EXECUTION_STOP, resources.constants().stop(),
                    presenter::stopExecution));
        } else if (item.getBatchStatus() == STOPPED) {
            actions.add(new ItemAction<>(Ids.JOP_EXECUTION_RESTART, resources.constants().restart(),
                    presenter::restartExecution));
        }
        return actions;
    }

    private String failsSafeTime(ExecutionNode execution, String attribute, Function<ExecutionNode, String> fn) {
        if (execution.hasDefined(attribute)) {
            return fn.apply(execution);
        }
        return Names.NOT_AVAILABLE;
    }
}
