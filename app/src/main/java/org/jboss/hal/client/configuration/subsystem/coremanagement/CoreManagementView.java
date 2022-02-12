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
package org.jboss.hal.client.configuration.subsystem.coremanagement;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

@MbuiView
public abstract class CoreManagementView extends MbuiViewImpl<CoreManagementPresenter>
        implements CoreManagementPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static CoreManagementView create(final MbuiContext mbuiContext) {
        return new Mbui_CoreManagementView(mbuiContext);
    }

    @MbuiElement("core-mgmt-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("core-mgmt-conf-change-form") Form<ModelNode> confChangesForm;
    @MbuiElement("core-mgmt-prc-state-table") Table<NamedNode> prcStateListenerTable;
    @MbuiElement("core-mgmt-prc-state-form") Form<NamedNode> prcStateListenerForm;

    CoreManagementView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    // ------------------------------------------------------ form and table updates from DMR

    @Override
    public void update(ModelNode model) {
        confChangesForm.view(model);
    }

    @Override
    public void updateProcessStateListener(final List<NamedNode> items) {
        prcStateListenerForm.clear();
        prcStateListenerTable.update(items);
    }
}
