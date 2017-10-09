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
package org.jboss.hal.client.configuration.subsystem.datasource;

import java.util.List;

import com.google.common.collect.Lists;
import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.core.datasource.DataSource;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.datasource.AddressTemplates.XA_DATA_SOURCE_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

class DataSourcePreview extends PreviewContent<DataSource> {

    private static final String DATASOURCE = "datasource";
    private static final String XA_DATASOURCE = "XA datasource";

    DataSourcePreview(final DataSourceColumn column, final DataSource dataSource, final Resources resources) {
        super(dataSource.getName(), dataSource.isXa() ? Names.XA_DATASOURCE : Names.DATASOURCE);
        boolean enabled = dataSource.hasDefined(ENABLED) && dataSource.get(ENABLED).asBoolean();
        String type = dataSource.isXa() ? XA_DATASOURCE: DATASOURCE;

        if (enabled) {
            previewBuilder().add(
                    new Alert(Icons.OK, resources.messages().resourceEnabled(type, dataSource.getName()),
                            resources.constants().disable(), event -> column.disable(dataSource),
                            Constraint.writable(dataSource.isXa() ? DATA_SOURCE_TEMPLATE : XA_DATA_SOURCE_TEMPLATE,
                                    ENABLED)));

        } else {
            previewBuilder().add(
                    new Alert(Icons.DISABLED, resources.messages().resourceDisabled(type, dataSource.getName()),
                            resources.constants().enable(), event -> column.enable(dataSource),
                            Constraint.writable(dataSource.isXa() ? DATA_SOURCE_TEMPLATE : XA_DATA_SOURCE_TEMPLATE,
                                    ENABLED)));
        }

        List<String> attributes = Lists
                .newArrayList(JNDI_NAME, DRIVER_NAME, CONNECTION_URL, ENABLED, STATISTICS_ENABLED);
        if (dataSource.isXa()) {
            attributes.remove(2);
        }
        PreviewAttributes<DataSource> previewAttributes = new PreviewAttributes<>(dataSource, attributes);
        previewBuilder().addAll(previewAttributes);
    }
}
