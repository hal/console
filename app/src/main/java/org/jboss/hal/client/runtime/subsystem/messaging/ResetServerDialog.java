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
package org.jboss.hal.client.runtime.subsystem.messaging;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.dialog.Dialog.Size;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.SwitchItem;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.form.RequireAtLeastOneAttributeValidation;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.Strings;

import elemental2.dom.CSSProperties.WidthUnionType;

import static java.util.Arrays.asList;
import static org.jboss.elemento.Elements.asHtmlElement;
import static org.jboss.elemento.Elements.htmlElements;
import static org.jboss.elemento.Elements.stream;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESET_ALL_MESSAGE_COUNTERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESET_ALL_MESSAGE_COUNTER_HISTORIES;
import static org.jboss.hal.resources.CSS.halFormInput;
import static org.jboss.hal.resources.CSS.halFormLabel;

class ResetServerDialog {

    private final ServerColumn column;
    private final Metadata metadata;
    private final Resources resources;

    ResetServerDialog(ServerColumn column, Metadata metadata, Resources resources) {
        this.column = column;
        this.metadata = metadata;
        this.resources = resources;
    }

    void reset(String messagingServer) {
        LabelBuilder labelBuilder = new LabelBuilder();

        String l1 = labelBuilder.label(RESET_ALL_MESSAGE_COUNTERS);
        Property p1 = metadata.getDescription().findOperation(RESET_ALL_MESSAGE_COUNTERS);
        if (p1 != null && p1.getValue().hasDefined(DESCRIPTION)) {
            l1 = p1.getValue().get(DESCRIPTION).asString();
            l1 = Strings.sanitize(l1);
        }

        String l2 = labelBuilder.label(RESET_ALL_MESSAGE_COUNTER_HISTORIES);
        Property p2 = metadata.getDescription().findOperation(RESET_ALL_MESSAGE_COUNTER_HISTORIES);
        if (p2 != null && p2.getValue().hasDefined(DESCRIPTION)) {
            l2 = p2.getValue().get(DESCRIPTION).asString();
            l2 = Strings.sanitize(l2);
        }

        Form<ModelNode> form = new ModelNodeForm.Builder<>(Ids.RESET_MESSAGE_COUNTERS, Metadata.empty())
                .unboundFormItem(new SwitchItem(RESET_ALL_MESSAGE_COUNTERS, l1))
                .unboundFormItem(new SwitchItem(RESET_ALL_MESSAGE_COUNTER_HISTORIES, l2))
                .onSave((f, changedValues) -> column.resetServer(messagingServer,
                        !f.getFormItem(RESET_ALL_MESSAGE_COUNTERS).isEmpty(),
                        !f.getFormItem(RESET_ALL_MESSAGE_COUNTER_HISTORIES).isEmpty()))
                .build();
        form.addFormValidation(new RequireAtLeastOneAttributeValidation<>(
                asList(RESET_ALL_MESSAGE_COUNTERS, RESET_ALL_MESSAGE_COUNTER_HISTORIES), resources));

        // Make the long labels more readable
        stream(form.element().querySelectorAll("." + halFormLabel + ", ." + halFormInput))
                .filter(htmlElements())
                .map(asHtmlElement())
                .forEach(element -> element.style.width = WidthUnionType.of("50%"));

        Dialog dialog = new Dialog.Builder(resources.constants().reset())
                .add(form.element())
                .primary(resources.constants().reset(), form::save)
                .size(Size.MEDIUM)
                .closeIcon(true)
                .closeOnEsc(true)
                .cancel()
                .build();
        dialog.registerAttachable(form);
        form.edit(new ModelNode());
        dialog.show();
    }
}
