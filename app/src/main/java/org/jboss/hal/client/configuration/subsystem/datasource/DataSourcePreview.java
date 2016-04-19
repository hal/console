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
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.resources.CSS.alert;
import static org.jboss.hal.resources.CSS.alertInfo;
import static org.jboss.hal.resources.CSS.alertSuccess;
import static org.jboss.hal.resources.CSS.pfIcon;

/**
 * @author Harald Pehl
 */
class DataSourcePreview extends PreviewContent {

    private static final String ALERT = "alertElement";
    private static final String DATASOURCE = "datasource";
    private static final String XA_DATASOURCE = "XA datasource";

    DataSourcePreview(final DataSourceColumn column, final DataSource dataSource, final Resources resources) {
        super(dataSource.getName(), dataSource.isXa() ? Names.XA_DATASOURCE : Names.DATASOURCE);
        boolean enabled = dataSource.hasDefined(ENABLED) && dataSource.get(ENABLED).asBoolean();

        previewBuilder().div();
        if (enabled) {
            previewBuilder().css(alert, alertSuccess).span().css(pfIcon("ok")).end();
        } else {
            previewBuilder().css(alert, alertInfo).span().css(pfIcon("info")).end();
        }
        previewBuilder().span().rememberAs(ALERT);
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
        Element alertElement = previewBuilder().referenceFor(ALERT);
        NodeList links = alertElement.getElementsByTagName("a"); //NON-NLS
        if (links.getLength() != 0) {
            Element link = (Element) links.item(0);
            if (enabled) {
                link.setOnclick(event -> column.disable(dataSource));
            } else {
                link.setOnclick(event -> column.enable(dataSource));
            }
        }

        List<String> attributes = Lists
                .newArrayList(JNDI_NAME, DRIVER_NAME, CONNECTION_URL, ENABLED, STATISTICS_ENABLED);
        if (dataSource.isXa()) {
            attributes.remove(2);
        }
        PreviewAttributes<DataSource> previewAttributes = new PreviewAttributes<>(dataSource,
                resources.constants().main_attributes(), attributes).end();
        previewBuilder().addAll(previewAttributes);
    }
}
