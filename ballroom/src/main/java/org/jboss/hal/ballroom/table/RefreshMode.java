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
 * Typesafe enum for the paging parameter of {@link Api#draw(String)}.
 *
 * @see <a href="https://datatables.net/reference/api/draw()">https://datatables.net/reference/api/draw()</a>
 */
@SuppressWarnings("HardCodedStringLiteral")
public enum RefreshMode {

    /**
     * The ordering and search will be recalculated and the rows redrawn in their new positions. The paging will be
     * reset back to the first page.
     */
    RESET("full-reset"),

    /**
     * The ordering and search will be recalculated and the rows redrawn in their new positions. The paging will
     * not be reset - i.e. the current page will still be shown.
     */
    HOLD("full-hold"),

    /**
     * Ordering and search will not be updated and the paging position held where is was. This is useful for paging
     * when data has not been changed between draws.
     */
    PAGE("page");

    private final String mode;

    RefreshMode(String mode) {
        this.mode = mode;
    }

    public String mode() {
        return mode;
    }
}
