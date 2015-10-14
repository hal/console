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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author Harald Pehl
 */
public class DataTableResources implements CellTable.Resources {

    CellTable.Resources builtIn = GWT.create(CellTable.Resources.class);

    public ImageResource cellTableFooterBackground() {
        return builtIn.cellTableFooterBackground();
    }

    @Override
    public ImageResource cellTableHeaderBackground() {
        return builtIn.cellTableHeaderBackground();
    }

    @Override
    public ImageResource cellTableLoading() {
        return builtIn.cellTableLoading();
    }

    @Override
    public ImageResource cellTableSelectedBackground() {
        return builtIn.cellTableSelectedBackground();
    }

    @Override
    public ImageResource cellTableSortAscending() {
        return builtIn.cellTableSortAscending();
    }

    @Override
    public ImageResource cellTableSortDescending() {
        return builtIn.cellTableSortDescending();
    }

    @Override
    public CellTable.Style cellTableStyle() {
        return new DataTableStyles();
    }
}
