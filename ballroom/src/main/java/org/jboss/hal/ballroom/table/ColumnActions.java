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

import java.util.LinkedHashMap;

public class ColumnActions<T> {

    private final LinkedHashMap<String, InlineActionHandler<T>> columnActions;

    ColumnActions() {
        columnActions = new LinkedHashMap<>();
    }

    public void add(String id, InlineActionHandler<T> columnAction) {
        columnActions.put(id, columnAction);
    }

    public boolean isEmpty() {
        return columnActions.isEmpty();
    }

    public InlineActionHandler<T> get(String key) {
        return columnActions.get(key);
    }

    public LinkedHashMap<String, InlineActionHandler<T>> getColumnActions() {
        return columnActions;
    }
}
