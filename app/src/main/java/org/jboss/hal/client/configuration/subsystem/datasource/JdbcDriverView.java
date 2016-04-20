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
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.JDBC_DRIVER_ATTRIBUTES_FORM;
import static org.jboss.hal.resources.Ids.JDBC_DRIVER_ATTRIBUTES_TAB;

/**
 * @author Harald Pehl
 */
public class JdbcDriverView extends PatternFlyViewImpl implements JdbcDriverPresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";

    private final List<Form<JdbcDriver>> forms;
    private Element header;
    private JdbcDriverPresenter presenter;

    @Inject
    public JdbcDriverView(MetadataRegistry metadataRegistry,
            Resources resources) {

        Metadata metadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);
        Element info = new Elements.Builder().p().textContent(metadata.getDescription().getDescription()).end().build();

        forms = new ArrayList<>();
        Tabs tabs = new Tabs();
        ModelNodeForm<JdbcDriver> currentForm;
        Form.SaveCallback<JdbcDriver> saveCallback = (form, changedValues) -> presenter.saveJdbcDriver(changedValues);

        currentForm = new ModelNodeForm.Builder<JdbcDriver>(JDBC_DRIVER_ATTRIBUTES_FORM, metadata)
                .include(DRIVER_NAME, DRIVER_MODULE_NAME, DRIVER_CLASS_NAME,
                        DRIVER_DATASOURCE_CLASS_NAME, DRIVER_XA_DATASOURCE_CLASS_NAME,
                        DRIVER_MAJOR_VERSION, DRIVER_MINOR_VERSION)
                .onSave(saveCallback)
                .build();
        forms.add(currentForm);
        tabs.add(JDBC_DRIVER_ATTRIBUTES_TAB, resources.constants().attributes(), currentForm.asElement());

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.JDBC_DRIVER).rememberAs(HEADER_ELEMENT).end()
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
    public void setPresenter(final JdbcDriverPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final JdbcDriver driver) {
        header.setTextContent(driver.getName());
        for (Form<JdbcDriver> form : forms) {
            form.view(driver);
        }
    }
}
