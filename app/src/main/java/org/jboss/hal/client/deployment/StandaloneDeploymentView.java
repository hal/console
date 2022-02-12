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

import javax.inject.Inject;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.Tabs;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.deployment.Deployment;
import org.jboss.hal.core.modelbrowser.ModelBrowser;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import com.google.web.bindery.event.shared.EventBus;

import elemental2.dom.CSSProperties;
import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.CSS.navTabsHal;

public class StandaloneDeploymentView extends HalViewImpl implements StandaloneDeploymentPresenter.MyView {

    private final boolean supportsReadContent;
    private final Tabs tabs;
    private boolean initialHeightAdjusted;
    private BrowseContentElement browseContent;
    private DeploymentModelElement deploymentModel;
    private StandaloneDeploymentPresenter presenter;

    @Inject
    public StandaloneDeploymentView(Dispatcher dispatcher, ModelBrowser modelBrowser,
            Environment environment, EventBus eventBus, MetadataRegistry metadataRegistry, Resources resources) {
        Metadata metadata = metadataRegistry.lookup(StandaloneDeploymentColumn.DEPLOYMENT_TEMPLATE);
        supportsReadContent = ManagementModel.supportsReadContentFromDeployment(environment.getManagementVersion());
        browseContent = new BrowseContentElement(dispatcher, environment, eventBus, metadata, resources);
        deploymentModel = new DeploymentModelElement(modelBrowser, resources);

        if (supportsReadContent) {
            tabs = new Tabs(Ids.DEPLOYMENT_TAB_CONTAINER)
                    .add(Ids.CONTENT_TAB, resources.constants().content(), browseContent.element())
                    .add(Ids.DEPLOYMENT_TAB, Names.MANAGEMENT_MODEL, deploymentModel);
            ((HTMLElement) tabs.element().querySelector("." + navTabsHal)).style.marginTop = CSSProperties.MarginTopUnionType
                    .of(0);
            initElement(tabs.element());
        } else {
            tabs = null;
            HTMLElement root = div().element();
            deploymentModel.forEach(root::appendChild);
            initElement(root);
        }
    }

    @Override
    public void attach() {
        super.attach();
        if (supportsReadContent) {
            browseContent.attach();

            HTMLElement ul = (HTMLElement) tabs.element().querySelector("ul." + navTabsHal); // NON-NLS
            if (ul != null) {
                int tabsHeight = (int) (ul.offsetHeight + 5);
                browseContent.setSurroundingHeight(tabsHeight);
                deploymentModel.setSurroundingHeight(tabsHeight);

                // The heights of the elements on the initially hidden tab need to be adjusted once they're visible
                tabs.onShow(Ids.DEPLOYMENT_TAB, () -> {
                    if (!initialHeightAdjusted) {
                        deploymentModel.setSurroundingHeight(tabsHeight);
                        initialHeightAdjusted = true;
                    }
                });
            }
        }
    }

    @Override
    public void setPresenter(StandaloneDeploymentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void reset() {
        if (supportsReadContent) {
            tabs.showTab(0);
        }
    }

    @Override
    public void update(Deployment deployment, int tab) {
        if (supportsReadContent) {
            HTMLElement contentElement = tabs.tabElement(Ids.CONTENT_TAB);
            if (deployment.isManaged()) {
                browseContent.setContent(deployment);
                tabs.showTab(tab);
            } else {
                tabs.showTab(1);
            }
            Elements.setVisible(contentElement, deployment.isManaged());
        }
        deploymentModel.update(deployment, () -> presenter.enable(deployment.getName()));
    }
}
