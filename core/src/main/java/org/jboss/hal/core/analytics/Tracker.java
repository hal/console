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
package org.jboss.hal.core.analytics;

import java.util.Set;

import com.google.common.collect.Sets;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.NavigationHandler;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.core.finder.FinderContext;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.finder.FinderContextEvent.FinderContextHandler;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.core.modelbrowser.ModelBrowserPath;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent.ModelBrowserPathHandler;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tracker implements NavigationHandler, FinderContextHandler, ModelBrowserPathHandler {

    private static final Logger logger = LoggerFactory.getLogger(Tracker.class);
    private static final Set<String> IGNORE_PLACES = Sets.newHashSet(
            NameTokens.ACCESS_CONTROL,
            NameTokens.CONFIGURATION,
            NameTokens.DEPLOYMENTS,
            // NameTokens.MANAGEMENT,
            NameTokens.MODEL_BROWSER,
            NameTokens.PATCHING,
            NameTokens.RUNTIME);

    private final GoogleAnalytics ga;

    public Tracker(GoogleAnalytics ga) {
        this.ga = ga;
    }

    @Override
    public void onNavigation(NavigationEvent event) {
        PlaceRequest request = event.getRequest();
        if (request == null && !IGNORE_PLACES.contains(request.getNameToken())) {

            StringBuilder page = new StringBuilder();
            page.append("/").append(request.getNameToken());
            for (String parameter : request.getParameterNames()) {
                append(page, parameter, request.getParameter(parameter, null));
            }

            logger.debug("Track page request {}", page);
            ga.trackPageView(page.toString());
        }
    }

    @Override
    public void onFinderContext(FinderContextEvent event) {
        FinderContext context = event.getFinderContext();

        StringBuilder page = new StringBuilder();
        page.append("/" + context.getToken());
        for (FinderSegment segment : context.getPath()) {
            append(page, segment.getColumnId(), segment.getItemId());
        }

        logger.debug("Track finder selection {}", page);
        ga.trackPageView(page.toString());
    }

    @Override
    public void onModelBrowserAddress(ModelBrowserPathEvent event) {
        ModelBrowserPath path = event.getPath();

        StringBuilder page = new StringBuilder();
        page.append("/" + NameTokens.MODEL_BROWSER);
        for (ModelBrowserPath.Segment[] segments : path) {
            append(page, segments[0].text, segments[1] != null ? segments[1].text : null);
        }

        logger.debug("Track model browser selection {}", page);
        ga.trackPageView(page.toString());
    }

    private void append(StringBuilder builder, String key, String value) {
        builder.append("/").append(Strings.strip(key, "/"));
        if (value != null) {
            builder.append("/").append(Strings.strip(value, "/"));
        }
    }
}
