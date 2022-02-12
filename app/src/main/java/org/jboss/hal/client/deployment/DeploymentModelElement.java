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
package org.jboss.hal.client.deployment;

import java.util.Iterator;

import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Callback;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;
import static org.jboss.gwt.elemento.core.Elements.setVisible;
import static org.jboss.hal.core.deployment.Deployment.Status.OK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginTopLarge;
import static org.jboss.hal.resources.CSS.stopCircleO;

/**
 * UI element to show the management model of a deployment in the model browser. Shows an empty state element in case the
 * deployment is inactive.
 */
class DeploymentModelElement implements Iterable<HTMLElement> {

    private final ModelBrowser modelBrowser;
    private final Resources resources;
    private final EmptyState notEnabled;
    private final Iterable<HTMLElement> elements;

    DeploymentModelElement(ModelBrowser modelBrowser, Resources resources) {
        this.modelBrowser = modelBrowser;
        this.resources = resources;

        notEnabled = new EmptyState.Builder(Ids.DEPLOYMENT_NOT_ENABLED_EMPTY, resources.constants().notEnabled())
                .icon(fontAwesome(stopCircleO))
                .build();
        notEnabled.element().classList.add(marginTopLarge);
        setVisible(notEnabled.element(), false);

        elements = asList(modelBrowser.element(), notEnabled.element());
    }

    @Override
    public Iterator<HTMLElement> iterator() {
        return elements.iterator();
    }

    void setSurroundingHeight(int surroundingHeight) {
        modelBrowser.setSurroundingHeight(surroundingHeight);
    }

    void update(Deployment deployment, Callback enableAction) {
        boolean active = deployment.getStatus() == OK;
        setVisible(notEnabled.element(), !active);
        setVisible(modelBrowser.element(), active);

        if (active) {
            ResourceAddress address = deployment.getReferenceServer().getServerAddress()
                    .add(DEPLOYMENT, deployment.getName());
            modelBrowser.setRoot(address, false);
        } else {
            notEnabled.setDescription(resources.messages().deploymentNotEnabled(deployment.getName()));
            notEnabled.setPrimaryAction(resources.constants().enable(), enableAction);
        }
    }
}
