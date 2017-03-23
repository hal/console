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

import elemental.dom.Element;
import elemental.html.InputElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.InputType;
import org.jboss.hal.ballroom.wizard.WizardStep;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.formHorizontal;
import static org.jboss.hal.resources.CSS.radio;

/**
 * @author Harald Pehl
 */
public class ChoosePolicyStep extends WizardStep<HaPolicyWizard.Context, HaPolicyWizard.State> {

    private static final String REPLICATION_FORM = "replicationForm";
    private static final String REPLICATION_LIVE_RADIO = "replicationLiveRadio";
    private static final String REPLICATION_MASTER_RADIO = "replicationMasterRadio";
    private static final String REPLICATION_SLAVE_RADIO = "replicationSlaveRadio";
    private static final String REPLICATION_COLOCATED_RADIO = "replicationColocatedRadio";

    private static final String SHARED_STORE_FORM = "sharedStoreForm";
    private static final String SHARED_STORE_MASTER_RADIO = "sharedStoreMasterRadio";
    private static final String SHARED_STORE_SLAVE_RADIO = "sharedStoreSlaveRadio";
    private static final String SHARED_STORE_COLOCATED_RADIO = "sharedStoreColocatedRadio";

    private final Element root;

    private final Element replicationForm;
    private final InputElement replicationLiveRadio;
    private final InputElement replicationMasterRadio;
    private final InputElement replicationSlaveRadio;
    private final InputElement replicationColocatedRadio;

    private final Element sharedStoreForm;
    private final InputElement sharedStoreMasterRadio;
    private final InputElement sharedStoreSlaveRadio;
    private final InputElement sharedStoreColocatedRadio;

    ChoosePolicyStep(Resources resources) {
        super(resources.constants().choosePolicy());

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div()
                .div().css(formHorizontal).rememberAs(REPLICATION_FORM)
                    .p().innerHtml(resources.messages().chooseReplication()).end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(REPLICATION_LIVE_RADIO)
                                .id(Ids.MESSAGING_HA_REPLICATION_LIVE_ONLY)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.LIVE_ONLY)
                            .span().innerHtml(resources.messages().replicationLiveOnly()).end()
                        .end()
                    .end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(REPLICATION_MASTER_RADIO)
                                .id(Ids.MESSAGING_HA_REPLICATION_MASTER)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_MASTER)
                            .span().innerHtml(resources.messages().replicationMaster()).end()
                        .end()
                    .end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(REPLICATION_SLAVE_RADIO)
                                .id(Ids.MESSAGING_HA_REPLICATION_SLAVE)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_SLAVE)
                            .span().innerHtml(resources.messages().replicationSlave()).end()
                        .end()
                    .end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(REPLICATION_COLOCATED_RADIO)
                                .id(Ids.MESSAGING_HA_REPLICATION_COLOCATED)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_REPLICATION)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.REPLICATION_COLOCATED)
                            .span().innerHtml(resources.messages().replicationColocated()).end()
                        .end()
                    .end()
                .end()
                .div().css(formHorizontal).rememberAs(SHARED_STORE_FORM)
                    .p().innerHtml(resources.messages().chooseSharedStore()).end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(SHARED_STORE_MASTER_RADIO)
                                .id(Ids.MESSAGING_HA_SHARED_STORE_MASTER)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_MASTER)
                            .span().innerHtml(resources.messages().sharedStoreMaster()).end()
                        .end()
                    .end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(SHARED_STORE_SLAVE_RADIO)
                                .id(Ids.MESSAGING_HA_SHARED_STORE_SLAVE)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_SLAVE)
                            .span().innerHtml(resources.messages().sharedStoreSlave()).end()
                        .end()
                    .end()
                    .div().css(radio)
                        .label()
                            .input(InputType.radio)
                                .rememberAs(SHARED_STORE_COLOCATED_RADIO)
                                .id(Ids.MESSAGING_HA_SHARED_STORE_COLOCATED)
                                .attr(UIConstants.NAME, Ids.MESSAGING_HA_SHARED_STORE)
                                .on(click, e -> wizard().getContext().haPolicy = HaPolicy.SHARED_STORE_COLOCATED)
                            .span().innerHtml(resources.messages().sharedStoreColocated()).end()
                        .end()
                    .end()
                .end()
            .end();
        // @formatter:on

        replicationForm = builder.referenceFor(REPLICATION_FORM);
        replicationLiveRadio = builder.referenceFor(REPLICATION_LIVE_RADIO);
        replicationColocatedRadio = builder.referenceFor(REPLICATION_COLOCATED_RADIO);
        replicationMasterRadio = builder.referenceFor(REPLICATION_MASTER_RADIO);
        replicationSlaveRadio = builder.referenceFor(REPLICATION_SLAVE_RADIO);

        sharedStoreForm = builder.referenceFor(SHARED_STORE_FORM);
        sharedStoreColocatedRadio = builder.referenceFor(SHARED_STORE_COLOCATED_RADIO);
        sharedStoreMasterRadio = builder.referenceFor(SHARED_STORE_MASTER_RADIO);
        sharedStoreSlaveRadio = builder.referenceFor(SHARED_STORE_SLAVE_RADIO);

        root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    protected void onShow(final HaPolicyWizard.Context context) {
        Elements.setVisible(replicationForm, context.replication);
        Elements.setVisible(sharedStoreForm, !context.replication);

        if (context.haPolicy != null) {
            replicationLiveRadio.setChecked(context.haPolicy == HaPolicy.LIVE_ONLY);
            replicationMasterRadio.setChecked(context.haPolicy == HaPolicy.REPLICATION_MASTER);
            replicationSlaveRadio.setChecked(context.haPolicy == HaPolicy.REPLICATION_SLAVE);
            replicationColocatedRadio.setChecked(context.haPolicy == HaPolicy.REPLICATION_COLOCATED);

            sharedStoreMasterRadio.setChecked(context.haPolicy == HaPolicy.SHARED_STORE_MASTER);
            sharedStoreSlaveRadio.setChecked(context.haPolicy == HaPolicy.SHARED_STORE_SLAVE);
            sharedStoreColocatedRadio.setChecked(context.haPolicy == HaPolicy.SHARED_STORE_COLOCATED);

        } else {
            replicationLiveRadio.setChecked(true);
            replicationMasterRadio.setChecked(false);
            replicationSlaveRadio.setChecked(false);
            replicationColocatedRadio.setChecked(false);

            sharedStoreMasterRadio.setChecked(true);
            sharedStoreSlaveRadio.setChecked(false);
            sharedStoreColocatedRadio.setChecked(false);
        }
    }
}
