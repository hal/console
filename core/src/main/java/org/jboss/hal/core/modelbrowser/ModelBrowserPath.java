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
package org.jboss.hal.core.modelbrowser;

import org.jboss.hal.ballroom.tree.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Harald Pehl
 */
public class ModelBrowserPath implements Iterable<ModelBrowserPath.Segment[]> {

    public static final Segment WILDCARD = new Segment(null, "*");

    public static class Segment {

        public final String id;
        public final String text;

        Segment(final String id, final String text) {
            this.id = id;
            this.text = text;
        }
    }


    private final ModelBrowser modelBrowser;
    private final Node<Context> node;
    private final List<Segment[]> segments;

    ModelBrowserPath(final ModelBrowser modelBrowser, final Node<Context> node) {
        this.modelBrowser = modelBrowser;
        this.node = node;
        this.segments = new ArrayList<>();

        Node<Context> current = node;
        List<Node<Context>> nodes = new ArrayList<>();
        while (current != null && !ModelBrowser.ROOT_ID.equals(current.id)) {
            nodes.add(current);
            current = modelBrowser.tree.api().getNode(current.parent);
        }
        Collections.reverse(nodes);

        for (Iterator<Node<Context>> iterator = nodes.iterator(); iterator.hasNext(); ) {
            Node<Context> n = iterator.next();
            Segment[] segments = new Segment[2];
            segments[0] = new Segment(n.id, n.text);
            if (iterator.hasNext()) {
                n = iterator.next();
                segments[1] = new Segment(n.id, n.text);
            } else {
                segments[1] = WILDCARD;
            }
            this.segments.add(segments);
        }
    }

    public boolean isEmpty() {return segments.isEmpty();}

    @Override
    public Iterator<Segment[]> iterator() {
        return segments.iterator();
    }

    public ModelBrowser getModelBrowser() {
        return modelBrowser;
    }
}
