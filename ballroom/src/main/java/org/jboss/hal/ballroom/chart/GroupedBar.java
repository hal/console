/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.js.JsHelper;
import org.jboss.hal.resources.UIConstants;

import com.google.common.collect.ArrayListMultimap;

import elemental2.core.JsArray;
import elemental2.dom.HTMLElement;
import jsinterop.base.JsPropertyMap;

import static elemental2.dom.DomGlobal.window;
import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.resources.UIConstants.HASH;

/**
 * Grouped bar chart to visualize quantitative data. Can be rendered horizontal or vertical. Values can be stacked.
 *
 * @see <a href=
 *      "https://www.patternfly.org/pattern-library/data-visualization/bar-chart/">https://www.patternfly.org/pattern-library/data-visualization/bar-chart/</a>
 */
public class GroupedBar implements IsElement<HTMLElement>, Attachable {

    private static final int HORIZONTAL_ROW = 33;
    private static final int HORIZONTAL_STACKED_HEIGHT = 100;
    private static final int VERTICAL_HEIGHT = 300;

    private final Builder builder;
    private final HTMLElement root;
    private final Options options;
    private Api api;

    @SuppressWarnings("unchecked")
    private GroupedBar(Builder builder) {
        this.builder = builder;

        root = div().id().element();
        options = Charts.get().defaultGroupedBarOptions();
        options.axis = new Options.Axis();
        options.axis.rotated = builder.orientation == Orientation.HORIZONTAL;
        options.axis.x = new Options.X();
        options.axis.x.categories = new JsArray<>();
        for (String category : builder.categories) {
            options.axis.x.categories.push(category);
        }
        options.axis.x.type = "category";
        options.bindto = HASH + root.id;
        options.data = new Options.Data();
        options.data.columns = new JsArray<>();
        if (builder.stacked) {
            JsArray<String> names = new JsArray<>();
            for (String id : builder.order) {
                names.push(builder.names.get(id));
            }
            options.data.groups = new JsArray<>();
            options.data.groups.push(names);
        }
        options.data.names = JsHelper.asJsMap(builder.names);
        options.color = new Options.Color();
        options.data.colors = JsHelper.asJsMap(builder.colors);

        options.data.type = "bar";
    }

    @Override
    @SuppressWarnings("Duplicates")
    public void attach() {
        if (api == null) {
            api = C3.generate(options);
            if (builder.responsive) {
                window.onresize = event -> {
                    resizeInParent();
                    return null;
                };
                resizeInParent();
            }
        }
    }

    @Override
    public void detach() {
        if (api != null) {
            api.destroy();
            api = null;
            window.onresize = null;
        }
    }

    private Api api() {
        if (api == null) {
            throw new IllegalStateException(
                    "GroupedBar is not attached. Call GroupedBar.attach() before using any of the API methods!");
        }
        return api;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    /** Use this method if you created the bar chart with a single category. */
    public void update(Map<String, Long> data) {
        ArrayListMultimap<String, Long> multimap = ArrayListMultimap.create();
        for (Map.Entry<String, Long> entry : data.entrySet()) {
            multimap.put(entry.getKey(), entry.getValue());
        }
        update(multimap);
    }

    /**
     * Use this method if you created the bar chart with multiple categories and provide a value for each category in the
     * multimap.
     */
    @SuppressWarnings("unchecked")
    public void update(ArrayListMultimap<String, Long> data) {
        JsPropertyMap<Object> dataMap = JsPropertyMap.of();
        JsArray<JsArray<Object>> columns = new JsArray<>();

        for (Map.Entry<String, Collection<Long>> entry : data.asMap().entrySet()) {
            JsArray<Object> column = new JsArray<>();
            column.push(entry.getKey());
            for (Long value : entry.getValue()) {
                column.push(value.longValue());
            }
            columns.push(column);
        }

        dataMap.set("columns", columns); // NON-NLS
        api().load(dataMap);
    }

    public void resize(int width) {
        JsPropertyMap<Object> dimension = JsPropertyMap.of();
        dimension.set(UIConstants.WIDTH, width);
        int height;
        if (builder.orientation == Orientation.HORIZONTAL) {
            height = builder.stacked
                    ? HORIZONTAL_STACKED_HEIGHT
                    : HORIZONTAL_ROW * builder.names.size() * builder.categories.size() + 40;
        } else {
            height = VERTICAL_HEIGHT;
        }
        dimension.set(UIConstants.HEIGHT, height);
        api().resize(dimension);
    }

    private void resizeInParent() {
        HTMLElement parent = (HTMLElement) root.parentNode;
        resize((int) $(parent).width());
    }

    private enum Orientation {
        HORIZONTAL, VERTICAL
    }

    public static class Builder {

        private final List<String> categories;
        private final Set<String> order;
        private final Map<String, String> colors;
        private final Map<String, String> names;
        private Orientation orientation;
        private boolean stacked;
        private boolean responsive;

        /** Creates a new builder with the specified categories (rows or columns) */
        public Builder(String category, String... moreCategories) {
            this.categories = new ArrayList<>();
            this.categories.add(category);
            if (moreCategories != null) {
                this.categories.addAll(asList(moreCategories));
            }
            this.order = new LinkedHashSet<>();
            this.colors = new HashMap<>();
            this.names = new HashMap<>();
            this.orientation = Orientation.HORIZONTAL;
            this.responsive = false;
            this.stacked = false;
        }

        public Builder add(String id, String text, String color) {
            order.add(id);
            colors.put(id, color);
            names.put(id, text);
            return this;
        }

        public Builder horizontal() {
            this.orientation = Orientation.HORIZONTAL;
            return this;
        }

        public Builder vertical() {
            this.orientation = Orientation.VERTICAL;
            return this;
        }

        public Builder responsive(boolean responsive) {
            this.responsive = responsive;
            return this;
        }

        public Builder stacked(boolean stacked) {
            this.stacked = stacked;
            return this;
        }

        public GroupedBar build() {
            return new GroupedBar(this);
        }
    }
}
