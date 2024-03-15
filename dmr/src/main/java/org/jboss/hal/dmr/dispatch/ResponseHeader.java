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
package org.jboss.hal.dmr.dispatch;

public enum ResponseHeader {

    // response header (https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Response_fields)
    ACCEPT_PATCH("Accept-Patch"),

    ACCEPT_RANGES("Accept-Ranges"),

    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),

    AGE("Age"),

    ALLOW("Allow"),

    ALT_SVC("Alt-Svc"),

    CACHE_CONTROL("Cache-Control"),

    CONNECTION("Connection"),

    CONTENT_DISPOSITION("Content-Disposition"),

    CONTENT_ENCODING("Content-Encoding"),

    CONTENT_LANGUAGE("Content-Language"),

    CONTENT_LENGTH("Content-Length"),

    CONTENT_LOCATION("Content-Location"),

    CONTENT_MD5("Content-MD5"),

    CONTENT_RANGE("Content-Range"),

    CONTENT_SECURITY_POLICY("Content-Security-Policy"),

    CONTENT_TYPE("Content-Type"),

    DATE("Date"),

    ETAG("ETag"),

    EXPIRES("Expires"),

    LAST_MODIFIED("Last-Modified"),

    LINK("Link"),

    LOCATION("Location"),

    P3P("P3P"),

    PRAGMA("Pragma"),

    PROXY_AUTHENTICATE("Proxy-Authenticate"),

    PUBLIC_KEY_PINS("Public-Key-Pins"),

    REFRESH("Refresh"),

    RETRY_AFTER("Retry-After"),

    SERVER("Server"),

    SET_COOKIE("Set-Cookies"),

    STATUS("Status"),

    STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),

    TRAILER("Trailer"),

    TRANSFER_ENCODING("Transfer-Encoding"),

    TSV("TSV"),

    UPGRADE("Upgrade"),

    UPGRADE_INSECURE_REQUESTS("Upgrade-Insecure-Requests"),

    VARY("Vary"),

    VIA("Via"),

    WARNING("Warning"),

    WWW_AUTHENTICATE("WWW-Authenticate"),

    X_CONTENT_DURATION("X-Content-Duration"),

    X_CONTENT_SECURITY_POLICY("X-Content-Security-Policy"),

    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),

    X_CORRELATION_ID("X-Correlation-ID"),

    X_FRAME_OPTIONS("X-Frame-Options"),

    X_POWERED_BY("X-Powered-By"),

    X_REQUEST_ID("X-Request-ID"),

    X_UA_COMPATIBLE("X-UA-Compatible"),

    X_WEBKIT_CSP("X-WebKit-CSP"),

    X_XSS_PROTECTION("X-XSS-Protection");

    private final String header;

    ResponseHeader(String header) {
        this.header = header;
    }

    public String header() {
        return header;
    }
}
