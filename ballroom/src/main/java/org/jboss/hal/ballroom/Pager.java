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

import org.jboss.elemento.Elements;
import org.jboss.elemento.IsElement;
import org.jboss.hal.ballroom.dataprovider.DataProvider;
import org.jboss.hal.ballroom.dataprovider.Display;
import org.jboss.hal.ballroom.dataprovider.PageInfo;
import org.jboss.hal.ballroom.dataprovider.SelectionInfo;
import org.jboss.hal.config.Settings;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;

import com.google.gwt.core.client.GWT;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLSelectElement;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.Elements.form;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.EventType.change;
import static org.jboss.elemento.EventType.click;
import static org.jboss.elemento.EventType.submit;
import static org.jboss.elemento.InputType.text;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.CSS.i;

/**
 * PatternFly pager. Should be connected to a {@link DataProvider} (which in turn updates its displays e.g. a list view):
 *
 * <pre>
 * DataProvider dataProvider = ...;
 * ListView listView = ...;
 * Pager pager = ...;
 *
 * dataProvider.addDisplay(listView);
 * dataProvider.addDisplay(pager);
 * ...
 * dataProvider.update(items);
 * </pre>
 *
 * <p>
 * Please note that the pager uses its own {@code <div class="row"/>} element. This is important if you add the toolbar using
 * the methods from {@link org.jboss.hal.ballroom.LayoutBuilder}:
 * </p>
 *
 * <pre>
 * Pager pager = ...;
 * elements()
 *     .add(row()
 *         .add(column()
 *             .add(...)))
 *     .add(pager)
 * </pre>
 *
 * @see <a href=
 *      "https://www.patternfly.org/pattern-library/navigation/pagination/">https://www.patternfly.org/pattern-library/navigation/pagination/</a>
 */
public class Pager<T> implements Display<T>, IsElement<HTMLElement> {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private final HTMLElement root;
    private final HTMLElement current;
    private final HTMLElement total;
    private final HTMLInputElement pageInput;
    private final HTMLElement pages;
    private final HTMLElement firstPage;
    private final HTMLElement previousPage;
    private final HTMLElement nextPage;
    private final HTMLElement lastPage;
    private final DataProvider<T> dataProvider;

    public Pager(DataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        String pageId = Ids.uniqueId();

        HTMLSelectElement pageSizeSelect;
        root = div().css(row, paginationHal)
                .add(column()
                        .add(form().css(contentViewPfPagination, clearfix)
                                .on(submit, Event::preventDefault)
                                .add(div().css(formGroup)
                                        .add(pageSizeSelect = select().css(selectpicker, paginationPfPagesize)
                                                .apply(s -> s.tabIndex = -98)
                                                .on(change, e -> setPageSize(
                                                        Integer.parseInt(((HTMLSelectElement) e.currentTarget).value)))
                                                .element())
                                        .add(span().textContent(CONSTANTS.perPage())))
                                .add(div().css(formGroup)
                                        .add(span()
                                                .add(current = span().css(paginationPfItemsCurrent).element())
                                                .add(" " + CONSTANTS.of() + " ")
                                                .add(total = span().css(paginationPfItemsTotal).element()))
                                        .add(ul().css(pagination, paginationPfBack)
                                                .add(firstPage = li()
                                                        .add(a().css(clickable)
                                                                .on(click, e -> firstPage())
                                                                .title(CONSTANTS.firstPage())
                                                                .add(span().css(CSS.i,
                                                                        fontAwesome("angle-double-left"))))
                                                        .element())
                                                .add(previousPage = li()
                                                        .add(a().css(clickable)
                                                                .on(click, e -> previousPage())
                                                                .title(CONSTANTS.previousPage())
                                                                .add(span().css(i, fontAwesome("angle-left"))))
                                                        .element()))
                                        .add(label().css(srOnly)
                                                .textContent(CONSTANTS.currentPage())
                                                .apply(label -> label.htmlFor = pageId))
                                        .add(pageInput = input(text).css(paginationPfPage)
                                                .apply(input -> input.value = "1")
                                                .id(pageId)
                                                .on(change, e -> gotoPage(((HTMLInputElement) e.currentTarget).value))
                                                .element())
                                        .add(span()
                                                .add(CONSTANTS.of() + " ")
                                                .add(pages = span().css(paginationPfPages).element()))
                                        .add(ul().css(pagination, paginationPfForward)
                                                .add(nextPage = li()
                                                        .add(a().css(clickable)
                                                                .on(click, e -> nextPage())
                                                                .title(CONSTANTS.nextPage())
                                                                .add(span().css(i, fontAwesome("angle-right"))))
                                                        .element())
                                                .add(lastPage = li()
                                                        .add(a().css(clickable)
                                                                .on(click, e -> lastPage())
                                                                .title(CONSTANTS.lastPage())
                                                                .add(span().css(i, fontAwesome("angle-double-right"))))
                                                        .element())))))
                .element();

        for (int i = 0; i < Settings.PAGE_SIZE_VALUES.length; i++) {
            String pageLength = String.valueOf(Settings.PAGE_SIZE_VALUES[i]);
            pageSizeSelect.appendChild(option().apply(o -> o.value = pageLength)
                    .textContent(pageLength).element());
        }

        // initial reset
        pageSizeSelect.value = String.valueOf(dataProvider.getPageInfo().getPageSize());
        pageInput.value = "1";
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    public void showItems(Iterable<T> items, PageInfo pageInfo) {
        current.textContent = pageInfo.getFrom() + "-" + pageInfo.getTo();
        total.textContent = String.valueOf(pageInfo.getTotal());
        pageInput.value = String.valueOf(pageInfo.getPage() + 1);
        pages.textContent = String.valueOf(pageInfo.getPages());
        Elements.toggle(firstPage, disabled, pageInfo.getPage() == 0);
        Elements.toggle(previousPage, disabled, pageInfo.getPage() == 0);
        Elements.toggle(nextPage, disabled, pageInfo.getPage() == pageInfo.getPages() - 1);
        Elements.toggle(lastPage, disabled, pageInfo.getPage() == pageInfo.getPages() - 1);
    }

    @Override
    public void updateSelection(SelectionInfo selectionInfo) {

    }

    // ------------------------------------------------------ event handler

    private void setPageSize(int pageSize) {
        dataProvider.setPageSize(pageSize);
    }

    private void firstPage() {
        dataProvider.gotoFirstPage();
    }

    private void previousPage() {
        dataProvider.gotoPreviousPage();
    }

    private void nextPage() {
        dataProvider.gotoNextPage();
    }

    private void lastPage() {
        dataProvider.gotoLastPage();
    }

    // only method where page is one-based!
    private void gotoPage(String page) {
        try {
            dataProvider.gotoPage(Integer.parseInt(page) - 1);
        } catch (NumberFormatException e) {
            pageInput.value = String.valueOf(dataProvider.getPageInfo().getPage() + 1);
        }
    }
}
