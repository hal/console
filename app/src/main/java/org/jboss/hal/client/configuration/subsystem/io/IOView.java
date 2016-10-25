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
package org.jboss.hal.client.configuration.subsystem.io;

import java.util.List;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Api.RefreshMode;
import org.jboss.hal.ballroom.table.DataTable;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Claudio Miranda
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public abstract class IOView extends MbuiViewImpl<IOPresenter> implements IOPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static IOView create(final MbuiContext mbuiContext) {
        return new Mbui_IOView(mbuiContext);
    }

    @MbuiElement("io-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("io-buffer-pool-table") DataTable<NamedNode> bufferPoolTable;
    @MbuiElement("io-buffer-pool-form") Form<NamedNode> bufferPoolForm;
    @MbuiElement("io-worker-table") DataTable<NamedNode> workerTable;
    @MbuiElement("io-worker-form") Form<NamedNode> workerForm;

    IOView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }


    // ------------------------------------------------------ buffer pool

    @Override
    public void updateBufferPool(final List<NamedNode> items) {
        bufferPoolTable.api().clear().add(items).refresh(RefreshMode.RESET);
        bufferPoolForm.clear();
    }


    // ------------------------------------------------------ worker

    @Override
    public void updateWorkers(final List<NamedNode> items) {
        workerTable.api().clear().add(items).refresh(RefreshMode.RESET);
        workerForm.clear();
    }
}
