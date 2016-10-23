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

import javax.annotation.PostConstruct;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.DataElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventHandler;
import org.jboss.gwt.elemento.core.Templated;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.ui.UIRegistry;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.disabled;
import static org.jboss.hal.resources.CSS.pulse;

/**
 * @author Harald Pehl
 */
@Templated("MainLayout.html#footer")
public abstract class FooterView extends HalViewImpl implements FooterPresenter.MyView {

    // @formatter:off
    public static FooterView create(final Places places, final UIRegistry uiRegistry, final Resources resources) {
        return new Templated_FooterView(places, uiRegistry, resources);
    }

    public abstract Places places();
    public abstract UIRegistry uiRegistry();
    public abstract Resources resources();
    // @formatter:on


    @NonNls private static Logger logger = LoggerFactory.getLogger(FooterView.class);

    private FooterPresenter presenter;
    private Environment environment;

    @DataElement ProgressElement progress = new ProgressElement();
    @DataElement Element halVersion;
    @DataElement Element updateAvailable;
    @DataElement Element macroRecorder;
    @DataElement Element macroEditor;
    @DataElement Element recordingContainer;
    @DataElement Element steps;
    @DataElement Element recording;

    @PostConstruct
    void init() {
        uiRegistry().register(progress);
        Elements.setVisible(recordingContainer, false);
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
            logger.info("A new HAL version is available. Current version: {}, new version: {}",
                    environment.getHalVersion(), version);
            String message = resources().messages().updateAvailable(environment.getHalVersion().toString(),
                    version.toString());
            updateAvailable.setTitle(message);
            updateAvailable.getDataset().setAt(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            updateAvailable.getDataset().setAt(UIConstants.PLACEMENT, UIConstants.TOP);
            updateAvailable.getDataset().setAt(UIConstants.CONTAINER, UIConstants.BODY);
            Tooltip.element(updateAvailable).init();
            Elements.setVisible(updateAvailable, true);
        }
    }

    @Override
    public void startRecording() {
        macroRecorder.setTextContent(resources().constants().stopMacro());
        macroEditor.getClassList().add(disabled);
        Elements.setVisible(recordingContainer, true);
        recording.getClassList().add(pulse);
    }

    @Override
    public void steps(final int size) {
        steps.setTextContent(resources().messages().recordedOperations(size));
    }

    @Override
    public void stopRecording() {
        recording.getClassList().remove(pulse);
        Elements.setVisible(recordingContainer, false);
        macroEditor.getClassList().remove(disabled);
        macroRecorder.setTextContent(resources().constants().startMacro());
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

    @EventHandler(element = "macroRecorder", on = click)
    void onMacroRecorder() {
        presenter.onMacroRecording();
    }

    @EventHandler(element = "macroEditor", on = click)
    void onMacroEditor() {
        presenter.onMacroEditor();
    }

    @EventHandler(element = "settings", on = click)
    void onSettings() {
        presenter.onSettings();
    }
}
