/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
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

import javax.inject.Inject;

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
        void applicationMode();
        void updateBack(FinderContext finderContext);
        void updateBreadcrumb(FinderContext finderContext);
        void updateBreadcrumb(ModelBrowserPath path);
    }
    // @formatter:on


    static final int MESSAGE_TIMEOUT = 8000; // ms

    private final PlaceManager placeManager;
    private final Environment environment;
    private final Endpoints endpoints;
    private final User user;

    @Inject
    public HeaderPresenter(final EventBus eventBus,
            final MyView view,
            final PlaceManager placeManager,
            final Environment environment,
            final Endpoints endpoints,
            final User user) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.environment = environment;
        this.endpoints = endpoints;
        this.user = user;
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

    public void goTo(final String token) {
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
}
