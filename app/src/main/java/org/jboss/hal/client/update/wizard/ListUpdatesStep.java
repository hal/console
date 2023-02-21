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
package org.jboss.hal.client.update.wizard;

import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.dd;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.dl;
import static org.jboss.elemento.Elements.dt;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;

class ListUpdatesStep extends WizardStep<UpdateContext, UpdateState> {

    private final HTMLElement root;
    private final HTMLElement pre;

    ListUpdatesStep(Resources resources) {
        super("List Updates");

        root = div()
                .add(p().textContent("The following updates are available:"))
                .add(pre = pre().element())
                .add(p().textContent("This wizard will guide you through the steps to apply these updates."))
                .add(dl()
                        .add(dt().textContent("Prepare server"))
                        .add(dd().textContent(
                                "This step prepares the new server installation. Once this step is completed, you can still decide whether you want to cancel the update."))
                        .add(dt().textContent("Apply update"))
                        .add(dd().textContent("This step will restart the server and apply the updates.")))
                .add(p().textContent(
                        "Each of these steps blocks the console and uses a specific timeout for successful execution. If the step runs into a timeout, the wizard is canceled. This does not necessarily mean that the update has failed. In this case, please check the log files to see if the update was successful."))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(final UpdateContext context) {
        pre.textContent = String.join("\n", context.updates);
    }
}
