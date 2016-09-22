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

import java.util.ArrayList;
import javax.inject.Inject;

import com.google.common.collect.Lists;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.PatternFlyViewImpl;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.deployment.Deployment.Status.OK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.hal.resources.CSS.fontAwesome;
import static org.jboss.hal.resources.CSS.marginTop20;
import static org.jboss.hal.resources.CSS.stopCircleO;

/**
 * @author Harald Pehl
 */
public class DeploymentDetailView extends PatternFlyViewImpl implements DeploymentDetailPresenter.MyView {

    private final ModelBrowser modelBrowser;
    private final Places places;
    private final Resources resources;
    private final EmptyState noReferenceServer;
    private final EmptyState notActive;
    private DeploymentDetailPresenter presenter;

    @Inject
    public DeploymentDetailView(final ModelBrowser modelBrowser, final Places places, final Resources resources) {
        this.modelBrowser = modelBrowser;
        this.places = places;
        this.resources = resources;

        noReferenceServer = new EmptyState.Builder(resources.constants().noReferenceServer())
                .icon(CSS.pfIcon("server"))
                .build();
        noReferenceServer.asElement().getClassList().add(marginTop20);
        Elements.setVisible(noReferenceServer.asElement(), false);

        notActive = new EmptyState.Builder(resources.constants().notActive())
                .icon(fontAwesome(stopCircleO))
                .build();
        notActive.asElement().getClassList().add(marginTop20);
        Elements.setVisible(notActive.asElement(), false);

        ArrayList<Element> elements = Lists.newArrayList(modelBrowser.asElements());
        elements.add(noReferenceServer.asElement());
        elements.add(notActive.asElement());
        initElements(elements);
    }

    @Override
    public void setPresenter(final DeploymentDetailPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateStandalone(final Deployment deployment) {
        Elements.setVisible(noReferenceServer.asElement(), false); // there's always a ref server in standalone
        handleActive(deployment);
    }

    @Override
    public void updateDomain(final String serverGroup, final ServerGroupDeployment sgd) {
        if (sgd.getDeployment() == null) {
            Elements.setVisible(noReferenceServer.asElement(), true);
            Elements.setVisible(notActive.asElement(), false);
            modelBrowser.asElements().forEach(element -> Elements.setVisible(element, false));

            noReferenceServer.setDescription(
                    resources.messages().noReferenceServerEmptyState(sgd.getName(), serverGroup));
            noReferenceServer.setPrimaryAction(
                    resources.messages().goTo(Names.SERVER_GROUP), () -> presenter.goToServerGroup());

        } else {
            Elements.setVisible(noReferenceServer.asElement(), false);
            handleActive(sgd.getDeployment());
        }
    }

    private void handleActive(Deployment deployment) {
        boolean active = deployment.getStatus() == OK;
        Elements.setVisible(notActive.asElement(), !active);
        modelBrowser.asElements().forEach(element -> Elements.setVisible(element, active));

        if (active) {
            ResourceAddress address = deployment.getReferenceServer().getServerAddress()
                    .add(DEPLOYMENT, deployment.getName());
            modelBrowser.setRoot(address, false);
        } else {
            notActive.setDescription(resources.messages().deploymentNotEnabled(deployment.getName()));
            notActive.setPrimaryAction(resources.constants().enable(), () -> presenter.enable(deployment.getName()));
        }
    }
}
