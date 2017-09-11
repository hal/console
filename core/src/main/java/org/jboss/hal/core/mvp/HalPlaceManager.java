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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.proxy.DefaultPlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import org.jboss.hal.core.configuration.ProfileSelectionEvent;
import org.jboss.hal.core.runtime.group.ServerGroupSelectionEvent;
import org.jboss.hal.core.runtime.host.HostSelectionEvent;
import org.jboss.hal.core.runtime.server.ServerSelectionEvent;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.StatementContext.Tuple;
import org.jboss.hal.meta.processing.MetadataProcessor;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;

import static org.jboss.hal.meta.StatementContext.Tuple.*;

/**
 * Custom place manager for HAL. The most important task of this place manager is to extract well-known place request
 * parameters and to process required resources associated with the name token.
 * <p>
 * It's crucial that this happens <strong>before</strong> the place is revealed. Therefore this place manager intercepts
 * the {@link #doRevealPlace(PlaceRequest, boolean)} method and
 * <ol>
 * <li>looks for place request parameters which match the the {@linkplain Tuple#resource() resource names} in the
 * statement context tuple enum. </li>
 * <li>if found, fires the related selection events ({@link ProfileSelectionEvent}, {@link ServerGroupSelectionEvent},
 * {@link HostSelectionEvent} or {@link ServerSelectionEvent})</li>
 * <li>processes the required resources according to the value of the {@code @Requires} annotation on the proxy
 * place</li>
 * <li>finally calls {@code super.doRevealPlace(request, updateBrowserUrl)}</li>
 * </ol>
 */
public class HalPlaceManager extends DefaultPlaceManager {

    private final MetadataProcessor metadataProcessor;
    private final Provider<Progress> progress;
    private final Map<Tuple, Consumer<String>> selectFunctions;
    private Resources resources;
    private boolean firstRequest;

    @Inject
    public HalPlaceManager(EventBus eventBus,
            TokenFormatter tokenFormatter,
            @DefaultPlace String defaultPlaceNameToken,
            @ErrorPlace String errorPlaceNameToken,
            @UnauthorizedPlace String unauthorizedPlaceNameToken,
            MetadataProcessor metadataProcessor,
            @Footer Provider<Progress> progress,
            Resources resources) {
        super(eventBus, tokenFormatter, defaultPlaceNameToken, errorPlaceNameToken, unauthorizedPlaceNameToken,
                new PlaceHistoryHandler.DefaultHistorian());
        this.metadataProcessor = metadataProcessor;
        this.progress = progress;
        this.resources = resources;
        this.firstRequest = true;

        selectFunctions = new HashMap<>();
        selectFunctions.put(SELECTED_PROFILE, param -> getEventBus().fireEvent(new ProfileSelectionEvent(param)));
        selectFunctions.put(SELECTED_GROUP, param -> getEventBus().fireEvent(new ServerGroupSelectionEvent(param)));
        selectFunctions.put(SELECTED_HOST, param -> getEventBus().fireEvent(new HostSelectionEvent(param)));
        selectFunctions.put(SELECTED_SERVER, param -> getEventBus().fireEvent(new ServerSelectionEvent(param)));
        selectFunctions.put(SELECTED_SERVER_CONFIG, param -> getEventBus().fireEvent(new ServerSelectionEvent(param)));
    }

    @Override
    protected void doRevealPlace(final PlaceRequest request, final boolean updateBrowserUrl) {
        // Special treatment for statement context relevant parameters: The {selected.*} tokens in the @Requires
        // annotations on proxy places need to have a value in the statement context *before* the metadata processor
        // kicks in. Thus we need to look into the place request for well-known parameters and trigger a selection.
        for (Map.Entry<Tuple, Consumer<String>> entry : selectFunctions.entrySet()) {
            String param = request.getParameter(entry.getKey().resource(), null);
            if (param != null) {
                entry.getValue().accept(param);
            }
        }

        metadataProcessor.process(request.getNameToken(), progress.get(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(final Throwable throwable) {
                unlock();
                if (firstRequest) {
                    revealDefaultPlace();
                } else {
                    revealCurrentPlace();
                }
                if (throwable == null) {
                    getEventBus().fireEvent(new MessageEvent(Message.error(resources.messages().metadataError())));
                } else {
                    getEventBus().fireEvent(new MessageEvent(
                            Message.error(resources.messages().metadataError(), throwable.getMessage())));
                }
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

    @Override
    public void revealUnauthorizedPlace(final String unauthorizedHistoryToken) {
        MessageEvent.fire(getEventBus(), Message.error(resources.messages().unauthorized()));
        if (firstRequest) {
            // TODO find a more elegant way to get hold of the very first request
            super.revealUnauthorizedPlace(unauthorizedHistoryToken);
        }
    }
}
