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
package org.jboss.hal.client.configuration.subsystem.elytron;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class ElytronSubsystemView extends MbuiViewImpl<ElytronSubsystemPresenter>
        implements ElytronSubsystemPresenter.MyView {

    public static ElytronSubsystemView create(final MbuiContext mbuiContext) {
        return new Mbui_ElytronSubsystemView(mbuiContext);
    }

    @MbuiElement("elytron-global-settings-form") Form<ModelNode> form;

    ElytronSubsystemView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void update(final ModelNode modelNode) {
        form.view(modelNode);
    }
}
