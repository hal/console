/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.table;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.HalConstants;
import org.jboss.hal.resources.HalMessages;
import org.jboss.hal.resources.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction.*;

/**
 * @author Harald Pehl
 */
public class DataTable<T> implements IsElement, SecurityContextAware {

    class Pager extends AbstractPager {

        public Pager(final int pageSize) {
            setPageSize(pageSize);

            appearance.navInfo.setInnerText(constants.table_info_empty());
            appearance.navFirst.getClassList().add("disabled");
            appearance.navPrev.getClassList().add("disabled");
            appearance.navCurrentPage.setValue("");
            appearance.navNext.getClassList().add("disabled");
            appearance.navLast.getClassList().add("disabled");

            appearance.navFirst.setOnclick(event -> firstPage());
            appearance.navPrev.setOnclick(event -> previousPage());
            appearance.navCurrentPage.setOnchange(event -> gotoPage());
            appearance.navNext.setOnclick(event -> nextPage());
            appearance.navLast.setOnclick(event -> lastPage());
        }

        @Override
        protected void onRangeOrRowCountChanged() {
            HasRows display = getDisplay();
            if (display.getRowCount() == 0) {
                appearance.navInfo.setInnerText(constants.table_info_empty());
                appearance.navCurrentPage.setValue("");
                appearance.navPages.setInnerText("");
            } else {
                appearance.navInfo.setInnerHTML(info().asString());
                appearance.navCurrentPage.setValue(String.valueOf(1 + getPage()));
                appearance.navPages.setInnerHTML(messages.table_pages(getPageCount()).asString());
            }
            if (hasPreviousPage()) {
                appearance.navFirst.getClassList().remove("disabled");
                appearance.navPrev.getClassList().remove("disabled");
            } else {
                appearance.navFirst.getClassList().add("disabled");
                appearance.navPrev.getClassList().add("disabled");
            }
            if (hasNextPage()) {
                appearance.navNext.getClassList().remove("disabled");
                appearance.navLast.getClassList().remove("disabled");
            } else {
                appearance.navNext.getClassList().add("disabled");
                appearance.navLast.getClassList().add("disabled");
            }
        }

        private SafeHtml info() {
            // Default text is 1 based.
            HasRows display = getDisplay();
            Range range = display.getVisibleRange();
            int pageStart = range.getStart() + 1;
            int pageSize = range.getLength();
            int dataSize = display.getRowCount();
            int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
            endIndex = Math.max(pageStart, endIndex);
            return messages.table_info(pageStart, endIndex, dataSize);
        }

        private void gotoPage() {
            String value = appearance.navCurrentPage.getValue();
            if (Strings.emptyToNull(value) != null) {
                try {
                    int pageIndex = Integer.parseInt(value) - 1;
                    pageIndex = Math.max(0, pageIndex);
                    pageIndex = Math.min(getPageCount() - 1, pageIndex);
                    if (pageIndex != getPage()) {
                        setPage(pageIndex);
                        if (!String.valueOf(pageIndex + 1).equals(value)) {
                            // set adjusted value
                            appearance.navCurrentPage.setValue(String.valueOf(pageIndex + 1));
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("Cannot go to page {}: {}", value, e.getMessage());
                }
            }
        }
    }


    public static final int DEFAULT_PAGE_SIZE = 7;
    public static final String DEFAULT_BUTTON_GROUP = "hal-dataTable-defaultButtonGroup";

    private static final Logger logger = LoggerFactory.getLogger(DataTable.class);
    private static HalConstants constants = GWT.create(HalConstants.class);
    private static HalMessages messages = GWT.create(HalMessages.class);

    private final List<DataTableButton> buttons;
    private final ListDataProvider<T> dataProvider;
    private final SelectionModel<T> selectionModel;
    private final CellTable<T> cellTable;
    private final Appearance appearance;
    private SecurityContext securityContext;

    public DataTable(final String id, final ProvidesKey<T> keyProvider, final SecurityContext securityContext) {
        this(id, keyProvider, true, securityContext);
    }

    public DataTable(final String id, final ProvidesKey<T> keyProvider, boolean singleSelection,
            final SecurityContext securityContext) {
        this.securityContext = securityContext;

        buttons = new ArrayList<>();

        Element loadingIndicator = new Elements.Builder()
                .div().css("hal-data-table-loading")
                .div().css("spinner spinner-sm").end()
                .end().build();
        Element empty = new Elements.Builder()
                .div()
                .css("hal-data-table-empty")
                .innerText(constants.table_info_empty())
                .build();
        cellTable = new CellTable<T>(DEFAULT_PAGE_SIZE, new DataTableResources(), keyProvider,
                Elements.asWidget(loadingIndicator)) {{
            // Since the CellTable element not the widget gets attached to the DOM,
            // we need to manually invoke the onAttach() method to wire the DOM events.
            onAttach();
        }};
        cellTable.setEmptyTableWidget(Elements.asWidget(empty));
        cellTable.setWidth("100%");
        cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
        cellTable.setAutoHeaderRefreshDisabled(true);
        cellTable.setAutoFooterRefreshDisabled(true);
        cellTable.getElement().setId(id + "-data-table");
        cellTable.getElement().setAttribute("role", "grid");

        dataProvider = new ListDataProvider<>(keyProvider);
        dataProvider.addDataDisplay(cellTable);

        // pager constructor references appearance!
        appearance = Appearance.create(id, new I18n(constants, messages));
        Pager pager = new Pager(DEFAULT_PAGE_SIZE);
        pager.setDisplay(cellTable);
        appearance.cellTableHolder.appendChild(Elements.asElement(cellTable));

        if (singleSelection) {
            selectionModel = new SingleSelectionModel<>(keyProvider);
            cellTable.setSelectionModel(selectionModel);

        } else {
            this.selectionModel = new MultiSelectionModel<>(keyProvider);
            cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.createCustomManager(
                    new DefaultSelectionEventManager.CheckboxEventTranslator<T>() {
                        @Override
                        public DefaultSelectionEventManager.SelectAction translateSelectionEvent(
                                CellPreviewEvent<T> event) {
                            DefaultSelectionEventManager.SelectAction action = super.translateSelectionEvent(event);
                            if (action.equals(IGNORE)) {
                                T value = event.getValue();
                                boolean selected = selectionModel.isSelected(value);
                                return selected ? DESELECT : SELECT;
                            }
                            return action;
                        }
                    }));
            Column<T, Boolean> checkColumn = new Column<T, Boolean>(new CheckboxCell(true, false)) {
                @Override
                public Boolean getValue(T data) {
                    return selectionModel.isSelected(data);
                }
            };
            cellTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
            cellTable.setColumnWidth(checkColumn, 40, PX);
        }
        selectionModel.addSelectionChangeHandler(selectionChangeEvent -> enableDisableButtons());
    }

