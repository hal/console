/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.datasource.wizard;

import java.util.List;

import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplate;
import org.jboss.hal.client.configuration.subsystem.datasource.DataSourceTemplates;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;

class ChooseTemplateStep extends WizardStep<Context, State> {

    private final HTMLElement root;

    ChooseTemplateStep(DataSourceTemplates templates, Resources resources, boolean xa) {
        super(resources.constants().chooseTemplate());

        root = div()
                .add(p().textContent(resources.messages().chooseTemplate(Names.CUSTOM))).element();

        root.appendChild(div().css(CSS.radio)
                .add(label()
                        .add(input(InputType.radio)
                                .attr("name", "template") // NON-NLS
                                .attr("value", "custom")
                                .on(click, event -> wizard().getContext().template = null))
                        .add(span().textContent(Names.CUSTOM)))
                .element());

        List<DataSourceTemplate> matchingTemplates = stream(templates.spliterator(), false)
                .filter(t -> t.getDataSource().isXa() == xa).collect(toList());
        for (DataSourceTemplate template : matchingTemplates) {
            root.appendChild(div().css(CSS.radio)
                    .add(label()
                            .add(input(InputType.radio)
                                    .attr("name", "template") // NON-NLS
                                    .attr("value", template.getId())
                                    .on(click, event -> {
                                        String id = ((HTMLInputElement) event.target).value;
                                        wizard().getContext().template = templates.getTemplate(id);
                                    }))
                            .add(span().textContent(template.getVendor().label)))
                    .element());
        }

        HTMLInputElement firstRadio = (HTMLInputElement) root.querySelector("input[type=radio]"); // NON-NLS
        firstRadio.checked = true;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected boolean onNext(Context context) {
        if (context.template != null) {
            context.useTemplate();
        } else {
            context.custom();
        }
        return true;
    }
}
