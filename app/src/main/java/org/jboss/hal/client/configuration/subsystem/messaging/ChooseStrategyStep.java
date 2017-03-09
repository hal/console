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
class ChooseStrategyStep extends WizardStep<HaPolicyWizard.Context, HaPolicyWizard.State> {

    private static final String REPLICATION_RADIO = "replicationRadio";
    private static final String SHARED_STORE_RADIO = "sharedStoreRadio";

    private final Element root;
    private final InputElement replicationRadio;
    private final InputElement sharedStoreRadio;

    ChooseStrategyStep(Resources resources) {
        super(resources.constants().chooseStrategy());

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(formHorizontal)
                .p().innerHtml(resources.messages().chooseStrategy()).end()
                .div().css(radio)
                    .label()
                        .input(InputType.radio)
                            .rememberAs(REPLICATION_RADIO)
                            .id(Ids.MESSAGING_HA_REPLICATION)
                            .attr(UIConstants.NAME, Ids.MESSAGING_HA_CHOOSE_STRATEGY)
                            .on(click, e -> wizard().getContext().replication = true)
                        .span().innerHtml(resources.messages().replicationStrategy()).end()
                    .end()
                .end()
                .div().css(radio)
                    .label()
                        .input(InputType.radio)
                            .rememberAs(SHARED_STORE_RADIO)
                            .id(Ids.MESSAGING_HA_SHARED_STORE)
                            .attr(UIConstants.NAME, Ids.MESSAGING_HA_CHOOSE_STRATEGY)
                            .on(click, e -> wizard().getContext().replication = false)
                        .span().innerHtml(resources.messages().sharedStoreStrategy()).end()
                    .end()
                .end()
            .end();
        // @formatter:on

        replicationRadio = builder.referenceFor(REPLICATION_RADIO);
        sharedStoreRadio = builder.referenceFor(SHARED_STORE_RADIO);
        root = builder.build();
    }

    @Override
    public Element asElement() {
        return root;
    }

    @Override
    protected void onShow(final HaPolicyWizard.Context context) {
        replicationRadio.setChecked(context.replication);
        sharedStoreRadio.setChecked(!context.replication);
    }
}
