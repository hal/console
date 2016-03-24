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

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.client.tools.MacroEditorPresenter;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.macro.MacroFinishedEvent;
import org.jboss.hal.dmr.macro.MacroFinishedEvent.MacroFinishedHandler;
import org.jboss.hal.dmr.macro.MacroOperationEvent;
import org.jboss.hal.dmr.macro.MacroOperationEvent.MacroOperationHandler;
import org.jboss.hal.dmr.macro.Macros;
import org.jboss.hal.dmr.macro.RecordingEvent;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import javax.inject.Inject;

import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class FooterPresenter extends PresenterWidget<FooterPresenter.MyView>
        implements IsElement, MacroOperationHandler, MacroFinishedHandler {

    // @formatter:off
    public interface MyView extends View, IsElement, HasPresenter<FooterPresenter> {
        void updateEnvironment(Environment environment);
        void updateVersion(Version version);
        void startRecording();
        void steps(int size);
        void stopRecording();
    }
    // @formatter:on


    private final Environment environment;
    private final PlaceManager placeManager;
    private final Macros macros;
    private final Capabilities capabilities;
    private final Resources resources;
    private final CheckForUpdate checkForUpdate;
    private boolean recording;

    @Inject
    public FooterPresenter(final EventBus eventBus,
            final MyView view,
            final Environment environment,
            final PlaceManager placeManager,
            final Macros macros,
            final Capabilities capabilities,
            final Resources resources) {
        super(eventBus, view);
        this.environment = environment;
        this.placeManager = placeManager;
        this.macros = macros;
        this.capabilities = capabilities;
        this.resources = resources;
        this.checkForUpdate = new CheckForUpdate(environment);
    }

    @Override
    public Element asElement() {
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
        Window.alert(NYI);
    }

    void onModelBrowser() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(NameTokens.MODEL_BROWSER).build());
    }

    void onExpressionResolver() {
        Window.alert(NYI);
    }

    void onMacroRecording() {
        if (recording) {
            recording = false;
            getView().stopRecording();
            getEventBus().fireEvent(RecordingEvent.stop());

        } else {
            new MacroOptionsDialog(macros, capabilities, resources, options -> {
                MessageEvent.fire(getEventBus(), Message.info(resources.constants().recordingStarted()));
                getEventBus().fireEvent(RecordingEvent.start(options));
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
    public void onMacroOperation(final MacroOperationEvent event) {
        getView().steps(event.getMacro().getOperations().size());
    }

    @Override
    public void onMacroFinished(final MacroFinishedEvent event) {
        MessageEvent.fire(getEventBus(), Message.info(resources.constants().recordingStopped()));

        if (event.getOptions().openInEditor()) {
            PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(NameTokens.MACRO_EDITOR)
                    .with(MacroEditorPresenter.MACRO_PARAM, event.getMacro().getName())
                    .build();
            placeManager.revealPlace(placeRequest);
        }
    }

    void onSettings() {
        Window.alert(NYI);
    }

    boolean isRecording() {
        return recording;
    }
}
