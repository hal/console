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
package org.jboss.hal.client.widget;

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
import org.jboss.hal.core.messaging.MessageEvent;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class HeaderPresenter extends PresenterWidget<HeaderPresenter.MyView> implements MessageEvent.MessageHandler {

    public interface MyView extends View, HasPresenter<HeaderPresenter> {

        void update(Environment environment, Endpoints endpoints, User user);

        void select(String nameToken);

        void showMessage(Message.Level level, String message);

        void updateMessageCount(int messages);
    }

    static class MessageHolder {
        final Message.Level level;
        final String message;
        boolean new_;

        MessageHolder(final Message.Level level, final String message) {
            this.level = level;
            this.message = message;
            this.new_ = true;
        }
    }


    private final PlaceManager placeManager;
    private final Environment environment;
    private final Endpoints endpoints;
    private final User user;
    private final List<MessageHolder> messages;

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
        this.messages = new ArrayList<>();
    }

    @Override
    protected void onBind() {
        super.onBind();
        addRegisteredHandler(MessageEvent.getType(), this);
        getView().setPresenter(this);
        getView().update(environment, endpoints, user);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().select(placeManager.getCurrentPlaceRequest().getNameToken());
        getView().updateMessageCount(messages.size());
    }

    public void navigateTo(final String place) {
        PlaceRequest placeRequest = new PlaceRequest.Builder().nameToken(place).build();
        placeManager.revealPlace(placeRequest);
    }

    public void toggleMessages() {

    }

    public void reconnect() {

    }

    public void logout() {

    }

    @Override
    public void onMessage(final MessageEvent event) {
        getView().showMessage(event.getLevel(), event.getMessage());
        messages.add(new MessageHolder(event.getLevel(), event.getMessage()));
        getView().updateMessageCount(messages.size());
    }
}
