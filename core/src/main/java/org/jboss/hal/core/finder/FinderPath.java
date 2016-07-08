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
package org.jboss.hal.core.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * The finder path holds the current selection in the finder. It's a collection of segments with each segment holding
 * four values:
 * <ol>
 * <li>{@code columnId} The column id. Used to lookup columns from the column registry</li>
 * <li>{@code itemId} The selected item id</li>
 * <li>{@code columnTitle} The title of the column used in the UI. If not specified defaults to {@code columnId}</li>
 * <li>{@code itemTitle} The title of the selected item used un the UI. If not specified defaults to {@code
 * itemId}</li>
 * </ol>
 *
 * @author Harald Pehl
 */
public class FinderPath implements Iterable<FinderSegment> {

    // ------------------------------------------------------ static methods

    public static FinderPath from(String path) {
        List<FinderSegment> segments = new ArrayList<>();

        Map<String, String> parts = Splitter.on('/').withKeyValueSeparator('=').split(path);
        for (Map.Entry<String, String> entry : parts.entrySet()) {
            segments.add(new FinderSegment(entry.getKey(), entry.getValue()));
        }

        return new FinderPath(segments);
    }


    // ------------------------------------------------------ instance section

    private final List<FinderSegment> segments;

    public FinderPath() {
        this(Collections.emptyList());
    }

    public FinderPath(final List<FinderSegment> segments) {
        this.segments = new ArrayList<>();
        this.segments.addAll(segments);
    }

    public FinderPath append(String columnId, String itemId) {
        return append(columnId, itemId, columnId, itemId);
    }

    public FinderPath append(String columnId, String itemId, String columnTitle, String itemTitle) {
        segments.add(new FinderSegment(columnId, itemId, columnTitle, itemTitle));
        return this;
    }

    public <T> FinderPath append(FinderColumn<T> finderColumn) {
        FinderSegment<T> segment = new FinderSegment<>(finderColumn);
        segments.add(segment);
        return this;
    }

    @Override
    public Iterator<FinderSegment> iterator() {
        return segments.iterator();
    }

    public boolean isEmpty() {return segments.isEmpty();}

    public int size() {return segments.size();}

    public void clear() {segments.clear();}

    public FinderPath subPathAfter(String columnId) {
        FinderPath subPath = new FinderPath();
        boolean hit = false;
        for (Iterator<FinderSegment> iterator = iterator(); iterator.hasNext(); ) {
            FinderSegment segment = iterator.next();
            if (hit) {
                subPath.append(segment.getColumnId(), segment.getItemId());
            }
            if (segment.getColumnId().equals(columnId)) {
                hit = true;
            }
        }
        return subPath;
    }

    /**
     * @return a reversed copy of this path. The current path is not modified.
     */
    FinderPath reversed() {
        return new FinderPath(Lists.reverse(segments));
    }

    @Override
    public String toString() {
        return segments.stream()
                .filter(segment -> segment.getItemId() != null)
                .map(FinderSegment::toString)
                .collect(Collectors.joining("/"));
    }
}
