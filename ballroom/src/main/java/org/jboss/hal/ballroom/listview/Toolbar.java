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
package org.jboss.hal.ballroom.listview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.intendia.rxgwt.elemento.RxElemento;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.form;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keyup;
import static org.jboss.gwt.elemento.core.InputType.text;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.CSS.label;

/**
 * PatternFly toolbar.
 *
 * <p>Please note that the toolbar uses an own {@code <div class="row"/>} element. This is important if you add the
 * toolbar using the methods from {@link org.jboss.hal.ballroom.LayoutBuilder}:</p>
 *
 * <pre>
 * Toolbar toolbar = ...;
 * elements()
 *     .add(toolbar)
 *     .add(row()
 *         .add(column()
 *             .add(...)))
 * </pre>
 *
 * @see <a href="http://www.patternfly.org/pattern-library/forms-and-controls/toolbar/">http://www.patternfly.org/pattern-library/forms-and-controls/toolbar/</a>
 */
public class Toolbar<T> implements IsElement<HTMLElement> {

    @FunctionalInterface
    public interface Filter<T> {

        boolean test(T model, String filter);
    }


    public static class Column<T> {

        private final String name;
        private final String title;
        private final Filter<T> filter;
        private final Comparator<T> comparator;

        public Column(String name) {
            this(name, new LabelBuilder().label(name), null, null);
        }

        public Column(String name, Filter<T> filter, Comparator<T> comparator) {
            this(name, new LabelBuilder().label(name), filter, comparator);
        }

        public Column(String name, String title) {
            this(name, title, null, null);
        }

        public Column(String name, String title, Filter<T> filter, Comparator<T> comparator) {
            this.name = name;
            this.title = title;
            this.filter = filter;
            this.comparator = comparator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (!(o instanceof Column)) { return false; }

            Column<?> column = (Column<?>) o;

            return name.equals(column.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "Toolbar.Column(" + name + ")";
        }
    }


    private static class Action {

        private final String id;
        private final String text;
        private final Callback callback;
        private final Constraints constraints;

        private Action(String id, String text, Callback callback, Constraints constraints) {
            this.id = id;
            this.text = text;
            this.callback = callback;
            this.constraints = constraints;
        }
    }


    public static class Builder<T> {

        private final List<Column<T>> columns;
        private final List<Action> actions;

        public Builder(Iterable<Column<T>> columns) {
            this.columns = Lists.newArrayList(columns);
            this.actions = new ArrayList<>();
        }

        public Builder<T> action(String id, String text, Callback callback) {
            return action(id, text, callback, Constraints.empty());
        }

        public Builder<T> action(String id, String text, Callback callback, Constraint constraint) {
            this.actions.add(new Action(id, text, callback, Constraints.single(constraint)));
            return this;
        }

        public Builder<T> action(String id, String text, Callback callback, Constraints constraints) {
            this.actions.add(new Action(id, text, callback, constraints));
            return this;
        }

        public Toolbar<T> build() {
            return new Toolbar<>(this);
        }
    }


    private static final String DATA_FILTER = "filter";
    private static final String DATA_ACTIVE_FILTER = "activeFilter";
    private static final String DATA_ACTIVE_FILTER_VALUE = "activeFilterValue";
    private static final String DATA_SORT = "sort";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);
    @NonNls private static final Logger logger = LoggerFactory.getLogger(Toolbar.class);

    private final HTMLElement root;
    private final List<Column<T>> filterColumns;
    private final Map<Column<T>, String> activeFilters;
    private final List<Column<T>> sortColumns;
    private Column<T> filterColumn;
    private Column<T> sortColumn;
    private boolean asc;
    private ListView<T> listView;

    private HTMLElement filterLabel;
    private HTMLElement filterButtonText;
    private HTMLElement filterUl;
    private HTMLInputElement filterInput;

    private HTMLElement sortButtonText;
    private HTMLElement sortStaticText;
    private HTMLElement sortOrderIcon;
    private HTMLElement sortUl;

    private HTMLElement numberOfResults;
    private HTMLElement filters;
    private HTMLElement activeFiltersUl;

