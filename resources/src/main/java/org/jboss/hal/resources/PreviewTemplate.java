/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.resources;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Template processor for HTML previews. HTML previews can contain the following variables, which are replaced by this class
 * depending on the build type (community or product):
 *
 * <ul>
 * <li><code>${build.shortName}</code>: "WildFly" or "JBoss EAP"</li>
 * <li><code>${build.fullName}</code>: "WildFly" or "JBoss Enterprise Application Platform"</li>
 * <li><code>${build.installDir}</code>: <code>WILDFLY_HOME</code> or <code>EAP_HOME</code></li>
 * </ul>
 */
@SuppressWarnings("HardCodedStringLiteral")
class PreviewTemplate {

    private static final String SHORT_NAME = "build.shortName";
    private static final String FULL_NAME = "build.fullName";
    private static final String INSTALL_DIR = "build.installDir";

    static final PreviewTemplate COMMUNITY = new PreviewTemplate(new ImmutableMap.Builder<String, String>()
            .put(SHORT_NAME, "WildFly")
            .put(FULL_NAME, "WildFly")
            .put(INSTALL_DIR, "WILDFLY_HOME")
            .build());

    static final PreviewTemplate PRODUCT = new PreviewTemplate(new ImmutableMap.Builder<String, String>()
            .put(SHORT_NAME, "JBoss EAP")
            .put(FULL_NAME, "JBoss Enterprise Application Platform")
            .put(INSTALL_DIR, "EAP_HOME")
            .build());

    static PreviewTemplate get() {
        String build = System.getProperty("hal.build", "community");
        return "community".equals(build) ? COMMUNITY : PRODUCT;
    }

    private final Map<String, String> context;

    private PreviewTemplate(Map<String, String> context) {
        this.context = context;
    }

    String evaluate(String text) {
        if (text != null) {
            if (text.contains("${")) {
                for (String key : context.keySet()) {
                    if (text.contains(expression(key))) {
                        text = evaluate(text, key);
                    }
                }
            }
        }
        return text;
    }

    private String evaluate(String text, String key) {
        return text.replace(expression(key), context.get(key));
    }

    private String expression(String key) {
        return "${" + key + "}";
    }
}
