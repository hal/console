/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.deployment;

import java.util.ArrayList;

import javax.inject.Inject;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.deployment.ServerGroupDeployment;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.common.collect.Lists;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.resources.CSS.marginTopLarge;

public class ServerGroupDeploymentView extends HalViewImpl implements ServerGroupDeploymentPresenter.MyView {

    private final DeploymentModelElement deploymentModel;
    private final Resources resources;
    private final EmptyState noReferenceServer;
    private ServerGroupDeploymentPresenter presenter;

    @Inject
    public ServerGroupDeploymentView(ModelBrowser modelBrowser, Resources resources) {
        this.deploymentModel = new DeploymentModelElement(modelBrowser, resources);
        this.resources = resources;

        noReferenceServer = new EmptyState.Builder(Ids.REFERENCE_SERVER_EMPTY,
                resources.constants().noReferenceServer())
                        .icon(CSS.pfIcon("server"))
                        .build();
        noReferenceServer.element().classList.add(marginTopLarge);
        Elements.setVisible(noReferenceServer.element(), false);

        ArrayList<HTMLElement> elements = Lists.newArrayList(deploymentModel);
        elements.add(noReferenceServer.element());
        initElements(elements);
    }

    @Override
    public void setPresenter(ServerGroupDeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void update(String serverGroup, ServerGroupDeployment sgd) {
        if (sgd.getDeployment() == null) {
            Elements.setVisible(noReferenceServer.element(), true);
            deploymentModel.forEach(element -> Elements.setVisible(element, false));

            noReferenceServer.setDescription(
                    resources.messages().noReferenceServerEmptyState(sgd.getName(), serverGroup));
            noReferenceServer.setPrimaryAction(
                    resources.messages().goTo(Names.SERVER_GROUP), () -> presenter.goToServerGroup());

        } else {
            Elements.setVisible(noReferenceServer.element(), false);
            handleActive(sgd.getDeployment());
        }
    }

    private void handleActive(Deployment deployment) {
        deploymentModel.update(deployment, () -> presenter.enable(deployment.getName()));
    }
}
