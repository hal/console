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
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NEW_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OLD_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;

class InitStep<S extends Enum<S>> extends WizardStep<InstallerContext, S> {

    private final HTMLElement root;
    private final Table<ModelNode> table;

    InitStep(final String baseId,
            final String title,
            final SafeHtml tableDescription,
            final SafeHtml stepsDescription) {
        super(title);

        table = new ModelNodeTable.Builder<ModelNode>(Ids.build(baseId, Ids.TABLE),
                Metadata.staticDescription(InstallerResources.INSTANCE.artifactChange()))
                .columns(NAME, STATUS, OLD_VERSION, NEW_VERSION)
                .build();
        registerAttachable(table);

        root = div()
                .add(div().innerHtml(tableDescription))
                .add(table)
                .add(div().innerHtml(stepsDescription))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(final InstallerContext context) {
        table.update(context.updates);
    }
}
