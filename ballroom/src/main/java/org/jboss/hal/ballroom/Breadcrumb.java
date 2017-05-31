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
package org.jboss.hal.ballroom;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.Elements.ol;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.active;
import static org.jboss.hal.resources.CSS.breadcrumb;
import static org.jboss.hal.resources.CSS.clickable;

/**
 * Breadcrumb element as specified by <a href="https://www.patternfly.org/pattern-library/widgets/#breadcrumbs">PatternFly</a>
 *
 * @author Harald Pehl
 * @see <a href="https://www.patternfly.org/pattern-library/widgets/#breadcrumbs>https://www.patternfly.org/pattern-library/widgets/#breadcrumbs</a>
 */
public class Breadcrumb implements IsElement {

    @FunctionalInterface
    public interface SegmentHandler {

        void onClick();
    }


    private final HTMLElement root;

    public Breadcrumb() {
        root = ol().css(breadcrumb).asElement();
    }

    public void clear() {
        Elements.removeChildrenFrom(root);
    }

    public Breadcrumb append(final String segment, final SegmentHandler handler) {
        root.appendChild(li()
                .add(a().css(clickable)
                        .textContent(segment)
                        .on(click, e -> handler.onClick()))
                .asElement());
        return this;
    }

    public Breadcrumb append(final String segment) {
        root.appendChild(li().css(active).textContent(segment).asElement());
        return this;
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }
}
