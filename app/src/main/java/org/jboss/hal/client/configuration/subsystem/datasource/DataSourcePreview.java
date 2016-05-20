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
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.*;

/**
 * @author Harald Pehl
 */
class DataSourcePreview extends PreviewContent {

    private static final String DATASOURCE = "datasource";
    private static final String XA_DATASOURCE = "XA datasource";

    DataSourcePreview(final DataSourceColumn column, final DataSource dataSource, final Resources resources) {
        super(dataSource.getName(), dataSource.isXa() ? Names.XA_DATASOURCE : Names.DATASOURCE);
        boolean enabled = dataSource.hasDefined(ENABLED) && dataSource.get(ENABLED).asBoolean();
        String type = dataSource.isXa() ? DATASOURCE : XA_DATASOURCE;

        previewBuilder().div();
        if (enabled) {
            previewBuilder().css(alert, alertSuccess)
                    .span().css(pfIcon("ok")).end()
                    .span().innerHtml(resources.messages().resourceEnabled(type, dataSource.getName())).end()
                    .span().textContent(" ").end()
                    .a().css(clickable, alertLink).on(click, event -> column.disable(dataSource))
                    .textContent(resources.constants().disable()).end();

        } else {
            previewBuilder().css(alert, alertInfo)
                    .span().css(pfIcon("info")).end()
                    .span().innerHtml(resources.messages().resourceDisabled(type, dataSource.getName())).end()
                    .span().textContent(" ").end()
                    .a().css(clickable, alertLink).on(click, event -> column.enable(dataSource))
                    .textContent(resources.constants().enable()).end();
        }
        previewBuilder().end(); // </div>

        List<String> attributes = Lists
                .newArrayList(JNDI_NAME, DRIVER_NAME, CONNECTION_URL, ENABLED, STATISTICS_ENABLED);
        if (dataSource.isXa()) {
            attributes.remove(2);
        }
        PreviewAttributes<DataSource> previewAttributes = new PreviewAttributes<>(dataSource,
                resources.constants().mainAttributes(), attributes).end();
        previewBuilder().addAll(previewAttributes);
    }
}
