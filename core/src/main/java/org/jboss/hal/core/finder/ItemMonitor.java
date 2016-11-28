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
package org.jboss.hal.core.finder;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.hal.spi.Callback;

import static org.jboss.hal.resources.CSS.withProgress;
import static org.jboss.hal.resources.UIConstants.MEDIUM_TIMEOUT;

/**
 * Class to monitor item actions and show a progress indicator if they take longer than a given timeout. Relies on an
 * unique item id implemented by {@link ItemDisplay#getId()} and specified in the column setup.
 *
 * @author Harald Pehl
 */
public class ItemMonitor {

    public static void startProgress(final String itemId) {
        Element element = Browser.getDocument().getElementById(itemId);
        if (element != null) {
            element.getClassList().add(withProgress);
        }
    }

    public static void stopProgress(final String itemId) {
        Element element = Browser.getDocument().getElementById(itemId);
        if (element != null) {
            element.getClassList().remove(withProgress);
        }
    }


    private final EventBus eventBus;
    private int timeoutHandle = -1;
    private HandlerRegistration handlerRegistration;

    @Inject
    public ItemMonitor(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Wraps and monitors an item action which triggers a place request.
     */
    public <T> ItemActionHandler<T> monitorPlaceRequest(final String itemId, final String nameToken,
            final Callback callback) {
        return itm -> {
            callback.execute();
            startProgress(itemId);
            timeoutHandle = Browser.getWindow().setTimeout(() ->
                    handlerRegistration = eventBus.addHandler(NavigationEvent.getType(), navigationEvent -> {
                        if (nameToken.equals(navigationEvent.getRequest().getNameToken())) {
                            handlerRegistration.removeHandler();
                            Browser.getWindow().clearTimeout(timeoutHandle);
                            stopProgress(itemId);
                        }
                    }), MEDIUM_TIMEOUT);
        };
    }
}
