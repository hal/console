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
package org.jboss.hal.client.skeleton;

import com.gwtplatform.mvp.client.ViewImpl;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.ui.UIRegistry;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

import static org.jboss.gwt.elemento.core.EventType.click;

/**
 * @author Harald Pehl
 */
@Templated("MainLayout.html#footer")
public abstract class FooterView extends ViewImpl implements FooterPresenter.MyView {

    // @formatter:off
    public static FooterView create(final UIRegistry uiRegistry, final Resources resources) {
        return new Templated_FooterView(uiRegistry, resources);
    }

    public abstract UIRegistry uiRegistry();
    public abstract Resources resources();
    // @formatter:on


    private static Logger logger = LoggerFactory.getLogger(FooterView.class);

    private FooterPresenter presenter;
    private Environment environment;

    @DataElement ProgressElement progress = new ProgressElement();
    @DataElement Element halVersion;
    @DataElement Element updateAvailable;

    @PostConstruct
    void init() {
        uiRegistry().register(progress);
        Elements.setVisible(updateAvailable, false);
    }

    @Override
    public void setPresenter(final FooterPresenter presenter) {
        this.presenter = presenter;
    }

    public void updateEnvironment(Environment environment) {
        this.environment = environment;
        halVersion.setInnerText(environment.getHalVersion().toString());
    }

    @Override
    public void updateVersion(final Version version) {
        if (version.greaterThan(environment.getHalVersion())) {
            logger.info("A new HAL version is available. Current version: {}, new version: {}", //NON-NLS
                    environment.getHalVersion(), version);
            String updateAvailable = resources().messages().updateAvailable(environment.getHalVersion().toString(),
                    version.toString());
            this.updateAvailable.setTitle(updateAvailable);
            this.updateAvailable.getDataset().setAt(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            this.updateAvailable.getDataset().setAt(UIConstants.PLACEMENT, "top");
            this.updateAvailable.getDataset().setAt("container", "body"); //NON-NLS
            Elements.setVisible(this.updateAvailable, true);
        }
    }

    @EventHandler(element = "showVersion", on = click)
    void onShowVersion() {
        presenter.onShowVersion();
    }

    @EventHandler(element = "modelBrowser", on = click)
    void onModelBrowser() {
        presenter.onModelBrowser();
    }

    @EventHandler(element = "expressionResolver", on = click)
    void onExpressionResolver() {
        presenter.onExpressionResolver();
    }

    @EventHandler(element = "settings", on = click)
    void onSettings() {
        presenter.onSettings();
    }
}
