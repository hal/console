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
package org.jboss.hal.config.keycloak;

import org.jboss.hal.config.keycloak.Keycloak.KeycloakConfig;
import org.jboss.hal.js.Json;
import org.jboss.hal.js.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elemental2.dom.HTMLScriptElement;
import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.fetch;

public class KeycloakSingleton {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakSingleton.class);

    private static final String ELYTRON_OIDC_CLIENT_WILDFLY_CONSOLE = "/oidc/wildfly-console";
    private static final String KEYCLOAK_JS_PATH = "/js/keycloak.js";
    private static final String AUTH_SERVER_URL = "auth-server-url";
    private static final String REALM = "realm";
    private static final String RESOURCE = "resource";
    static final int TOKEN_TIMEOUT = 32;

    private static Keycloak instance = null;

    /**
     * Checks if Keycloak has been configured. If so it creates a new {@link Keycloak} instance and assigns it to the
     * {@linkplain #instance() singleton}. Otherwise the promise is resolved with an error message.
     */
    public static Promise<Keycloak> check(String baseUrl) {
        String configUrl = baseUrl + ELYTRON_OIDC_CLIENT_WILDFLY_CONSOLE;
        return fetch(configUrl)
                .then(response -> {
                    if (response.ok) {
                        return response.text();
                    } else {
                        return Promise.reject("No OIDC configuration found at " + configUrl);
                    }
                })
                .then(text -> {
                    JsonObject json = Json.parse(text);
                    KeycloakConfig config = new KeycloakConfig();
                    config.url = json.getString(AUTH_SERVER_URL);
                    config.realm = json.getString(REALM);
                    config.clientId = json.getString(RESOURCE);
                    return loadKeycloakJs(config);
                })
                .then(config -> {
                    if (instance != null) {
                        logger.warn("OIDC has already been initialized!");
                    }
                    instance = new Keycloak(config);
                    return Promise.resolve(instance);
                });
    }

    private static Promise<KeycloakConfig> loadKeycloakJs(KeycloakConfig config) {
        return new Promise<>((resolve, reject) -> {
            // load keycloak.js from Keycloak server
            HTMLScriptElement script = (HTMLScriptElement) document.createElement("script");
            script.src = config.url + KEYCLOAK_JS_PATH;
            script.onload = onLoadEvent -> {
                resolve.onInvoke(config);
            };
            document.head.appendChild(script);
        });
    }

    public static native boolean presentAndValid()/*-{
        var keycloak = $wnd[keycloak];
        return keycloak != null && !keycloak.isTokenExpired();
    }-*/;

    public static Keycloak instance() {
        return instance;
    }
}
