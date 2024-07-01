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
package org.jboss.hal.config;

import static org.jboss.hal.resources.CSS.fontAwesome;

/**
 * @see <a href=
 *      "https://docs.wildfly.org/32/Admin_Guide.html#Feature_stability_levels">https://docs.wildfly.org/32/Admin_Guide.html#Feature_stability_levels</a>
 */
public enum StabilityLevel {

    // Keep the order!

    EXPERIMENTAL("experimental", fontAwesome("flask")),

    PREVIEW("preview", fontAwesome("exclamation-triangle")),

    COMMUNITY("community", ""),

    DEFAULT("default", "");

    public final String label;
    public final String icon;

    StabilityLevel(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }
}
