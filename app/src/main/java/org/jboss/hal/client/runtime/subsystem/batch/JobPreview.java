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

import java.util.Collection;
import java.util.Map;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.metric.Utilization;
import org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.*;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INSTANCE_COUNT;

class JobPreview extends PreviewContent<JobNode> {

    private final EmptyState empty;
    private final HTMLElement status;
    private final Utilization running;
    private final Utilization stopped;
    private final Utilization completed;
    private final Utilization failed;
    private final Utilization abandoned;

    JobPreview(JobColumn column, JobNode job, Resources resources) {
        super(job.getName(), job.getDeployment());

        empty = new EmptyState.Builder(resources.constants().noExecutions())
                .description(resources.messages().noExecutions())
                .build();

        // TODO Replace with Donut
        running = new Utilization(resources.constants().running(), Names.EXECUTIONS, false, false);
        stopped = new Utilization(resources.constants().stopped(), Names.EXECUTIONS, false, false);
        completed = new Utilization(resources.constants().completed(), Names.EXECUTIONS, false, false);
        failed = new Utilization(resources.constants().failed(), Names.EXECUTIONS, false, false);
        abandoned = new Utilization(resources.constants().abandoned(), Names.EXECUTIONS, false, false);
        status = section()
                .addAll(running, stopped, completed, failed, abandoned)
                .asElement();
        Elements.setVisible(status, false);

        getHeaderContainer().appendChild(refreshLink(() -> column.refresh(RESTORE_SELECTION)));
        previewBuilder().addAll(empty.asElement(), status);
    }

    @Override
    public void update(JobNode item) {
        int instanceCount = item.get(INSTANCE_COUNT).asInt();
        if (instanceCount == 0) {
            Elements.setVisible(empty.asElement(), true);
            Elements.setVisible(status, false);

        } else {
            Elements.setVisible(empty.asElement(), false);
            Elements.setVisible(status, true);

            Collection<ExecutionNode> executions = item.byInstanceIdMostRecentExecution().values();
            Map<BatchStatus, Long> byBatchStatus = executions.stream()
                    .collect(groupingBy(ExecutionNode::getBatchStatus, counting()));
            running.update(byBatchStatus.getOrDefault(STARTED, 0L), instanceCount);
            stopped.update(byBatchStatus.getOrDefault(STOPPED, 0L), instanceCount);
            completed.update(byBatchStatus.getOrDefault(COMPLETED, 0L), instanceCount);
            failed.update(byBatchStatus.getOrDefault(FAILED, 0L), instanceCount);
            abandoned.update(byBatchStatus.getOrDefault(ABANDONED, 0L), instanceCount);
        }
    }
}
