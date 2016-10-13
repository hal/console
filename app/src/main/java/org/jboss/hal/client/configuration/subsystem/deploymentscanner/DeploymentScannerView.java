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
package org.jboss.hal.client.configuration.subsystem.deploymentscanner;

import java.util.List;

import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.ui.UIContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class DeploymentScannerView extends MbuiViewImpl<DeploymentScannerPresenter> implements DeploymentScannerPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static DeploymentScannerView create(final UIContext mbuiContext) {
        return new Mbui_DeploymentScannerView(mbuiContext);
    }

    @MbuiElement("deployment-scanner-table") DataTable<NamedNode> deploymentscannerTable;
    @MbuiElement("deployment-scanner-form") Form<NamedNode> deploymentscannerForm;

    DeploymentScannerView(final UIContext mbuiContext) {
        super(mbuiContext);
    }

    // ------------------------------------------------------ scanners

    @Override
    public void updateScanners(final List<NamedNode> items) {
        deploymentscannerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        deploymentscannerForm.clear();
    }

}
