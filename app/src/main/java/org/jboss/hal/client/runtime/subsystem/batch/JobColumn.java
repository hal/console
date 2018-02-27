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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.runtime.subsystem.batch.ExecutionNode.BatchStatus;
import org.jboss.hal.core.deployment.DeploymentResources;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.finder.ItemMonitor;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static elemental2.dom.DomGlobal.clearInterval;
import static elemental2.dom.DomGlobal.setInterval;
import static java.util.Arrays.asList;
import static org.jboss.hal.client.runtime.subsystem.batch.AddressTemplates.BATCH_DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.runtime.subsystem.batch.AddressTemplates.BATCH_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.UIConstants.POLLING_INTERVAL;

@AsyncColumn(Ids.JOB)
@Requires(BATCH_DEPLOYMENT_ADDRESS)
public class JobColumn extends FinderColumn<JobNode> {

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private final Map<String, Double> intervalHandles;

    @Inject
    public JobColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            ItemActionFactory itemActionFactory,
            DeploymentResources deploymentResources,
            FinderPathFactory finderPathFactory,
            Places places,
            EventBus eventBus,
            Dispatcher dispatcher,
            MetadataRegistry metadataRegistry,
            Resources resources) {

        super(new Builder<JobNode>(finder, Ids.JOB, Names.JOB)
                .columnAction(columnActionFactory.refresh(Ids.JOB_REFRESH))
                .useFirstActionAsBreadcrumbHandler()
                .showCount()
                .withFilter()
                .filterDescription(resources.messages().jobExecutionColumnFilterDescription()));

        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.intervalHandles = new HashMap<>();

        setItemsProvider(
                (context, callback) -> deploymentResources.readChildren(BATCH_JBERET, JOB, JobNode::new, jobs -> {
                    callback.onSuccess(jobs);

                    // turn progress animation on/off
                    clearIntervals();
                    for (JobNode job : jobs) {
                        String jobId = Ids.job(job.getDeployment(), job.getSubdeployment(), job.getName());
                        if (job.getRunningExecutions() > 0) {
                            ItemMonitor.startProgress(jobId);
                            intervalHandles.put(jobId, setInterval(o -> pollJob(job), POLLING_INTERVAL));
                        } else {
                            ItemMonitor.stopProgress(jobId);
                        }
                    }
                }));

        setItemRenderer(item -> new ItemDisplay<JobNode>() {
            @Override
            public String getId() {
                return Ids.job(item.getDeployment(), item.getSubdeployment(), item.getName());
            }

            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public HTMLElement asElement() {
                return ItemDisplay.withSubtitle(item.getName(), item.getPath());
            }

            @Override
            public String getFilterData() {
                Set<String> data = new HashSet<>(asList(item.getName(), item.getDeployment()));
                if (item.getSubdeployment() != null) {
                    data.add(item.getSubdeployment());
                }
                if (item.getInstanceCount() > 0) {
                    item.getExecutions().stream()
                            .map(e -> e.getBatchStatus().name())
                            .forEach(data::add);
                }
                return String.join(" ", data);
            }

            @Override
            public HTMLElement getIcon() {
                if (item.getInstanceCount() == 0) {
                    return Icons.info();
                } else if (item.hasExecutions(EnumSet.of(BatchStatus.FAILED, BatchStatus.ABANDONED))) {
                    return Icons.warning();
                } else if (item.hasExecutions(BatchStatus.STOPPED)) {
                    return Icons.stopped();
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
                List<ItemAction<JobNode>> actions = new ArrayList<>();
                PlaceRequest.Builder builder = places.selectedServer(NameTokens.JOB)
                        .with(DEPLOYMENT, item.getDeployment());
                if (item.getSubdeployment() != null) {
                    builder.with(SUBDEPLOYMENT, item.getSubdeployment());
                }
                PlaceRequest placeRequest = builder.with(NAME, item.getName()).build();
                actions.add(itemActionFactory.view(placeRequest));
                actions.add(new ItemAction.Builder<JobNode>()
                        .title(resources.constants().start())
                        .constraint(Constraint.executable(BATCH_DEPLOYMENT_TEMPLATE, START_JOB))
                        .handler(itm -> startJob(itm))
                        .build());
                return actions;
            }
        });

        setPreviewCallback(itm -> new JobPreview(this, itm, finderPathFactory, places, resources));
    }

    private void pollJob(JobNode job) {
        Operation operation = new Operation.Builder(job.getAddress(), READ_ATTRIBUTE_OPERATION)
                .param(NAME, RUNNING_EXECUTIONS)
                .build();
        String jobId = Ids.job(job.getDeployment(), job.getSubdeployment(), job.getName());
        dispatcher.execute(operation,
                result -> {
                    if (result.asInt() == 0) {
                        ItemMonitor.stopProgress(jobId);
                        if (intervalHandles.containsKey(jobId)) {
                            double handle = intervalHandles.remove(jobId);
                            clearInterval(handle);
                        }
                        JobColumn.this.refresh(RESTORE_SELECTION);
                    }
                },
                (o, failure) -> ItemMonitor.stopProgress(jobId),
                (o, exception) -> ItemMonitor.stopProgress(jobId));
    }

    private void startJob(JobNode job) {
        int xmlNames = job.get(JOB_XML_NAMES).asList().size();
        Metadata metadata = metadataRegistry.lookup(BATCH_DEPLOYMENT_TEMPLATE);
        Metadata operationMetadata = metadata.forOperation(START_JOB);
        if (xmlNames > 1) {
            operationMetadata.getDescription()
                    .get(ATTRIBUTES)
                    .get(JOB_XML_NAME)
                    .get(ALLOWED).set(job.get(JOB_XML_NAMES));
        } else {
            operationMetadata.getDescription()
                    .get(ATTRIBUTES)
                    .get(JOB_XML_NAME)
                    .get(ACCESS_TYPE).set(READ_ONLY);
        }
        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.build(Ids.JOB, START_JOB), operationMetadata)
                .addOnly()
                .unsorted()
                .include(JOB_XML_NAME, PROPERTIES)
                .build();
        Dialog dialog = new Dialog.Builder(resources.constants().startJob())
                .add(form.asElement())
                .closeIcon(true)
                .closeOnEsc(true)
                .primary(resources.constants().start(), () -> {
                    if (form.save()) {
                        Operation operation = new Operation.Builder(job.getAddress().getParent(), START_JOB)
                                .payload(form.getModel())
                                .build();
                        dispatcher.execute(operation, result -> {
                            String xmlName = form.getModel().get(JOB_XML_NAME).asString();
                            long executionId = result.asLong();
                            MessageEvent.fire(eventBus,
                                    Message.success(resources.messages().startJobSuccess(xmlName, executionId)));
                            refresh(RESTORE_SELECTION);
                        });
                        return true;
                    }
                    return false;
                })
                .cancel()
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
        if (xmlNames == 1) {
            String xmlName = job.get(JOB_XML_NAMES).asList().get(0).asString();
            form.getFormItem(JOB_XML_NAME).setValue(xmlName);
        }
    }

    @Override
    public void detach() {
        super.detach();
        clearIntervals();
    }

    private void clearIntervals() {
        for (Double handle : intervalHandles.values()) {
            clearInterval(handle);
        }
        intervalHandles.clear();
    }
}
