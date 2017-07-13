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
import java.util.EnumSet;
import java.util.List;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Collections.singletonList;
import static org.jboss.hal.client.runtime.subsystem.batch.AddressTemplates.DEPLOYMENT_JOB_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.batch.AddressTemplates.SUBDEPLOYMENT_JOB_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@AsyncColumn(Ids.JOB)
public class JobColumn extends FinderColumn<JobNode> {

    @Inject
    public JobColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            Dispatcher dispatcher,
            StatementContext statementContext,
            Resources resources) {

        super(new Builder<JobNode>(finder, Ids.JOB, Names.JOB)
                .columnAction(columnActionFactory.refresh(Ids.JOB_REFRESH))

                .itemsProvider((context, callback) -> {
                    Operation deploymentJobOperation = new Operation.Builder(
                            DEPLOYMENT_JOB_TEMPLATE.resolve(statementContext),
                            READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE, true)
                            .build();
                    Operation subdeploymentJobOperation = new Operation.Builder(
                            SUBDEPLOYMENT_JOB_TEMPLATE.resolve(statementContext),
                            READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .param(RECURSIVE, true)
                            .build();
                    dispatcher.execute(new Composite(deploymentJobOperation, subdeploymentJobOperation),
                            (CompositeResult result) -> {
                                List<JobNode> jobs = new ArrayList<>();
                                result.step(0).get(RESULT).asList().forEach(node -> {
                                    ResourceAddress deployment = new ResourceAddress(node.get(ADDRESS));
                                    ModelNode job = node.get(RESULT);
                                    jobs.add(new JobNode(deployment, job));
                                });
                                callback.onSuccess(jobs);

                                // mark jobs w/ running executions
                                for (JobNode job : jobs) {
                                    if (job.getRunningExecutions() > 0) {
                                        // TODO Add an interval which checks whether the execution has been completed
                                        ItemMonitor.startProgress(Ids.job(job.getName()));
                                    } else {
                                        ItemMonitor.stopProgress(Ids.job(job.getName()));
                                    }
                                }
                            });
                })

                .itemRenderer(item -> new ItemDisplay<JobNode>() {
                    @Override
                    public String getId() {
                        return Ids.job(item.getName());
                    }

                    @Override
                    public String getTitle() {
                        return item.getName();
                    }

                    @Override
                    public HTMLElement asElement() {
                        return ItemDisplay.withSubtitle(item.getName(), item.getDeployment());
                    }

                    @Override
                    public HTMLElement getIcon() {
                        if (item.getInstanceCount() == 0) {
                            return Icons.info();
                        } else if (item.hasExecutions(EnumSet.of(BatchStatus.FAILED, BatchStatus.ABANDONED))) {
                            return Icons.error();
                        } else if (item.hasExecutions(BatchStatus.STOPPED)) {
                            return Icons.warning();
                        }
                        return Icons.ok();
                    }

                    @Override
                    public String getTooltip() {
                        if (item.getInstanceCount() == 0) {
                            return resources.constants().noExecutions();
                        } else if (item.hasExecutions(EnumSet.of(BatchStatus.FAILED, BatchStatus.ABANDONED))) {
                            return resources.constants().failedExecutions();
                        } else if (item.hasExecutions(BatchStatus.STOPPED)) {
                            return resources.constants().stoppedExecution();
                        }
                        return resources.constants().completedExecutions();
                    }

                    @Override
                    public List<ItemAction<JobNode>> actions() {
                        return singletonList(itemActionFactory.view(NameTokens.JOB, NAME, item.getName()));
                    }
                })
                .useFirstActionAsBreadcrumbHandler()
                .onPreview(itm -> new JobPreview(itm, resources))
                .showCount()
                .withFilter()
        );
    }

    @Override
    public void detach() {
        super.detach();
    }
}
