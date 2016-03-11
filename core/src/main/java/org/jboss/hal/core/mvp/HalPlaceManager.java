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
