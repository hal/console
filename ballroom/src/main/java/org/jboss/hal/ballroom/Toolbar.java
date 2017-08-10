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
package org.jboss.hal.ballroom;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.dataprovider.Display;
import org.jboss.hal.ballroom.dataprovider.Filter;
import org.jboss.hal.ballroom.dataprovider.FilterValue;
import org.jboss.hal.ballroom.dataprovider.PageInfo;
import org.jboss.hal.ballroom.dataprovider.SelectionInfo;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import rx.Subscription;

import static com.intendia.rxgwt.elemento.RxElemento.fromEvent;
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
 * PatternFly toolbar. Should be connected to a {@link DataProvider} (which in turn updates its displays e.g. a list
 * view):
 *
 * <pre>
 * DataProvider dataProvider = ...;
 * ListView listView = ...;
 * Toolbar toolbar = ...;
 *
 * dataProvider.addDisplay(listView);
 * dataProvider.addDisplay(toolbar);
 * ...
 * dataProvider.update(items);
 * </pre>
 *
 * <p>Please note that the toolbar uses its own {@code <div class="row"/>} element. This is important if you add the
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
public class Toolbar<T> implements Display<T>, IsElement<HTMLElement>, Attachable {

    public static class Attribute<T> {

        private final String name;
        private final String title;
        private final Filter<T> filter;
        private final Comparator<T> comparator;

        public Attribute(String name, Filter<T> filter) {
            this(name, new LabelBuilder().label(name), filter, null);
        }

        public Attribute(String name, Comparator<T> comparator) {
            this(name, new LabelBuilder().label(name), null, comparator);
        }

        public Attribute(String name, Filter<T> filter, Comparator<T> comparator) {
            this(name, new LabelBuilder().label(name), filter, comparator);
        }

        public Attribute(String name, String title, Filter<T> filter) {
            this(name, title, filter, null);
        }

        public Attribute(String name, String title, Comparator<T> comparator) {
            this(name, title, null, comparator);
        }

        public Attribute(String name, String title, Filter<T> filter, Comparator<T> comparator) {
            this.name = name;
            this.title = title;
            this.filter = filter;
            this.comparator = comparator;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (!(o instanceof Toolbar.Attribute)) { return false; }

            Attribute<?> attribute = (Attribute<?>) o;

            return name.equals(attribute.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "Toolbar.Attribute(" + name + ")";
        }
    }


    public static class Action {

        private final String id;
        private final String text;
        private final Constraints constraints;
        private final Callback callback;

        public Action(String id, String text, Callback callback) {
            this(id, text, Constraints.empty(), callback);
        }

        public Action(String id, String text, Constraint constraint, Callback callback) {
            this(id, text, Constraints.single(constraint), callback);
        }

        public Action(String id, String text, Constraints constraints, Callback callback) {
            this.id = id;
            this.text = text;
            this.constraints = constraints;
            this.callback = callback;
        }

        public Constraints getConstraints() {
            return constraints;
        }
    }


    private static final String DATA_FILTER = "filter";
    private static final String DATA_ACTIVE_FILTER = "activeFilter";
    private static final String DATA_ACTIVE_FILTER_VALUE = "activeFilterValue";
    private static final String DATA_SORT = "sort";
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final Messages MESSAGES = GWT.create(Messages.class);

    private final DataProvider<T> dataProvider;
    private Attribute<T> selectedFilter;
    private Attribute<T> selectedSort;
    private boolean asc;

    private final HTMLElement root;
    private HTMLElement filterLabel;
    private HTMLElement filterButtonText;
    private HTMLElement filterUl;
    private HTMLInputElement filterInput;
    private Subscription keyUpSubscription;

    private HTMLElement sortButtonText;
    private HTMLElement sortStaticText;
    private HTMLElement sortOrderIcon;
    private HTMLElement sortUl;

    private HTMLElement results;
    private HTMLElement selection;
    private HTMLElement filters;
    private HTMLElement activeFiltersUl;

