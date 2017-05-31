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
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.radio;

/**
 * @author Harald Pehl
 */
public class ChoosePolicyStep extends WizardStep<HaPolicyWizard.Context, HaPolicyWizard.State> {

    private final HTMLElement root;

    private final HTMLElement replicationForm;
    private final HTMLInputElement replicationLiveRadio;
    private final HTMLInputElement replicationMasterRadio;
    private final HTMLInputElement replicationSlaveRadio;
    private final HTMLInputElement replicationColocatedRadio;

    private final HTMLElement sharedStoreForm;
    private final HTMLInputElement sharedStoreMasterRadio;
    private final HTMLInputElement sharedStoreSlaveRadio;
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
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().replicationLiveOnly()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationMasterRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_MASTER)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_MASTER)
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().replicationMaster()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationSlaveRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_SLAVE)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_SLAVE)
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().replicationSlave()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(replicationColocatedRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_REPLICATION_COLOCATED)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_COLOCATED)
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().replicationColocated()))))
                        .asElement())

                .add(sharedStoreForm = div().css(formHorizontal)
                        .add(p().innerHtml(resources.messages().chooseSharedStore()))
                        .add(div().css(radio)
                                .add(label()
                                        .add(sharedStoreMasterRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_SHARED_STORE_MASTER)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_MASTER)
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().sharedStoreMaster()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(sharedStoreSlaveRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_SHARED_STORE_SLAVE)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_SLAVE)
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().sharedStoreSlave()))))
                        .add(div().css(radio)
                                .add(label()
                                        .add(sharedStoreColocatedRadio = input(InputType.radio)
                                                .id(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED)
                                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                                .on(click,
                                                        e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_COLOCATED)
                                                .asElement())
                                        .add(span().innerHtml(resources.messages().sharedStoreColocated()))))
                        .asElement())
                .asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    @Override
    protected void onShow(final HaPolicyWizard.Context context) {
        Elements.setVisible(replicationForm, context.replication);
        Elements.setVisible(sharedStoreForm, !context.replication);

        if (context.haPolicy != null) {
            replicationLiveRadio.checked = context.haPolicy == HaPolicy.LIVE_ONLY;
            replicationMasterRadio.checked = context.haPolicy == HaPolicy.REPLICATION_MASTER;
            replicationSlaveRadio.checked = context.haPolicy == HaPolicy.REPLICATION_SLAVE;
            replicationColocatedRadio.checked = context.haPolicy == HaPolicy.REPLICATION_COLOCATED;

            sharedStoreMasterRadio.checked = context.haPolicy == HaPolicy.SHARED_STORE_MASTER;
            sharedStoreSlaveRadio.checked = context.haPolicy == HaPolicy.SHARED_STORE_SLAVE;
            sharedStoreColocatedRadio.checked = context.haPolicy == HaPolicy.SHARED_STORE_COLOCATED;

        } else {
            replicationLiveRadio.checked = true;
            replicationMasterRadio.checked = false;
            replicationSlaveRadio.checked = false;
            replicationColocatedRadio.checked = false;

            sharedStoreMasterRadio.checked = true;
            sharedStoreSlaveRadio.checked = false;
            sharedStoreColocatedRadio.checked = false;
        }
    }
}
