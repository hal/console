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
package org.jboss.hal.ballroom.table;

/**
 * @author Harald Pehl
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public enum Scope {
    SELECTED("selected"), SELECTED_SINGLE("selectedSingle");

    public static Scope fromSelector(String selector) {
        if (SELECTED.selector().equals(selector)) {
            return SELECTED;
        } else if (SELECTED_SINGLE.selector().equals(selector)) {
            return SELECTED_SINGLE;
        } else {
            throw new IllegalArgumentException("Illegal selector: " + selector);
        }
    }

    private final String selector;

    Scope(final String selector) {
        this.selector = selector;
    }

    public String selector() {
        return selector;
    }
}
