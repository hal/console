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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Harald Pehl
 */
public class FinderPath implements Iterable<FinderPath.Segment> {

    public static final class Segment {

        public final String key;
        public final String value;

        public Segment(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static FinderPath empty() {
        return new FinderPath(new ArrayList<>());
    }

    public static FinderPath from(String path) {
        List<Segment> s = new ArrayList<>();
        Map<String, String> segments = Splitter.on('/').withKeyValueSeparator('=').split(path);
        for (Map.Entry<String, String> entry : segments.entrySet()) {
            s.add(new Segment(entry.getKey(), entry.getValue()));
        }
        return new FinderPath(s);
    }

    private final List<Segment> segments;


    private FinderPath(final List<Segment> segments) {this.segments = segments;}

    public FinderPath append(String key, String value) {
        segments.add(new Segment(key, value));
        return this;
    }

    @Override
    public Iterator<Segment> iterator() {
        return segments.iterator();
    }

    public boolean isEmpty() {return segments.isEmpty();}

    public int size() {return segments.size();}

    public void clear() {segments.clear();}

    public Segment last() {
        if (!isEmpty()) {
            return segments.get(segments.size() - 1);
        }
        return null;
    }

    /**
     * @return a reversed copy of this path. The current path is not modified.
     */
    public FinderPath reversed() {
        return new FinderPath(Lists.reverse(segments));
    }

    @Override
    public String toString() {
        return Joiner.on("/").join(segments);
    }
}
