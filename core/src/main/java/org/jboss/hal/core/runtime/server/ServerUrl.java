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
package org.jboss.hal.core.runtime.server;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ServerUrl {

    private final String url;
    private final boolean custom;

    ServerUrl(String url, boolean custom) {
        this.url = url;
        this.custom = custom;
    }

    @Override
    public String toString() {
        return "ServerUrl(" + url + '\'' + ", custom=" + custom + ')';
    }

    public SafeHtml getUrl() {
        return SafeHtmlUtils.fromString(url);
    }

    public boolean isCustom() {
        return custom;
    }
}
