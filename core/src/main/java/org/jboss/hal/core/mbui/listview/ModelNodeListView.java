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
package org.jboss.hal.core.mbui.listview;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import elemental2.dom.CSSProperties.MarginTopUnionType;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.builder.ElementsBuilder;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.listview.DataProvider;
import org.jboss.hal.ballroom.listview.Display;
import org.jboss.hal.ballroom.listview.ItemAction;
import org.jboss.hal.ballroom.listview.ItemRenderer;
import org.jboss.hal.ballroom.listview.ListView;
import org.jboss.hal.ballroom.toolbar.Toolbar;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.Core;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.security.AuthorisationDecision;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jetbrains.annotations.NonNls;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.ballroom.Skeleton.applicationOffset;
import static org.jboss.hal.resources.CSS.vh;

/**
 * A list view for model nodes with a toolbar and an empty state (if no items are available). Actions are filtered
 * according to their constraints.
 *
 * <p>Please note that the {@code ModelNodeListView} uses an own {@code <div class="row"/>} element. This is important
 * if you add the toolbar using the methods from {@link org.jboss.hal.ballroom.LayoutBuilder}</p>
 */
public class ModelNodeListView<T extends ModelNode> implements Display<T>, HasElements, Attachable {

    public static class Builder<T extends ModelNode> {

        private final String id;
        private final Metadata metadata;
        private final String[] contentWidths;
        private final List<Toolbar.Attribute<T>> toolbarAttributes;
        private final List<Toolbar.Action> toolbarActions;
        private final DataProvider<T> dataProvider;
        private final ItemRenderer<T> itemRenderer;
        private boolean multiselect;
        private boolean stacked;

        public Builder(@NonNls String id, Metadata metadata, DataProvider<T> dataProvider,
                ItemRenderer<T> itemRenderer) {
            this.id = id;
            this.metadata = metadata;
            this.dataProvider = dataProvider;
            this.itemRenderer = itemRenderer;
            this.contentWidths = new String[]{"60%", "40%"};
            this.toolbarAttributes = new ArrayList<>();
            this.toolbarActions = new ArrayList<>();
            this.multiselect = false;
            this.stacked = true;
        }

        public Builder<T> stacked(boolean stacked) {
            this.stacked = stacked;
            return this;
        }

        public Builder<T> multiselect(boolean multiselect) {
            this.multiselect = multiselect;
            return this;
        }

        public Builder<T> contentWidths(String main, String additional) {
            contentWidths[0] = main;
            contentWidths[1] = additional;
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

        public ModelNodeListView<T> build() {
            return new ModelNodeListView<>(this);
        }
    }


    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final Toolbar<T> toolbar;
    private final ListView<T> listView;
    private final EmptyState emptyState;
    private final ElementsBuilder elements;
    private int surroundingHeight;

    private ModelNodeListView(Builder<T> builder) {
        // toolbar
        Environment environment = Core.INSTANCE.environment();
        if (!builder.toolbarAttributes.isEmpty() || !builder.toolbarActions.isEmpty()) {
            List<Toolbar.Action> allowedActions = builder.toolbarActions.stream()
                    .filter(action -> AuthorisationDecision.from(environment,
                            builder.metadata.getSecurityContext()).isAllowed(action.getConstraints()))
                    .collect(toList());
            toolbar = new Toolbar<>(Ids.build(builder.id, "toolbar"), builder.dataProvider,
                    builder.toolbarAttributes, allowedActions);
        } else {
            toolbar = null;
        }

        // list view
        listView = new ListView<T>(builder.id, builder.itemRenderer, builder.stacked, builder.multiselect,
                builder.contentWidths) {
            @Override
            protected List<ItemAction<T>> allowedActions(List<ItemAction<T>> actions) {
                return actions.stream()
                        .filter(action -> AuthorisationDecision.from(environment,
                                builder.metadata.getSecurityContext()).isAllowed(action.getConstraints()))
                        .collect(toList());
            }
        };

        // empty state
        emptyState = new EmptyState.Builder("")
                .primaryAction(CONSTANTS.clearAllFilters(), () -> {
                    if (toolbar != null) {
                        toolbar.clearAllFilters();
                        toolbar.apply();
                    }
                })
                .build();
        emptyState.asElement().style.marginTop = MarginTopUnionType.of("20px");

        // root elements
        elements = Elements.elements();
        if (toolbar != null) {
            elements.add(toolbar)
                    .add(row()
                            .add(column()
                                    .addAll(listView, emptyState)));
        } else {
            elements.add(row()
                    .add(column()
                            .addAll(listView, emptyState)));
        }
        surroundingHeight = 0;

        // wire displays
        if (toolbar != null) {
            builder.dataProvider.addDisplay(toolbar);
        }
        builder.dataProvider.addDisplay(listView);
        builder.dataProvider.addDisplay(this);
    }

    @Override
    public Iterable<HTMLElement> asElements() {
        return elements.asElements();
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
        int toolbarHeight = toolbar != null ? (int) (toolbar.asElement().offsetHeight) : 0;
        listView.asElement().style.height = vh(applicationOffset() + toolbarHeight + surroundingHeight + 2);
        listView.asElement().style.overflow = "scroll"; //NON-NLS
    }

    /**
     * Use this method if you embed the list view into an application view and if you have additional elements
     * before or after the list view. This method should be called before the list view is attached.
     *
     * @param surroundingHeight the sum of the height of all surrounding elements
     */
    public void setSurroundingHeight(int surroundingHeight) {
        this.surroundingHeight = surroundingHeight;
        adjustHeight();
    }

    @Override
    public void showItems(Iterable<T> items, int visible, int total) {
        if (total == 0) {
            emptyState.setHeader(CONSTANTS.noItems());
            emptyState.showPrimaryAction(false);
            if (toolbar != null) {
                Elements.setVisible(toolbar.asElement(), false);
            }
            Elements.setVisible(listView.asElement(), false);
            Elements.setVisible(emptyState.asElement(), true);

        } else if (visible == 0) {
            emptyState.setHeader(CONSTANTS.noMatchingItems());
            emptyState.showPrimaryAction(true);
            if (toolbar != null) {
                Elements.setVisible(toolbar.asElement(), true);
            }
            Elements.setVisible(listView.asElement(), false);
            Elements.setVisible(emptyState.asElement(), true);

        } else {
            Elements.setVisible(emptyState.asElement(), false);
            if (toolbar != null) {
                Elements.setVisible(toolbar.asElement(), true);
            }
            Elements.setVisible(listView.asElement(), true);
        }
        adjustHeight();
    }
}