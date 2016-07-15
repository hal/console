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

import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderColumn;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all application presenters displayed in {@link Slots#MAIN}.
 * <p>
 * If the view implements {@link HasVerticalNavigation} this presenter takes care of calling {@link
 * VerticalNavigation#on()} when the presenter is revealed and {@link VerticalNavigation#off()} when the presenter is
 * hidden.
 *
 * @author Harald Pehl
 */
public abstract class ApplicationPresenter<V extends PatternFlyView, Proxy_ extends ProxyPlace<?>>
        extends PatternFlyPresenter<V, Proxy_> {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ApplicationPresenter.class);

    protected final Finder finder;

    protected ApplicationPresenter(final EventBus eventBus, final V view, final Proxy_ proxy, final Finder finder) {
        super(eventBus, view, proxy, Slots.MAIN);
        this.finder = finder;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        if (getView() instanceof HasVerticalNavigation) {
            VerticalNavigation navigation = ((HasVerticalNavigation) getView()).getVerticalNavigation();
            if (navigation != null) {
                navigation.on();
                Scheduler.get().scheduleDeferred(navigation::showInitial);
            }
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        updateBreadcrumb();
    }

    @Override
    protected void onHide() {
        super.onHide();
        if (getView() instanceof HasVerticalNavigation) {
            VerticalNavigation navigation = ((HasVerticalNavigation) getView()).getVerticalNavigation();
            if (navigation != null) {
                navigation.off();
            }
        }
    }

    /**
     * Application presenters need to provide information about their path in the finder. Normally this path is
     * updated automatically when navigating in the finder. However since application presenters can also be revealed
     * using the breadcrumb dropdown or by entering the URL directly this information is crucial to restore the path
     * in the finder context.
     * <p>
     * Please make sure that the IDs for selected items in the finder path match to the IDs returned by {@link
     * org.jboss.hal.core.finder.ItemDisplay#getId()}
     * <p>
     * If this method returns {@code null} the path in the finder context is not touched.
     * <p>
     * Lifecycle: The method is called in {@link #onReset()}.
     */
    protected abstract FinderPath finderPath();

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
}
