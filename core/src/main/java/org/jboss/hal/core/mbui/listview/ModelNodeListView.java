/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.mbui.listview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.CSSProperties.MarginTopUnionType;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Pager;
import org.jboss.hal.ballroom.Toolbar;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.dataprovider.Display;
import org.jboss.hal.ballroom.dataprovider.PageInfo;
import org.jboss.hal.ballroom.dataprovider.SelectionInfo;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemRenderer;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.Core;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jetbrains.annotations.NonNls;

import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.MARGIN_BIG;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.resources.CSS.vh;

/**
 * A list view for model nodes with a toolbar, pager and empty states. Actions are filtered according to their
 * constraints.
 *
 * <p>Please note that the {@code ModelNodeListView} uses its own {@code <div class="row"/>} element. This is important
 * if you add the toolbar using the methods from {@link org.jboss.hal.ballroom.LayoutBuilder}</p>
 */
public class ModelNodeListView<T extends ModelNode> implements Display<T>, Iterable<HTMLElement>, Attachable {

    private static final String NO_ITEMS = "org.jboss.hal.core.mbui.listview.NoItems";
    private static final String NO_MATCHING_ITEMS = "org.jboss.hal.core.mbui.listview.NoMatchingItems";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final DataProvider<T> dataProvider;
    private final Toolbar<T> toolbar;
    private final ListView<T> listView;
    private final Pager<T> pager;
    private final Iterable<HTMLElement> elements;
    private final Map<String, HTMLElement> emptyStates;
    private int surroundingHeight;

    private ModelNodeListView(Builder<T> builder) {
        this.dataProvider = builder.dataProvider;

        // toolbar
        Environment environment = Core.INSTANCE.environment();
        List<Toolbar.Action> allowedActions = builder.toolbarActions.stream()
                .filter(action -> AuthorisationDecision.from(environment,
                        builder.metadata.getSecurityContext()).isAllowed(action.getConstraints()))
                .collect(toList());
        toolbar = new Toolbar<>(dataProvider, builder.toolbarAttributes, allowedActions);

        // list view
        listView = new ListView<T>(builder.id, dataProvider, builder.itemRenderer, builder.stacked,
                builder.multiSelect) {
            @Override
            protected List<ItemAction<T>> allowedActions(List<ItemAction<T>> actions) {
                return actions.stream()
                        .filter(action -> AuthorisationDecision.from(environment,
                                builder.metadata.getSecurityContext()).isAllowed(action.getConstraints()))
                        .collect(toList());
            }
        };

        // pager
        pager = new Pager<>(dataProvider);

        // empty states
        emptyStates = new HashMap<>();
        builder.emptyStates.get(NO_MATCHING_ITEMS).setPrimaryAction(CONSTANTS.clearAllFilters(),
                toolbar::clearAllFilters);
        builder.emptyStates.forEach((key, emptyState) -> {
            HTMLElement element = emptyState.element();
            element.style.marginTop = MarginTopUnionType.of(MARGIN_BIG + "px"); //NON-NLS
            emptyStates.put(key, element);
        });
        HTMLElement emptyStatesContainer = div().get();
        for (HTMLElement element : emptyStates.values()) {
            emptyStatesContainer.appendChild(element);
        }

        // root elements
        elements = Elements.collect()
                .add(toolbar)
                .add(row()
                        .add(column()
                                .addAll(listView.element(), emptyStatesContainer)))
                .add(pager)
                .get();
        surroundingHeight = 0;

        // wire displays
        dataProvider.addDisplay(toolbar);
        dataProvider.addDisplay(listView);
        dataProvider.addDisplay(pager);
        dataProvider.addDisplay(this);
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        return elements.iterator();
    }

    @Override
    public void attach() {
        adjustHeight();
        if (toolbar != null) {
            toolbar.attach();
        }
    }

    @Override
    public void detach() {
        if (toolbar != null) {
            toolbar.detach();
        }
    }

    private void adjustHeight() {
        int toolbarHeight = (int) (toolbar.element().offsetHeight);
        int pagerHeight = (int) pager.element().offsetHeight;
        listView.element().style.height = vh(
                applicationOffset() + toolbarHeight + pagerHeight + surroundingHeight + 2);
        listView.element().style.overflow = "scroll"; //NON-NLS
    }

