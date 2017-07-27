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

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.messaging.HaPolicyWizard.State.CHOOSE_POLICY;
import static org.jboss.hal.client.configuration.subsystem.messaging.HaPolicyWizard.State.CHOOSE_STRATEGY;

class HaPolicyWizard {

    enum State {
        CHOOSE_STRATEGY, CHOOSE_POLICY
    }


    static class Context {

        boolean replication = true;
        HaPolicy haPolicy = null;
    }


    private final Wizard<Context, State> wizard;

    HaPolicyWizard(final Resources resources, Wizard.FinishCallback<Context, State> callback) {
        wizard = new Wizard.Builder<Context, State>(resources.messages().addResourceTitle(Names.HA_POLICY),
                new Context())

                .onBack((context, currentState) -> {
                    State state = null;
                    switch (currentState) {
                        case CHOOSE_POLICY:
                            state = CHOOSE_STRATEGY;
                            break;
                    }
                    return state;
                })

                .onNext((context, currentState) -> {
                    State state = null;
                    switch (currentState) {
                        case CHOOSE_STRATEGY:
                            state = CHOOSE_POLICY;
                            break;
                    }
                    return state;
                })

                .onFinish(callback)

                .addStep(CHOOSE_STRATEGY, new ChooseStrategyStep(resources))
                .addStep(CHOOSE_POLICY, new ChoosePolicyStep(resources))
                .build();
    }

    void show() {
        wizard.show();
    }
}
