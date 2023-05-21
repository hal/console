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
package org.jboss.hal.ballroom;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
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

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.HandlerRegistration;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static java.util.stream.Collectors.toList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.button;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.ul;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;
import static org.jboss.elemento.EventType.keyup;
import static org.jboss.elemento.InputType.text;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.resources.CSS.btnLink;
import static org.jboss.hal.resources.CSS.caret;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.dropdown;
import static org.jboss.hal.resources.CSS.dropdownKebabPf;
import static org.jboss.hal.resources.CSS.dropdownMenu;
import static org.jboss.hal.resources.CSS.dropdownMenuRight;
import static org.jboss.hal.resources.CSS.dropdownToggle;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.formControl;
import static org.jboss.hal.resources.CSS.formControlStatic;
import static org.jboss.hal.resources.CSS.formGroup;
import static org.jboss.hal.resources.CSS.inputGroupBtn;
import static org.jboss.hal.resources.CSS.label;
import static org.jboss.hal.resources.CSS.labelInfo;
import static org.jboss.hal.resources.CSS.listHalSelected;
import static org.jboss.hal.resources.CSS.listInline;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.CSS.pfIcon;
import static org.jboss.hal.resources.CSS.row;
import static org.jboss.hal.resources.CSS.selected;
import static org.jboss.hal.resources.CSS.srOnly;
import static org.jboss.hal.resources.CSS.toolbarPf;
import static org.jboss.hal.resources.CSS.toolbarPfActionRight;
import static org.jboss.hal.resources.CSS.toolbarPfActions;
import static org.jboss.hal.resources.CSS.toolbarPfFilter;
import static org.jboss.hal.resources.CSS.toolbarPfResults;

/**
 * PatternFly toolbar. Should be connected to a {@link DataProvider} (which in turn updates its displays e.g. a list view):
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
 * <p>
 * Please note that the toolbar uses its own {@code <div class="row"/>} element. This is important if you add the toolbar using
 * the methods from {@link org.jboss.hal.ballroom.LayoutBuilder}:
 * </p>
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
 * @see <a href=
 *      "https://www.patternfly.org/pattern-library/forms-and-controls/toolbar/">https://www.patternfly.org/pattern-library/forms-and-controls/toolbar/</a>
 */
public class Toolbar<T> implements Display<T>, IsElement<HTMLElement>, Attachable {

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
    private HandlerRegistration keyUpSubscription;

    private HTMLElement sortButtonText;
    private HTMLElement sortStaticText;
    private HTMLElement sortOrderIcon;
    private HTMLElement sortUl;

    private final HTMLElement results;
    private final HTMLElement selection;
    private final HTMLElement filters;
    private final HTMLElement activeFiltersUl;

