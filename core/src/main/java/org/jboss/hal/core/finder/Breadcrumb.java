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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Breadcrumb implements Iterable<Breadcrumb.Segment> {

    public static final class Segment {

        public final String key;
        public final String value;

        public Segment(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + " = " + value;
        }
    }

    public static Breadcrumb empty() {
        return new Breadcrumb(new ArrayList<>());
    }

    public static Breadcrumb from(String[][] segments) {
        List<Segment> s = new ArrayList<>();
        for (String[] segment : segments) {
            s.add(new Segment(segment[0], segment[1]));
        }
        return new Breadcrumb(s);
    }

    private final List<Segment> segments;


    private Breadcrumb(final List<Segment> segments) {this.segments = segments;}

    public Breadcrumb append(String key, String value) {
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

    @Override
    public String toString() {
        return segments.isEmpty() ? "<empty>" : Joiner.on(" / ").join(segments);
    }
}
