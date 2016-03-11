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

import com.google.gwt.core.client.GWT;

import javax.inject.Inject;

import static org.jboss.hal.resources.Urls.*;

/**
 * Class for getting absolute URLs to the different endpoints used in HAL.
 *
 * @author Harald Pehl
 */
public class Endpoints {

    /**
     * Please use this constant only in cases where no DI is available.
     */
    @Inject
    public static Endpoints INSTANCE;

    /**
     * @return the base url w/o a trailing slash
     */
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
    }

    public String dmr() {
        return dmr;
    }

    public String upload() {
        return upload;
    }

    public String logout() {
        return logout;
    }

    public boolean isSameOrigin() {
        return sameOrigin;
    }

    public void setSameOrigin(final boolean sameOrigin) {
        this.sameOrigin = sameOrigin;
    }
}