    private Toolbar(Builder<T> builder) {
        HTMLElement control;
        HTMLElement results;
        this.root = div().css(row, toolbarPf)
                .add(column()
                        .add(control = form().css(toolbarPfActions)
                                .asElement())
                        .add(results = div().css(row, toolbarPfResults)
                                .asElement()))
                .asElement();

        // filter
        filterColumns = builder.columns.stream()
                .filter(c -> c.filter != null)
                .collect(toList());
        if (!filterColumns.isEmpty()) {
            HTMLElement inputGroup;
            control.appendChild(div().css(formGroup, toolbarPfFilter)
                    .add(filterLabel = label()
                            .css(srOnly)
                            .apply(l -> l.htmlFor = Ids.TOOLBAR_FILTER)
                            .asElement())
                    .add(inputGroup = div().css(CSS.inputGroup)
                            .asElement())
                    .asElement());

            if (filterColumns.size() > 1) {
                inputGroup.appendChild(div().css(inputGroupBtn)
                        .add(button().css(btn, btnDefault, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(filterButtonText = span().css(marginRight5).asElement())
                                .add(span().css(caret)))
                        .add(filterUl = ul().css(dropdownMenu).asElement())
                        .asElement());
                for (Column<T> column : filterColumns) {
                    filterUl.appendChild(li()
                            .data(DATA_FILTER, column.name)
                            .add(a().css(clickable)
                                    .on(click, e -> setFilterColumn(column))
                                    .textContent(column.title)).asElement());
                }
            }
            inputGroup.appendChild(filterInput = input(text)
                    .css(formControl)
                    .id(Ids.TOOLBAR_FILTER)
                    .asElement());
            RxElemento.fromEvent(filterInput, keyup)
                    .throttleLast(750, MILLISECONDS)
                    .subscribe(keyboardEvent -> {
                        addOrModifyActiveFilter(filterColumn);
                        apply();
                    });
        }

        // sort
        sortColumns = builder.columns.stream()
                .filter(c -> c.comparator != null)
                .collect(toList());
        if (!sortColumns.isEmpty()) {
            HTMLElement formGroup;
            control.appendChild(formGroup = div().css(CSS.formGroup)
                    .asElement());
            if (sortColumns.size() > 1) {
                formGroup.appendChild(div().css(dropdown, btnGroup)
                        .add(button().css(btn, btnDefault, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(sortButtonText = span().css(marginRight5).asElement())
                                .add(span().css(caret)))
                        .add(sortUl = ul().css(dropdownMenu).asElement())
                        .asElement());
                for (Column<T> column : sortColumns) {
                    sortUl.appendChild(li()
                            .data(DATA_SORT, column.name)
                            .add(a().css(clickable)
                                    .on(click, e -> {
                                        setSortColumn(column);
                                        apply();
                                    })
                                    .textContent(column.title)).asElement());
                }
            } else {
                formGroup.appendChild(sortStaticText = span().css(formControlStatic).asElement());
            }
            asc = true;
            formGroup.appendChild(button().css(btn, btnLink)
                    .apply(b -> b.type = UIConstants.BUTTON)
                    .on(click, e -> {
                        setAsc(!asc);
                        apply();
                    })
                    .add(sortOrderIcon = span().asElement())
                    .asElement());
        }

        // actions
        if (!builder.actions.isEmpty()) {
            HTMLElement actions;
            control.appendChild(div().css(toolbarPfActionRight)
                    .add(actions = div().css(formGroup)
                            .asElement())
                    .asElement());
            int i = 0;
            HTMLElement ul = null;
            for (Iterator<Action> iterator = builder.actions.iterator(); iterator.hasNext(); i++) {
                Action action = iterator.next();
                String actionId = Ids.build(Ids.TOOLBAR, "action", action.id);
                if (i <= 2) {
                    actions.appendChild(button()
                            .css(btn, btnDefault)
                            .id(actionId)
                            .textContent(action.text)
                            .on(click, e -> action.callback.execute())
                            .apply(b -> b.type = UIConstants.BUTTON)
                            .asElement());
                    if (i == 2) {
                        actions.appendChild(div().css(dropdown, btnGroup, dropdownKebabPf)
                                .add(button().css(btn, btnLink, dropdownToggle)
                                        .id(Ids.TOOLBAR_ACTION_DROPDOWN)
                                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                        .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                        .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                        .add(span().css(fontAwesome("ellipsis-v"))))
                                .add(ul = ul().css(dropdownMenu)
                                        .aria(UIConstants.LABELLED_BY, Ids.TOOLBAR_ACTION_DROPDOWN)
                                        .asElement())
                                .asElement());
                    }
                } else {
                    //noinspection ConstantConditions
                    ul.appendChild(li()
                            .add(a().css(clickable)
                                    .on(click, e -> action.callback.execute())
                                    .textContent(action.text))
                            .asElement());
                }
            }
        }

        // search and change view not yet implemented!

        // results
        activeFilters = new HashMap<>();
        results.appendChild(column()
                .add(numberOfResults = h(5).textContent(MESSAGES.results(0)).asElement())
                .add(filters = span()
                        .add(p().css(marginRight5).textContent(CONSTANTS.activeFilters()))
                        .add(activeFiltersUl = ul().css(listInline).asElement())
                        .add(p().add(a()
                                .css(clickable)
                                .textContent(CONSTANTS.clearAllFilters())
                                .on(click, e -> {
                                    clearAllFilters();
                                    apply();
                                })))
                        .asElement())
                .asElement());

        // initial reset
        reset();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    public void bindListView(ListView<T> listView) {
        this.listView = listView;
    }

    public void reset() {
        if (filterColumns.isEmpty()) {
            filterColumn = null;
        } else {
            filterColumn = filterColumns.get(0);
        }
        if (sortColumns.isEmpty()) {
            sortColumn = null;
        } else {
            sortColumn = sortColumns.get(0);
        }
        setFilterColumn(filterColumn);
        setSortColumn(sortColumn);
        setAsc(true);
        clearAllFilters();
        clearResults();
        apply();
    }

    private void setFilterColumn(Column<T> column) {
        filterColumn = column;
        if (filterUl != null) {
            selectDropdownItem(filterUl, DATA_FILTER, column);
        }
        filterLabel.textContent = column.title;
        if (filterButtonText != null) {
            filterButtonText.textContent = column.title;
        }
        filterInput.value = activeFilters.getOrDefault(column, "");
        filterInput.placeholder = MESSAGES.filterBy(column.title);
    }

    private void addOrModifyActiveFilter(Column<T> column) {
        if (Strings.isNullOrEmpty(filterInput.value)) {
            activeFilters.remove(column);
            clearFilter(column);

        } else {
            activeFilters.put(column, filterInput.value);
            Element activeFilterValue = activeFiltersUl.querySelector(
                    "span[data-active-filter-value=" + column.name + "]"); //NON-NLS
            if (activeFilterValue != null) {
                activeFilterValue.textContent = filterInput.value;
            } else {
                activeFiltersUl.appendChild(li()
                        .data(DATA_ACTIVE_FILTER, column.name)
                        .add(span().css(label, labelInfo)
                                .add(span().textContent(column.title + ": "))
                                .add(span().data(DATA_ACTIVE_FILTER_VALUE, column.name)
                                        .textContent(filterInput.value))
                                .add(a().css(clickable)
                                        .on(click, e -> {
                                            clearFilter(column);
                                            apply();
                                        })
                                        .add(span().css(pfIcon("close")))))
                        .asElement());
            }
        }
        Elements.setVisible(filters, !activeFilters.isEmpty());
    }

    private void clearFilter(Column<T> column) {
        activeFilters.remove(column);
        Element activeFilter = activeFiltersUl.querySelector("li[data-active-filter=" + column.name + "]"); //NON-NLS
        Elements.failSafeRemove(activeFiltersUl, activeFilter);
        Elements.setVisible(filters, !activeFilters.isEmpty());
    }

    private void clearAllFilters() {
        activeFilters.clear();
        Elements.setVisible(filters, false);
        Elements.removeChildrenFrom(activeFiltersUl);
    }

    private void setSortColumn(Column<T> column) {
        sortColumn = column;
        if (sortUl != null) {
            selectDropdownItem(sortUl, DATA_SORT, column);
        }
        if (sortButtonText != null) {
            sortButtonText.textContent = column.title;
        }
        if (sortStaticText != null) {
            sortStaticText.textContent = column.title;
        }
    }

    private void setAsc(boolean asc) {
        this.asc = asc;
        if (asc) {
            sortOrderIcon.className = fontAwesome("sort-alpha-asc");
        } else {
            sortOrderIcon.className = fontAwesome("sort-alpha-desc");
        }
    }

    private void selectDropdownItem(HTMLElement ul, String data, Column<T> column) {
        for (HTMLElement li : Elements.children(ul)) {
            li.classList.remove(selected);
        }
        Element li = ul.querySelector("li[data-" + data + "=" + column.name + "]"); //NON-NLS
        if (li != null) {
            li.classList.add(selected);
        }
    }

    private void clearResults() {
        numberOfResults.textContent = MESSAGES.results(0);
        Elements.setVisible(filters, false);
    }

    private void apply() {
        logger.debug("Apply filters {} and sort by {} {} to list view {}",
                activeFilters, sortColumn, (asc ? "asc" : "desc"),
                listView != null ? listView.asElement().id : Names.NOT_AVAILABLE);
    }
}
