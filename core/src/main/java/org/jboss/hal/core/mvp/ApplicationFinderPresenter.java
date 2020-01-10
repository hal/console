/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.core.mvp;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all application presenters which interact with the finder. The presenter updates the breadcrumb by
 * taking the information from {@link #finderPath()} and fires a {@link FinderContextEvent} in {@link #onReset()}.
 */
public abstract class ApplicationFinderPresenter<V extends HalView, Proxy_ extends ProxyPlace<?>>
        extends ApplicationPresenter<V, Proxy_> implements HasFinderPath, SupportsExternalMode {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationFinderPresenter.class);

    private final Finder finder;

    protected ApplicationFinderPresenter(EventBus eventBus, V view, Proxy_ proxy, Finder finder) {
        super(eventBus, view, proxy);
        this.finder = finder;
    }

    /**
     * Updates the breadcrumb by taking the information from {@link #finderPath()} and fires a {@link
     * FinderContextEvent}. Finally calls {@code reload()}.
     */
    @Override
    protected void onReset() {
        super.onReset();
        updateBreadcrumb();
        reload();
    }

    @SuppressWarnings("unchecked")
    private void updateBreadcrumb() {
        FinderPath applicationPath = finderPath();
        if (applicationPath != null) {
            // try to connect segments with existing columns from the finder
            for (FinderSegment segment : applicationPath) {
                FinderColumn column = finder.getColumn(segment.getColumnId());
                if (column != null) {
                    segment.connect(column);
                } else {
                    logger.warn("Unable to find column '{}' to connect breadcrumb segment '{}' for token '{}'",
                            segment.getColumnId(), segment, getProxy().getNameToken());
                }
            }
            finder.getContext().reset(applicationPath);
        }
        // The breadcrumb is part of the header. Notify the header presenter to take care of updating the breadcrumb
        getEventBus().fireEvent(new FinderContextEvent(finder.getContext()));
    }

    /**
     * Override this method to
     * <ol>
     * <li>load the data from the backend and</li>
     * <li>update the view</li>
     * </ol>
     * It's called as part of the {@link #onReset()} method.
     */
    protected abstract void reload();
}
