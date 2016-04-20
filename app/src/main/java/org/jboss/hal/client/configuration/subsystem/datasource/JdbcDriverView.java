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

import javax.inject.Inject;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver.Provider;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.JDBC_DRIVER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JDBC_DRIVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;

/**
 * @author Harald Pehl
 */
public class JdbcDriverView extends PatternFlyViewImpl implements JdbcDriverPresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";

    private final Resources resources;
    private final Form<JdbcDriver> form;
    private final Element header;
    private final Element providedBy;
    private JdbcDriverPresenter presenter;

    @Inject
    public JdbcDriverView(MetadataRegistry metadataRegistry, Resources resources) {
        this.resources = resources;

        Metadata metadata = metadataRegistry.lookup(JDBC_DRIVER_TEMPLATE);
        Element info = new Elements.Builder().p().textContent(metadata.getDescription().getDescription()).end().build();
        providedBy = Browser.getDocument().createElement("p"); //NON-NLS

        form = new ModelNodeForm.Builder<JdbcDriver>(IdBuilder.build(JDBC_DRIVER, "form"), metadata)
                .exclude(DEPLOYMENT_NAME, PROFILE)
                .onSave((f, changedValues) -> presenter.saveJdbcDriver(changedValues))
                .build();

        // @formatter:off
        //noinspection Guava
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.JDBC_DRIVER).rememberAs(HEADER_ELEMENT).end()
                    .add(info)
                    .add(providedBy)
                    .add(form)
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        header = layoutBuilder.referenceFor(HEADER_ELEMENT);
        registerAttachable(form);
        initElement(root);
    }

    @Override
    public void setPresenter(final JdbcDriverPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {
        header.setTextContent(Names.JDBC_DRIVER);
        Elements.setVisible(providedBy, false);
        form.clear();
    }

    @Override
    public void update(final JdbcDriver driver) {
        header.setTextContent(driver.getName());

        Provider provider = driver.getProvider();
        Elements.setVisible(providedBy, true);
        providedBy.setInnerHTML(
                resources.messages().jdbcDriverProvidedByPreview(provider.text(), driver.getModule()).asString());

        form.view(driver);
    }
}
