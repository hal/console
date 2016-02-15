/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
