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
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.SpanElement;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.client.deployment.wizard.ContentContext;
import org.jboss.hal.client.deployment.wizard.ContentState;
import org.jboss.hal.client.deployment.wizard.NamesStep;
import org.jboss.hal.client.deployment.wizard.UploadContentStep;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.CompositeResult;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.Requires;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jboss.hal.client.deployment.ContentColumn.DEPLOYMENT_ADDRESS;
import static org.jboss.hal.client.deployment.wizard.ContentState.NAMES;
import static org.jboss.hal.client.deployment.wizard.ContentState.UPLOAD;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.spi.MessageEvent.fire;

/**
 * Column used in domain mode to manage content in the content repository.
 *
 * @author Harald Pehl
 */
@Column(Ids.CONTENT)
@Requires(DEPLOYMENT_ADDRESS)
public class ContentColumn extends FinderColumn<Content> {

    static final String DEPLOYMENT_ADDRESS = "/deployment=*";
    static final AddressTemplate DEPLOYMENT_TEMPLATE = AddressTemplate.of(DEPLOYMENT_ADDRESS);

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final MetadataRegistry metadataRegistry;
    private final Resources resources;

    @Inject
    public ContentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Environment environment,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final Places places,
            @Footer final Provider<Progress> progress,
            final MetadataRegistry metadataRegistry,
            final Resources resources) {

        super(new FinderColumn.Builder<Content>(finder, Ids.CONTENT, resources.constants().content())

                .itemsProvider((context, callback) -> {
                    final Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                        @Override
                        public void onFailure(final FunctionContext context) {
                            callback.onFailure(context.getError());
                        }

                        @Override
                        public void onSuccess(final FunctionContext context) {
                            List<Content> content = context.pop();
                            callback.onSuccess(content);
                        }
                    };
                    new Async<FunctionContext>(progress.get()).single(new FunctionContext(), outcome,
                            new DeploymentFunctions.LoadContentAssignments(dispatcher));
                })

                .withFilter());

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;

        List<ColumnAction<Content>> addActions = new ArrayList<>();
        addActions.add(new ColumnAction<>(Ids.CONTENT_ADD_MANAGED,
                resources.constants().uploadContent(),
                column -> addContent()));
        addActions.add(new ColumnAction<>(Ids.CONTENT_ADD_UNMANAGED,
                resources.messages().addResourceTitle(Names.UNMANAGED_DEPLOYMENT),
                column -> addUnmanaged()));
        addColumnActions(Ids.CONTENT_ADD_ACTIONS, pfIcon("add-circle-o"), resources.constants().add(), addActions);
        addColumnAction(columnActionFactory.refresh(Ids.CONTENT_REFRESH));
        setPreviewCallback(item -> new ContentPreview(this, item, places, resources));

        setItemRenderer(item -> new ItemDisplay<Content>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public String getTooltip() {
                return String.join(", ",
                        item.isExploded() ? resources.constants().exploded() : resources.constants().archived(),
                        item.isManaged() ? resources.constants().managed() : resources.constants().unmanaged());
            }

            @Override
            public Element getIcon() {
                String icon = item.isExploded() ? fontAwesome("folder-open") : fontAwesome("archive");
                SpanElement spanElement = Browser.getDocument().createSpanElement();
                spanElement.setClassName(icon);
                return spanElement;
            }

            @Override
            public String getFilterData() {
                String status = String.join(" ",
                        item.isExploded() ? resources.constants().exploded() : resources.constants().archived(),
                        item.isManaged() ? resources.constants().managed() : resources.constants().unmanaged());
                String assignments = item.getAssignments().isEmpty()
                        ? resources.constants().unassigned()
                        : item.getAssignments().stream().map(Assignment::getServerGroup).collect(joining(" "));
                return getTitle() + " " + status + " " + assignments;
            }

            @Override
            public List<ItemAction<Content>> actions() {
                List<ItemAction<Content>> actions = new ArrayList<>();

                // order is: assign, (explode), replace, unassign / remove
                actions.add(new ItemAction<>(resources.constants().assign(), itm -> assign(itm)));
                if (item.getAssignments().isEmpty() && !item.isExploded()) {
                    actions.add(new ItemAction<>(resources.constants().explode(), itm -> explode(itm)));
                }
                actions.add(new ItemAction<>(resources.constants().replace(), itm -> replace(itm)));
                if (item.getAssignments().isEmpty()) {
                    actions.add(new ItemAction<>(resources.constants().remove(), itm -> remove(itm)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().unassign(), itm -> unassign(itm)));
                }
                return actions;
            }
        });

        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.upload(this, environment, dispatcher, eventBus, progress, resources,
                    event.dataTransfer.files));
        }
    }

    private void addContent() {
        Metadata metadata = metadataRegistry.lookup(DEPLOYMENT_TEMPLATE);
        Wizard<ContentContext, ContentState> wizard = new Wizard.Builder<ContentContext, ContentState>(
                resources.messages().addResourceTitle(resources.constants().content()), new ContentContext())

                .addStep(ContentState.UPLOAD, new UploadContentStep(resources))
                .addStep(ContentState.NAMES, new NamesStep(metadata, resources))

                .onBack((context, currentState) -> currentState == NAMES ? UPLOAD : null)
                .onNext((context, currentState) -> currentState == UPLOAD ? NAMES : null)

                .stayOpenAfterFinish()
                .onFinish((wzd, context) -> {
                    wzd.showProgress(resources.constants().uploadInProgress(),
                            resources.messages().uploadInProgress(context.names.getName()));
                    Browser.getWindow().alert("Simulating upload...");
                    wzd.showSuccess(resources.constants().uploadSuccessful(),
                            resources.messages().uploadSuccessful(context.names.getName()));
                })
                .build();
        wizard.show();
    }

    private void addUnmanaged() {
        Browser.getWindow().alert(Names.NYI);
    }

    private void replace(Content content) {
        Browser.getWindow().alert(Names.NYI);
    }

    private void explode(Content content) {
        Operation operation = new Operation.Builder("explode", contentAddress(content)).build();
        dispatcher.execute(operation, result -> {
            refresh(RESTORE_SELECTION);
            MessageEvent
                    .fire(eventBus, Message.success(resources.messages().deploymentExploded(content.getName())));
        });
    }

    void assign(Content content) {
        Operation operation = new Operation.Builder(READ_CHILDREN_NAMES_OPERATION, ResourceAddress.ROOT)
                .param(CHILD_TYPE, SERVER_GROUP)
                .build();
        dispatcher.execute(operation, result -> {
            Set<String> unassignedServerGroups = result.asList().stream()
                    .map(ModelNode::asString)
                    .collect(toSet());
            Set<String> assignedServerGroups = content.getAssignments().stream()
                    .map(Assignment::getServerGroup)
                    .collect(toSet());
            unassignedServerGroups.removeAll(assignedServerGroups);

            if (unassignedServerGroups.isEmpty()) {
                MessageEvent.fire(eventBus, Message.warning(
                        resources.messages().contentAlreadyAssignedToAllServerGroups(content.getName())));

            } else {
                new AssignContentDialog(content, unassignedServerGroups, resources, (cnt, serverGroups, enable) -> {
                    List<Operation> operations = serverGroups.stream()
                            .map(serverGroup -> {
                                ResourceAddress resourceAddress = new ResourceAddress()
                                        .add(SERVER_GROUP, serverGroup)
                                        .add(DEPLOYMENT, content.getName());
                                return new Operation.Builder(ADD, resourceAddress)
                                        .param(RUNTIME_NAME, content.getRuntimeName())
                                        .param(ENABLED, enable)
                                        .build();
                            })
                            .collect(toList());
                    dispatcher.execute(new Composite(operations), (CompositeResult cr) -> {
                        refresh(RESTORE_SELECTION);
                        MessageEvent.fire(eventBus,
                                Message.success(resources.messages().contentAssigned(content.getName())));
                    });
                }).show();
            }
        });
    }

    private void unassign(Content content) {
        if (!content.getAssignments().isEmpty()) {
            Set<String> assignedServerGroups = content.getAssignments().stream()
                    .map(Assignment::getServerGroup)
                    .collect(toSet());
            new AssignContentDialog(content, assignedServerGroups, resources, (cnt, serverGroups) -> {
                List<Operation> operations = serverGroups.stream()
                        .map(serverGroup -> {
                            ResourceAddress resourceAddress = new ResourceAddress()
                                    .add(SERVER_GROUP, serverGroup)
                                    .add(DEPLOYMENT, content.getName());
                            return new Operation.Builder(REMOVE, resourceAddress).build();
                        })
                        .collect(toList());
                dispatcher.execute(new Composite(operations), (CompositeResult cr) -> {
                    refresh(RESTORE_SELECTION);
                    MessageEvent.fire(eventBus,
                            Message.success(resources.messages().contentUnassigned(content.getName())));
                });
            }).show();

        } else {
            MessageEvent.fire(eventBus, Message.warning(resources.messages().unassignedContent(content.getName())));
        }
    }

    void unassign(Content content, String serverGroup) {
        ResourceAddress address = new ResourceAddress().add(SERVER_GROUP, serverGroup)
                .add(DEPLOYMENT, content.getName());
        Operation operation = new Operation.Builder(REMOVE, address).build();
        dispatcher.execute(operation, result -> {
            refresh(RESTORE_SELECTION);
            fire(eventBus,
                    Message.success(resources.messages().deploymentUnassigned(content.getName(), serverGroup)));
        });
    }

    private void remove(Content content) {
        DialogFactory.showConfirmation(
                resources.messages().removeResourceConfirmationTitle(content.getName()),
                resources.messages().removeResourceConfirmationQuestion(content.getName()),
                () -> {
                    ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
                    Operation operation = new Operation.Builder(REMOVE, address).build();
                    dispatcher.execute(operation, result -> {
                        fire(eventBus, Message.success(resources.messages()
                                .removeResourceSuccess(resources.constants().content(),
                                        content.getName())));
                        refresh(CLEAR_SELECTION);
                    });
                });
    }

    private ResourceAddress contentAddress(Content content) {
        return new ResourceAddress().add(DEPLOYMENT, content.getName());
    }
}
