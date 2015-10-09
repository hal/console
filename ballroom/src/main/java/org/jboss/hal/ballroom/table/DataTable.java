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
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SingleSelectionModel;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.EventListener;
import elemental.html.ButtonElement;
import elemental.html.DivElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.HalConstants;
import org.jboss.hal.resources.HalMessages;
import org.jboss.hal.resources.I18n;

/**
 * @author Harald Pehl
 */
public class DataTable<T> implements IsElement {

    public static final int DEFAULT_PAGE_SIZE = 7;
    public static final String DEFAULT_BUTTON_GROUP = "hal.dataTable.defaultButtonGroup";

    private static HalConstants constants = GWT.create(HalConstants.class);
    private static HalMessages messages = GWT.create(HalMessages.class);

    private final String id;
    private final SecurityContext securityContext;
    private final ListDataProvider<T> dataProvider;
    private final SingleSelectionModel<T> selectionModel;
    private final CellTable<T> cellTable;
    private final Pager pager;
    private final Appearance appearance;

    public DataTable(final String id, final ProvidesKey<T> keyProvider, final SecurityContext securityContext) {
        this.id = id;
        this.securityContext = securityContext;

        this.dataProvider = new ListDataProvider<>(keyProvider);
        this.selectionModel = new SingleSelectionModel<>(keyProvider);
        this.cellTable = new CellTable<>(DEFAULT_PAGE_SIZE, keyProvider);
        this.pager = new Pager();
        this.appearance = Appearance.create(id, new I18n(constants, messages));

        dataProvider.addDataDisplay(cellTable);
        pager.setDisplay(cellTable);
        cellTable.setSelectionModel(selectionModel);
    }

    @Override
    public Element asElement() {
        return appearance.asElement();
    }


    // ------------------------------------------------------ buttons

    public void addButton(String label, EventListener listener) {
        addButton(button(label), DEFAULT_BUTTON_GROUP, listener);
    }

    public void addButton(String label, String group, EventListener listener) {
        addButton(button(label), group, listener);
    }

    public void addButton(Element button, EventListener listener) {
        addButton(button, DEFAULT_BUTTON_GROUP, listener);
    }

    public void addButton(Element button, String group, EventListener listener) {
        button.setOnclick(listener);
        Element groupElement = Browser.getDocument().querySelector("[data-button-group]");
        if (groupElement == null) {
            groupElement = buttonGroup(group);
            appearance.buttonToolbar.appendChild(groupElement);
        }
        groupElement.appendChild(button);
    }

    private Element button(String label) {
        // <button type="button" class="btn btn-default">1</button>
        ButtonElement button = Browser.getDocument().createButtonElement();
        button.setClassName("btn btn-default");
        button.setAttribute("type", "button");
        button.setInnerText(label);
        return button;
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
}
