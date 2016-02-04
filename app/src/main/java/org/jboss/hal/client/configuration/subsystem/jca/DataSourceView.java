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
package org.jboss.hal.client.configuration.subsystem.jca;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.tab.Tabs;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.HOLD;
import static org.jboss.hal.resources.Ids.*;
import static org.jboss.hal.resources.Names.*;

/**
 * @author Harald Pehl
 */
public class DataSourceView extends PatternFlyViewImpl implements DataSourcePresenter.MyView {

    private final DataTable<ModelNode> table;
    private final List<Form<ModelNode>> forms;
    private DataSourcePresenter presenter;

    @Inject
    public DataSourceView(ResourceDescriptions descriptions,
            SecurityFramework securityFramework) {

        ResourceDescription description = descriptions.lookup(DataSourcePresenter.ROOT_TEMPLATE);
        SecurityContext securityContext = securityFramework.lookup(DataSourcePresenter.ROOT_TEMPLATE);

        Element info = new Elements.Builder().p().innerText(description.getDescription()).end().build();
        Options<ModelNode> options = new ModelNodeTable.Builder<>(description)
                .column(NAME_KEY, NAME_LABEL, (cell, type, row, meta) -> row.get(NAME_KEY).asString())
                .columns(JNDI_NAME, ENABLED)
                .build();
        table = new ModelNodeTable<>(DATA_SOURCE_TABLE, securityContext, options);

        forms = new ArrayList<>();
        Tabs tabs = new Tabs();
        ModelNodeForm<ModelNode> currentForm;
        Form.SaveCallback<ModelNode> saveCallback = (form, changedValues) -> {
            ModelNode selectedRow = table.api().selectedRow();
            if (selectedRow != null) {
                presenter.saveDataSource(selectedRow.get(NAME_KEY).asString(), changedValues);
            }
        };

        currentForm = new ModelNodeForm.Builder<>(DATA_SOURCE_ATTRIBUTES_FORM, securityContext, description)
                .include(JNDI_NAME, ENABLED, "statistics-enabled", "driver-name")
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(DATA_SOURCE_ATTRIBUTES_TAB, ATTRIBUTES, currentForm.asElement());

        currentForm = new ModelNodeForm.Builder<>(DATA_SOURCE_CONNECTION_FORM, securityContext, description)
                .include("connection-url", "new-connection-sql", "transaction-isolation", "jta", "use-ccm")
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(DATA_SOURCE_CONNECTION_TAB, "Connection", currentForm.asElement());

        // @formatter:off
        Element element = new LayoutBuilder()
            .startRow()
                .header("DataSources")
                .add(info)
                .add(table.asElement(), tabs.asElement())
            .endRow()
        .build();
        // @formatter:on

        registerAttachable(table, forms.toArray(new Attachable[forms.size()]));
        initWidget(Elements.asWidget(element));
    }

    @Override
    public void attach() {
        super.attach();
        table.api().bindForms(forms);
    }

    @Override
    public void setPresenter(final DataSourcePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final List<ModelNode> datasources) {
        // TODO Restore selection!
        table.api().clear().add(datasources).refresh(HOLD);
    }
}
