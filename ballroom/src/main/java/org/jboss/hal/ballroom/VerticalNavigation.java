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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.FluentIterable;
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * An element which implements the <a href="https://www.patternfly.org/patterns/vertical-with-persistent-secondary/">vertical
 * navigation</a> from PatternFly.
 * <p>
 * The vertical navigation consists of two parts:
 * <ol>
 * <li>The navigation entries which are child elements of the vertical navigation</li>
 * <li>The panes which visibility is controlled by the vertical navigation, but which are <strong>not</strong> children
 * of the vertical navigation. The panes are typically children of the root container.</li>
 * </ol>
 * <p>
 * The vertical navigation itself is not a child but a sibling of the root container. That's why you have to use the
 * methods {@link VerticalNavigation#on()} and {@link VerticalNavigation#off()} to insert and remove the vertical
 * navigation from the DOM.
 * <p>
 *
 * @author Harald Pehl
 * @see <a href="https://www.patternfly.org/patterns/vertical-with-persistent-secondary/">https://www.patternfly.org/patterns/vertical-with-persistent-secondary/</a>
 */
public class VerticalNavigation {

    private static class Entry implements IsElement {

        private final String id;
        private final String text;
        private final Element element;
        private final List<Entry> children;
        private int badge;

        private Entry(final String id, final String text, final Element element) {
            this.id = id;
            this.text = text;
            this.element = element;
            this.children = new ArrayList<>();
        }

        public boolean add(final Entry entry) {return children.add(entry);}

        @Override
        public Element asElement() {
            return element;
        }
    }


    private static class Pane implements IsElement {

        private final Element element;
        private final IsElement isElement;

        private Pane(final Element element) {
            this.element = element;
            this.isElement = null;
        }

        Pane(final IsElement isElement) {
            this.element = null;
            this.isElement = isElement;
        }

        @Override
        public Element asElement() {
            //noinspection ConstantConditions
            return element != null ? element : isElement.asElement();
        }
    }


    private static final String UL = "ul";
    private static VerticalNavigation singleton = null;
    private static final Logger logger = LoggerFactory.getLogger(VerticalNavigation.class);

    private final Element root;
    private final Element ul;
    private final Map<String, Entry> entries;
    private final LinkedHashMap<String, Pane> panes;

    public VerticalNavigation() {
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(navPfVertical, navPfVerticalHal)
                .ul().rememberAs(UL).css(listGroup)
                .end()
            .end();
        // @formatter:on

        this.ul = builder.referenceFor(UL);
        this.root = builder.build();
        this.entries = new HashMap<>();
        this.panes = new LinkedHashMap<>();
    }

    /**
     * Inserts the vertical instance before the root container and adds the related CSS classes to the root container.
     */
    public void on() {
        if (singleton != null) {
            //noinspection HardCodedStringLiteral
            logger.error(
                    "There's another vertical navigation which is still attached to the DOM. Did you forget to call VerticalNavigation.off()?");
            off();
        }

        Element rootContainer = Browser.getDocument().getElementById(Ids.ROOT_CONTAINER);
        if (rootContainer != null) {
            Browser.getDocument().getBody().insertBefore(root, rootContainer);
            rootContainer.getClassList().add(containerPfNavPfVertical);
            VerticalNavigation.singleton = this;
        }
    }

    /**
     * Removes the vertical navigation from the body and removes the related CSS class from the root container.
     */
    public void off() {
        if (singleton != null && singleton.root != null && Browser.getDocument().getBody().contains(singleton.root)) {
            Browser.getDocument().getBody().removeChild(singleton.root);
            singleton = null;
        }
        Element rootContainer = Browser.getDocument().getElementById(Ids.ROOT_CONTAINER);
        if (rootContainer != null) {
            rootContainer.getClassList().remove(containerPfNavPfVertical);
        }
    }


    // ------------------------------------------------------ add primary items

    public VerticalNavigation addPrimary(String id, String text) {
        return addPrimary(id, text, null, (Pane)null);
    }

    /**
     * Adds a primary navigation entry to the navigation which controls the visibility of the specified element.
     * <p>
     * Unlike similar UI elements such as {@code Tabs} the element is <strong>not</strong> added as a child of this
     * navigation. The element should be rather a child of the root container.
     *
     * @param id      An unique id for the navigation entry
     * @param text    the text shown in the vertical navigation
     * @param element the element which visibility is controlled by this vertical navigation.
     */
    public VerticalNavigation addPrimary(String id, String text, IsElement element) {
        return addPrimary(id, text, null, new Pane(element));
    }

    public VerticalNavigation addPrimary(String id, String text, Element element) {
        return addPrimary(id, text, null, new Pane(element));
    }

    public VerticalNavigation addPrimary(String id, String text, String iconClass, IsElement element) {
        return addPrimary(id, text, iconClass, new Pane(element));
    }

    public VerticalNavigation addPrimary(String id, String text, String iconClass, Element element) {
        return addPrimary(id, text, iconClass, new Pane(element));
    }

    private VerticalNavigation addPrimary(String id, String text, String iconClass, Pane pane) {
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .li().css(listGroupItem).id(id)
                .a().css(clickable);
                    if (pane != null) {
                        builder.on(click, event -> show(id));
                    }
                    if (iconClass != null) {
                        builder.span().css(iconClass).end();
                    }
                    builder.span().css(listGroupItemValue).textContent(text).end()
                .end()
            .end();
        // @formatter:on

        Entry entry = new Entry(id, text, builder.build());
        ul.appendChild(entry.asElement());
        entries.put(id, entry);
        if (pane != null) {
            panes.put(id, pane);
        }

        return this;
    }


    // ------------------------------------------------------ add secondary items

    public VerticalNavigation addSecondary(String primaryId, String id, IsElement element) {
        return addSecondary(primaryId, id, new Pane(element));
    }

    public VerticalNavigation addSecondary(String primaryId, String id, Element element) {
        return addSecondary(primaryId, id, new Pane(element));
    }

    private VerticalNavigation addSecondary(String primaryId, String id, Pane pane) {
        return this;
    }


    // ------------------------------------------------------ misc

    public void show(String id) {
        for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            if (entry.getKey().equals(id)) {
                entry.getValue().asElement().getClassList().add(active);
            } else {
                entry.getValue().asElement().getClassList().remove(active);
            }
        }
        for (Map.Entry<String, Pane> entry : panes.entrySet()) {
            Elements.setVisible(entry.getValue().asElement(), entry.getKey().equals(id));
        }
    }

    /**
     * Returns the elements which were registered using the {@code add()} methods. Use this method to add those
     * elements to another container.
     */
    public HasElements panes() {
        //noinspection Guava
        return () -> FluentIterable.from(panes.values()).transform(Pane::asElement);
    }
}
