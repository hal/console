/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.datasource;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.datasource.JdbcDriver;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewAttributes.PreviewAttribute;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.DEPLOYMENT;
import static org.jboss.hal.core.datasource.JdbcDriver.Provider.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_DATASOURCE_CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_XA_DATASOURCE_CLASS_NAME;

class JdbcDriverPreview extends PreviewContent<JdbcDriver> {

    JdbcDriverPreview(final JdbcDriver driver, final Resources resources) {
        super(driver.getName());

        JdbcDriver.Provider provider = driver.getProvider();
        if (provider == MODULE) {
            previewBuilder().add(
                    p().innerHtml(resources.messages().jdbcDriverProvidedBy(provider.text(), driver.getModule())));
        } else if (provider == DEPLOYMENT) {
            previewBuilder().add(
                    p().innerHtml(
                            resources.messages().jdbcDriverProvidedBy(provider.text(), driver.getDeploymentName())));
            previewBuilder().add(p().innerHtml(resources.messages().jdbcDriverDeploymentHint()));
        }

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<JdbcDriver> attributes = new PreviewAttributes<>(driver)
                .append(DRIVER_CLASS_NAME)
                .append(DRIVER_DATASOURCE_CLASS_NAME)
                .append(DRIVER_XA_DATASOURCE_CLASS_NAME)
                .append(model -> new PreviewAttribute(labelBuilder.label(DRIVER_VERSION), model.getDriverVersion()))
                .append("jdbc-compliant"); //NON-NLS
        previewBuilder().addAll(attributes);
    }
}
