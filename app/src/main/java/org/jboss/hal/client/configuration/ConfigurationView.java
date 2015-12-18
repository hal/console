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
package org.jboss.hal.client.configuration;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.Console;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.table.Button.Scope;
import org.jboss.hal.ballroom.table.ColumnBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.ballroom.table.OptionsBuilder;
import org.jboss.hal.client.bootstrap.endpoint.Endpoint;
import org.jboss.hal.client.bootstrap.endpoint.EndpointResources;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;

import javax.inject.Inject;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.meta.security.SecurityContext.RWX;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ConfigurationView extends ViewImpl implements ConfigurationPresenter.MyView {

    static class FooBarBaz {

        final String foo;
        final String bar;
        final String baz;

        FooBarBaz() {
            foo = "Foo-" + String.valueOf(Random.nextInt(12345));
            bar = "Bar-" + String.valueOf(Random.nextInt(12345));
            baz = "Baz-" + String.valueOf(Random.nextInt(12345));
        }

        @Override
        public String toString() {
            return "(" + foo + ", " + bar + ", " + baz + ")";
        }
    }


    private DataTable<FooBarBaz> fooBarBazTable;
    private ModelNodeTable<Endpoint> endpointTable;

    @Inject
    public ConfigurationView(EndpointResources endpointResources) {
        Options<FooBarBaz> fooBarBazOptions = new OptionsBuilder<FooBarBaz>()
                .button("Add row", (event, api) -> api.add(new FooBarBaz()).refresh(RESET))
                .button("Select something", Scope.SELECTED, (event, api) -> Window.alert("Good job!"))
                .column("foo", "Foo", (cell, type, row, meta) -> row.foo)
                .column(new ColumnBuilder<FooBarBaz>("bar", "Bar", (cell, type, row, meta) -> row.bar)
                        .orderable(false)
                        .build())
                .column("baz", "Baz", (cell, type, row, meta) -> row.baz)
                .multiselect()
                .build();
        fooBarBazTable = new DataTable<>("foo-bar-baz-table", RWX, fooBarBazOptions);

        ResourceDescription endpointDescription = StaticResourceDescription.from(endpointResources.endpoint());
        Options<Endpoint> endpointOptions = new ModelNodeTable.Builder<Endpoint>(endpointDescription)
                .columns(NAME_KEY, HOST, PORT)
                .build();
        endpointTable = new ModelNodeTable<>("endpoint-table", RWX, endpointOptions);

        Element element = new LayoutBuilder()
                .header("Data Table")
                .add(fooBarBazTable.asElement())
                .header("Endpoint Table")
                .add(endpointTable.asElement())
                .build();
        initWidget(Elements.asWidget(element));
    }

    @Override
    public void attach() {
        Console console = Browser.getWindow().getConsole();

        fooBarBazTable.attach();
        fooBarBazTable.api().onSelect((api, select) ->
                console.log("Row was selected. Current selection:   " + api.selectedRows()));
        fooBarBazTable.api().onDeselect((api) ->
                console.log("Row was deselected. Current selection: " + api.selectedRows()));
        endpointTable.attach();
        PatternFly.initComponents();
    }
}
