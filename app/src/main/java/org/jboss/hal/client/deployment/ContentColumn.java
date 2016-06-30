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
import org.jboss.gwt.elemento.core.Elements;
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
import org.jboss.hal.resources.IdBuilder;
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
import static org.jboss.hal.resources.CSS.itemText;
import static org.jboss.hal.resources.CSS.subtitle;

/**
 * @author Harald Pehl
 */
@Column(Ids.CONTENT_COLUMN)
public class ContentColumn extends FinderColumn<Content> {

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
                    String serverGroups = item.getAssignments().stream()
                            .map(Assignment::getServerGroup)
                            .collect(joining(", "));
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
                    String serverGroups = item.getAssignments().stream()
                            .map(Assignment::getServerGroup)
                            .collect(joining(" "));
                    return getTitle() + " " + serverGroups;
                }
                return getTitle() + " unassigned"; //NON-NLS
            }

            @Override
            public List<ItemAction<Content>> actions() {
                List<ItemAction<Content>> actions = new ArrayList<>();
                actions.add(new ItemAction<>(resources.constants().assign(),
                        content -> Browser.getWindow().alert(Names.NYI)));

                if (item.getAssignments().isEmpty()) {
                    actions.add(new ItemAction<>(resources.constants().remove(),
                            content -> {
                                DialogFactory.confirmation(
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

        if (JsHelper.supportsAdvancedUpload()) {
            setOnDrop(event -> DeploymentFunctions.upload(this, dispatcher, eventBus, progress, resources,
                    event.dataTransfer.files));
        }
    }
}
