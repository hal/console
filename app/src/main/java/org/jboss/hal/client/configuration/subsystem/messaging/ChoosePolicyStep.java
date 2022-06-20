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
package org.jboss.hal.client.configuration.subsystem.messaging;

import org.jboss.elemento.Elements;
import org.jboss.elemento.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.input;
import static org.jboss.elemento.Elements.label;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.radio;

public class ChoosePolicyStep extends WizardStep<HaPolicyWizard.Context, HaPolicyWizard.State> {

    private final HTMLElement root;

    private final HTMLElement replicationForm;
    private final HTMLInputElement replicationLiveRadio;
    private final HTMLInputElement replicationPrimaryRadio;
    private final HTMLInputElement replicationSecondaryRadio;
    private final HTMLInputElement replicationColocatedRadio;

    private final HTMLElement sharedStoreForm;
    private final HTMLInputElement sharedStorePrimaryRadio;
    private final HTMLInputElement sharedStoreSecondaryRadio;
    private final HTMLInputElement sharedStoreColocatedRadio;

    ChoosePolicyStep(Resources resources) {
        super(resources.constants().choosePolicy());

        root = div()
                .add(replicationForm = div().css(formHorizontal)
                        .add(p().innerHtml(resources.messages().chooseReplication()))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationLiveRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_LIVE_ONLY)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.LIVE_ONLY)
                                                .element())
                                        .add(span().innerHtml(resources.messages().replicationLiveOnly()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationPrimaryRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_PRIMARY)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_PRIMARY)
                                                .element())
                                        .add(span().innerHtml(resources.messages().replicationPrimary()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationSecondaryRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_SECONDARY)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_SECONDARY)
                                                .element())
                                        .add(span().innerHtml(resources.messages().replicationSecondary()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationColocatedRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_COLOCATED)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_COLOCATED)
                                                .element())
                                        .add(span().innerHtml(resources.messages().replicationColocated()))))
                        .element())

                .add(sharedStoreForm = div().css(formHorizontal)
                        .add(p().innerHtml(resources.messages().chooseSharedStore()))
                        .add(div().css(radio)
                                .add(label()
                                        .add(sharedStorePrimaryRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_SHARED_STORE_PRIMARY)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_PRIMARY)
                                                .element())
                                        .add(span().innerHtml(resources.messages().sharedStorePrimary()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(sharedStoreSecondaryRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_SHARED_STORE_SECONDARY)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_SECONDARY)
                                                .element())
                                        .add(span().innerHtml(resources.messages().sharedStoreSecondary()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(sharedStoreColocatedRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_COLOCATED)
                                                .element())
                                        .add(span().innerHtml(resources.messages().sharedStoreColocated()))))
                        .element())
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    @Override
    protected void onShow(HaPolicyWizard.Context context) {
        Elements.setVisible(replicationForm, context.replication);
        Elements.setVisible(sharedStoreForm, !context.replication);

        if (context.haPolicy != null) {
            // change the default checked policy if user go back and change the strategy
            if (context.replication && isNotReplicationPolicySelected(context.haPolicy)) {
                replicationLiveRadio.checked = true;
                context.haPolicy = HaPolicy.LIVE_ONLY;
            } else if (!context.replication && isNotSharedStorePolicySelected(context.haPolicy)) {
                sharedStorePrimaryRadio.checked = true;
                context.haPolicy = HaPolicy.SHARED_STORE_PRIMARY;
            } else {
                // check the policy when user go back and do not change the strategy
                replicationLiveRadio.checked = context.haPolicy == HaPolicy.LIVE_ONLY;
                replicationPrimaryRadio.checked = context.haPolicy == HaPolicy.REPLICATION_PRIMARY;
                replicationSecondaryRadio.checked = context.haPolicy == HaPolicy.REPLICATION_SECONDARY;
                replicationColocatedRadio.checked = context.haPolicy == HaPolicy.REPLICATION_COLOCATED;

                sharedStorePrimaryRadio.checked = context.haPolicy == HaPolicy.SHARED_STORE_PRIMARY;
                sharedStoreSecondaryRadio.checked = context.haPolicy == HaPolicy.SHARED_STORE_SECONDARY;
                sharedStoreColocatedRadio.checked = context.haPolicy == HaPolicy.SHARED_STORE_COLOCATED;
            }
        } else {
            // set the default policy, if the user clicks "finish" without selecting a policy
            if (context.replication) {
                context.haPolicy = HaPolicy.LIVE_ONLY;
            } else {
                context.haPolicy = HaPolicy.SHARED_STORE_PRIMARY;
            }

            replicationLiveRadio.checked = true;
            replicationPrimaryRadio.checked = false;
            replicationSecondaryRadio.checked = false;
            replicationColocatedRadio.checked = false;

            sharedStorePrimaryRadio.checked = true;
            sharedStoreSecondaryRadio.checked = false;
            sharedStoreColocatedRadio.checked = false;
        }
    }

    private boolean isNotReplicationPolicySelected(HaPolicy haPolicy) {
        return haPolicy != HaPolicy.LIVE_ONLY
                && haPolicy != HaPolicy.REPLICATION_PRIMARY
                && haPolicy != HaPolicy.REPLICATION_SECONDARY
                && haPolicy != HaPolicy.REPLICATION_COLOCATED;
    }

    private boolean isNotSharedStorePolicySelected(HaPolicy haPolicy) {
        return haPolicy != HaPolicy.SHARED_STORE_PRIMARY
                && haPolicy != HaPolicy.SHARED_STORE_SECONDARY
                && haPolicy != HaPolicy.SHARED_STORE_COLOCATED;
    }
}
