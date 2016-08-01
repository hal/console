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
package org.jboss.hal.client.runtime.subsystem.datasource;

import javax.inject.Inject;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LayoutBuilder;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_JDBC_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.DATA_SOURCE_POOL_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_JDBC_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_POOL_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeGet;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
public class DataSourceView extends PatternFlyViewImpl implements DataSourcePresenter.MyView {

    private static final String HEADER_ELEMENT = "headerElement";
    private static final String POOL_PATH = "statistics/pool";
    private static final String JDBC_PATH = "statistics/jdbc";
    private static final String[] XA_ATTRIBUTES = {
            "XACommitAverageTime",
            "XACommitCount",
            "XACommitMaxTime",
            "XACommitTotalTime",
            "XAEndAverageTime",
            "XAEndCount",
            "XAEndMaxTime",
            "XAEndTotalTime",
            "XAForgetAverageTime",
            "XAForgetCount",
            "XAForgetMaxTime",
            "XAForgetTotalTime",
            "XAPrepareAverageTime",
            "XAPrepareCount",
            "XAPrepareMaxTime",
            "XAPrepareTotalTime",
            "XARecoverAverageTime",
            "XARecoverCount",
            "XARecoverMaxTime",
            "XARecoverTotalTime",
            "XARollbackAverageTime",
            "XARollbackCount",
            "XARollbackMaxTime",
            "XARollbackTotalTime",
            "XAStartAverageTime",
            "XAStartCount",
            "XAStartMaxTime",
            "XAStartTotalTime"
    };

    private final MetadataRegistry metadataRegistry;
    private final Resources resources;
    private DataSourcePresenter presenter;
    private Element header;
    private Tabs nonXaTabs;
    private Tabs xaTabs;
    private Form<ModelNode> poolForm;
    private Form<ModelNode> xaPoolForm;
    private Form<ModelNode> jdbcForm;
    private Form<ModelNode> xaJdbcForm;
    private boolean setup;

    @Inject
    public DataSourceView(final MetadataRegistry metadataRegistry, final Resources resources) {
        this.metadataRegistry = metadataRegistry;
        this.resources = resources;
        this.setup = false;
    }

    @Override
    public void setup() {
        if (setup) {
            return;
        }

        // For some reason the statistic resources are returned with the DS name in it.
        // That's why we cannot setup the UI in the constructor like in other views and
        // using wildcards in the address templates. As a workaround we defer the UI setup
        // until the DS name is known and replace the wildcards with the DS name.
        Metadata poolMeta;
        Metadata jdbcMeta;
        if (presenter.isXa()) {
            poolMeta = metadataRegistry
                    .lookup(XA_DATA_SOURCE_POOL_TEMPLATE.replaceWildcards(presenter.getDataSource()));
            jdbcMeta = metadataRegistry
                    .lookup(XA_DATA_SOURCE_JDBC_TEMPLATE.replaceWildcards(presenter.getDataSource()));
        } else {
            poolMeta = metadataRegistry.lookup(DATA_SOURCE_POOL_TEMPLATE.replaceWildcards(presenter.getDataSource()));
            jdbcMeta = metadataRegistry.lookup(DATA_SOURCE_JDBC_TEMPLATE.replaceWildcards(presenter.getDataSource()));
        }

        nonXaTabs = new Tabs();
        xaTabs = new Tabs();

        poolForm = new ModelNodeForm.Builder<>(Ids.DATA_SOURCE_RUNTIME_POOL_FORM, poolMeta)
                .viewOnly()
                .includeRuntime()
                .exclude(STATISTICS_ENABLED)
                .exclude(XA_ATTRIBUTES)
                .build();
        nonXaTabs.add(Ids.DATA_SOURCE_RUNTIME_POOL_TAB, resources.constants().pool(), poolForm.asElement());

        xaPoolForm = new ModelNodeForm.Builder<>(Ids.XA_DATA_SOURCE_RUNTIME_POOL_FORM, poolMeta)
                .viewOnly()
                .includeRuntime()
                .exclude(STATISTICS_ENABLED)
                .build();
        xaTabs.add(Ids.XA_DATA_SOURCE_RUNTIME_POOL_TAB, resources.constants().pool(), xaPoolForm.asElement());

        jdbcForm = new ModelNodeForm.Builder<>(Ids.DATA_SOURCE_RUNTIME_JDBC_FORM, jdbcMeta)
                .viewOnly()
                .includeRuntime()
                .exclude(STATISTICS_ENABLED)
                .build();
        nonXaTabs.add(Ids.DATA_SOURCE_RUNTIME_JDBC_TAB, Names.JDBC, jdbcForm.asElement());

        xaJdbcForm = new ModelNodeForm.Builder<>(Ids.XA_DATA_SOURCE_RUNTIME_JDBC_FORM, jdbcMeta)
                .viewOnly()
                .includeRuntime()
                .exclude(STATISTICS_ENABLED)
                .build();
        xaTabs.add(Ids.XA_DATA_SOURCE_RUNTIME_JDBC_TAB, Names.JDBC, xaJdbcForm.asElement());

        // @formatter:off
        LayoutBuilder layoutBuilder = new LayoutBuilder()
            .row()
                .column()
                    .header(Names.DATASOURCE).rememberAs(HEADER_ELEMENT).end()
                    .p().css(clearfix)
                        .a().css(clickable, pullRight).on(click, event -> refresh())
                            .span().css(fontAwesome("refresh"), marginRight4).end()
                            .span().textContent(resources.constants().refresh()).end()
                        .end()
                    .end()
                    .add(nonXaTabs.asElement())
                    .add(xaTabs.asElement())
                .end()
            .end();
        // @formatter:on

        Element root = layoutBuilder.build();
        header = layoutBuilder.referenceFor(HEADER_ELEMENT);
        registerAttachables(asList(poolForm, xaPoolForm, jdbcForm, xaJdbcForm));
        initElement(root);
        setup = true;
    }

    @Override
    public void setPresenter(final DataSourcePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final DataSource dataSource) {
        showHide(dataSource.isXa());
        header.setTextContent(dataSource.getName());
        if (dataSource.isXa()) {
            xaPoolForm.view(failSafeGet(dataSource, POOL_PATH));
            xaJdbcForm.view(failSafeGet(dataSource, JDBC_PATH));
        } else {
            poolForm.view(failSafeGet(dataSource, POOL_PATH));
            jdbcForm.view(failSafeGet(dataSource, JDBC_PATH));
        }
    }

    private void showHide(boolean xa) {
        Elements.setVisible(nonXaTabs.asElement(), !xa);
        Elements.setVisible(xaTabs.asElement(), xa);
    }

    private void refresh() {
        if (presenter != null) {
            presenter.load();
        }
    }
}
