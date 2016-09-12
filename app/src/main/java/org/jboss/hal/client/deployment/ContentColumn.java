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
import elemental.html.SpanElement;
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
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * Column used in domain mode to manage content in the content repository.
 *
 * @author Harald Pehl
 */
@Column(Ids.CONTENT)
public class ContentColumn extends FinderColumn<Content> {

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Resources resources;

    @Inject
    public ContentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            final Places places,
            @Footer final Provider<Progress> progress,
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
        this.resources = resources;

        addColumnAction(columnActionFactory.add(Ids.CONTENT_ADD, resources.constants().content(), column -> add()));
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
                actions.add(new ItemAction<>(resources.constants().assign(), content -> assign(content)));
                actions.add(new ItemAction<>(resources.constants().replace(), content -> replace(content)));

                if (item.getAssignments().isEmpty()) {
                    actions.add(new ItemAction<>(resources.constants().remove(), itm -> remove(itm)));
                } else {
                    actions.add(new ItemAction<>(resources.constants().unassign(), itm -> unassign(itm)));
                }
                return actions;
            }
        });

        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.upload(this, dispatcher, eventBus, progress, resources,
                    event.dataTransfer.files));
        }
    }

    private void add() {
        Browser.getWindow().alert(Names.NYI);
    }

    private void replace(Content content) {
        Browser.getWindow().alert(Names.NYI);
    }

    void assign(Content content) {
        Browser.getWindow().alert(Names.NYI);
    }

    private void unassign(Content content) {
        Browser.getWindow().alert(Names.NYI);
    }

    void unassign(Content content, String serverGroup) {
        Browser.getWindow().alert(Names.NYI);
    }

    private void remove(Content content) {
        DialogFactory.showConfirmation(
                resources.messages().removeResourceConfirmationTitle(content.getName()),
                resources.messages().removeResourceConfirmationQuestion(content.getName()),
                () -> {
                    ResourceAddress address = new ResourceAddress().add(DEPLOYMENT, content.getName());
                    Operation operation = new Operation.Builder(REMOVE, address).build();
                    dispatcher.execute(operation, result -> {
                        MessageEvent.fire(eventBus, Message.success(resources.messages()
                                .removeResourceSuccess(resources.constants().content(),
                                        content.getName())));
                        refresh(CLEAR_SELECTION);
                    });
                });
    }
}
