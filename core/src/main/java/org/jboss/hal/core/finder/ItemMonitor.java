/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.finder;

import javax.inject.Inject;

import org.jboss.hal.spi.Callback;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.NavigationHandler;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.hal.resources.CSS.withProgress;

/**
 * Class to monitor item actions and show a progress indicator if they take longer than a given timeout. Relies on an unique
 * item id implemented by {@link ItemDisplay#getId()} and specified in the column setup.
 */
public class ItemMonitor implements NavigationHandler {

    public static void startProgress(final String itemId) {
        elemental2.dom.Element element = document.getElementById(itemId);
        if (element != null && !element.classList.contains(withProgress)) {
            element.classList.add(withProgress);
        }
    }

    public static void stopProgress(final String itemId) {
        elemental2.dom.Element element = document.getElementById(itemId);
        if (element != null) {
            element.classList.remove(withProgress);
        }
    }

    private String itemId;
    private String nameToken;

    @Inject
    public ItemMonitor(final EventBus eventBus) {
        eventBus.addHandler(NavigationEvent.getType(), this);
    }

    @Override
    public void onNavigation(final NavigationEvent navigationEvent) {
        if (activeMonitor() && nameToken.equals(navigationEvent.getRequest().getNameToken())) {
            stopCurrentMonitor();
        }
    }

    /**
     * Wraps and monitors an item action which triggers a place request.
     */
    public <T> ItemActionHandler<T> monitorPlaceRequest(final String itemId, final String nameToken,
            final Callback callback) {
        return itm -> {
            callback.execute();
            startMonitor(itemId, nameToken);
        };
    }

    private void startMonitor(final String itemId, final String nameToken) {
        // first make sure to cancel a running monitor
        stopCurrentMonitor();

        // register new monitor and start progress indicator
        this.itemId = itemId;
        this.nameToken = nameToken;
        startProgress(itemId);
    }

    private void stopCurrentMonitor() {
        if (itemId != null) {
            stopProgress(itemId);
        }
        this.itemId = null;
        this.nameToken = null;
    }

    private boolean activeMonitor() {
        return itemId != null && nameToken != null;
    }
}
