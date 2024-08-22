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
package org.jboss.hal.client.runtime.subsystem.elytron.wizardpassword;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SALT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SET_PASSWORD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.resources.Ids.FORM;

public class ReviewPasswordStep extends WizardStep<PasswordContext, PasswordState> {

    private HTMLElement section;
    private Metadata metadata;
    private Form<ModelNode> form;
    private HTMLElement header;
    private HTMLElement description;

    public ReviewPasswordStep(Resources resources, Metadata metadata) {
        super(resources.constants().review());
        this.metadata = metadata;

        section = section()
                .add(header = h(1).element())
                .add(description = p().element()).element();
    }

    @Override
    public HTMLElement element() {
        return section;
    }

    @Override
    protected void onShow(PasswordContext context) {
        AddressTemplate template = metadata.getTemplate();
        Metadata passwordMetadata = metadata.forOperation(SET_PASSWORD).forComplexAttribute(context.type.name);
        LabelBuilder labelBuilder = new LabelBuilder();
        header.textContent = labelBuilder.label(context.type.name);
        description.textContent = passwordMetadata.getDescription().getDescription();
        String id = Ids.build(template.lastName(), SET_PASSWORD, "review", FORM);
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, passwordMetadata)
                .readOnly();
        passwordMetadata.getDescription().attributes().forEach(attr -> {
            if (ModelType.BYTES.equals(attr.getValue().get(TYPE).asType())) {
                builder.customFormItem(attr.getName(),
                        desc -> new TextBoxItem(attr.getName(), labelBuilder.label(attr.getName())));
            }
        });
        form = builder.build();
        HTMLElement formElement = form.element();
        form.attach();
        form.view(context.model);
        if (context.model.hasDefined(SALT)) {
            form.getFormItem(SALT).setValue(context.model.get(SALT).asString());
        }

        // as the form is dynamically added to the section, we must remove the previous form element
        if (section.childElementCount > 0) {
            section.removeChild(section.lastChild);
        }
        section.appendChild(formElement);

    }

    @Override
    protected boolean onNext(PasswordContext context) {
        return true;
    }
}