    public Toolbar(DataProvider<T> dataProvider, List<Attribute<T>> attributes, List<Action> actions) {
        this.dataProvider = dataProvider;

        HTMLElement controlContainer;
        HTMLElement resultContainer;
        this.root = div().css(row, toolbarPf)
                .add(column()
                        .add(controlContainer = form().css(toolbarPfActions).element())
                        .add(resultContainer = div().css(row, toolbarPfResults).element()))
                .element();

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
                            .apply(l -> l.htmlFor = Ids.TOOLBAR_FILTER).element())
                    .add(inputGroup = div().css(CSS.inputGroup).element()).element());

            if (filterAttributes.size() > 1) {
                inputGroup.appendChild(div().css(inputGroupBtn)
                        .add(button().css(btn, btnDefault, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(filterButtonText = span().css(marginRight5).element())
                                .add(span().css(caret)))
                        .add(filterUl = ul().css(dropdownMenu).element()).element());
                for (Attribute<T> attribute : filterAttributes) {
                    filterUl.appendChild(li()
                            .data(DATA_FILTER, attribute.name)
                            .add(a().css(clickable)
                                    .on(click, e -> setSelectedFilter(attribute))
                                    .textContent(attribute.title))
                            .element());
                }
            }
            inputGroup.appendChild(filterInput = input(text)
                    .css(formControl)
                    .id(Ids.TOOLBAR_FILTER).element());
        }

        // sort
        List<Attribute<T>> sortAttributes = attributes.stream()
                .filter(attribute -> attribute.comparator != null)
                .collect(toList());
        if (!sortAttributes.isEmpty()) {
            HTMLElement formGroup;
            controlContainer.appendChild(formGroup = div().css(CSS.formGroup).element());
            if (sortAttributes.size() > 1) {
                formGroup.appendChild(div().css(dropdown, btnGroup)
                        .add(button().css(btn, btnDefault, dropdownToggle)
                                .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                .add(sortButtonText = span().css(marginRight5).element())
                                .add(span().css(caret)))
                        .add(sortUl = ul().css(dropdownMenu).element()).element());
                for (Attribute<T> attribute : sortAttributes) {
                    sortUl.appendChild(li()
                            .data(DATA_SORT, attribute.name)
                            .add(a().css(clickable)
                                    .on(click, e -> sort(attribute))
                                    .textContent(attribute.title))
                            .element());
                }
            } else {
                formGroup.appendChild(sortStaticText = span().css(formControlStatic).element());
            }
            asc = true;
            formGroup.appendChild(button().css(btn, btnLink)
                    .apply(b -> b.type = UIConstants.BUTTON)
                    .on(click, e -> toggleSortOrder())
                    .add(sortOrderIcon = span().element()).element());
        }

        // actions
        if (!actions.isEmpty()) {
            HTMLElement actionsContainer;
            controlContainer.appendChild(div().css(toolbarPfActionRight)
                    .add(actionsContainer = div().css(formGroup).element()).element());
            int i = 0;
            HTMLElement ul = null;
            for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext(); i++) {
                Action action = iterator.next();
                String actionId = Ids.build(Ids.TOOLBAR, "actions", action.id);
                if (i < 3) {
                    actionsContainer.appendChild(button()
                            .css(btn, btnDefault)
                            .id(actionId)
                            .textContent(action.text)
                            .title(action.title)
                            .on(click, e -> action.callback.execute())
                            .apply(b -> b.type = UIConstants.BUTTON).element());
                    if (i == 2) {
                        actionsContainer.appendChild(div().css(dropdown, btnGroup, dropdownKebabPf)
                                .add(button().css(btn, btnLink, dropdownToggle)
                                        .id(Ids.TOOLBAR_ACTION_DROPDOWN)
                                        .data(UIConstants.TOGGLE, UIConstants.DROPDOWN)
                                        .aria(UIConstants.HAS_POPUP, UIConstants.TRUE)
                                        .aria(UIConstants.EXPANDED, UIConstants.FALSE)
                                        .add(span().css(fontAwesome("ellipsis-v"))))
                                .add(ul = ul().css(dropdownMenu, dropdownMenuRight)
                                        .aria(UIConstants.LABELLED_BY, Ids.TOOLBAR_ACTION_DROPDOWN).element())
                                .element());
                    }
                } else {
                    // noinspection ConstantConditions
                    ul.appendChild(li()
                            .add(a().css(clickable)
                                    .on(click, e -> action.callback.execute())
                                    .textContent(action.text))
                            .element());
                }
            }
        }

        // search and change view not yet implemented!

        // results
        resultContainer.appendChild(column(9)
                .add(results = h(5).textContent(MESSAGES.results(0)).element())
                .add(filters = span()
                        .add(p().css(marginRight5).textContent(CONSTANTS.activeFilters()))
                        .add(activeFiltersUl = ul().css(listInline).element())
                        .add(p().add(a()
                                .css(clickable)
                                .textContent(CONSTANTS.clearAllFilters())
                                .on(click, e -> clearAllFilters())))
                        .element())
                .element());
        resultContainer.appendChild(selection = column(3).css(listHalSelected).element());

        // initial reset
        filterInput.value = "";
        Elements.setVisible(filters, false);
        Elements.removeChildrenFrom(activeFiltersUl);

        if (filterAttributes.isEmpty()) {
            selectedFilter = null;
        } else {
            setSelectedFilter(filterAttributes.get(0));
        }

        this.asc = true;
        sortOrderIcon.className = fontAwesome("sort-alpha-asc");
        if (sortAttributes.isEmpty()) {
            selectedSort = null;
        } else {
            setSelectedSort(sortAttributes.get(0));
        }

        this.results.textContent = MESSAGES.results(0);
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void attach() {
        if (filterInput != null) {
            keyUpSubscription = bind(filterInput, keyup, e -> addOrModifySelectedFilter(selectedFilter));
        }
    }

    @Override
    public void detach() {
        if (keyUpSubscription != null) {
            keyUpSubscription.removeHandler();
        }
    }

    @Override
    public void showItems(Iterable<T> items, PageInfo pageInfo) {
        results.textContent = MESSAGES.results(pageInfo.getVisible());
    }

    @Override
    public void updateSelection(SelectionInfo<T> selectionInfo) {
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
                    "span[data-active-filter-value=" + attribute.name + "]"); // NON-NLS
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
                        .element());
            }
            Elements.setVisible(filters, dataProvider.hasFilters());
            dataProvider.addFilter(attribute.name, new FilterValue<>(attribute.filter, filterInput.value));
        }
    }

    private void clearFilter(Attribute<T> attribute) {
        Element activeFilter = activeFiltersUl.querySelector("li[data-active-filter=" + attribute.name + "]"); // NON-NLS
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
        Element li = ul.querySelector("li[data-" + data + "=" + attribute.name + "]"); // NON-NLS
        if (li != null) {
            li.classList.add(selected);
        }
    }

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
            if (this == o) {
                return true;
            }
            if (!(o instanceof Toolbar.Attribute)) {
                return false;
            }

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
        private final String title;
        private final Constraints constraints;
        private final Callback callback;

        public Action(String id, String text, Callback callback) {
            this(id, text, null, Constraints.empty(), callback);
        }

        public Action(String id, String text, String title, Callback callback) {
            this(id, text, title, Constraints.empty(), callback);
        }

        public Action(String id, String text, Constraint constraint, Callback callback) {
            this(id, text, null, Constraints.single(constraint), callback);
        }

        public Action(String id, String text, String title, Constraints constraints, Callback callback) {
            this.id = id;
            this.text = text;
            this.title = title != null ? title : "";
            this.constraints = constraints;
            this.callback = callback;
        }

        public Constraints getConstraints() {
            return constraints;
        }
    }
}
