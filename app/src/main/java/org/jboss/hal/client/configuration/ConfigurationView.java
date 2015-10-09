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

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Button;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.meta.security.SecurityContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class ConfigurationView extends ViewImpl implements ConfigurationPresenter.MyView {

    static class FooBar {

        private final String foo;
        private final String bar;

        FooBar() {
            foo = "Foo-" + String.valueOf(Random.nextInt(12345));
            bar = "Bar-" + String.valueOf(Random.nextInt(12345));
        }

        String id() {
            return foo + "-" + bar;
        }
    }

    public ConfigurationView() {
        DataTable<FooBar> dataTable = new DataTable<>("foobar-table", FooBar::id, SecurityContext.RWX, true);

        dataTable.addButton(new Button("Foo", "btn btn-default"), event -> Window.alert("Bar"));
        dataTable.addButton(new Button("Bar", "btn btn-default"), event -> Window.alert("Foo"));

        dataTable.addColumn(new TextColumn<FooBar>() {
            @Override
            public String getValue(final FooBar fooBar) {
                return fooBar.foo;
            }
        }, "Foo");
        dataTable.addColumn(new TextColumn<FooBar>() {
            @Override
            public String getValue(final FooBar fooBar) {
                return fooBar.bar;
            }
        }, "Bar");

        List<FooBar> data = new ArrayList<>();
        int rows = 5 + Random.nextInt(12);
        for (int i = 0; i < rows; i++) {
            data.add(new FooBar());
        }
        dataTable.setData(data);

        Element element = new LayoutBuilder()
                .header("Sample Table")
                .add(dataTable.asElement())
                .build();
        initWidget(Elements.asWidget(element));
    }
}
