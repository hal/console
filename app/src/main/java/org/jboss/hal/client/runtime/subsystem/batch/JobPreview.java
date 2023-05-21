/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.batch;

import java.util.Map;

import org.jboss.elemento.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.setVisible;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.ABANDONED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.COMPLETED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.FAILED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STARTED;
import static org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus.STOPPED;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INSTANCE_COUNT;

class JobPreview extends PreviewContent<JobNode> {

    private final EmptyState empty;
    private final Donut executions;

    JobPreview(JobColumn column, JobNode job, FinderPathFactory finderPathFactory, Places places, Resources resources) {
        super(job.getName(), job.getPath());

        FinderPath path = finderPathFactory.deployment(job.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(job.getPath())
                .title(resources.messages().goTo(Names.DEPLOYMENTS)).element());

        empty = new EmptyState.Builder(Ids.JOP_EXECUTION_EMPTY, resources.constants().noExecutions())
                .description(resources.messages().noExecutions())
                .build();

        executions = new Donut.Builder(Names.EXECUTIONS)
                .add(STARTED.name(), resources.constants().running(), PatternFly.colors.blue)
                .add(STOPPED.name(), resources.constants().stopped(), PatternFly.colors.black500)
                .add(COMPLETED.name(), resources.constants().completed(), PatternFly.colors.green)
                .add(FAILED.name(), resources.constants().failed(), PatternFly.colors.red)
                .add(ABANDONED.name(), resources.constants().abandoned(), PatternFly.colors.red300)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(executions);

        setVisible(executions.element(), false);

        getHeaderContainer().appendChild(refreshLink(() -> column.refresh(RESTORE_SELECTION)));
        previewBuilder().addAll(empty, executions);
    }

    @Override
    public void update(JobNode item) {
        int instanceCount = item.get(INSTANCE_COUNT).asInt();
        if (instanceCount == 0) {
            setVisible(empty.element(), true);
            setVisible(executions.element(), false);

        } else {
            setVisible(empty.element(), false);
            setVisible(executions.element(), true);

            Map<String, Long> byBatchStatus = item.getExecutions().stream()
                    .collect(groupingBy(e -> e.getBatchStatus().name(), counting()));
            executions.update(byBatchStatus);
        }
    }
}
