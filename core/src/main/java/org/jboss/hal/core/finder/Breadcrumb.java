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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Harald Pehl
 */
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
