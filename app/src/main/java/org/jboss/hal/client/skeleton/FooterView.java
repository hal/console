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

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.template.DataElement;
import org.jboss.gwt.elemento.template.Templated;
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

import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.disabled;
import static org.jboss.hal.resources.CSS.pulse;

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
    @DataElement HTMLElement halVersion;
    @DataElement HTMLElement updateAvailable;
    @DataElement HTMLElement showVersion;
    @DataElement HTMLElement modelBrowser;
    @DataElement HTMLElement expressionResolver;
    @DataElement HTMLElement macroRecorder;
    @DataElement HTMLElement macroEditor;
    @DataElement HTMLElement recordingContainer;
    @DataElement HTMLElement steps;
    @DataElement HTMLElement recording;
    @DataElement HTMLElement settings;

    @PostConstruct
    void init() {
        uiRegistry().register(progress);
        Elements.setVisible(recordingContainer, false);
        Elements.setVisible(updateAvailable, false);

        bind(showVersion, click, event -> presenter.onShowVersion());
        bind(modelBrowser, click, event -> presenter.onModelBrowser());
        bind(expressionResolver, click, event -> presenter.onExpressionResolver());
        bind(macroRecorder, click, event -> presenter.onMacroRecording());
        bind(macroEditor, click, event -> presenter.onMacroEditor());
        bind(settings, click, event -> presenter.onSettings());
    }

    @Override
    public void setPresenter(final FooterPresenter presenter) {
        this.presenter = presenter;
    }

    public void updateEnvironment(Environment environment) {
        this.environment = environment;
        halVersion.textContent = environment.getHalVersion().toString();
    }

    @Override
    public void updateVersion(final Version version) {
        if (version.greaterThan(environment.getHalVersion())) {
            logger.info("A new HAL version is available. Current version: {}, new version: {}",
                    environment.getHalVersion(), version);
            String message = resources().messages().updateAvailable(environment.getHalVersion().toString(),
                    version.toString());
            updateAvailable.title = message;
            updateAvailable.dataset.set(UIConstants.TOGGLE, UIConstants.TOOLTIP);
            updateAvailable.dataset.set(UIConstants.PLACEMENT, UIConstants.TOP);
            updateAvailable.dataset.set(UIConstants.CONTAINER, UIConstants.BODY);
            Tooltip.element(updateAvailable).init();
            Elements.setVisible(updateAvailable, true);
        }
    }

    @Override
    public void startRecording() {
        macroRecorder.textContent = resources().constants().stopMacro();
        macroEditor.classList.add(disabled);
        Elements.setVisible(recordingContainer, true);
        recording.classList.add(pulse);
    }

    @Override
    public void steps(final int size) {
        steps.textContent = resources().messages().recordedOperations(size);
    }

    @Override
    public void stopRecording() {
        recording.classList.remove(pulse);
        Elements.setVisible(recordingContainer, false);
        macroEditor.classList.remove(disabled);
        macroRecorder.textContent = resources().constants().startMacro();
    }
}
