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
import org.jboss.hal.dmr.macro.RecordingEvent;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Resources;

import javax.inject.Inject;

import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class FooterPresenter extends PresenterWidget<FooterPresenter.MyView> implements IsElement {

    // @formatter:off
    public interface MyView extends View, IsElement, HasPresenter<FooterPresenter> {
        void updateEnvironment(Environment environment);
        void updateVersion(Version version);
        void recordingLabel(String label);
    }
    // @formatter:on


    private final Environment environment;
    private final PlaceManager placeManager;
    private final Resources resources;
    private final CheckForUpdate checkForUpdate;
    private boolean recording;

    @Inject
    public FooterPresenter(final EventBus eventBus,
            final MyView view,
            final Environment environment,
            final PlaceManager placeManager,
            final Resources resources) {
        super(eventBus, view);
        this.environment = environment;
        this.placeManager = placeManager;
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
            getView().recordingLabel(resources.constants().startMacro());
            getEventBus().fireEvent(new RecordingEvent(RecordingEvent.Action.STOP));
        } else {
            recording = true;
            getView().recordingLabel(resources.constants().stopMacro());
            getEventBus().fireEvent(new RecordingEvent(RecordingEvent.Action.START));
        }
    }

    void onSettings() {
        Window.alert(NYI);
    }
}
