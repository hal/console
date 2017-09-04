/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.runtime.subsystem.webservice;

import java.util.List;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.URLItem;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.webservice.AddressTemplates.WEBSERVICES_DEPLOYMENT_ENDPOINT_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_DEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WSDL_URL;
import static org.jboss.hal.resources.Ids.FORM_SUFFIX;
import static org.jboss.hal.resources.Ids.TABLE_SUFFIX;
import static org.jboss.hal.resources.Ids.WEBSERVICES;

public class WebservicesView extends HalViewImpl implements WebservicesPresenter.MyView {

    private final Table<NamedNode> endpointTable;
    private final Form<NamedNode> endpointForm;
    private WebservicesPresenter presenter;

    @Inject
    public WebservicesView(final MetadataRegistry metadataRegistry, final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(WEBSERVICES_DEPLOYMENT_ENDPOINT_TEMPLATE);

        endpointTable = new ModelNodeTable.Builder<NamedNode>(Ids.build(WEBSERVICES, TABLE_SUFFIX), metadata)
                .button(resources.constants().reload(), table -> presenter.reload())
                .column(Names.ENDPOINT, (cell, type, row, meta) -> row.getName().replace("%3A", ":"))
                .column(Names.CONTEXT, (cell, type, row, meta) -> row.get(CONTEXT).asString())
                .column(Names.DEPLOYMENT, (cell, type, row, meta) -> row.get(HAL_DEPLOYMENT).asString())
                .build();

        endpointForm = new ModelNodeForm.Builder<NamedNode>(Ids.build(WEBSERVICES, FORM_SUFFIX), metadata)
                .includeRuntime()
                .customFormItem(WSDL_URL, desc -> new URLItem(WSDL_URL))
                .readOnly()
                .build();

        HTMLElement section = section()
                .add(h(1).textContent(Names.WEBSERVICES))
                .add(p().textContent(metadata.getDescription().getDescription()))
                .add(endpointTable)
                .add(endpointForm)
                .asElement();

        registerAttachable(endpointTable, endpointForm);

        initElement(row()
                .add(column()
                        .add(section)));
    }

    @Override
    public void attach() {
        super.attach();
        endpointTable.bindForm(endpointForm);
    }

    @Override
    public void setPresenter(final WebservicesPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final List<NamedNode> model) {
        endpointForm.clear();
        endpointTable.update(model);
    }

}
