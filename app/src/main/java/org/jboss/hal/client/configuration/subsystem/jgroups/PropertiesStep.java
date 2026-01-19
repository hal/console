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
package org.jboss.hal.client.configuration.subsystem.jgroups;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.dialog.NameItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;

class PropertiesStep extends WizardStep<ProtocolWizard.Context, ProtocolWizard.State> {

    private final HTMLElement root;
    private ModelNodeForm<ModelNode> form;
    private String selectedProtocol;
    private final Resources resources;

    PropertiesStep(Resources resources) {
        super(resources.constants().attributes());
        this.resources = resources;
        root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(ProtocolWizard.Context context) {
        if (context.protocolName.equals(selectedProtocol)) {
            return;
        }
        selectedProtocol = context.protocolName;

        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(
                Ids.build(Ids.JGROUPS_PROTOCOL, Ids.ADD, Ids.FORM), context.protocolMetadata);
        if (context.protocolName.equals("*")) {
            builder.unboundFormItem(new NameItem(), 0);
        }
        builder.fromRequestProperties();

        form = builder.build();
        form.attach();
        ProtocolElement.addCrValidation(resources, form, context.protocolMetadata.getDescription().requestProperties());
        root.replaceChildren(form.element());
        form.edit(new ModelNode());
    }

    @Override
    protected boolean onNext(ProtocolWizard.Context context) {
        boolean valid = form.save();
        context.payload = ModelNodeHelper.flatToNested(form.getModel());
        return valid;
    }
}