    @Override
    public Element asElement() {
        return appearance.asElement();
    }


    // ------------------------------------------------------ data & selection

    public void setData(List<T> data) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(data);
        if (selectionModel instanceof SingleSelectionModel) {
            ((SingleSelectionModel<T>) selectionModel).clear();
        } else if (selectionModel instanceof MultiSelectionModel) {
            ((MultiSelectionModel<T>) selectionModel).clear();
        }
    }

    public void onSelectionChange(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
    }

    public boolean hasSelection() {
        boolean hasSelection = false;
        if (selectionModel instanceof SingleSelectionModel) {
            hasSelection = selectedElement() != null;
        } else if (selectionModel instanceof MultiSelectionModel) {
            hasSelection = !selectedElements().isEmpty();
        }
        return hasSelection;
    }

    public T selectedElement() {
        if (selectionModel instanceof SingleSelectionModel) {
            return ((SingleSelectionModel<T>) selectionModel).getSelectedObject();
        } else if (selectionModel instanceof MultiSelectionModel) {
            Set<T> selection = ((MultiSelectionModel<T>) selectionModel).getSelectedSet();
            if (!selection.isEmpty()) {
                return selection.iterator().next();
            } else {
                return null;
            }
        }
        return null;
    }

    public Set<T> selectedElements() {
        if (selectionModel instanceof SingleSelectionModel) {
            return ((SingleSelectionModel<T>) selectionModel).getSelectedSet();
        } else if (selectionModel instanceof MultiSelectionModel) {
            return ((MultiSelectionModel<T>) selectionModel).getSelectedSet();
        }
        return null;
    }

    public void select(final T element) {
        selectionModel.setSelected(element, true);
    }


    // ------------------------------------------------------ columns

    public void addColumn(final Column<T, ?> col, final String headerString) {
        cellTable.addColumn(col, headerString);
    }

    public void addColumn(final Column<T, ?> col, final SafeHtml headerHtml) {cellTable.addColumn(col, headerHtml);}

    // ------------------------------------------------------ buttons

    public void addButton(DataTableButton button) {
        addButton(button, DEFAULT_BUTTON_GROUP);
    }

    public void addButton(DataTableButton button, String group) {
        Element groupElement = appearance.asElement().querySelector("[data-button-group='" + group + "']");
        if (groupElement == null) {
            groupElement = buttonGroup(group);
            appearance.buttonToolbar.appendChild(groupElement);
        }
        groupElement.appendChild(button.asElement());
        buttons.add(button);
    }

    private Element buttonGroup(String name) {
        // <div class="btn-group" data-button-group="<name>" role="group" aria-label="messages.table_named_group(<name>)">
        return new Elements.Builder().div()
                .css("btn-group")
                .data("buttonGroup", name)
                .aria("label", messages.table_named_group(name))
                .attr("role", "group")
                .build();
    }

    private void enableDisableButtons() {
        boolean hasSelection = hasSelection();
        for (DataTableButton button : buttons) {
            if (button.getTarget() == DataTableButton.Target.ROW) {
                button.setDisabled(!hasSelection);
            }
        }
    }


    // ------------------------------------------------------ security

    @Override
    public void updateSecurityContext(final SecurityContext securityContext) {
        this.securityContext = securityContext;
        applySecurity();
    }

    protected void applySecurity() {
        for (DataTableButton button : buttons) {
            button.updateSecurityContext(securityContext);
        }
    }
}
