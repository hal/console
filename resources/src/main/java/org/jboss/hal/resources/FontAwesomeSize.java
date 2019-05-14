/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.resources;

import org.jetbrains.annotations.NonNls;

public enum FontAwesomeSize {

    large("lg"), x2("2x"), x3("3x"), x4("4x"), x5("5x");

    private final String size;

    FontAwesomeSize(@NonNls String size) {
        this.size = size;
    }

    public String size() {
        return size;
    }
}
