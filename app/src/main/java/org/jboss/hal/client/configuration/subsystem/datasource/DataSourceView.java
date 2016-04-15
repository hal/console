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
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.ANY_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONNECTION_URL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JNDI_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.resources.Ids.DATA_SOURCE_ATTRIBUTES_FORM;
import static org.jboss.hal.resources.Ids.DATA_SOURCE_ATTRIBUTES_TAB;
import static org.jboss.hal.resources.Ids.DATA_SOURCE_CONNECTION_FORM;
import static org.jboss.hal.resources.Ids.DATA_SOURCE_CONNECTION_TAB;
import static org.jboss.hal.resources.Names.ATTRIBUTES;

/**
 * @author Harald Pehl
 */
public class DataSourceView extends PatternFlyViewImpl implements DataSourcePresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";

    private final List<Form<ModelNode>> forms;
    private Element header;
    private DataSourcePresenter presenter;

    @Inject
    public DataSourceView(MetadataRegistry metadataRegistry) {

        Metadata metadata = metadataRegistry.lookup(ANY_DATA_SOURCE_TEMPLATE);
        Element info = new Elements.Builder().p().textContent(metadata.getDescription().getDescription()).end().build();

        forms = new ArrayList<>();
        Tabs tabs = new Tabs();
        ModelNodeForm<ModelNode> currentForm;
        Form.SaveCallback<ModelNode> saveCallback = (form, changedValues) -> presenter.saveDataSource(changedValues);

        currentForm = new ModelNodeForm.Builder<>(DATA_SOURCE_ATTRIBUTES_FORM, metadata)
                .include(JNDI_NAME, ENABLED, DRIVER_NAME, STATISTICS_ENABLED)
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(DATA_SOURCE_ATTRIBUTES_TAB, ATTRIBUTES, currentForm.asElement());

        currentForm = new ModelNodeForm.Builder<>(DATA_SOURCE_CONNECTION_FORM, metadata)
                .include(CONNECTION_URL, "new-connection-sql", "transaction-isolation", "jta", "use-ccm")
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(DATA_SOURCE_CONNECTION_TAB, "Connection", currentForm.asElement()); //NON-NLS

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.DATASOURCE).rememberAs(HEADER_ELEMENT).end()
                    .add(info)
                    .add(tabs.asElement())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        header = layoutBuilder.referenceFor(HEADER_ELEMENT);
        registerAttachables(forms);
        initElement(root);
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
