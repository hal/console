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
package org.jboss.hal.client.skeleton;

import javax.inject.Inject;

import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.ProgressElement;
import org.jboss.hal.ballroom.Tooltip;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Version;
import org.jboss.hal.core.mvp.HalViewImpl;
import org.jboss.hal.core.ui.UIRegistry;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.Elements.footer;
import static org.jboss.gwt.elemento.core.Elements.nav;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.ProgressElement.Label.NONE;
import static org.jboss.hal.ballroom.ProgressElement.Size.XS;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.CSS.footer;
import static org.jboss.hal.resources.UIConstants.DROPDOWN;
import static org.jboss.hal.resources.UIConstants.TOGGLE;

public class FooterView extends HalViewImpl implements FooterPresenter.MyView {

    private static Logger logger = LoggerFactory.getLogger(FooterView.class);

    private final Resources resources;

    private final HTMLElement halVersion;
    private final HTMLElement updateAvailable;
    private final HTMLElement macroRecorder;
    private final HTMLElement macroEditor;
    private final HTMLElement recordingContainer;
    private final HTMLElement steps;
    private final HTMLElement recording;

    private FooterPresenter presenter;
    private Environment environment;

    @Inject
    public FooterView(UIRegistry uiRegistry, Resources resources) {
        this.resources = resources;

        ProgressElement progress = new ProgressElement(XS, NONE, false);
        HTMLElement showVersion;
        HTMLElement modelBrowser;
        HTMLElement expressionResolver;
        HTMLElement settings;
        HTMLElement root = footer().css(footer)
                .add(nav().css(navbar, navbarFooter, navbarFixedBottom)
                        .add(ul().css(CSS.nav, navbarNav)
                                .add(li().css(footerProgress)
                                        .add(progress)))
                        .add(ul().css(CSS.nav, navbarNav, footerTools)
                                .add(recordingContainer = li()
                                        .add(div().css(tool)
                                                .add(steps = span().element())
                                                .add(recording = span()
                                                        .css(CSS.recording, fontAwesome("dot-circle-o"),
                                                                "fa-pulse")
                                                        .element()))
                                        .element())
                                .add(li()
                                        .add(showVersion = a().css(tool, clickable)
                                                .add(updateAvailable = span().css(pfIcon("info")).element())
                                                .add(halVersion = span().element())
                                                .element()))
                                .add(li().css(dropdown, hidden).id(Ids.FOOTER_EXTENSIONS_DROPDOWN)
                                        .add(a().css(tool, clickable, dropdownToggle)
                                                .data(TOGGLE, DROPDOWN)
                                                .title(Names.EXTENSIONS)
                                                .add(span().css(fontAwesome("th-large")))
                                                .add(b().css(caret)))
                                        .add(ul().css(dropdown, dropdownMenu).id(Ids.FOOTER_EXTENSIONS)))
                                .add(li().css(dropdown)
                                        .add(a("#").css(tool, dropdownToggle).data(TOGGLE, DROPDOWN)
                                                .add(span().css(fontAwesome("wrench")))
                                                .add(span().textContent(resources.constants().tools()))
                                                .add(b().css(caret)))
                                        .add(ul().css(dropdown, dropdownMenu)
                                                .add(li()
                                                        .add(modelBrowser = a().css(clickable)
                                                                .textContent(resources.constants().modelBrowser())
                                                                .element()))
                                                .add(li()
                                                        .add(expressionResolver = a().css(clickable)
                                                                .textContent(resources.constants().expressionResolver())
                                                                .element()))
                                                .add(li()
                                                        .add(macroRecorder = a().css(clickable)
                                                                .textContent(resources.constants().startMacro())
                                                                .element()))
                                                .add(li()
                                                        .add(macroEditor = a().css(clickable)
                                                                .textContent(resources.constants().macroEditor())
                                                                .element()))))
                                .add(li()
                                        .add(settings = a().css(tool, clickable)
                                                .add(span().css(fontAwesome("cogs")))
                                                .add(span().textContent(resources.constants().settings()))
                                                .element()))))
                .element();
        initElement(root);

        uiRegistry.register(progress);
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
    public void setPresenter(FooterPresenter presenter) {
        this.presenter = presenter;
    }

    public void updateEnvironment(Environment environment) {
        this.environment = environment;
        halVersion.textContent = environment.getHalVersion().toString();
    }

    @Override
    public void updateVersion(Version version) {
        if (version.greaterThan(environment.getHalVersion())) {
            logger.info("A new HAL version is available. Current version: {}, new version: {}",
                    environment.getHalVersion(), version);
            updateAvailable.title = resources.messages().updateAvailable(environment.getHalVersion().toString(),
                    version.toString());
            updateAvailable.dataset.set(TOGGLE, UIConstants.TOOLTIP);
            updateAvailable.dataset.set(UIConstants.PLACEMENT, UIConstants.TOP);
            updateAvailable.dataset.set(UIConstants.CONTAINER, UIConstants.BODY);
            Tooltip.element(updateAvailable).init();
            Elements.setVisible(updateAvailable, true);
        }
    }

    @Override
    public void startRecording() {
        macroRecorder.textContent = resources.constants().stopMacro();
        macroEditor.classList.add(disabled);
        Elements.setVisible(recordingContainer, true);
        recording.classList.add(pulse);
    }

    @Override
    public void steps(int size) {
        steps.textContent = resources.messages().recordedOperations(size);
    }

    @Override
    public void stopRecording() {
        recording.classList.remove(pulse);
        Elements.setVisible(recordingContainer, false);
        macroEditor.classList.remove(disabled);
        macroRecorder.textContent = resources.constants().startMacro();
    }
}
