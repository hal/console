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
package org.jboss.hal.client.installer;

import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;

import com.google.gwt.safehtml.shared.SafeHtml;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.marginBottomLarge;
import static org.jboss.hal.resources.CSS.marginTopLarge;

class ListUpdatesStep<S extends Enum<S>> extends WizardStep<UpdateManagerContext, S> {

    private final HTMLElement root;
    private final Table<ModelNode> table;

    ListUpdatesStep(final String title,
            final SafeHtml tableDescription,
            final SafeHtml stepsDescription) {
        super(title);

        table = new ModelNodeTable.Builder<ModelNode>(Ids.build(Ids.UPDATE_MANAGER_LIST_UPDATES),
                Metadata.staticDescription(UpdateManagerResources.INSTANCE.artifactChange()))
                .columns(NAME, STATUS, OLD_VERSION, NEW_VERSION)
                .build();
        registerAttachable(table);

        root = div()
                .add(div().css(marginBottomLarge).innerHtml(tableDescription))
                .add(table)
                .add(div().css(marginTopLarge).innerHtml(stepsDescription))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(final UpdateManagerContext context) {
        table.update(context.updates);
    }
}
