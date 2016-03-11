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
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import com.google.common.collect.FluentIterable;
import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
class ChooseTemplateStep extends WizardStep<Context, State> {

    private final Element root;

    ChooseTemplateStep(final NewDataSourceWizard wizard, final DataSourceTemplates templates,
            final Resources resources, final boolean xa) {
        super(wizard, resources.constants().chooseTemplate());

        Elements.Builder builder = new Elements.Builder().div();
        // @formatter:off
            builder.div().css(CSS.radio)
                .label()
                    .input(InputType.radio)
                        .attr("value", "custom")
                        .on(click, event -> wizard.getContext().template  = null)
                    .span().textContent(resources.constants().custom()).end()
                .end()
            .end();
            // @formatter:on

        for (DataSourceTemplate template : FluentIterable.from(templates).filter(t -> t.getDataSource().isXa() == xa)) {
            // @formatter:off
            builder.div().css(CSS.radio)
                .label()
                    .input(InputType.radio)
                        .attr("value", template.getId())
                        .on(click, event ->
                                wizard.getContext().template = templates.getTemplate(((InputElement)event).getValue()))
                    .span().textContent(template.toString()).end()
                .end()
            .end();
            // @formatter:on
        }
        this.root = builder.end().build();

        InputElement firstRadio = (InputElement) root.querySelector("input[type=radio]"); //NON-NLS
        firstRadio.setChecked(true);
    }

    @Override
    public Element asElement() {
        return root;
    }
}
