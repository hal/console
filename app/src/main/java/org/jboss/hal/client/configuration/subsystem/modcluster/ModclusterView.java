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
package org.jboss.hal.client.configuration.subsystem.modcluster;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.form.FailSafeForm;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;

/**
 * @author Harald Pehl
 */
@MbuiView
public class ModclusterView extends MbuiViewImpl<ModclusterPresenter> implements ModclusterPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static ModclusterView create(final MbuiContext mbuiContext) {
        return new Mbui_ModclusterView(mbuiContext);
    }

    @MbuiElement("modcluster-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("modcluster-configuration") Form<ModelNode> configurationForm;
    @MbuiElement("modcluster-ssl-form") FailSafeForm<ModelNode> sslForm;

    ModclusterView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @Override
    public void updateConfiguration(final ModelNode modelNode) {
        configurationForm.view(modelNode);
        sslForm.view(failSafeGet(modelNode, "ssl/configuration"));
    }
}
