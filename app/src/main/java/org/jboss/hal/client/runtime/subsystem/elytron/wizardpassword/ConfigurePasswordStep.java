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
package org.jboss.hal.client.runtime.subsystem.elytron.wizardpassword;

import java.util.Map;

import elemental2.dom.HTMLElement;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.ballroom.form.ValidationResult;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.Ids.FORM;

public class ConfigurePasswordStep extends WizardStep<PasswordContext, PasswordState> {

    private HTMLElement section;
    private Resources resources;
    private Metadata metadata;
    private Form<ModelNode> form;
    private Map<String, Object> changedValues;
    private HTMLElement header;
    private HTMLElement description;

    public ConfigurePasswordStep(Resources resources, Metadata metadata) {
        super(resources.constants().configuration());
        this.resources = resources;
        this.metadata = metadata;

        section = section()
                .add(header = h(1).asElement())
                .add(description = p().asElement())
                .asElement();
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

        String id = Ids.build(template.lastName(), SET_PASSWORD, FORM);
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(id, passwordMetadata)
                .onSave((form1, changedValues) -> this.changedValues = changedValues);
        passwordMetadata.getDescription().getAttributes(ATTRIBUTES).forEach(attr -> {
            if (ModelType.BYTES.equals(attr.getValue().get(TYPE).asType())) {
                builder.customFormItem(attr.getName(), desc -> {
                    TextBoxItem saltItem = new TextBoxItem(attr.getName(), labelBuilder.label(attr.getName()));
                    saltItem.setRequired(desc.getValue().get(REQUIRED).asBoolean());
                    saltItem.addValidationHandler(value -> {
                        ValidationResult result = ValidationResult.OK;
                        // accordingly to the :set-password operation, the salt must be exactly 16 bytes
                        if (value.length() != 16) {
                            result = ValidationResult.invalid(resources.messages().invalidLength());
                        }
                        return result;
                    });
                    return saltItem;
                });
            }
        });
        form = builder.build();
        HTMLElement formElement = form.element();
        form.attach();
        form.edit(new ModelNode());

        // as the form is dynamically added to the section, we must remove the previous form element
        if (section.childElementCount > 2) {
            section.removeChild(section.lastChild);
        }
        section.appendChild(formElement);

    }

    @Override
    protected boolean onNext(PasswordContext context) {
        boolean valid = form.save();
        if (valid) {
            context.model = new ModelNode();
            this.changedValues.forEach((key, value) -> {
                if (value != null) {
                    ModelNode modelValue = new ModelNode();
                    if (SALT.equals(key)) {
                        byte[] bytes = value.toString().getBytes();
                        modelValue.set(bytes);
                        /*
                        StringBuilder bb = new StringBuilder("bytes{");
                        for (int i = 0; i < bytes.length; i++) {
                            bb.append(bytes[i]);
                            if (i + 1 < bytes.length) {
                                bb.append(",");
                            }
                        }
                        bb.append("}");
                        modelValue.set(bb.toString());
                        */
                    } else {
                        modelValue.set(value.toString());
                    }
                    context.model.get(key).set(modelValue);
                }
            });
        }
        return valid;
    }

}
