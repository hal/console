/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.config;

import com.google.gwt.core.client.GWT;

/**
 * Class for getting absolute URLs to the different endpoints used in HAL.
 *
 * @author Harald Pehl
 */
public class Endpoints {

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
        dmr = safeUrl + "/management";
        logout = safeUrl + "/logout";
        upload = safeUrl + "/management-upload";
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
