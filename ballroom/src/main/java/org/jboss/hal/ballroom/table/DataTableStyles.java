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

import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author Harald Pehl
 */
public class DataTableStyles implements CellTable.Style {

    public String cellTableCell() {
        return "";
    }

    @Override
    public String cellTableEvenRow() {
        return "even";
    }

    @Override
    public String cellTableEvenRowCell() {
        return "";
    }

    @Override
    public String cellTableFirstColumn() {
        return "";
    }

    @Override
    public String cellTableFirstColumnFooter() {
        return "";
    }

    @Override
    public String cellTableFirstColumnHeader() {
        return "";
    }

    @Override
    public String cellTableFooter() {
        return "";
    }

    @Override
    public String cellTableHeader() {
        return "";
    }

    @Override
    public String cellTableHoveredRow() {
        return "hal-data-table-hovered-row";
    }

    @Override
    public String cellTableHoveredRowCell() {
        return "";
    }

    @Override
    public String cellTableKeyboardSelectedCell() {
        return "";
    }

    @Override
    public String cellTableKeyboardSelectedRow() {
        return "hal-data-table-keyboard-selected-row";
    }

    @Override
    public String cellTableKeyboardSelectedRowCell() {
        return "";
    }

    @Override
    public String cellTableLastColumn() {
        return "";
    }

    @Override
    public String cellTableLastColumnFooter() {
        return "";
    }

    @Override
    public String cellTableLastColumnHeader() {
        return "";
    }

    @Override
    public String cellTableLoading() {
        return "";
    }

    @Override
    public String cellTableOddRow() {
        return "odd";
    }

    @Override
    public String cellTableOddRowCell() {
        return "";
    }

    @Override
    public String cellTableSelectedRow() {
        return "hal-data-table-selected-row";
    }

    @Override
    public String cellTableSelectedRowCell() {
        return "";
    }

    @Override
    public String cellTableSortableHeader() {
        return "";
    }

    @Override
    public String cellTableSortedHeaderAscending() {
        return "";
    }

    @Override
    public String cellTableSortedHeaderDescending() {
        return "";
    }

    @Override
    public String cellTableWidget() {
        return "datatable table table-striped table-bordered dataTable no-footer";
    }

    @Override
    public boolean ensureInjected() {
        return true;
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public String getName() {
        return "DefaulCellTableStyle";
    }
}
