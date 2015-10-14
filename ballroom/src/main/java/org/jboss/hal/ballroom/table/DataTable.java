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
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.DivElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityContextAware;
import org.jboss.hal.resources.HalConstants;
import org.jboss.hal.resources.HalMessages;
import org.jboss.hal.resources.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction.*;

/**
 * @author Harald Pehl
 */
public class DataTable<T> implements IsElement, SecurityContextAware {

    class Pager extends AbstractPager {

        public Pager() {
            appearance.navInfo.setInnerText(constants.table_info_empty());
            appearance.navFirst.getClassList().add("disabled");
            appearance.navPrev.getClassList().add("disabled");
            appearance.navCurrentPage.setValue("");
            appearance.navNext.getClassList().add("disabled");
            appearance.navLast.getClassList().add("disabled");

            appearance.navFirst.setOnclick(event -> firstPage());
            appearance.navPrev.setOnclick(event -> previousPage());
            appearance.navNext.setOnclick(event -> nextPage());
            appearance.navLast.setOnclick(event -> lastPage());
        }

        @Override
        protected void onRangeOrRowCountChanged() {
            HasRows display = getDisplay();
            if (display.getRowCount() == 0) {
                appearance.navInfo.setInnerText(constants.table_info_empty());
            } else {
                appearance.navInfo.setInnerHTML(info().asString());
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

        SafeHtml info() {
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

        void setPages(int pages) {
            if (pages == 0) {

            } else {
                
            }
            appearance.navPages.setInnerText(String.valueOf(pages));
        }
    }


    public static final int DEFAULT_PAGE_SIZE = 7;
    public static final String DEFAULT_BUTTON_GROUP = "hal-dataTable-defaultButtonGroup";

    private static HalConstants constants = GWT.create(HalConstants.class);
    private static HalMessages messages = GWT.create(HalMessages.class);

    private final String id;
    private final boolean singleSelection;
    private final Appearance appearance;
    private final List<DataTableButton> buttons;
    private final ListDataProvider<T> dataProvider;
    private final SelectionModel<T> selectionModel;
    private final CellTable<T> cellTable;
    private final Pager pager;

    public DataTable(final String id, final ProvidesKey<T> keyProvider) {
        this(id, keyProvider, true);
    }

    public DataTable(final String id, final ProvidesKey<T> keyProvider, boolean singleSelection) {
        this.id = id;
        this.singleSelection = singleSelection;

        appearance = Appearance.create(id, new I18n(constants, messages));
        buttons = new ArrayList<>();
        cellTable = new CellTable<>(DEFAULT_PAGE_SIZE, keyProvider);
        cellTable.setWidth("100%");
        cellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
        cellTable.setAutoHeaderRefreshDisabled(true);
        cellTable.setAutoFooterRefreshDisabled(true);
        cellTable.setRowData(0, Collections.<T>emptyList());
        cellTable.getElement().setAttribute("role", "grid");
        dataProvider = new ListDataProvider<>(keyProvider);
        dataProvider.addDataDisplay(cellTable);
        pager = new Pager();
        pager.setDisplay(cellTable);

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
        selectionModel.addSelectionChangeHandler(selectionChangeEvent -> enableDisableButtons(singleSelection));

        appearance.cellTableHolder.appendChild(Elements.asElement(cellTable));
    }

    @Override
    public Element asElement() {
        return appearance.asElement();
    }


    // ------------------------------------------------------ data & selection

    public void setData(List<T> data) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(data);
        enableDisableButtons(singleSelection);
    }

    @Override
    public void updateSecurityContext(final SecurityContext securityContext) {
        for (DataTableButton button : buttons) {
            button.updateSecurityContext(securityContext);
        }
    }

    public void onSelectionChange(SelectionChangeEvent.Handler handler) {
        selectionModel.addSelectionChangeHandler(handler);
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

    // ------------------------------------------------------ columns

    public void addColumn(final Column<T, ?> col, final String headerString) {cellTable.addColumn(col, headerString);}


    // ------------------------------------------------------ buttons

    public void addButton(DataTableButton button, EventListener listener) {
        addButton(button, DEFAULT_BUTTON_GROUP, listener);
    }

    public void addButton(DataTableButton button, String group, EventListener listener) {
        button.asElement().setOnclick(listener);
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
        DivElement group = Browser.getDocument().createDivElement();
        group.getClassList().add("btn-group");
        group.getDataset().setAt("buttonGroup", name);
        group.setAttribute("role", "group");
        group.setAttribute("aria-label", messages.table_named_group(name));
        return group;
    }

    private void enableDisableButtons(final boolean singleSelection) {
        boolean hasSelection;
        if (singleSelection) {
            hasSelection = ((SingleSelectionModel<T>) selectionModel).getSelectedObject() != null;
        } else {
            hasSelection = !((MultiSelectionModel<T>) selectionModel).getSelectedSet().isEmpty();
        }
        for (DataTableButton button : buttons) {
            if (button.getTarget() == DataTableButton.Target.ROW) {
                button.setDisabled(!hasSelection);
            }
        }
    }
}
