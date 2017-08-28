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
package org.jboss.hal.client.runtime.subsystem.web;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.deployment.DeploymentResource;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mbui.form.OperationFormBuilder;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.MEDIUM;
import static org.jboss.hal.client.runtime.subsystem.web.AddressTemplates.WEB_DEPLOYMENT_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.web.AddressTemplates.WEB_SUBDEPLOYMENT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@AsyncColumn(Ids.UNDERTOW_RUNTIME_DEPLOYMENT)
public class DeploymentsColumn extends FinderColumn<DeploymentResource> {

    /**
     *  The regular Dialogs in DialogFactory uses button with a simple Callback that closes the dialog, even when
     *  the form contains errors as "required fields" not set. This custom dialog uses a <code>Dialog.ResultCallback</code>
     *  that returns a boolean, thus the dialog is closed only if there are no form errors.
     */
    private static Dialog buildConfirmation(Resources resources, String title, SafeHtml question, HTMLElement element,
            Dialog.ResultCallback confirm) {
        HTMLElement content;
        if (element != null) {
            content = div()
                    .add(p().innerHtml(question))
                    .add(element)
                    .asElement();
        } else {
            content = p().innerHtml(question).asElement();
        }

        return new Dialog.Builder(title)
                .primary(resources.constants().invalidate(), confirm)
                .secondary(resources.constants().cancel(), null)
                .size(MEDIUM)
                .add(content)
                .build();
    }

    private Dispatcher dispatcher;
    private EventBus eventBus;
    private Resources resources;
    private Provider<Progress> progress;
    private MetadataProcessor metadataProcessor;

    @Inject
    public DeploymentsColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final ItemActionFactory itemActionFactory,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final Places places,
            final Resources resources,
            @Footer final Provider<Progress> progress,
            final MetadataProcessor metadataProcessor,
            final StatementContext statementContext) {

        super(new Builder<DeploymentResource>(finder, Ids.UNDERTOW_RUNTIME_DEPLOYMENT, Names.DEPLOYMENT)
                .columnAction(columnActionFactory.refresh(Ids.UNDERTOW_RUNTIME_REFRESH))
                .itemsProvider((context, callback) -> {
                    ResourceAddress addressDeploy = WEB_DEPLOYMENT_TEMPLATE.resolve(statementContext);
                    Operation operationDeploy = new Operation.Builder(addressDeploy, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    ResourceAddress addressSubdeploy = WEB_SUBDEPLOYMENT_TEMPLATE.resolve(statementContext);
                    Operation operationSubDeploy = new Operation.Builder(addressSubdeploy, READ_RESOURCE_OPERATION)
                            .param(INCLUDE_RUNTIME, true)
                            .build();
                    dispatcher.execute(new Composite(operationDeploy, operationSubDeploy), (CompositeResult result) -> {
                        List<DeploymentResource> deployments = new ArrayList<>();
                        result.step(0).get(RESULT).asList().forEach(r -> {
                            ResourceAddress _address = new ResourceAddress(r.get(ADDRESS));
                            deployments.add(new DeploymentResource(_address, r.get(RESULT)));
                        });
                        result.step(1).get(RESULT).asList().forEach(r -> {
                            ResourceAddress _address = new ResourceAddress(r.get(ADDRESS));
                            deployments.add(new DeploymentResource(_address, r.get(RESULT)));
                        });
                        callback.onSuccess(deployments);
                    });
                })
                .onPreview(item -> new DeploymentPreview(dispatcher, item)));

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.resources = resources;
        this.progress = progress;
        this.metadataProcessor = metadataProcessor;

        setItemRenderer(item -> new ItemDisplay<DeploymentResource>() {
            @Override
            public String getId() {
                return Ids.asId(item.getPath());
            }

            @Override
            public String getTitle() {
                return item.getPath();
            }

            //@Override
            //public String nextColumn() {
            //    return Ids.UNDERTOW_RUNTIME_MODCLUSTER_BALANCER;
            //}


            @Override
            public List<ItemAction<DeploymentResource>> actions() {
                List<ItemAction<DeploymentResource>> actions = new ArrayList<>();
                actions.add(new ItemAction.Builder<DeploymentResource>()
                        .title(resources.constants().invalidateSession())
                        .constraint(Constraint.executable(WEB_DEPLOYMENT_TEMPLATE, INVALIDATE_SESSION_OPERATION))
                        .handler(item1 -> invalidateSession(item))
                        .build());
                actions.add(itemActionFactory.view(places.selectedProfile(NameTokens.UNDERTOW_RUNTIME_DEPLOYMENT_VIEW)
                        .with(DEPLOYMENT, item.getDeployment())
                        .with(SUBDEPLOYMENT, item.getSubdeployment())
                        .build()));
                return actions;
            }
        });
    }

    private void invalidateSession(final DeploymentResource item) {

        metadataProcessor.lookup(WEB_DEPLOYMENT_TEMPLATE, progress.get(),
                new MetadataProcessor.MetadataCallback() {
                    @Override
                    public void onMetadata(Metadata metadata) {
                        String id = Ids.build(INVALIDATE_SESSION_OPERATION, Ids.FORM_SUFFIX);
                        Form<ModelNode> form = new OperationFormBuilder<>(id, metadata, INVALIDATE_SESSION_OPERATION)
                                .build();

                        // uses a custom dialog that only closes a dialog if there are no form errors
                        Dialog dialog = buildConfirmation(resources,
                                resources.messages().invalidateSessionTitle(),
                                resources.messages().invalidateSessionQuestion(),
                                form.asElement(), () -> {
                                    boolean formOk = form.save();
                                    if (formOk) {
                                        String sessionId = form.<String>getFormItem("session-id").getValue();
                                        Operation operation = new Operation.Builder(item.getAddress(),
                                                INVALIDATE_SESSION_OPERATION)
                                                .param("session-id", sessionId)
                                                .build();
                                        dispatcher.execute(operation, result -> {
                                            if (result.asBoolean()) {
                                                MessageEvent.fire(eventBus, Message.success(
                                                        resources.messages().invalidateSessionSuccess(sessionId)));
                                            } else {
                                                MessageEvent.fire(eventBus, Message.warning(
                                                        resources.messages().invalidateSessionNotExist(sessionId)));
                                            }
                                        }, (operation1, failure) -> MessageEvent.fire(eventBus, Message.error(
                                                resources.messages().invalidateSessionError(sessionId, failure))));
                                    }
                                    return formOk;
                                });
                        dialog.show();
                        form.edit(new ModelNode());
                    }

                    @Override
                    public void onError(final Throwable error) {
                        MessageEvent
                                .fire(eventBus,
                                        Message.error(resources.messages().metadataError(), error.getMessage()));
                    }
                });
    }

}