    /**
     * Use this method if you embed the list view into an application view and if you have additional elements
     * before or after the list view. This method should be called before the list view is attached.
     *
     * @param surroundingHeight the sum of the height of all surrounding elements
     */
    @SuppressWarnings("unused")
    public void setSurroundingHeight(int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
        adjustHeight();
    }

    @Override
    public void showItems(Iterable<T> items, PageInfo pageInfo) {
        if (pageInfo.getTotal() == 0) {
            if (dataProvider.hasFilters()) {
                showEmptyState(NO_MATCHING_ITEMS);
                Elements.setVisible(toolbar.element(), true);
            } else {
                showEmptyState(NO_ITEMS);
            }

        } else {
            hideEmptyStates();
            Elements.setVisible(toolbar.element(), true);
            Elements.setVisible(pager.element(), pageInfo.getPages() > 1);
        }
        adjustHeight();
    }

    @Override
    public void updateSelection(SelectionInfo selectionInfo) {
        // already covered by listView
    }

    public void showEmptyState(String name) {
        if (emptyStates.containsKey(name)) {
            Elements.setVisible(toolbar.element(), false);
            Elements.setVisible(listView.element(), false);
            Elements.setVisible(pager.element(), false);
            emptyStates.forEach((n, element) -> Elements.setVisible(element, n.equals(name)));
        }
    }

    private void hideEmptyStates() {
        for (HTMLElement element : emptyStates.values()) {
            Elements.setVisible(element, false);
        }
        Elements.setVisible(listView.element(), true);
    }


    public static class Builder<T extends ModelNode> {

        private final String id;
        private final Metadata metadata;
        private final List<Toolbar.Attribute<T>> toolbarAttributes;
        private final List<Toolbar.Action> toolbarActions;
        private final DataProvider<T> dataProvider;
        private final ItemRenderer<T> itemRenderer;
        private final Map<String, EmptyState> emptyStates;
        private boolean multiSelect;
        private boolean stacked;

        public Builder(@NonNls String id, Metadata metadata, DataProvider<T> dataProvider,
                ItemRenderer<T> itemRenderer) {
            this.id = id;
            this.metadata = metadata;
            this.dataProvider = dataProvider;
            this.itemRenderer = itemRenderer;
            this.toolbarAttributes = new ArrayList<>();
            this.toolbarActions = new ArrayList<>();
            this.emptyStates = new HashMap<>();
            this.multiSelect = false;
            this.stacked = true;

            emptyStates.put(NO_ITEMS, new EmptyState.Builder(Ids.build(id, Ids.EMPTY), CONSTANTS.noItems())
                    .description(MESSAGES.noItems())
                    .build());
            emptyStates.put(NO_MATCHING_ITEMS,
                    new EmptyState.Builder(Ids.build(id, Ids.NO_MATCH), CONSTANTS.noMatchingItems())
                            .description(MESSAGES.noMatchingItems())
                            .build());
        }

        public Builder<T> stacked(boolean stacked) {
            this.stacked = stacked;
            return this;
        }

        public Builder<T> multiSelect(boolean multiSelect) {
            this.multiSelect = multiSelect;
            return this;
        }

        public Builder<T> toolbarAttribute(Toolbar.Attribute<T> attribute) {
            toolbarAttributes.add(attribute);
            return this;
        }

        public Builder<T> toolbarAction(Toolbar.Action action) {
            toolbarActions.add(action);
            return this;
        }

        public Builder<T> noItems(String header) {
            return noItems(header, null);
        }

        public Builder<T> noItems(String header, SafeHtml description) {
            emptyStates.get(NO_ITEMS).setHeader(header);
            emptyStates.get(NO_ITEMS).setDescription(description);
            return this;
        }

        public Builder<T> noMatchingItems(String header) {
            return noMatchingItems(header, null);
        }

        public Builder<T> noMatchingItems(String header, SafeHtml description) {
            emptyStates.get(NO_MATCHING_ITEMS).setHeader(header);
            emptyStates.get(NO_MATCHING_ITEMS).setDescription(description);
            return this;
        }

        public Builder<T> emptyState(String name, EmptyState emptyState) {
            emptyStates.put(name, emptyState);
            return this;
        }

        public ModelNodeListView<T> build() {
            return new ModelNodeListView<>(this);
        }
    }
}