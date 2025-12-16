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
import java.util.function.Function;

import org.jboss.hal.ballroom.wizard.Wizard;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.jgroups.ProtocolWizard.State.CHOOSE_PROTOCOL_STEP;
import static org.jboss.hal.client.configuration.subsystem.jgroups.ProtocolWizard.State.PROPERTIES_STEP;

public class ProtocolWizard {

    private final Wizard<Context, State> wizard;
    private final Function<String, Metadata> getProtocolMetadata;

    public ProtocolWizard(Resources resources, Set<String> protocolNames,
            Function<String, Metadata> getProtocolMetadata,
            Wizard.FinishCallback<Context, State> callback) {
        this.getProtocolMetadata = getProtocolMetadata;

        wizard = new Wizard.Builder<Context, State>(resources.messages().addResourceTitle(Names.PROTOCOL),
                new Context())
                .onBack((context, state) -> state == PROPERTIES_STEP ? CHOOSE_PROTOCOL_STEP : null)
                .onNext((context, state) -> state == CHOOSE_PROTOCOL_STEP ? PROPERTIES_STEP : null)
                .onFinish(callback)
                .addStep(CHOOSE_PROTOCOL_STEP, new ChooseProtocolStep(resources, protocolNames, this))
                .addStep(PROPERTIES_STEP, new PropertiesStep(resources))
                .setInitialState(protocolNames.size() > 1 ? CHOOSE_PROTOCOL_STEP : PROPERTIES_STEP)
                .build();

        setProtocol("*");
    }

    public void show() {
        wizard.show();
    }

    public void setProtocol(String name) {
        Context context = wizard.getContext();
        context.protocolName = name;
        context.protocolMetadata = getProtocolMetadata.apply(name);
    }

    static class Context {
        ModelNode payload = new ModelNode();
        String protocolName;
        Metadata protocolMetadata;
    }

    enum State {
        CHOOSE_PROTOCOL_STEP, PROPERTIES_STEP;
    }
}
