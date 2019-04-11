/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.config.keycloak;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

/**
 * https://www.keycloak.org/docs/latest/securing_apps/index.html#_javascript_adapter
 */
@JsType(namespace = GLOBAL, isNative = true)
public class Keycloak {

    public String subject;
    public String token;

    public RealmAccess realmAccess;

    public UserProfile userProfile;

    public Keycloak(String kcConfigUrl) {
    }

    public native Api init(Api options);

    /**
     * Redirects to logout.
     *
     * @param options Options is an Object, where: redirectUri - Specifies the uri to redirect to after logout.
     *
     * */
    public native String logout(String options);

    /**
     * Returns the URL to logout the user.
     *
     * @param options Options is an Object, where: redirectUri - Specifies the uri to redirect to after logout.
     *
     * */
    public native String createLogoutUrl(String options);

    /** Returns the URL of the Account Management Console in keycloak server. */
    public native String createAccountUrl();

    public native UserProfile loadUserProfile();

    public native void updateToken(int time);

    @JsType(name = "options")
    public static class Api {

        public String onLoad = "login-required";
        // the default responseMode is "fragment" but there is a problem as the keycloak javascript appends the
        // token in the url fragment and as it is parsed in the ParameterTokenFormatter, it uses the '=' char to split
        // each gwt place parameter and doesn't consider the keycloak values, then throws an exception because there is
        // key=value pair
        public String responseMode = "query";

        public native Api success(SuccessCallback authenticated);

        public native Api error(ErrorCallback callback);

        @JsFunction
        public interface SuccessCallback {
            void success(boolean authenticated);
        }

        @JsFunction
        public interface ErrorCallback {
            void error();
        }
    }

    @JsType(isNative = true, namespace = GLOBAL)
    public static class UserProfile {

        public String email;
        public String firstName;
        public String lastName;
        public String username;

        public native void success(SuccessCallback profile);

        @JsOverlay
        @Override
        public final String toString() {
            return "UserProfile{" + firstName + " " + lastName + ", " + "email='" + email + '\'' + ", username='" +
                    username + '}';
        }

        @JsFunction
        public interface SuccessCallback {
            void success(UserProfile profile);
        }
    }

    @JsType(isNative = true, namespace = GLOBAL)
    public static class RealmAccess {

        public String[] roles;

    }

}
