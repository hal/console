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
package org.jboss.hal.ballroom.toolbar;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.listview.DataProvider;
import org.jboss.hal.ballroom.listview.Display;
import org.jboss.hal.ballroom.listview.Filter;
import org.jboss.hal.ballroom.listview.FilterValue;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * toolbar.bind(dataProvider();
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
    @NonNls private static final Logger logger = LoggerFactory.getLogger(Toolbar.class);

    private final DataProvider<T> dataProvider;
    private final List<Attribute<T>> filterAttributes;
    private final Map<Attribute<T>, String> activeFilters;
    private final List<Attribute<T>> sortAttributes;
    private Attribute<T> filterAttribute;
    private Attribute<T> sortAttribute;
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

    private HTMLElement numberOfResults;
    private HTMLElement filters;
    private HTMLElement activeFiltersUl;

    public Toolbar(@NonNls String id, DataProvider<T> dataProvider, List<Attribute<T>> attributes,
            List<Action> actions) {
        this.dataProvider = dataProvider;

        HTMLElement control;
        HTMLElement results;
        this.root = div().css(row, toolbarPf).id(id)
                .add(column()
                        .add(control = form().css(toolbarPfActions).asElement())
                        .add(results = div().css(row, toolbarPfResults).asElement()))
                .asElement();

        // filter
        filterAttributes = attributes.stream()
                .filter(attribute -> {
                    Filter<T> filter = attribute.filter;
                    return filter != null;
                })
                .collect(toList());
        if (!filterAttributes.isEmpty()) {
            HTMLElement inputGroup;
            control.appendChild(div().css(formGroup, toolbarPfFilter)
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
                                    .on(click, e -> setFilterAttribute(attribute))
                                    .textContent(attribute.title)).asElement());
                }
            }
            inputGroup.appendChild(filterInput = input(text)
                    .css(formControl)
                    .id(Ids.TOOLBAR_FILTER)
                    .asElement());
        }

        // sort
        sortAttributes = attributes.stream()
                .filter(attribute -> attribute.comparator != null)
                .collect(toList());
        if (!sortAttributes.isEmpty()) {
            HTMLElement formGroup;
            control.appendChild(formGroup = div().css(CSS.formGroup)
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
                                    .on(click, e -> {
                                        setSortAttribute(attribute);
                                        apply();
                                    })
                                    .textContent(attribute.title)).asElement());
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
        if (!actions.isEmpty()) {
            HTMLElement actionsContainer;
            control.appendChild(div().css(toolbarPfActionRight)
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
    public void attach() {
        if (filterInput != null) {
            keyUpSubscription = fromEvent(filterInput, keyup)
                    .throttleLast(750, MILLISECONDS)
                    .subscribe(e -> {
                        addOrModifyActiveFilter(filterAttribute);
                        apply();
                    });
        }
    }

    @Override
    public void detach() {
        if (keyUpSubscription != null) {
            keyUpSubscription.unsubscribe();
        }
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    public void reset() {
        if (filterAttributes.isEmpty()) {
            filterAttribute = null;
        } else {
            filterAttribute = filterAttributes.get(0);
        }
        if (sortAttributes.isEmpty()) {
            sortAttribute = null;
        } else {
            sortAttribute = sortAttributes.get(0);
        }
        if (filterAttribute != null) {
            setFilterAttribute(filterAttribute);
            clearAllFilters();
        }
        if (sortAttribute != null) {
            setSortAttribute(sortAttribute);
            setAsc(true);
        }
        clearResults();
        apply();
    }

    @Override
    public void showItems(Iterable<T> items, int visible, int total) {
        if (visible != total) {
            numberOfResults.textContent = MESSAGES.resultsFiltered(visible, total);
        } else {
            numberOfResults.textContent = MESSAGES.results(total);
        }
    }

    private void setFilterAttribute(Attribute<T> attribute) {
        filterAttribute = attribute;
        if (filterUl != null) {
            selectDropdownItem(filterUl, DATA_FILTER, attribute);
        }
        filterLabel.textContent = attribute.title;
        if (filterButtonText != null) {
            filterButtonText.textContent = attribute.title;
        }
        filterInput.value = activeFilters.getOrDefault(attribute, "");
        filterInput.placeholder = MESSAGES.filterBy(attribute.title);
    }

    private void addOrModifyActiveFilter(Attribute<T> attribute) {
        if (Strings.isNullOrEmpty(filterInput.value)) {
            activeFilters.remove(attribute);
            clearFilter(attribute);

        } else {
            activeFilters.put(attribute, filterInput.value);
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
                                        .on(click, e -> {
                                            clearFilter(attribute);
                                            apply();
                                        })
                                        .add(span().css(pfIcon("close")))))
                        .asElement());
            }
        }
        Elements.setVisible(filters, !activeFilters.isEmpty());
    }

    private void clearFilter(Attribute<T> attribute) {
        activeFilters.remove(attribute);
        Element activeFilter = activeFiltersUl.querySelector("li[data-active-filter=" + attribute.name + "]"); //NON-NLS
        Elements.failSafeRemove(activeFiltersUl, activeFilter);
        Elements.setVisible(filters, !activeFilters.isEmpty());
    }

    public void clearAllFilters() {
        activeFilters.clear();
        filterInput.value = "";
        Elements.setVisible(filters, false);
        Elements.removeChildrenFrom(activeFiltersUl);
    }

    private void setSortAttribute(Attribute<T> attribute) {
        sortAttribute = attribute;
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

    private void setAsc(boolean asc) {
        this.asc = asc;
        if (asc) {
            sortOrderIcon.className = fontAwesome("sort-alpha-asc");
        } else {
            sortOrderIcon.className = fontAwesome("sort-alpha-desc");
        }
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

    private void clearResults() {
        numberOfResults.textContent = MESSAGES.results(0);
        Elements.setVisible(filters, false);
    }

    /** Applies the filters and sort order to the data provider */
    public void apply() {
        logger.debug("Apply filters {} and sort by {} {}", activeFilters, sortAttribute, (asc ? "asc" : "desc"));
        List<FilterValue<T>> filterValues = activeFilters.entrySet().stream()
                .map(entry -> new FilterValue<>(entry.getKey().filter, entry.getValue()))
                .collect(toList());
        dataProvider.apply(filterValues, sortAttribute != null ? sortAttribute.comparator : null, asc);
    }
}
