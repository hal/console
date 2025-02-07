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
package org.jboss.hal.client.runtime.subsystem.datasource;

import javax.inject.Inject;

import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.ballroom.LayoutBuilder.column;
import static org.jboss.hal.ballroom.LayoutBuilder.row;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_JDBC_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_POOL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.clearfix;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginRight5;
import static org.jboss.hal.resources.CSS.pullRight;

public class DataSourceXAView extends HalViewImpl implements DataSourceXAPresenter.MyView {

    private static final String POOL_PATH = "statistics/pool";
    private static final String JDBC_PATH = "statistics/jdbc";

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private DataSourceXAPresenter presenter;
    private HTMLElement header;
    private HTMLElement container;
    private Form<ModelNode> poolForm;
    private Form<ModelNode> jdbcForm;
    private boolean isSetUp = false;

    @Inject
    public DataSourceXAView(MetadataRegistry metadataRegistry, Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
    }

    @Override
    public void setup() {
        // The metadata for the "statistic" resources is only available for existing data-sources.
        // That's why we cannot set up the UI in the constructor like in other views and
        // using wildcards in the address templates. As a workaround we defer the UI setup
        // until the DS name is known and replace the wildcards with the DS name.
        Metadata poolMeta;
        Metadata jdbcMeta;
        poolMeta = metadataRegistry.lookup(XA_DATA_SOURCE_POOL_TEMPLATE.replaceWildcards(presenter.getDataSource()));
        jdbcMeta = metadataRegistry.lookup(XA_DATA_SOURCE_JDBC_TEMPLATE.replaceWildcards(presenter.getDataSource()));

        Tabs tabs = new Tabs(Ids.XA_DATA_SOURCE_RUNTIME_TAB_CONTAINER);

        poolForm = new ModelNodeForm.Builder<>(Ids.XA_DATA_SOURCE_RUNTIME_POOL_FORM, poolMeta)
                .readOnly()
                .includeRuntime()
                .exclude(STATISTICS_ENABLED)
                .build();
        tabs.add(Ids.XA_DATA_SOURCE_RUNTIME_POOL_TAB, Names.POOL, poolForm.element());

        jdbcForm = new ModelNodeForm.Builder<>(Ids.XA_DATA_SOURCE_RUNTIME_JDBC_FORM, jdbcMeta)
                .readOnly()
                .includeRuntime()
                .exclude(STATISTICS_ENABLED)
                .build();
        tabs.add(Ids.XA_DATA_SOURCE_RUNTIME_JDBC_TAB, Names.JDBC, jdbcForm.element());

        registerAttachables(asList(poolForm, jdbcForm));
        if (!isSetUp) {
            HTMLElement root = row()
                    .add(column()
                            .add(header = h(1).textContent(Names.DATASOURCE).element())
                            .add(p().css(clearfix)
                                    .add(a().css(clickable, pullRight).on(click, event -> refresh())
                                            .add(span().css(fontAwesome("refresh"), marginRight5))
                                            .add(span().textContent(resources.constants().refresh()))))
                            .add(container = tabs.element()))
                    .element();

            initElement(root);
            isSetUp = true;
        } else {
            // if the view is already attached overwriting a variable does not change the DOM,
            // but 'replaceWith' doesn't change the object reference so we still need to do it
            container.replaceWith(tabs.element());
            container = tabs.element();
        }
    }

    @Override
    public void setPresenter(DataSourceXAPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(DataSource dataSource) {
        header.textContent = dataSource.getName();
        poolForm.view(failSafeGet(dataSource, POOL_PATH));
        jdbcForm.view(failSafeGet(dataSource, JDBC_PATH));
    }

    private void refresh() {
        if (presenter != null) {
            presenter.reload();
        }
    }
}
