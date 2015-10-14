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

    private static final String UNUSED = "hal-data-table-unused";    

    public String cellTableCell() {
        return UNUSED;
    }

    @Override
    public String cellTableEvenRow() {
        return "even";
    }

    @Override
    public String cellTableEvenRowCell() {
        return UNUSED;
    }

    @Override
    public String cellTableFirstColumn() {
        return UNUSED;
    }

    @Override
    public String cellTableFirstColumnFooter() {
        return UNUSED;
    }

    @Override
    public String cellTableFirstColumnHeader() {
        return UNUSED;
    }

    @Override
    public String cellTableFooter() {
        return UNUSED;
    }

    @Override
    public String cellTableHeader() {
        return UNUSED;
    }

    @Override
    public String cellTableHoveredRow() {
        return "hal-data-table-hovered-row";
    }

    @Override
    public String cellTableHoveredRowCell() {
        return UNUSED;
    }

    @Override
    public String cellTableKeyboardSelectedCell() {
        return UNUSED;
    }

    @Override
    public String cellTableKeyboardSelectedRow() {
        return "hal-data-table-keyboard-selected-row";
    }

    @Override
    public String cellTableKeyboardSelectedRowCell() {
        return UNUSED;
    }

    @Override
    public String cellTableLastColumn() {
        return UNUSED;
    }

    @Override
    public String cellTableLastColumnFooter() {
        return UNUSED;
    }

    @Override
    public String cellTableLastColumnHeader() {
        return UNUSED;
    }

    @Override
    public String cellTableLoading() {
        return UNUSED;
    }

    @Override
    public String cellTableOddRow() {
        return "odd";
    }

    @Override
    public String cellTableOddRowCell() {
        return UNUSED;
    }

    @Override
    public String cellTableSelectedRow() {
        return "hal-data-table-selected-row";
    }

    @Override
    public String cellTableSelectedRowCell() {
        return UNUSED;
    }

    @Override
    public String cellTableSortableHeader() {
        return UNUSED;
    }

    @Override
    public String cellTableSortedHeaderAscending() {
        return UNUSED;
    }

    @Override
    public String cellTableSortedHeaderDescending() {
        return UNUSED;
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
        return UNUSED;
    }

    @Override
    public String getName() {
        return "DefaulCellTableStyle";
    }
}
