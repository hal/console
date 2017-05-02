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
package org.jboss.hal.config;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static org.jboss.hal.resources.Urls.LOGOUT;
import static org.jboss.hal.resources.Urls.MANAGEMENT;
import static org.jboss.hal.resources.Urls.UPLOAD;

/**
 * Class for getting absolute URLs to the different endpoints used in HAL.
 *
 * @author Harald Pehl
 */
@JsType
public class Endpoints {

    /**
     * Please use this constant only in cases where no DI is available.
     */
    @Inject
    @JsIgnore
    public static Endpoints INSTANCE;

    /**
     * @return the base url w/o a trailing slash
     */
    @JsIgnore
    public static String getBaseUrl() {
        String hostUrl = GWT.getHostPageBaseURL();
        int schemeIndex = hostUrl.indexOf("://");
        int slash = hostUrl.indexOf('/', schemeIndex + 3);
        if (slash != -1) {
            return hostUrl.substring(0, slash);
        }
        return hostUrl;
    }

    private String dmr;
    private String logout;
    private String upload;
    private boolean sameOrigin;

    @JsIgnore
    public Endpoints() {
        useBase(getBaseUrl());
    }

    public void useBase(final String baseUrl) {
        String safeUrl = baseUrl;
        if (baseUrl.endsWith("/")) {
            safeUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        dmr = safeUrl + MANAGEMENT;
        logout = safeUrl + LOGOUT;
        upload = safeUrl + UPLOAD;
        sameOrigin = baseUrl.equals(getBaseUrl());
    }

    @JsProperty(name = "dmr")
    public String dmr() {
        return dmr;
    }

    @JsProperty(name = "upload")
    public String upload() {
        return upload;
    }

    @JsIgnore
    public String logout() {
        return logout;
    }

    @JsProperty(name = "sameOrigin")
    public boolean isSameOrigin() {
        return sameOrigin;
    }
}
