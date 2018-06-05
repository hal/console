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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import elemental2.dom.HTMLScriptElement;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.core.analytics.GoogleAnalytics;
import org.jboss.hal.core.analytics.Tracker;
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent;
import org.jboss.hal.js.JsonObject;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.config.Settings.Key.COLLECT_USER_DATA;
import static org.jboss.hal.config.Settings.Key.LOCALE;
import static org.jboss.hal.js.Json.stringify;

/** Initialises google analytics and binds {@link Tracker} */
public class StartAnalytics implements BootstrapTask {

    private static final String PRODUCTION_ID = "UA-89365654-1";
    private static final String DEVELOPMENT_ID = "UA-89365654-2";
    private static final String UNKNOWN_ID = "UA-89365654-4";

    @NonNls private static final Logger logger = LoggerFactory.getLogger(StartAnalytics.class);

    private final Environment environment;
    private final Endpoints endpoints;
    private final Settings settings;
    private final EventBus eventBus;

    @Inject
    public StartAnalytics(Environment environment, Endpoints endpoints, Settings settings, EventBus eventBus) {
        this.environment = environment;
        this.endpoints = endpoints;
        this.settings = settings;
        this.eventBus = eventBus;
    }

    @Override
    public Completable call() {
        String pathname = window.location.getPathname();
        boolean testSuite = pathname.endsWith("ts.html");
        boolean collectUserData = settings.get(COLLECT_USER_DATA).asBoolean();
        if (!testSuite && collectUserData) {
            String id;
            boolean devMode = System.getProperty("superdevmode", "").equals("on");
            boolean productionMode = pathname.equals("/") || pathname.endsWith("index.html");
            if (devMode) {
                id = DEVELOPMENT_ID;
            } else if (productionMode) {
                id = PRODUCTION_ID;
            } else {
                id = UNKNOWN_ID;
            }
            logger.info("Collect user data is on: {}", id);

            HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
            script.text = "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');ga('create', " + stringify(
                    config(id)) + ");";
            document.head.appendChild(script);

            GoogleAnalytics ga = new GoogleAnalytics();
            ga.customDimension(1, environment.getAccessControlProvider().name().toLowerCase());
            ga.customDimension(2, environment.getHalBuild().name().toLowerCase());
            ga.customDimension(3, environment.getHalVersion().toString());
            ga.customDimension(4, environment.getManagementVersion().toString());
            ga.customDimension(5, environment.getOperationMode().name().toLowerCase());
            ga.customDimension(6, environment.getInstanceInfo().productName());
            ga.customDimension(7, environment.getInstanceInfo().productVersion());
            ga.customDimension(8, environment.getInstanceInfo().releaseName());
            ga.customDimension(9, environment.getInstanceInfo().releaseVersion());
            ga.customDimension(10, endpoints.isSameOrigin());
            ga.customDimension(11, environment.isSingleSignOn());
            ga.customDimension(12, settings.get(LOCALE).value());

            Tracker tracker = new Tracker(ga);
            eventBus.addHandler(NavigationEvent.getType(), tracker);
            eventBus.addHandler(FinderContextEvent.getType(), tracker);
            eventBus.addHandler(ModelBrowserPathEvent.getType(), tracker);

        } else {
            logger.info("Collect user data is off.");
        }
        return Completable.complete();
    }

    private JsonObject config(String id) {
        JsonObject config = JsonObject.create();
        config.put("anonymizeIp", true);
        config.put("cookieDomain", "auto");
        config.put("trackingId", id);
        return config;
    }
}
