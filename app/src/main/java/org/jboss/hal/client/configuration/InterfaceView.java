/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

@MbuiView
@SuppressWarnings("DuplicateStringLiteralInspection")
public abstract class InterfaceView extends MbuiViewImpl<InterfacePresenter> implements InterfacePresenter.MyView {

    public static InterfaceView create(final MbuiContext mbuiContext) {
        return new Mbui_InterfaceView(mbuiContext);
    }

    @MbuiElement("interface-form") Form<ModelNode> form;

    InterfaceView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    public void update(final ModelNode interfce) {
        form.view(interfce);
    }
}
