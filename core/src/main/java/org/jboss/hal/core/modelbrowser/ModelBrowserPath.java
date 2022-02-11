/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.modelbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.hal.ballroom.tree.Node;
import org.jboss.hal.resources.Ids;

public class ModelBrowserPath implements Iterable<ModelBrowserPath.Segment[]> {

    public static final Segment WILDCARD = new Segment(null, "*");

    private final ModelBrowser modelBrowser;
    private final List<Segment[]> segments;

    ModelBrowserPath(ModelBrowser modelBrowser, Node<Context> node) {
        this.modelBrowser = modelBrowser;
        this.segments = new ArrayList<>();

        Node<Context> current = node;
        List<Node<Context>> nodes = new ArrayList<>();
        while (current != null && !Ids.MODEL_BROWSER_ROOT.equals(current.id)) {
            nodes.add(current);
            current = modelBrowser.tree.getNode(current.parent);
        }
        Collections.reverse(nodes);

        for (Iterator<Node<Context>> iterator = nodes.iterator(); iterator.hasNext();) {
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

    public boolean isEmpty() {
        return segments.isEmpty();
    }

    @Override
    public Iterator<Segment[]> iterator() {
        return segments.iterator();
    }

    public ModelBrowser getModelBrowser() {
        return modelBrowser;
    }

    public static class Segment {

        public final String id;
        public final String text;

        Segment(String id, String text) {
            this.id = id;
            this.text = text;
        }
    }
}
