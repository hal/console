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

import java.util.Set;

import org.jboss.elemento.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CUSTOM;
import static org.jboss.hal.resources.CSS.radio;

class ChooseProtocolStep extends WizardStep<ProtocolWizard.Context, ProtocolWizard.State> {

    private final HTMLElement root;

    ChooseProtocolStep(Resources resources, Set<String> protocolNames, ProtocolWizard wizard) {
        super(resources.constants().chooseProtocol());
        root = div()
                .add(p().textContent(resources.messages().chooseProtocol(CUSTOM))).element();

        for (String protocolName : protocolNames) {
            String name = protocolName;
            if (name.equals("*")) {
                name = CUSTOM;
            }
            root.appendChild(div().css(radio)
                    .add(label()
                            .add(input(InputType.radio)
                                    .id(Ids.build(Ids.JGROUPS_PROTOCOL, name))
                                    .attr(UIConstants.NAME, Ids.JGROUPS_PROTOCOL)
                                    .on(click, e -> wizard.setProtocol(protocolName)).element())
                            .add(span().textContent(name)))
                    .element());
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }
}
