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
package org.jboss.hal.ballroom.dataprovider;

import java.util.Map;
import java.util.function.Function;

public class Selection<T> {

    private final Function<T, String> identifier;
    private final Map<String, T> selection; // contains only selected items
    private final boolean multiselect;
    private final int total;

    public Selection(Function<T, String> identifier, Map<String, T> selection, boolean multiselect, int total) {
        this.identifier = identifier;
        this.selection = selection;
        this.multiselect = multiselect;
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Selection)) { return false; }

        Selection<?> selection1 = (Selection<?>) o;

        if (multiselect != selection1.multiselect) { return false; }
        if (total != selection1.total) { return false; }
        return selection.equals(selection1.selection);
    }

    @Override
    public int hashCode() {
        int result = selection.hashCode();
        result = 31 * result + (multiselect ? 1 : 0);
        result = 31 * result + total;
        return result;
    }

    public Map<String, T> getSelection() {
        return selection;
    }

    public boolean hasSelection() {
        return !selection.isEmpty();
    }

    public boolean isSelected(T item) {
        return selection.containsKey(identifier.apply(item));
    }

    public int getSelectionCount() {
        return selection.size();
    }

    public boolean isMultiselect() {
        return multiselect;
    }

    public int getTotal() {
        return total;
    }
}
