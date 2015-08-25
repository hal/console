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

import com.ekuefler.supereventbus.EventRegistration;
import com.ekuefler.supereventbus.Subscribe;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.core.HasPresenter;
import org.jboss.hal.core.messaging.Message;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class HeaderPresenter extends PresenterWidget<HeaderPresenter.MyView> {

    // @formatter:off
    public interface MyView extends View, HasPresenter<HeaderPresenter> {
        void update(Environment environment, Endpoints endpoints, User user);
        void selectTlc(String nameToken);
        void showMessage(Message message);
    }

    interface MessageRegistration extends EventRegistration<HeaderPresenter> {}
    // @formatter:on


    private final PlaceManager placeManager;
    private final Environment environment;
    private final Endpoints endpoints;
    private final User user;

    @Inject
    public HeaderPresenter(final EventBus eventBus,
            final MyView view,
            final com.ekuefler.supereventbus.EventBus superEventBus,
            final MessageRegistration registration,
            final PlaceManager placeManager,
            final Environment environment,
            final Endpoints endpoints,
            final User user) {
        super(eventBus, view);
        superEventBus.register(this, registration);

        this.placeManager = placeManager;
        this.environment = environment;
        this.endpoints = endpoints;
        this.user = user;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
        getView().update(environment, endpoints, user);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().selectTlc(placeManager.getCurrentPlaceRequest().getNameToken());
    }

    public void navigateTo(final String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(token).build();
        placeManager.revealPlace(placeRequest);
    }

    public void toggleMessages() {
        Window.alert("Toggle messages not yet implemented");
    }

    public void reconnect() {
        Window.alert("Reconnect not yet implemented");
    }

    public void logout() {
        Window.alert("Logout not yet implemented");
    }

    @Subscribe
    public void onMessage(final Message message) {
        getView().showMessage(message);
    }
}
