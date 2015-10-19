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
import com.google.gwt.view.client.ProvidesKey;
import com.gwtplatform.mvp.client.ViewImpl;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.DataTableButton;
import org.jboss.hal.client.bootstrap.endpoint.EndpointResources;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.StaticResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.resources.I18n;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.hal.ballroom.table.DataTableButton.Target.GLOBAL;
import static org.jboss.hal.ballroom.table.DataTableButton.Target.ROW;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

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

    @Inject
    public ConfigurationView(EndpointResources endpointResources, I18n i18n) {
        //noinspection Convert2MethodRef
        DataTable<FooBar> dataTable = new DataTable<>("foobar-table", (ProvidesKey<FooBar>) fooBar -> fooBar.id(),
                SecurityContext.RWX);

        dataTable.addButton(new DataTableButton("Foo", GLOBAL, event -> Window.alert("Bar")));
        dataTable.addButton(new DataTableButton("Bar", ROW, event -> Window.alert("Foo")));

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

        ResourceDescription resourceDescription = StaticResourceDescription.from(endpointResources.endpoint());
        ModelNodeTable modelNodeTable = new ModelNodeTable.Builder("endpoint", node -> node.get(NAME).asString(),
                SecurityContext.RWX,
                resourceDescription)
                .addColumn("name", "host-name", "port")
                .addButton(new DataTableButton(i18n.constants().add(), GLOBAL,
                        event -> Browser.getWindow().alert("NYI")))
                .addButton(new DataTableButton(i18n.constants().remove(), ROW,
                        event -> Browser.getWindow().alert("NYI")))
                .build();

        ModelNode node = new ModelNode();
        node.get("name").set("local");
        node.get("host-name").set("127.0.0.1");
        node.get("port").set("9990");
        modelNodeTable.setData(Collections.singletonList(node));

        Element element = new LayoutBuilder()
                .header("DataTable")
                .add(dataTable.asElement())
                .header("ModelNodeTable")
                .add(modelNodeTable.asElement())
                .build();
        initWidget(Elements.asWidget(element));
    }
}
