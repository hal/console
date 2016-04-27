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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.core.finder.ColumnAction;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.ItemAction;
import org.jboss.hal.core.finder.ItemDisplay;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Footer;

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

        super(new FinderColumn.Builder<Content>(finder, Ids.CONTENT_COLUMN,
                resources.constants().content())

                .columnAction(columnActionFactory.add(IdBuilder.build(Ids.CONTENT_COLUMN, "add"),
                        resources.constants().contentRepository(), column -> Browser.getWindow().alert(Names.NYI)))
                .columnAction(new ColumnAction<>(IdBuilder.build(Ids.CONTENT_COLUMN, "filter"),
                        new Elements.Builder().span().css(CSS.fontAwesome("chain-broken"))
                                .title(resources.constants().unassignedContentOnly())
                                .data(UIConstants.TOGGLE, UIConstants.TOOLTIP)
                                .data(UIConstants.PLACEMENT, "bottom")
                                .end().<Element>build(),
                        column -> Browser.getWindow().alert(Names.NYI)))

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

                .itemRenderer(item -> new ItemDisplay<Content>() {
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
                                    .join(Joiner.on(", "));
                            return getTitle() + " " + serverGroups;
                        }
                        return getTitle();
                    }

                    @Override
                    public List<ItemAction<Content>> actions() {
                        List<ItemAction<Content>> actions = new ArrayList<>();
                        actions.add(new ItemAction<>(resources.constants().assign(),
                                itm -> Browser.getWindow().alert(Names.NYI)));

                        if (item.getAssignments().isEmpty()) {
                            actions.add(new ItemAction<>(resources.constants().remove(),
                                    itm -> Browser.getWindow().alert(Names.NYI)));
                        } else {
                            actions.add(new ItemAction<>(resources.constants().unassign(),
                                    itm -> Browser.getWindow().alert(Names.NYI)));
                        }
                        return actions;
                    }
                })

                .withFilter()
                .onPreview(item -> new ContentPreview(item, resources))
        );
    }
}
