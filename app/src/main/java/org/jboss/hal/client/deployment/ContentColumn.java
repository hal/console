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
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;

/**
 * Column used in domain mode to manage content in the content repository.
 *
 * @author Harald Pehl
 */
@Column(Ids.CONTENT)
public class ContentColumn extends FinderColumn<Content> {

    static String serverGroups(Content content) {
        return content.getAssignments().stream()
                .map(Assignment::getServerGroup)
                .sorted(naturalOrder())
                .collect(joining(" "));
    }

    @Inject
    public ContentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        super(new FinderColumn.Builder<Content>(finder, Ids.CONTENT, resources.constants().content())

                .columnAction(columnActionFactory.add(Ids.CONTENT_ADD,
                        resources.constants().content(), column -> Browser.getWindow().alert(Names.NYI)))

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

                .withFilter()
        );

        setItemRenderer(item -> new ItemDisplay<Content>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public Element asElement() {
                if (!item.getAssignments().isEmpty()) {
                    String serverGroups = item.getAssignments().stream()
                            .map(Assignment::getServerGroup)
                            .collect(joining(", "));
                    return ItemDisplay.withSubtitle(item.getName(), serverGroups);
                } else {
                    return ItemDisplay.withSubtitle(item.getName(), resources.constants().unassigned());
                }
            }

            @Override
            public String getTooltip() {
                if (item.getAssignments().isEmpty()) {
                    return resources.constants().unassigned();
                } else {
                    String serverGroups = item.getAssignments().stream()
                            .map(Assignment::getServerGroup)
                            .collect(joining(", "));
                    return resources.messages().assignedTo(serverGroups);
                }
            }

            @Override
            public Element getIcon() {
                return item.getAssignments().isEmpty() ? Icons.disabled() : Icons.info();
            }

            @Override
            public String getFilterData() {
                if (!item.getAssignments().isEmpty()) {
                    String serverGroups = item.getAssignments().stream()
                            .map(Assignment::getServerGroup)
                            .collect(joining(" "));
                    return getTitle() + " " + serverGroups;
                }
                return getTitle() + " " + resources.constants().unassigned();
            }

            @Override
            public List<ItemAction<Content>> actions() {
                List<ItemAction<Content>> actions = new ArrayList<>();
                actions.add(new ItemAction<>(resources.constants().assign(), content -> assign(content)));

                if (item.getAssignments().isEmpty()) {
                    actions.add(new ItemAction<>(resources.constants().remove(), content ->
                            DialogFactory.showConfirmation(
                                    resources.messages().removeResourceConfirmationTitle(item.getName()),
                                    resources.messages().removeResourceConfirmationQuestion(item.getName()),
                                    () -> {
                                        ResourceAddress address = new ResourceAddress()
                                                .add(DEPLOYMENT, item.getName());
                                        Operation operation = new Operation.Builder(REMOVE, address).build();
                                        dispatcher.execute(operation, result -> {
                                            MessageEvent.fire(eventBus, Message.success(resources.messages()
                                                    .removeResourceSuccess(resources.constants().content(),
                                                            item.getName())));
                                            refresh(CLEAR_SELECTION);
                                        });
                                    })));
                } else {
                    actions.add(new ItemAction<>(resources.constants().unassign(),
                            itm -> Browser.getWindow().alert(Names.NYI)));
                }
                return actions;
            }
        });

        setPreviewCallback(item -> new ContentPreview(ContentColumn.this, item, resources));

        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.upload(this, dispatcher, eventBus, progress, resources,
                    event.dataTransfer.files));
        }
    }

    void assign(Content content) {
        Browser.getWindow().alert(Names.NYI);
    }
}
