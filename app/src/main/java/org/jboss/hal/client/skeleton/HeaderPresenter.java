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

import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.finder.FinderContextEvent.FinderContextHandler;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent.ModelBrowserPathHandler;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jboss.hal.spi.MessageEvent.MessageHandler;

import static org.jboss.hal.resources.Names.NYI;

/**
 * @author Harald Pehl
 */
public class HeaderPresenter extends PresenterWidget<HeaderPresenter.MyView>
        implements IsElement, MessageHandler, FinderContextHandler, ModelBrowserPathHandler {

    // @formatter:off
    public interface MyView extends View, IsElement, HasPresenter<HeaderPresenter> {
        void update(Environment environment, Endpoints endpoints, User user);
        void selectTlc(String nameToken);
        void showMessage(Message message);
        void tlcMode();
        void fullscreenMode(String title);
        void applicationMode();
        void updateBack(FinderContext finderContext);
        void updateBreadcrumb(FinderContext finderContext);
        void updateBreadcrumb(ModelBrowserPath path);
    }
    // @formatter:on


    static final int MAX_BREADCRUMB_VALUE_LENGTH = 20;
    static final int MESSAGE_TIMEOUT = 4321; // ms

    private final PlaceManager placeManager;
    private final Environment environment;
    private final Endpoints endpoints;
    private final User user;
    private final Finder finder;

    @Inject
    public HeaderPresenter(final EventBus eventBus,
            final MyView view,
            final PlaceManager placeManager,
            final Environment environment,
            final Endpoints endpoints,
            final User user,
            final Finder finder) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.environment = environment;
        this.endpoints = endpoints;
        this.user = user;
        this.finder = finder;
    }

    @Override
    public Element asElement() {
        return getView().asElement();
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(MessageEvent.getType(), this));
        registerHandler(getEventBus().addHandler(FinderContextEvent.getType(), this));
        registerHandler(getEventBus().addHandler(ModelBrowserPathEvent.getType(), this));
        getView().setPresenter(this);
        getView().update(environment, endpoints, user);
    }

    @Override
    protected void onUnbind() {
        super.onUnbind();
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().selectTlc(placeManager.getCurrentPlaceRequest().getNameToken());
    }

    void goTo(final String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
        placeManager.revealPlace(placeRequest);
    }

    public void toggleMessages() {
        Window.alert(NYI);
    }

    public void reconnect() {
        Window.alert(NYI);
    }

    public void logout() {
        Window.alert(NYI);
    }

    @Override
    public void onMessage(final MessageEvent event) {
        getView().showMessage(event.getMessage());
    }

    @Override
    public void onFinderContext(final FinderContextEvent event) {
        getView().updateBack(event.getFinderContext());
        getView().updateBreadcrumb(event.getFinderContext());
    }

    @Override
    public void onModelBrowserAddress(final ModelBrowserPathEvent event) {
        getView().updateBreadcrumb(event.getPath());
    }

    public void tlcMode() {
        getView().tlcMode();
    }

    public void applicationMode() {
        getView().applicationMode();
    }

    public void fullscreenMode(final String title) {
        if (finder.getContext().getToken() != null) {
            getView().updateBack(finder.getContext());
        }
        getView().fullscreenMode(title);
    }
}
