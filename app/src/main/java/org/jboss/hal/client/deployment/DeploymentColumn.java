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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.client.deployment.Deployment.Status;
import org.jboss.hal.client.runtime.server.Server;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * Column used in domain *and* standalone mode to manage deployments.
 * TODO Add support for domain mode
 *
 * @author Harald Pehl
 */
@Column(ModelDescriptionConstants.DEPLOYMENT)
public class DeploymentColumn extends FinderColumn<Deployment> {

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Provider<Progress> progress;
    private final Resources resources;

    @Inject
    public DeploymentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        super(new Builder<Deployment>(finder, ModelDescriptionConstants.DEPLOYMENT, Names.DEPLOYMENT)

                .columnAction(columnActionFactory.add(IdBuilder.build(Ids.CONTENT_COLUMN, "add"),
                        resources.constants().content(), column -> Browser.getWindow().alert(Names.NYI)))

                .itemsProvider((context, callback) -> {
                    Operation operation = new Operation.Builder(READ_CHILDREN_RESOURCES_OPERATION,
                            ResourceAddress.ROOT)
                            .param(CHILD_TYPE, DEPLOYMENT)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.execute(operation, result -> {
                        List<Deployment> deployments = result.asPropertyList().stream()
                                .map(property -> new Deployment(Server.STANDALONE, property.getValue()))
                                .collect(toList());
                        callback.onSuccess(deployments);
                    });
                })

                .useFirstActionAsBreadcrumbHandler()
                .withFilter()
        );

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.resources = resources;

        setItemRenderer(item -> new ItemDisplay<Deployment>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public Element getIcon() {
                if (item.getStatus() == Status.FAILED) {
                    return Icons.error();
                } else {
                    return item.isEnabled() ? Icons.ok() : Icons.disabled();
                }
            }

            @Override
            public String getFilterData() {
                return item.getName() + " " + (item.isEnabled() ? ENABLED : DISABLED);
            }

            @Override
            public List<ItemAction<Deployment>> actions() {
                List<ItemAction<Deployment>> actions = new ArrayList<>();
                if (item.isEnabled()) {
                    actions.add(new ItemAction<>(resources.constants().disable(), deployment -> disable(deployment)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().enable(), deployment -> enable(deployment)));
                }
                actions.add(new ItemAction<>(resources.constants().remove(),
                        content -> {
                            DialogFactory.confirmation(
                                    resources.messages().removeResourceConfirmationTitle(item.getName()),
                                    resources.messages().removeResourceConfirmationQuestion(item.getName()),
                                    () -> {
                                        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, item.getName());
                                        Operation operation = new Operation.Builder(REMOVE, address).build();
                                        dispatcher.execute(operation, result -> refresh(CLEAR_SELECTION));
                                        return true;
                                    }).show();
                        }));
                return actions;
            }
        });

        setPreviewCallback(deployment -> new DeploymentPreview(DeploymentColumn.this, deployment, resources));
        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.upload(this, dispatcher, eventBus, progress, resources,
                    event.dataTransfer.files));
        }
    }

    void disable(Deployment deployment) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment.getName());
        Operation operation = new Operation.Builder("undeploy", address).build(); //NON-NLS
        dispatcher.execute(operation, result -> {
            MessageEvent.fire(eventBus, Message.success(resources.messages().deploymentDisabled(deployment.getName())));
            refresh(RESTORE_SELECTION);
        });
    }

    void enable(Deployment deployment) {
        ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, deployment.getName());
        Operation operation = new Operation.Builder("deploy", address).build(); //NON-NLS
        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                MessageEvent.fire(eventBus,
                        Message.error(resources.messages().deploymentEnabledError(deployment.getName()),
                                context.getErrorMessage()));
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                MessageEvent.fire(eventBus,
                        Message.success(resources.messages().deploymentEnabled(deployment.getName())));
                refresh(RESTORE_SELECTION);
            }
        };

        // execute using Async to make use of the progress bar
        new Async<FunctionContext>(progress.get()).single(new FunctionContext(), outcome,
                control -> dispatcher.executeInFunction(control, operation, result -> {
                    control.proceed();
                }));
    }
}
