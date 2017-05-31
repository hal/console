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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.HasElements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * An element which implements the <a href="https://www.patternfly.org/patterns/vertical-with-persistent-secondary/">vertical
 * navigation</a> from PatternFly.
 * <p>
 * The vertical navigation consists of two parts:
 * <ol>
 * <li>The actual menu / navigation entries which are child elements of the vertical navigation</li>
 * <li>The panes which visibility is controlled by the vertical navigation, but which are <strong>not</strong> children
 * of the vertical navigation. The panes are typically children of the root container.</li>
 * </ol>
 * <p>
 * The vertical navigation itself is not a child but a sibling of the root container. It gets attached / detached to
 * the DOM by calling {@link #attach()} and {@link #detach()}.
 *
 * @author Harald Pehl
 * @see <a href="https://www.patternfly.org/patterns/vertical-with-persistent-secondary/">https://www.patternfly.org/patterns/vertical-with-persistent-secondary/</a>
 */
public class VerticalNavigation implements Attachable {

    @JsType(isNative = true)
    static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge select();

        public native void setupVerticalNavigation(boolean handleItemSelections);
    }


    private static class Entry implements IsElement {

        private final String id;
        private final String parentId;
        private final String text;
        private final HTMLElement element;
        private final LinkedHashSet<String> children;

        private Entry(final String id, String parentId, final String text, final HTMLElement element) {
            this.id = id;
            this.parentId = parentId;
            this.text = text;
            this.element = element;
            this.children = new LinkedHashSet<>();
        }

        @Override
        public HTMLElement asElement() {
            return element;
        }

        private void addChild(String id) {
            children.add(id);
        }

        private boolean hasChildren() {
            return !children.isEmpty();
        }

        private String firstChild() {
            return children.iterator().next();
        }
    }


    private static class Pane implements IsElement {

        private final String id;
        private final HTMLElement element;

        private Pane(final String id, final HTMLElement element) {
            this.id = id;
            this.element = element;
        }

        private Pane(final String id, final IsElement isElement) {
            this.id = id;
            this.element = isElement.asElement();
        }

        @Override
        public HTMLElement asElement() {
            return element;
        }
    }


    private static final int PRIMARY_VISIBLE_TEXT_LENGTH = 13;
    private static final int SECONDARY_VISIBLE_TEXT_LENGTH = 23;
    @NonNls private static final Logger logger = LoggerFactory.getLogger(VerticalNavigation.class);

    private static HTMLElement rootContainer;
    private static final HTMLElement root;
    private static final HTMLElement ul;

    static {
        root = div().css(navPfVertical, navPfVerticalHal)
                .add(ul = ul().css(listGroup).asElement())
                .asElement();
        Elements.setVisible(root, false);
    }

    private static void init() {
        rootContainer = (HTMLElement) DomGlobal.document.getElementById(Ids.ROOT_CONTAINER);
        DomGlobal.document.body.insertBefore(root, rootContainer);
    }

    private LinkedHashMap<String, Entry> entries;
    private LinkedHashMap<String, Pane> panes;
    private Map<String, Callback> callbacks;

    public VerticalNavigation() {
        this.entries = new LinkedHashMap<>();
        this.panes = new LinkedHashMap<>();
        this.callbacks = new HashMap<>();
    }

    @Override
    public void attach() {
        if (rootContainer == null) {
            init();
        }
        rootContainer.classList.add(containerPfNavPfVertical);
        if (hasSecondary()) {
            rootContainer.classList.add(containerPfNavPfVerticalWithSubMenus);
            rootContainer.classList.add(navPfPersistentSecondary);
            root.classList.add(navPfVerticalWithSubMenus);
            root.classList.add(navPfPersistentSecondary);
        }
        entries.values().stream()
                .filter(entry -> entry.parentId == null)
                .forEach(entry -> ul.appendChild(entry.asElement()));
        Elements.setVisible(root, true);

        Bridge.select().setupVerticalNavigation(true);
        showInitial();
    }

    @Override
    public void detach() {
        Elements.removeChildrenFrom(ul);
        root.classList.remove(navPfPersistentSecondary);
        root.classList.remove(navPfVerticalWithSubMenus);
        root.classList.remove(secondaryVisiblePf);
        rootContainer.classList.remove(secondaryVisiblePf);
        rootContainer.classList.remove(navPfPersistentSecondary);
        rootContainer.classList.remove(containerPfNavPfVerticalWithSubMenus);
        rootContainer.classList.remove(containerPfNavPfVertical);

        Elements.setVisible(root, false);
    }


    // ------------------------------------------------------ add primary items

    /**
     * Adds a primary navigation entry which acts a container for secondary navigation entries.
     */
    public VerticalNavigation addPrimary(String id, String text) {
        return addPrimary(entries, panes, id, text, null, null);
    }

    public VerticalNavigation addPrimary(String id, String text, String iconClass) {
        return addPrimary(entries, panes, id, text, iconClass, null);
    }

    /**
     * Adds a primary navigation entry to the navigation which controls the visibility of the specified element.
     * <p>
     * Unlike similar UI elements such as {@code Tabs} the element is <strong>not</strong> added as a child of this
     * navigation. The element should be rather a child of the root container.
     */
    public VerticalNavigation addPrimary(String id, String text, String iconClass, IsElement element) {
        return addPrimary(entries, panes, id, text, iconClass, new Pane(id, element));
    }

    public VerticalNavigation addPrimary(String id, String text, String iconClass, HTMLElement element) {
        return addPrimary(entries, panes, id, text, iconClass, new Pane(id, element));
    }

    /**
     * Inserts a primary navigation entry <em>before</em> the specified entry. If {@code beforeId} is {@code null}, the
     * entry is inserted as last entry. If there's not entry with id {@code beforeId}, an error message is logged and
     * no entry is inserted.
     * <p>
     * You must call this method <em>after</em> at least one entry was added and <em>before</em> the navigation is
     * {@linkplain #attach() attached}.
     */
    public void insertPrimary(String id, String beforeId, String text, String iconClass, IsElement element) {
        insertPrimary(id, beforeId, text, iconClass, element.asElement());
    }

    public void insertPrimary(String id, String beforeId, String text, String iconClass, HTMLElement element) {
        if (entries.isEmpty()) {
            logger.error("Cannot insert {}: There has to be at least one other entry.", id);
            return;
        }

        if (beforeId == null) {
            // as last entry
            Pane lastPane = panes.values().iterator().next();
            Pane pane = new Pane(id, element);
            addPrimary(entries, panes, id, text, iconClass, pane);
            lastPane.asElement().parentNode.appendChild(pane.asElement());

        } else {
            if (entries.containsKey(beforeId)) {
                LinkedHashMap<String, Entry> reshuffledEntries = new LinkedHashMap<>();
                LinkedHashMap<String, Pane> reshuffledPanes = new LinkedHashMap<>();
                Iterator<String> entryIterator = entries.keySet().iterator();
                Iterator<String> paneIterator = panes.keySet().iterator();

                while (entryIterator.hasNext() && paneIterator.hasNext()) {
                    String currentId = entryIterator.next();
                    paneIterator.next();

                    if (currentId.equals(beforeId)) {
                        Pane pane = new Pane(id, element);
                        addPrimary(reshuffledEntries, reshuffledPanes, id, text, iconClass, pane);
                        Pane refPane = panes.get(currentId);
                        refPane.asElement().parentNode.insertBefore(pane.asElement(), refPane.asElement());
                        reshuffledEntries.put(currentId, entries.get(currentId));
                        reshuffledPanes.put(currentId, panes.get(currentId));

                    } else {
                        reshuffledEntries.put(currentId, entries.get(currentId));
                        reshuffledPanes.put(currentId, panes.get(currentId));
                    }
                }
                entries = reshuffledEntries;
                panes = reshuffledPanes;

            } else {
                logger.error("Cannot insert {} before {}: No entry with id {} found!", id, beforeId, beforeId);
            }
        }
    }

    private VerticalNavigation addPrimary(LinkedHashMap<String, Entry> entries, LinkedHashMap<String, Pane> panes,
            String id, String text, String iconClass, Pane pane) {

        HTMLAnchorElement a;
        HTMLElement span;
        HTMLElement primary = li().css(listGroupItem)
                .id(id)
                .add(a = a().css(clickable).asElement())
                .asElement();

        if (pane != null) {
            bind(a, click, event -> show(id));
        }
        if (iconClass != null) {
            a.appendChild(span().css(iconClass).asElement());
        }
        a.appendChild(span = span().css(listGroupItemValue).textContent(text).asElement());
        if (text.length() > PRIMARY_VISIBLE_TEXT_LENGTH) {
            span.title = text;
        }

        Entry entry = new Entry(id, null, text, primary);
        entries.put(id, entry);
        if (pane != null) {
            panes.put(id, pane);
        }

        return this;
    }


    // ------------------------------------------------------ add secondary items

    public VerticalNavigation addSecondary(String primaryId, String id, String text, HTMLElement element) {
        return addSecondary(primaryId, id, text, new Pane(id, element));
    }

    private VerticalNavigation addSecondary(String primaryId, String id, String text, Pane pane) {
        Entry primaryEntry = entries.get(primaryId);

        if (primaryEntry != null) {
            HTMLElement secondaryUl = (HTMLElement) primaryEntry.asElement()
                    .querySelector("." + navPfSecondaryNav + " > ul." + listGroup); //NON-NLS

            if (secondaryUl == null) {
                // seems to be the first secondary entry -> setup the secondary containers
                String secondaryContainerId = Ids.build(primaryId, "secondary");
                primaryEntry.asElement().classList.add(secondaryNavItemPf);
                primaryEntry.asElement().dataset.set(UIConstants.TARGET, "#" + secondaryContainerId);

                HTMLElement span;
                HTMLElement div = div().css(navPfSecondaryNav, navPfSecondaryNavHal)
                        .id(secondaryContainerId)
                        .add(div().css(navItemPfHeader)
                                .add(a().css(secondaryCollapseTogglePf)
                                        .data(UIConstants.TOGGLE, "collapse-secondary-nav")) //NON-NLS
                                .add(span = span().textContent(primaryEntry.text).asElement()))
                        .add(secondaryUl = ul().css(listGroup).asElement())
                        .asElement();

                if (text.length() > SECONDARY_VISIBLE_TEXT_LENGTH) {
                    span.title = text;
                }
                primaryEntry.asElement().appendChild(div);
            }

            HTMLElement li = li().css(listGroupItem)
                    .add(a().css(clickable).on(click, event -> show(id))
                            .add(span().css(listGroupItemValue).textContent(text)))
                    .asElement();

            primaryEntry.addChild(id);
            Entry secondaryEntry = new Entry(id, primaryId, text, li);
            secondaryUl.appendChild(secondaryEntry.asElement());
            entries.put(id, secondaryEntry);
            panes.put(id, pane);

        } else {
            logger.error("Unable to find primary navigation entry for id '{}'", primaryId);
        }
        return this;
    }


    // ------------------------------------------------------ misc

    private void showInitial() {
        if (!entries.isEmpty()) {
            String id;
            Map.Entry<String, Entry> entry = entries.entrySet().iterator().next();
            if (entry.getValue().hasChildren()) {
                id = entry.getValue().firstChild();
            } else {
                id = entry.getValue().id;
            }
            show(id);
        }
    }

    public void show(String id) {
        Entry show = entries.get(id);
        if (show != null) {
            if (show.parentId != null) {
                show(show.parentId);
            }
            for (Pane pane : panes.values()) {
                Elements.setVisible(pane.asElement(), pane.id.equals(id));
            }
            show.asElement().click();
            if (callbacks.containsKey(id)) {
                callbacks.get(id).execute();
            }

        } else {
            logger.error("Unable to show entry for id '{}': No such entry!", id);
        }
    }

    /**
     * Controls the visibility of the specified entry.
     */
    public void setVisible(String id, boolean visible) {
        Entry entry = entries.get(id);
        Pane pane = panes.get(id);
        if (entry != null && pane != null) {
            Elements.setVisible(entry.asElement(), visible);
            if (!visible && Elements.isVisible(pane.asElement())) {
                Elements.setVisible(pane.asElement(), false);
            }
        } else {
            logger.error("Unable to hide entry for id '{}': No such entry!", id);
        }
    }

    public void onShow(String id, Callback callback) {
        callbacks.put(id, callback);
    }

    public void updateBadge(String id, int count) {
        Entry entry = entries.get(id);
        if (entry != null) {
            Element a = entry.asElement().firstElementChild;
            HTMLElement badgeContainer = (HTMLElement) a.querySelector("." + badgeContainerPf);
            if (badgeContainer != null) {
                a.removeChild(badgeContainer);
            }
            badgeContainer = div().css(badgeContainerPf)
                    .add(span().css(badge).textContent(String.valueOf(count)))
                    .asElement();
            a.appendChild(badgeContainer);
        } else {
            logger.error("Unable to find navigation entry for id '{}'", id);
        }
    }

    /**
     * Returns the elements which were registered using the {@code add()} methods. Use this method to add those
     * elements to another container.
     */
    public HasElements panes() {
        return () -> panes.values().stream().map(Pane::asElement).collect(toList());
    }

    private boolean hasSecondary() {
        return entries.values().stream().anyMatch(entry -> !entry.children.isEmpty());
    }
}
