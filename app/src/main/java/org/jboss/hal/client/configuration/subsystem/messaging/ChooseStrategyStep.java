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
package org.jboss.hal.client.configuration.subsystem.messaging;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.radio;

class ChooseStrategyStep extends WizardStep<HaPolicyWizard.Context, HaPolicyWizard.State> {

    private final HTMLElement root;
    private final HTMLInputElement replicationRadio;
    private final HTMLInputElement sharedStoreRadio;

    ChooseStrategyStep(Resources resources) {
        super(resources.constants().chooseStrategy());

        root = div().css(formHorizontal)
                .add(p().innerHtml(resources.messages().chooseStrategy()))
                .add(div().css(radio)
                        .add(label()
                                .add(replicationRadio = input(InputType.radio)
                                        .id(Ids.MESSAGING_HA_REPLICATION)
                                        .attr(UIConstants.NAME, Ids.MESSAGING_HA_CHOOSE_STRATEGY)
                                        .on(click, e -> wizard().getContext().replication = true)
                                        .get())
                                .add(span().innerHtml(resources.messages().replicationStrategy()))))
                .add(div().css(radio)
                        .add(label()
                                .add(sharedStoreRadio = input(InputType.radio)
                                        .id(Ids.MESSAGING_HA_SHARED_STORE)
                                        .attr(UIConstants.NAME, Ids.MESSAGING_HA_CHOOSE_STRATEGY)
                                        .on(click, e -> wizard().getContext().replication = false)
                                        .get())
                                .add(span().innerHtml(resources.messages().sharedStoreStrategy()))))
                .get();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(HaPolicyWizard.Context context) {
        replicationRadio.checked = context.replication;
        sharedStoreRadio.checked = !context.replication;
    }
}
