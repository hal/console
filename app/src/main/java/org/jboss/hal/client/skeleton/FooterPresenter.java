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

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.client.tools.MacroEditorPresenter;
import org.jboss.hal.client.tools.MacroOptionsDialog;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.expression.ExpressionDialog;
import org.jboss.hal.core.expression.ExpressionResolver;
import org.jboss.hal.core.mvp.HalView;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.macro.MacroFinishedEvent;
import org.jboss.hal.dmr.macro.MacroFinishedEvent.MacroFinishedHandler;
import org.jboss.hal.dmr.macro.MacroOperationEvent;
import org.jboss.hal.dmr.macro.MacroOperationEvent.MacroOperationHandler;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.dmr.macro.Recording;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

public class FooterPresenter extends PresenterWidget<FooterPresenter.MyView>
        implements IsElement, MacroOperationHandler, MacroFinishedHandler {

    private final Environment environment;
    private final PlaceManager placeManager;
    private final Settings settings;
    private final Macros macros;
    private final ExpressionResolver expressionResolver;
    private final Resources resources;
    private final AboutDialog aboutDialog;
    private final CheckForUpdate checkForUpdate;
    private boolean recording;

    @Inject
    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    public FooterPresenter(EventBus eventBus,
            MyView view,
            Environment environment,
            Endpoints endpoints,
            PlaceManager placeManager,
            Settings settings,
            Macros macros,
            ExpressionResolver expressionResolver,
            Resources resources) {
        super(eventBus, view);
        this.environment = environment;
        this.placeManager = placeManager;
        this.settings = settings;
        this.macros = macros;
        this.expressionResolver = expressionResolver;
        this.resources = resources;
        this.aboutDialog = new AboutDialog(environment, endpoints, resources);
        this.checkForUpdate = new CheckForUpdate(environment);
    }

    @Override
    public HTMLElement asElement() {
        return getView().asElement();
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(MacroFinishedEvent.getType(), this));
        registerHandler(getEventBus().addHandler(MacroOperationEvent.getType(), this));
        getView().setPresenter(this);
        getView().updateEnvironment(environment);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        PatternFly.initComponents();
        checkForUpdate.execute(version -> getView().updateVersion(version));
    }

    void onShowVersion() {
        aboutDialog.show();
    }

    void onModelBrowser() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.MODEL_BROWSER).build());
    }

    void onExpressionResolver() {
        new ExpressionDialog(expressionResolver, environment, resources).show();
    }

    void onMacroRecording() {
        if (recording) {
            recording = false;
            getView().stopRecording();
            getEventBus().fireEvent(Recording.stop());

        } else {
            new MacroOptionsDialog(macros, resources,
                    options -> {
                        MessageEvent.fire(getEventBus(), Message.info(resources.messages().recordingStarted()));
                        getEventBus().fireEvent(Recording.start(options));
                        getView().startRecording();
                        getView().steps(0);
                        recording = true;
                    }).show();
        }
    }

    void onMacroEditor() {
        if (!recording) {
            placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.MACRO_EDITOR).build());
        }
    }

    @Override
    public void onMacroOperation(MacroOperationEvent event) {
        getView().steps(event.getMacro().getOperations().size());
    }

    @Override
    public void onMacroFinished(MacroFinishedEvent event) {
        MessageEvent.fire(getEventBus(), Message.info(resources.messages().recordingStopped()));

        if (event.getOptions().openInEditor()) {
            PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.MACRO_EDITOR)
                    .with(MacroEditorPresenter.MACRO_PARAM, event.getMacro().getName())
                    .build();
            placeManager.revealPlace(placeRequest);
        }
    }

    void onSettings() {
        new SettingsDialog(environment, settings, resources).show();
    }


    // @formatter:off
    public interface MyView extends HalView, HasPresenter<FooterPresenter> {
        void updateEnvironment(Environment environment);
        void updateVersion(Version version);
        void startRecording();
        void steps(int size);
        void stopRecording();
    }
    // @formatter:on
}
