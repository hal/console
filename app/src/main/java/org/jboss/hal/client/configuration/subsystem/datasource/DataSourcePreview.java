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

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class DataSourcePreview extends PreviewContent {

    private static final String DATASOURCE = "datasource";
    private static final String XA_DATASOURCE = "XA datasource";

    DataSourcePreview(final DataSource dataSource, final Resources resources) {
        super(dataSource.getName(), dataSource.isXa() ? Names.XA_DATASOURCE : Names.DATASOURCE);
        boolean enabled = dataSource.hasDefined(ENABLED) && dataSource.get(ENABLED).asBoolean();

        previewBuilder().div();
        if (enabled) {
            previewBuilder().css(alert, alertSuccess).span().css(pfIcon("ok")).end();
        } else {
            previewBuilder().css(alert, alertInfo).span().css(pfIcon("info")).end();
        }
        previewBuilder().span();
        if (dataSource.isXa()) {
            if (enabled) {
                previewBuilder()
                        .innerHtml(resources.messages().dataSourceEnabledPreview(XA_DATASOURCE, dataSource.getName()));
            } else {
                previewBuilder()
                        .innerHtml(resources.messages().dataSourceDisabledPreview(XA_DATASOURCE, dataSource.getName()));
            }
        } else {
            if (enabled) {
                previewBuilder()
                        .innerHtml(resources.messages().dataSourceEnabledPreview(DATASOURCE, dataSource.getName()));
            } else {
                previewBuilder()
                        .innerHtml(resources.messages().dataSourceDisabledPreview(DATASOURCE, dataSource.getName()));
            }
        }
        previewBuilder().end().end(); // </span> && </div>

        previewBuilder().h(2).textContent(resources.constants().attributes()).end();

        previewBuilder().ul().css(listGroup);
        addAttribute(dataSource, JNDI_NAME);
        addAttribute(dataSource, DRIVER_NAME);
        if (!dataSource.isXa()) {
            addAttribute(dataSource, CONNECTION_URL);
        }
        addAttribute(dataSource, ENABLED);
        addAttribute(dataSource, STATISTICS_ENABLED);
        previewBuilder().end();
    }

    private void addAttribute(DataSource dataSource, String name) {
        LabelBuilder labelBuilder = new LabelBuilder();
        String value = dataSource.get(name).asString();

        previewBuilder().li().css(listGroupItem)
                .span().css(key).textContent(labelBuilder.label(name)).end()
                .span().css(CSS.value).textContent(value);
        if (value.length() > 15) {
            previewBuilder().title(value);
        }
        previewBuilder().end().end();
    }
}
