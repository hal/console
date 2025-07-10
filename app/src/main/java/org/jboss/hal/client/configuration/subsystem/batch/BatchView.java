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
package org.jboss.hal.client.configuration.subsystem.batch;

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
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral" })
public abstract class BatchView extends MbuiViewImpl<BatchPresenter> implements BatchPresenter.MyView {

    // ------------------------------------------------------ initialization

    public static BatchView create(MbuiContext mbuiContext) {
        return new Mbui_BatchView(mbuiContext);
    }

    @MbuiElement("batch-vertical-navigation") VerticalNavigation navigation;
    @MbuiElement("batch-configuration-form") Form<ModelNode> configurationForm;
    @MbuiElement("batch-in-memory-job-repo-table") Table<NamedNode> inMemoryJobRepoTable;
    @MbuiElement("batch-in-memory-job-repo-form") Form<NamedNode> inMemoryJobRepoForm;
    @MbuiElement("batch-jdbc-job-repo-table") Table<NamedNode> jdbcJobRepoTable;
    @MbuiElement("batch-jdbc-job-repo-form") Form<NamedNode> jdbcJobRepoForm;
    @MbuiElement("batch-thread-factory-table") Table<NamedNode> threadFactoryTable;
    @MbuiElement("batch-thread-factory-form") Form<NamedNode> threadFactoryForm;
    @MbuiElement("batch-thread-pool-table") Table<NamedNode> threadPoolTable;
    @MbuiElement("batch-thread-pool-form") Form<NamedNode> threadPoolForm;

    BatchView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    // ------------------------------------------------------ form and table updates from DMR

    @Override
    public void updateConfiguration(ModelNode configuration) {
        configurationForm.view(configuration);
    }

    @Override
    public void updateInMemoryJobRepository(List<NamedNode> items) {
        inMemoryJobRepoForm.clear();
        inMemoryJobRepoTable.update(items);
    }

    @Override
    public void updateJdbcJobRepository(List<NamedNode> items) {
        jdbcJobRepoForm.clear();
        jdbcJobRepoTable.update(items);
    }

    @Override
    public void updateThreadFactory(List<NamedNode> items) {
        threadFactoryForm.clear();
        threadFactoryTable.update(items);
    }

    @Override
    public void updateThreadPool(List<NamedNode> items) {
        threadPoolForm.clear();
        threadPoolTable.update(items);
    }
}
