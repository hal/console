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
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEW_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OLD_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.resources.CSS.marginTopLarge;

class ListUpdatesStep extends WizardStep<UpdateContext, UpdateState> {

    private final HTMLElement root;
    private final Table<ModelNode> table;

    ListUpdatesStep(Resources resources) {
        super(resources.constants().listUpdates());

        table = new ModelNodeTable.Builder<ModelNode>(Ids.build(Ids.INSTALLER_UPDATE, Ids.TABLE),
                Metadata.staticDescription(InstallerResources.INSTANCE.artifactChange()))
                .columns(NAME, STATUS, OLD_VERSION, NEW_VERSION)
                .build();
        registerAttachable(table);

        root = div()
                .add(p().textContent(resources.constants().listUpdatesAvailable()))
                .add(table)
                .add(p().css(marginTopLarge)
                        .textContent(resources.constants().listUpdatesSteps()))
                .add(h(4).textContent(resources.constants().prepareServer()))
                .add(p().textContent(resources.constants().prepareServerDescription()))
                .add(h(4).textContent(resources.constants().applyUpdate()))
                .add(p().textContent(resources.constants().applyUpdateStepDescription()))
                .add(p().css(marginTopLarge).textContent(resources.constants().listUpdatesStepsSummary()))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(final UpdateContext context) {
        table.update(context.updates);
    }
}
