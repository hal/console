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

import com.google.common.base.Joiner;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver.Provider.DEPLOYMENT;
import static org.jboss.hal.client.configuration.subsystem.datasource.JdbcDriver.Provider.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DRIVER_VERSION;

/**
 * @author Harald Pehl
 */
class JdbcDriverPreview extends PreviewContent<JdbcDriver> {

    JdbcDriverPreview(final JdbcDriver driver, final Resources resources) {
        super(driver.getName());

        JdbcDriver.Provider provider = driver.getProvider();
        if (provider == MODULE) {
            previewBuilder().p().innerHtml(
                    resources.messages().jdbcDriverProvidedByPreview(provider.text(), driver.getModule())).end();
        } else if (provider == DEPLOYMENT) {
            previewBuilder().p().innerHtml(
                    resources.messages().jdbcDriverProvidedByPreview(provider.text(), driver.getDeploymentName()))
                    .end();
            previewBuilder().p().innerHtml(resources.messages().jdbcDriverDeploymentHint()).end();
        }

        LabelBuilder labelBuilder = new LabelBuilder();
        PreviewAttributes<JdbcDriver> attributes = new PreviewAttributes<>(driver)

                .append(model -> {
                    return new String[]{labelBuilder.label("driver-classes"), //NON-NLS
                            model.getDriverClasses().isEmpty() ? Names.NOT_AVAILABLE : Joiner.on(',')
                                    .skipNulls().join(model.getDriverClasses())};
                })

                .append(model -> {
                    return new String[]{labelBuilder.label(DRIVER_VERSION), model.getDriverVersion()};
                })

                .append("jdbc-compliant") //NON-NLS
                .end();

        previewBuilder().addAll(attributes);
    }
}
