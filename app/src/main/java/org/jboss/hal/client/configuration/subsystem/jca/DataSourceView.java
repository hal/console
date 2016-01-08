/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.configuration.subsystem.jca;

import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.layout.LayoutBuilder;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.description.ResourceDescriptions;
import org.jboss.hal.meta.security.SecurityContext;
import org.jboss.hal.meta.security.SecurityFramework;

import javax.inject.Inject;
import java.util.List;

import static org.jboss.hal.ballroom.table.Api.RefreshMode.RESET;
import static org.jboss.hal.resources.Names.ENABLED;
import static org.jboss.hal.resources.Names.JNDI_NAME;

/**
 * @author Harald Pehl
 */
@SuppressWarnings("HardCodedStringLiteral")
public class DataSourceView extends ViewImpl implements DataSourcePresenter.MyView {

    private final ModelNodeTable<ModelNode> dataSourcesTable;
    private DataSourcePresenter presenter;

    @Inject
    public DataSourceView(ResourceDescriptions descriptions,
            SecurityFramework securityFramework) {

        ResourceDescription description = descriptions.lookup(DataSourcePresenter.ROOT_TEMPLATE);
        SecurityContext securityContext = securityFramework.lookup(DataSourcePresenter.ROOT_TEMPLATE);

        Options<ModelNode> options = new ModelNodeTable.Builder<>(description)
                .columns(JNDI_NAME, ENABLED)
                .build();
        dataSourcesTable = new ModelNodeTable<>("data-sources-table", securityContext, options);

        Element info = new Elements.Builder().p().innerText(description.getDescription()).end().build();
        // @formatter:off
        Element element = new LayoutBuilder()
            .startRow()
                .header("DataSources")
                .add(info)
                .add(dataSourcesTable.asElement())
            .endRow()
        .build();
        // @formatter:on

        initWidget(Elements.asWidget(element));
    }

    @Override
    public void attach() {
        dataSourcesTable.attach();
    }

    @Override
    public void setPresenter(final DataSourcePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(final List<ModelNode> datasources) {
        dataSourcesTable.api().clear().add(datasources).refresh(RESET);
    }
}