    public Toolbar(DataProvider<T> dataProvider, List<Attribute<T>> attributes, List<Action> actions) {
        this.dataProvider = dataProvider;

        HTMLElement controlContainer;
        HTMLElement resultContainer;
        this.root = div().css(row, toolbarPf)
                .add(column()
                        .add(controlContainer = form().css(toolbarPfActions).asElement())
                        .add(resultContainer = div().css(row, toolbarPfResults).asElement()))
                .asElement();

        // filter
        List<Attribute<T>> filterAttributes = attributes.stream()
                .filter(attribute -> {
                    Filter<T> filter = attribute.filter;
                    return filter != null;
                })
                .collect(toList());
        if (!filterAttributes.isEmpty()) {
            HTMLElement inputGroup;
            controlContainer.appendChild(div().css(formGroup, toolbarPfFilter)
                    .add(filterLabel = label()
                            .css(srOnly)
                            .apply(l -> l.htmlFor = Ids.TOOLBAR_FILTER)
                            .asElement())
                    .add(inputGroup = div().css(CSS.inputGroup)
                            .asElement())
                    .asElement());

            if (filterAttributes.size() > 1) {
                inputGroup.appendChild(div().css(inputGroupBtn)
                        .add(button().css(btn, btnDefault, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(filterButtonText = span().css(marginRight5).asElement())
                                .add(span().css(caret)))
                        .add(filterUl = ul().css(dropdownMenu).asElement())
                        .asElement());
                for (Attribute<T> attribute : filterAttributes) {
                    filterUl.appendChild(li()
                            .data(DATA_FILTER, attribute.name)
                            .add(a().css(clickable)
                                    .on(click, e -> setSelectedFilter(attribute))
                                    .textContent(attribute.title)).asElement());
                }
            }
            inputGroup.appendChild(filterInput = input(text)
                    .css(formControl)
                    .id(Ids.TOOLBAR_FILTER)
                    .asElement());
        }

        // sort
        List<Attribute<T>> sortAttributes = attributes.stream()
                .filter(attribute -> attribute.comparator != null)
                .collect(toList());
        if (!sortAttributes.isEmpty()) {
            HTMLElement formGroup;
            controlContainer.appendChild(formGroup = div().css(CSS.formGroup)
                    .asElement());
            if (sortAttributes.size() > 1) {
                formGroup.appendChild(div().css(dropdown, btnGroup)
                        .add(button().css(btn, btnDefault, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(sortButtonText = span().css(marginRight5).asElement())
                                .add(span().css(caret)))
                        .add(sortUl = ul().css(dropdownMenu).asElement())
                        .asElement());
                for (Attribute<T> attribute : sortAttributes) {
                    sortUl.appendChild(li()
                            .data(DATA_SORT, attribute.name)
                            .add(a().css(clickable)
                                    .on(click, e -> sort(attribute))
                                    .textContent(attribute.title)).asElement());
                }
            } else {
                formGroup.appendChild(sortStaticText = span().css(formControlStatic).asElement());
            }
            asc = true;
            formGroup.appendChild(button().css(btn, btnLink)
                    .apply(b -> b.type = UIConstants.BUTTON)
                    .on(click, e -> toggleSortOrder())
                    .add(sortOrderIcon = span().asElement())
                    .asElement());
        }

        // actions
        if (!actions.isEmpty()) {
            HTMLElement actionsContainer;
            controlContainer.appendChild(div().css(toolbarPfActionRight)
                    .add(actionsContainer = div().css(formGroup)
                            .asElement())
                    .asElement());
            int i = 0;
            HTMLElement ul = null;
            for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext(); i++) {
                Action action = iterator.next();
                String actionId = Ids.build(Ids.TOOLBAR, "action", action.id);
                if (i < 2) {
                    actionsContainer.appendChild(button()
                            .css(btn, btnDefault)
                            .id(actionId)
                            .textContent(action.text)
                            .on(click, e -> action.callback.execute())
                            .apply(b -> b.type = UIConstants.BUTTON)
                            .asElement());
                    if (i == 1) {
                        actionsContainer.appendChild(div().css(dropdown, btnGroup, dropdownKebabPf)
                                .add(button().css(btn, btnLink, dropdownToggle)
                                        .id(Ids.TOOLBAR_ACTION_DROPDOWN)
                                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                        .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                        .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                        .add(span().css(fontAwesome("ellipsis-v"))))
                                .add(ul = ul().css(dropdownMenu, dropdownMenuRight)
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
        resultContainer.appendChild(column(9)
                .add(results = h(5).textContent(MESSAGES.results(0)).asElement())
                .add(filters = span()
                        .add(p().css(marginRight5).textContent(CONSTANTS.activeFilters()))
                        .add(activeFiltersUl = ul().css(listInline).asElement())
                        .add(p().add(a()
                                .css(clickable)
                                .textContent(CONSTANTS.clearAllFilters())
                                .on(click, e -> clearAllFilters())))
                        .asElement())
                .asElement());
        resultContainer.appendChild(selection = column(3).css(listHalSelected)
                .asElement());


        // initial reset
        filterInput.value = "";
        Elements.setVisible(filters, false);
        Elements.removeChildrenFrom(activeFiltersUl);
        if (filterAttributes.isEmpty()) {
            selectedFilter = null;
        } else {
            setSelectedFilter(filterAttributes.get(0));
        }
        if (sortAttributes.isEmpty()) {
            selectedSort = null;
        } else {
            setSelectedFilter(sortAttributes.get(0));
        }
        this.asc = true;
        if (selectedSort != null) {
            setSelectedSort(selectedSort);
        }
        this.results.textContent = MESSAGES.results(0);
        Elements.setVisible(filters, false);
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    public void attach() {
        if (filterInput != null) {
            keyUpSubscription = fromEvent(filterInput, keyup)
                    .throttleLast(750, MILLISECONDS)
                    .subscribe(e -> addOrModifySelectedFilter(selectedFilter));
        }
    }

    @Override
    public void detach() {
        if (keyUpSubscription != null) {
            keyUpSubscription.unsubscribe();
        }
    }

    @Override
    public void showItems(Iterable<T> items, PageInfo pageInfo) {
        results.textContent = MESSAGES.results(pageInfo.getVisible());
    }

    @Override
    public void updateSelection(SelectionInfo selectionInfo) {
        Elements.setVisible(this.selection, selectionInfo.hasSelection());
        if (selectionInfo.hasSelection() && selectionInfo.isMultiSelect()) {
            this.selection.innerHTML = MESSAGES.selected(selectionInfo.getSelectionCount(),
                    dataProvider.getPageInfo().getTotal()).asString();
        }
    }


    // ------------------------------------------------------ event handler

    private void setSelectedFilter(Attribute<T> attribute) {
        selectedFilter = attribute;
        if (filterUl != null) {
            selectDropdownItem(filterUl, DATA_FILTER, attribute);
        }
        filterLabel.textContent = attribute.title;
        if (filterButtonText != null) {
            filterButtonText.textContent = attribute.title;
        }
        filterInput.value = dataProvider.getFilter(attribute.name).getValue();
        filterInput.placeholder = MESSAGES.filterBy(attribute.title);
    }

    private void addOrModifySelectedFilter(Attribute<T> attribute) {
        if (Strings.isNullOrEmpty(filterInput.value)) {
            clearFilter(attribute);

        } else {
            Element activeFilterValue = activeFiltersUl.querySelector(
                    "span[data-active-filter-value=" + attribute.name + "]"); //NON-NLS
            if (activeFilterValue != null) {
                activeFilterValue.textContent = filterInput.value;
            } else {
                activeFiltersUl.appendChild(li()
                        .data(DATA_ACTIVE_FILTER, attribute.name)
                        .add(span().css(label, labelInfo)
                                .add(span().textContent(attribute.title + ": "))
                                .add(span().data(DATA_ACTIVE_FILTER_VALUE, attribute.name)
                                        .textContent(filterInput.value))
                                .add(a().css(clickable)
                                        .on(click, e -> clearFilter(attribute))
                                        .add(span().css(pfIcon("close")))))
                        .asElement());
            }
            Elements.setVisible(filters, dataProvider.hasFilters());
            dataProvider.addFilter(attribute.name, new FilterValue<>(attribute.filter, filterInput.value));
        }
    }

    private void clearFilter(Attribute<T> attribute) {
        Element activeFilter = activeFiltersUl.querySelector("li[data-active-filter=" + attribute.name + "]"); //NON-NLS
        Elements.failSafeRemove(activeFiltersUl, activeFilter);
        Elements.setVisible(filters, dataProvider.hasFilters());
        dataProvider.removeFilter(attribute.name);
    }

    public void clearAllFilters() {
        filterInput.value = "";
        Elements.setVisible(filters, false);
        Elements.removeChildrenFrom(activeFiltersUl);
        dataProvider.clearFilters();
    }

    private void sort(Attribute<T> attribute) {
        setSelectedSort(attribute);
        dataProvider.setComparator(asc ? selectedSort.comparator : selectedSort.comparator.reversed());
    }

    private void setSelectedSort(Attribute<T> attribute) {
        selectedSort = attribute;
        if (sortUl != null) {
            selectDropdownItem(sortUl, DATA_SORT, attribute);
        }
        if (sortButtonText != null) {
            sortButtonText.textContent = attribute.title;
        }
        if (sortStaticText != null) {
            sortStaticText.textContent = attribute.title;
        }
    }

    private void toggleSortOrder() {
        asc = !asc;
        if (asc) {
            sortOrderIcon.className = fontAwesome("sort-alpha-asc");
        } else {
            sortOrderIcon.className = fontAwesome("sort-alpha-desc");
        }
        dataProvider.setComparator(asc ? selectedSort.comparator : selectedSort.comparator.reversed());
    }

    private void selectDropdownItem(HTMLElement ul, String data, Attribute<T> attribute) {
        for (HTMLElement li : Elements.children(ul)) {
            li.classList.remove(selected);
        }
        Element li = ul.querySelector("li[data-" + data + "=" + attribute.name + "]"); //NON-NLS
        if (li != null) {
            li.classList.add(selected);
        }
    }
}
