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
import org.jboss.hal.core.finder.FinderContextEvent;
import org.jboss.hal.core.modelbrowser.ModelBrowserPathEvent;
import org.jboss.hal.core.mvp.NavigationTracker;
import org.jboss.hal.js.JsonObject;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.config.Settings.Key.COLLECT_USER_DATA;
import static org.jboss.hal.js.Json.stringify;

/** Initialises google analytics and binds {@link org.jboss.hal.core.mvp.NavigationTracker} */
public class StartAnalytics implements BootstrapTask {

    private static final String PRODUCTION_ID = "UA-89365654-1";
    private static final String DEVELOPMENT_ID = "UA-89365654-2";
    private static final String TEST_SUITE_ID = "UA-89365654-3";
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
        boolean collectUserData = settings.get(COLLECT_USER_DATA).asBoolean();
        if (collectUserData) {
            String id;
            String pathname = window.location.getPathname();
            boolean devMode = System.getProperty("superdevmode", "").equals("on");
            boolean testSuite = pathname.endsWith("ts.html");
            boolean productionMode = pathname.equals("/") || pathname.endsWith("index.html");
            if (devMode) {
                id = DEVELOPMENT_ID;
            } else if (testSuite) {
                id = TEST_SUITE_ID;
            } else if (productionMode) {
                id = PRODUCTION_ID;
            } else {
                id = UNKNOWN_ID;
            }
            logger.info("Collect user data is on: {}", id);

            HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
            script.text = "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\n" +
                    "ga('create', " + stringify(config(id)) + ");\n";
            document.head.appendChild(script);

            GoogleAnalytics ga = new GoogleAnalytics();
            NavigationTracker tracker = new NavigationTracker(ga);
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

        // custom data send with every page view
        // TODO Configure custom dimensions
        // config.put("access_control_provider", environment.getAccessControlProvider().name().toLowerCase());
        // config.put("hal_build", environment.getHalBuild().name().toLowerCase());
        // config.put("hal_version", environment.getHalVersion().toString());
        // config.put("management_version", environment.getManagementVersion().toString());
        // config.put("operation_mode", environment.getOperationMode().name().toLowerCase());
        // config.put("product_name", environment.getInstanceInfo().productName());
        // config.put("product_version", environment.getInstanceInfo().productVersion().toString());
        // config.put("release_name", environment.getInstanceInfo().releaseName());
        // config.put("release_version", environment.getInstanceInfo().releaseVersion().toString());
        // config.put("same_origin", endpoints.isSameOrigin());
        // config.put("sso", environment.isSingleSignOn());
        return config;
    }
}
