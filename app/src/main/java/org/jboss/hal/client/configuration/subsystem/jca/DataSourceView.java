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
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.tab.Tabs;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;
import org.jboss.hal.resources.Names;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.resources.Ids.*;
import static org.jboss.hal.resources.Names.ATTRIBUTES;

/**
 * @author Harald Pehl
 */
public class DataSourceView extends PatternFlyViewImpl implements DataSourcePresenter.MyView {

    private final List<Form<ModelNode>> forms;
    private Element header;
    private DataSourcePresenter presenter;

    @Inject
    public DataSourceView(ResourceDescriptions descriptions,
            SecurityFramework securityFramework) {

        ResourceDescription description = descriptions.lookup(DataSourcePresenter.ROOT_TEMPLATE);
        SecurityContext securityContext = securityFramework.lookup(DataSourcePresenter.ROOT_TEMPLATE);

        Element info = new Elements.Builder().p().innerText(description.getDescription()).end().build();

        forms = new ArrayList<>();
        Tabs tabs = new Tabs();
        ModelNodeForm<ModelNode> currentForm;
        Form.SaveCallback<ModelNode> saveCallback = (form, changedValues) -> presenter.saveDataSource(changedValues);

        currentForm = new ModelNodeForm.Builder<>(DATA_SOURCE_ATTRIBUTES_FORM, securityContext, description)
                .include(ModelDescriptionConstants.JNDI_NAME, ENABLED, "statistics-enabled", "driver-name")
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(DATA_SOURCE_ATTRIBUTES_TAB, ATTRIBUTES, currentForm.asElement());

        currentForm = new ModelNodeForm.Builder<>(DATA_SOURCE_CONNECTION_FORM, securityContext, description)
                .include("connection-url", "new-connection-sql", "transaction-isolation", "jta", "use-ccm")
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(DATA_SOURCE_CONNECTION_TAB, "Connection", currentForm.asElement()); //NON-NLS

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .startRow()
                .header(Names.DATASOURCE)
                .add(info)
                .add(tabs.asElement())
            .endRow();
        // @formatter:on

        header = layoutBuilder.headerElement();
        registerAttachables(forms);
        initWidget(Elements.asWidget(layoutBuilder.build()));
    }

    @Override
    public void setPresenter(final DataSourcePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final String name, final ModelNode datasource) {
        header.setTextContent(name);
        for (Form<ModelNode> form : forms) {
            form.view(datasource);
        }
    }
}
