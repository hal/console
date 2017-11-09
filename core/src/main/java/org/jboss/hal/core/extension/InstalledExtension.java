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
package org.jboss.hal.core.extension;

import java.util.Collections;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.jboss.hal.core.Strings;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.js.JsonArray;
import org.jboss.hal.js.JsonObject;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SCRIPT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STYLESHEETS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.URL;

public class InstalledExtension extends NamedNode {

    public static InstalledExtension fromJson(final String url, final JsonObject json) {
        ModelNode node = new ModelNode();
        node.get(URL).set(url);
        for (String key : json.keys()) {
            if (STYLESHEETS.equals(key)) {
                JsonArray array = json.getArray(STYLESHEETS);
                for (int j = 0; j < array.length(); j++) {
                    node.get(key).add(array.getString(j));
                }
            } else {
                node.get(key).set(json.getString(key));
            }
        }
        return new InstalledExtension(url, node);
    }

    static InstalledExtension fromModelNode(final ModelNode modelNode) {
        if (!modelNode.hasDefined(URL)) {
            throw new IllegalArgumentException("ModelNode does not contain an URL");
        }
        return new InstalledExtension(modelNode.get(URL).asString(), modelNode);
    }


    private final SafeUri domain;
    private final SafeUri baseUrl;

    private InstalledExtension(final String url, final ModelNode modelNode) {
        super(modelNode);
        this.domain = UriUtils.fromString(Strings.getDomain(url));
        this.baseUrl = UriUtils.fromString(baseUrl(url));
    }

    public String getDomain() {
        return domain.asString();
    }

    public String getFqScript() {
        return baseUrl.asString() + "/" + get(SCRIPT).asString();
    }

    public List<String> getFqStylesheets() {
        List<String> stylesheets;
        if (hasDefined(STYLESHEETS)) {
            stylesheets = get(STYLESHEETS).asList().stream().map(ModelNode::asString).collect(toList());
        } else {
            stylesheets = Collections.emptyList();
        }
        return stylesheets;
    }

    private String baseUrl(String url) {
        String result = url;
        if (url != null) {
            if (url.endsWith("/")) {
                result = url.substring(0, url.length() - 1);
            } else {
                int lastSlash = url.lastIndexOf('/', url.length());
                if (lastSlash != -1) {
                    result = url.substring(0, lastSlash);
                }
            }
        }
        return result;
    }
}
