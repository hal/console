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

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.web.bindery.event.shared.EventBus;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.FileList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.ballroom.dialog.DialogFactory;
import org.jboss.hal.ballroom.dragndrop.DragEvent;
import org.jboss.hal.ballroom.js.JsHelper;
import org.jboss.hal.client.deployment.DeploymentFunctions.CheckDeployment;
import org.jboss.hal.client.deployment.DeploymentFunctions.UploadOrReplace;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.hal.client.deployment.DeploymentFunctions.UPLOAD_STATISTICS;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.CLEAR_SELECTION;
import static org.jboss.hal.core.finder.FinderColumn.RefreshMode.RESTORE_SELECTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REMOVE;
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.ondrag;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@Column(Ids.CONTENT_COLUMN)
public class ContentColumn extends FinderColumn<Content> {

    private static final Logger logger = LoggerFactory.getLogger(ContentColumn.class);

    private final Dispatcher dispatcher;
    private final EventBus eventBus;
    private final Provider<Progress> progress;
    private final Resources resources;

    @Inject
    public ContentColumn(final Finder finder,
            final ColumnActionFactory columnActionFactory,
            final Dispatcher dispatcher,
            final EventBus eventBus,
            @Footer final Provider<Progress> progress,
            final Resources resources) {

        super(new FinderColumn.Builder<Content>(finder, Ids.CONTENT_COLUMN, resources.constants().content())

                .columnAction(columnActionFactory.add(IdBuilder.build(Ids.CONTENT_COLUMN, "add"),
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
                .onPreview(item -> new ContentPreview(item, resources))
        );

        setItemRenderer(item -> new ItemDisplay<Content>() {
            @Override
            public String getTitle() {
                return item.getName();
            }

            @Override
            public Element asElement() {
                if (!item.getAssignments().isEmpty()) {
                    //noinspection Guava
                    String serverGroups = FluentIterable.from(item.getAssignments())
                            .transform(Assignment::getServerGroup)
                            .join(Joiner.on(", "));
                    return new Elements.Builder()
                            .span().css(itemText)
                            .span().textContent(item.getName()).end()
                            .start("small").css(subtitle).textContent(serverGroups).end()
                            .end().build();
                }
                return null;
            }

            @Override
            public String getFilterData() {
                if (!item.getAssignments().isEmpty()) {
                    //noinspection Guava
                    String serverGroups = FluentIterable.from(item.getAssignments())
                            .transform(Assignment::getServerGroup)
                            .join(Joiner.on(" "));
                    return getTitle() + " " + serverGroups;
                }
                return getTitle() + " unassigned"; //NON-NLS
            }

            @Override
            public List<ItemAction<Content>> actions() {
                List<ItemAction<Content>> actions = new ArrayList<>();
                actions.add(new ItemAction<>(resources.constants().assign(),
                        itm -> Browser.getWindow().alert(Names.NYI)));

                if (item.getAssignments().isEmpty()) {
                    actions.add(new ItemAction<>(resources.constants().remove(),
                            itm -> {
                                DialogFactory.confirmation(
                                        resources.messages().removeResourceConfirmationTitle(item.getName()),
                                        resources.messages().removeResourceConfirmationQuestion(item.getName()),
                                        () -> {
                                            ResourceAddress address = new ResourceAddress()
                                                    .add(DEPLOYMENT, item.getName());
                                            Operation operation = new Operation.Builder(REMOVE, address).build();
                                            dispatcher.execute(operation, result -> refresh(CLEAR_SELECTION));
                                            return true;
                                        }).show();
                            }));
                } else {
                    actions.add(new ItemAction<>(resources.constants().unassign(),
                            itm -> Browser.getWindow().alert(Names.NYI)));
                }
                return actions;
            }
        });

        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
        this.progress = progress;
        this.resources = resources;
        if (JsHelper.supportsAdvancedUpload()) {
            addDragAndDropSupport();
        }
    }

    private void addDragAndDropSupport() {
        EventListener noop = event -> {
            event.preventDefault();
            event.stopPropagation();
        };
        EventListener addDragIndicator = event -> {
            noop.handleEvent(event);
            ulElement().getClassList().add(ondrag);
        };
        EventListener removeDragIndicator = event -> {
            noop.handleEvent(event);
            ulElement().getClassList().remove(ondrag);
        };

        ulElement().setOndrag(noop);
        ulElement().setOndragstart(noop);

        ulElement().setOndragenter(addDragIndicator);
        ulElement().setOndragover(addDragIndicator);

        ulElement().setOndragleave(removeDragIndicator);
        ulElement().setOndragend(removeDragIndicator);

        ulElement().setOndrop(event -> {
            noop.handleEvent(event);
            removeDragIndicator.handleEvent(event);

            DragEvent dragEvent = (DragEvent) event;
            upload(dragEvent.dataTransfer.files);
        });
    }

    private void upload(FileList files) {
        if (files.getLength() > 0) {

            StringBuilder builder = new StringBuilder();
            List<Function> functions = new ArrayList<>();

            for (int i = 0; i < files.getLength(); i++) {
                String name = files.item(i).getName();
                builder.append(name).append(" ");
                functions.add(new CheckDeployment(dispatcher, name));
                functions.add(new UploadOrReplace(dispatcher, files.item(i), false));
            }

            logger.debug("About to upload {} file(s): {}", files.getLength(), builder.toString()); //NON-NLS
            final Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
                @Override
                public void onFailure(final FunctionContext context) {
                    // Should not happen since UploadOrReplace functions proceed also for errors and exceptions!
                    MessageEvent.fire(eventBus, Message.error(resources.constants().deploymentFailed()));
                }

                @Override
                public void onSuccess(final FunctionContext context) {
                    UploadStatistics statistics = context.get(UPLOAD_STATISTICS);
                    if (statistics != null) {
                        eventBus.fireEvent(new MessageEvent(statistics.getMessage()));
                    } else {
                        logger.error("Unable to find upload statistics in the context using key '{}'", //NON-NLS
                                UPLOAD_STATISTICS);
                    }
                    refresh(RESTORE_SELECTION);
                }
            };
            new Async<FunctionContext>(progress.get()).waterfall(new FunctionContext(), outcome,
                    functions.toArray(new Function[functions.size()]));
        }
    }
}
