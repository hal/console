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
package org.jboss.hal.core.mvp;

import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.proxy.DefaultPlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import javax.inject.Inject;
import javax.inject.Provider;

public class HalPlaceManager extends DefaultPlaceManager {

    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private Resources resources;
    private boolean firstRequest;

    @Inject
    public HalPlaceManager(final EventBus eventBus,
            final TokenFormatter tokenFormatter,
            @DefaultPlace final String defaultPlaceNameToken,
            @ErrorPlace final String errorPlaceNameToken,
            @UnauthorizedPlace final String unauthorizedPlaceNameToken,
            final MetadataProcessor metadataProcessor,
            @Footer final Provider<Progress> progress,
            final Resources resources) {
        super(eventBus, tokenFormatter, defaultPlaceNameToken, errorPlaceNameToken, unauthorizedPlaceNameToken,
                new PlaceHistoryHandler.DefaultHistorian());
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.resources = resources;
        this.firstRequest = true;
    }

    @Override
    protected void doRevealPlace(final PlaceRequest request, final boolean updateBrowserUrl) {
        metadataProcessor.process(request.getNameToken(), progress.get(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(final Throwable throwable) {
                unlock();
                revealDefaultPlace();
                getEventBus().fireEvent(new MessageEvent(
                        Message.error(resources.constants().metadataError(), throwable.getMessage())));
            }

            @Override
            public void onSuccess(final Void whatever) {
                HalPlaceManager.super.doRevealPlace(request, updateBrowserUrl);
                firstRequest = false;
            }
        });
    }

    @Override
    public void revealErrorPlace(final String invalidHistoryToken) {
        MessageEvent.fire(getEventBus(), Message.error(resources.messages().pageNotFound(invalidHistoryToken)));
        if (firstRequest) {
            // TODO find a more elegant way to get hold of the very first request
            super.revealErrorPlace(invalidHistoryToken);
        }
    }
}
