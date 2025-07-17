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

import elemental2.promise.Promise;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.config.keycloak.KeycloakSingleton.TOKEN_TIMEOUT;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * https://www.keycloak.org/docs/latest/securing_apps/index.html#_javascript_adapter
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Keycloak {

    public String subject;
    public String token;
    public KeycloakRoles realmAccess;
    public KeycloakProfile profile;

    public Keycloak(KeycloakConfig config) {
    }

    /**
     * Initializes this Keycloak instance by calling the {@link #init(KeycloakInitOptions)} method, then the
     * {@link #updateToken(int)} method and finally the {@link #loadUserProfile()} method. The user profile is assigned to the
     * {@link #profile} field.
     */
    @JsOverlay
    public final Promise<Boolean> authenticate() {
        // the default responseMode is "fragment" but there is a problem as the keycloak javascript appends the
        // token in the url fragment and as it is parsed in the ParameterTokenFormatter, it uses the '=' char to split
        // each gwt place parameter and doesn't consider the keycloak values, then throws an exception because there is
        // key=value pair
        KeycloakInitOptions options = new KeycloakInitOptions();
        options.onLoad = "login-required";
        options.responseMode = "query";

        return init(options)
                .then(authenticated -> updateToken(TOKEN_TIMEOUT))
                .then(ignore -> loadUserProfile())
                .then(profile -> {
                    this.profile = profile;
                    return Promise.resolve(true);
                });
    }

    public native Promise<Boolean> init(KeycloakInitOptions options);

    /** Returns the URL of the Account Management Console in keycloak server. */
    public native String createAccountUrl();

    public native Promise<KeycloakProfile> loadUserProfile();

    public native Promise<Boolean> updateToken(int time);

    public native String logout(Object options);

    // ------------------------------------------------------ inner classes

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class KeycloakConfig {

        public String url;
        public String realm;
        public String clientId;
    }

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class KeycloakInitOptions {

        String onLoad;
        String responseMode;
    }

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class KeycloakRoles {

        public String[] roles;
    }

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class KeycloakProfile {

        public String id;
        public String username;
        public String email;
        public String firstName;
        public String lastName;
    }
}
