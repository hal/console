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
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
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
    private final Donut donut;

    JobPreview(JobColumn column, JobNode job, Resources resources) {
        super(job.getName(), job.getDeployment());

        empty = new EmptyState.Builder(resources.constants().noExecutions())
                .description(resources.messages().noExecutions())
                .build();

        donut = new Donut.Builder(Names.EXECUTIONS)
                .add(STARTED.name(), resources.constants().running(), PatternFly.colors.green)
                .add(STOPPED.name(), resources.constants().stopped(), PatternFly.colors.orange)
                .add(COMPLETED.name(), resources.constants().completed(), PatternFly.colors.blue)
                .add(FAILED.name(), resources.constants().failed(), PatternFly.colors.red)
                .add(ABANDONED.name(), resources.constants().abandoned(), PatternFly.colors.red300)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(donut);

        status = section()
                .add(donut)
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
            Map<String, Long> byBatchStatus = executions.stream()
                    .collect(groupingBy(e -> e.getBatchStatus().name(), counting()));
            donut.update(byBatchStatus);
        }
    }
}
