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
package org.jboss.hal.client.deployment;

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Resources;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.DEPLOYMENT;

/**
 * @author Harald Pehl
 */
class DeploymentPreview extends PreviewContent {

    DeploymentPreview(final DeploymentColumn column, final Deployment deployment, final Resources resources) {
        super(deployment.getName());

        previewBuilder().div();
        if (deployment.isEnabled()) {
            previewBuilder().css(alert, alertSuccess)
                    .span().css(pfIcon("ok")).end()
                    .span().innerHtml(resources.messages().resourceEnabled(DEPLOYMENT, deployment.getName())).end()
                    .span().textContent(" ").end()
                    .a().css(clickable, alertLink).on(click, event -> column.disable(deployment))
                    .textContent(resources.constants().disable()).end();

        } else {
            previewBuilder().css(alert, alertInfo)
                    .span().css(pfIcon("info")).end()
                    .span().innerHtml(resources.messages().resourceDisabled(DEPLOYMENT, deployment.getName())).end()
                    .span().textContent(" ").end()
                    .a().css(clickable, alertLink).on(click, event -> column.enable(deployment))
                    .textContent(resources.constants().enable()).end();
        }
        previewBuilder().end(); // </div>

        PreviewAttributes<Deployment> attributes = new PreviewAttributes<>(deployment,
                asList(RUNTIME_NAME, "disabled-timestamp", "enabled-timestamp", STATUS)).end();
        previewBuilder().addAll(attributes);
    }
}
