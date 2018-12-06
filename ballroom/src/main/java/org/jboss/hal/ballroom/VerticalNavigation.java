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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static java.util.stream.Collectors.toList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.resources.CSS.*;

/**
 * An element which implements the <a href="https://www.patternfly.org/patterns/vertical-with-persistent-secondary/">vertical
 * navigation</a> from PatternFly.
 *
 * <p>The vertical navigation consists of two parts:</p>
 * <ol>
 * <li>Items: The actual menu / navigation items which are child elements of the vertical navigation</li>
 * <li>Panes: The panes which are <strong>not</strong> children of the vertical navigation. The panes are typically
 * children
 * of the root container. Their visibility is controlled by the vertical navigation.</li>
 * </ol>
 *
 * <p>The vertical navigation itself is not a child but a sibling of the root container. It gets attached / detached to
 * the DOM by calling {@link #attach()} and {@link #detach()}.</p>
 *
 * <p>There are two groups of methods:</p>
 * <ul>
 * <li>{@code add*()}: Use these methods to add panes <strong>before</strong> the panes were added to the DOM</li>
 * <li>{@code insert*()}: Use these methods to add panes <strong>after</strong> the panes were added to the DOM</li>
 * </ul>
 *
 * @see <a href="https://www.patternfly.org/patterns/vertical-with-persistent-secondary/">https://www.patternfly.org/patterns/vertical-with-persistent-secondary/</a>
 */
// TODO Simplify: Replace linked collections. The order of items and panes should not matter, only the order
// TODO of elements in the DOM matters! This should simplify the insert*() methods.
public class VerticalNavigation implements Attachable {

    private static final int PRIMARY_VISIBLE_TEXT_LENGTH = 13;
    private static final int SECONDARY_VISIBLE_TEXT_LENGTH = 23;
    @NonNls private static final Logger logger = LoggerFactory.getLogger(VerticalNavigation.class);

    private static HTMLElement rootContainer;
    private static final HTMLElement root;
    private static final HTMLElement ul;

    static {
        root = div().css(navPfVertical, navPfVerticalHal)
                .add(ul = ul().css(listGroup).get())
                .get();
        Elements.setVisible(root, false);
    }

    private static void init() {
        rootContainer = (HTMLElement) document.getElementById(Ids.ROOT_CONTAINER);
        document.body.insertBefore(root, rootContainer);
    }

    private LinkedHashMap<String, Item> items;
    private LinkedHashMap<String, Pane> panes;
    private Map<String, Callback> callbacks;

    public VerticalNavigation() {
        this.items = new LinkedHashMap<>();
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
        items.values().stream()
                .filter(item -> item.parentId == null)
                .forEach(item -> ul.appendChild(item.element()));
        Elements.setVisible(root, true);

        Api.select().setupVerticalNavigation(true);
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
     * Adds a primary navigation item which acts a container for secondary navigation items.
     */
    public VerticalNavigation addPrimary(String id, String text) {
        return addPrimary(items, panes, id, text, null, null);
    }

    public VerticalNavigation addPrimary(String id, String text, String iconClass) {
        return addPrimary(items, panes, id, text, iconClass, null);
    }

    /**
     * Adds a primary navigation item to the navigation which controls the visibility of the specified element.
     * <p>
     * Unlike similar UI elements such as {@code Tabs} the element is <strong>not</strong> added as a child of this
     * navigation. The element should be rather a child of the root container.
     *
     * <p><strong>Please note</strong><br/>
     * This method does <strong>not</strong> add the item to the DOM. This has to be done manually using something
     * like</p>
     * <pre>
     * HTMLElement root = row()
     *     .add(column()
     *         .addAll(navigation.panes()))
     *     .get();
     * </pre>
     */
    public VerticalNavigation addPrimary(String id, String text, String iconClass, IsElement element) {
        return addPrimary(items, panes, id, text, iconClass, new Pane(id, element));
    }

    public VerticalNavigation addPrimary(String id, String text, String iconClass, HTMLElement element) {
        return addPrimary(items, panes, id, text, iconClass, new Pane(id, element));
    }

    /**
     * Inserts a primary navigation item <em>before</em> the specified item. If {@code beforeId} is {@code null}, the
     * item is inserted as last item. If there's not item with id {@code beforeId}, an error message is logged and
     * no item is inserted.
     * <p>
     * You must call this method <em>after</em> at least one item was added and <em>before</em> the navigation is
     * {@linkplain #attach() attached}.
     *
     * <p><strong>Please note</strong><br/>
     * Unlike {@link #addPrimary(String, String, String, IsElement)}, this method <strong>does</strong> add the item
     * to the DOM.</p>
     */
    public void insertPrimary(String id, String beforeId, String text, String iconClass, IsElement element) {
        insertPrimary(id, beforeId, text, iconClass, element.element());
    }

    public void insertPrimary(String id, String beforeId, String text, String iconClass) {
        HTMLElement ele = null;
        insertPrimary(id, beforeId, text, iconClass, ele);
    }

    public void insertPrimary(String id, String beforeId, String text, String iconClass, HTMLElement element) {
        if (items.isEmpty()) {
            logger.error("Cannot insert {}: There has to be at least one other item.", id);
            return;
        }

        if (beforeId == null) {
            // as last item
            Pane lastPane = panes.values().iterator().next();
            if (element != null) {
                Pane pane = new Pane(id, element);
                addPrimary(items, panes, id, text, iconClass, pane);
                lastPane.element().parentNode.appendChild(pane.element());
            } else {
                addPrimary(items, panes, id, text, iconClass, null);
            }
        } else {
            if (items.containsKey(beforeId)) {
                // TODO Could be simplified: The order of panes does not matter, only the order of items matters
                LinkedHashMap<String, Item> reshuffledItems = new LinkedHashMap<>();
                LinkedHashMap<String, Pane> reshuffledPanes = new LinkedHashMap<>();

                for (String currentId : items.keySet()) {
                    if (currentId.equals(beforeId)) {
                        if (element != null) {
                            Pane pane = new Pane(id, element);
                            addPrimary(reshuffledItems, reshuffledPanes, id, text, iconClass, pane);
                            Pane refPane = panes.get(currentId);
                            refPane.element().parentNode.insertBefore(pane.element(), refPane.element());
                        } else {
                            addPrimary(reshuffledItems, reshuffledPanes, id, text, iconClass, null);
                        }
                        reshuffledItems.put(currentId, items.get(currentId));
                        reshuffledPanes.put(currentId, panes.get(currentId));

                    } else {
                        reshuffledItems.put(currentId, items.get(currentId));
                        reshuffledPanes.put(currentId, panes.get(currentId));
                    }
                }
                items = reshuffledItems;
                panes = reshuffledPanes;

            } else {
                logger.error("Cannot insert {} before {}: No item with id {} found!", id, beforeId, beforeId);
            }
        }
    }

    private VerticalNavigation addPrimary(LinkedHashMap<String, Item> items, LinkedHashMap<String, Pane> panes,
            String id, String text, String iconClass, Pane pane) {

        HTMLAnchorElement a;
        HTMLElement span;
        HTMLElement primary = li().css(listGroupItem)
                .id(id)
                .add(a = a().css(clickable).get())
                .get();

        if (pane != null) {
            bind(a, click, event -> show(id));
        }
        if (iconClass != null) {
            a.appendChild(span().css(iconClass).get());
        }
        a.appendChild(span = span().css(listGroupItemValue).textContent(text).get());
        if (text.length() > PRIMARY_VISIBLE_TEXT_LENGTH) {
            span.title = text;
        }

        Item item = new Item(id, null, text, primary);
        items.put(id, item);
        if (pane != null) {
            panes.put(id, pane);
        }

        return this;
    }


    // ------------------------------------------------------ add secondary items

    /**
     * Adds a secondary navigation item to the navigation which controls the visibility of the specified element.
     * <p>
     * Unlike similar UI elements such as {@code Tabs} the element is <strong>not</strong> added as a child of this
     * navigation. The element should be rather a child of the root container.
     *
     * <p><strong>Please note</strong><br/>
     * This method does <strong>not</strong> add the item to the DOM. This has to be done manually using something
     * like</p>
     * <pre>
     * HTMLElement root = row()
     *     .add(column()
     *         .addAll(navigation.panes()))
     *     .get();
     * </pre>
     */
    public VerticalNavigation addSecondary(String primaryId, String id, String text, HTMLElement element) {
        return addSecondary(items, panes, primaryId, id, text, new Pane(id, element));
    }

    /**
     * Inserts a secondary navigation item <em>before</em> the specified item. If {@code beforeId} is {@code null},
     * the item is inserted as last item. If there's not item with id {@code beforeId}, an error message is logged
     * and no item is inserted.
     * <p>
     * You must call this method <em>after</em> at least one item was added and <em>before</em> the navigation is
     * {@linkplain #attach() attached}.
     *
     * <p><strong>Please note</strong><br/>
     * Unlike {@link #addSecondary(String, String, String, HTMLElement)}, this method <strong>does</strong> add the
     * item to the DOM.</p>
     */
    public void insertSecondary(String primaryId, String id, String beforeId, String text, HTMLElement element) {
        Item primaryItem = items.get(primaryId);
        if (primaryItem != null) {

            // The order of panes does not matter.
            Pane pane = new Pane(id, element);
            Pane lastPane = panes.values().iterator().next();
            lastPane.element().parentNode.appendChild(pane.element());

            // The order of items does matter
            if (beforeId == null) {
                // as last item
                addSecondary(items, panes, primaryId, id, text, pane);

            } else {
                // TODO insert instead of add!
                addSecondary(items, panes, primaryId, id, text, pane);
            }

        } else {
            logger.error("Unable to find primary navigation item for id '{}'", primaryId);
        }
    }

    private VerticalNavigation addSecondary(LinkedHashMap<String, Item> items, LinkedHashMap<String, Pane> panes,
            String primaryId, String id, String text, Pane pane) {
        Item primaryItem = items.get(primaryId);

        if (primaryItem != null) {
            HTMLElement secondaryUl = (HTMLElement) primaryItem.element()
                    .querySelector("." + navPfSecondaryNav + " > ul." + listGroup); //NON-NLS

            if (secondaryUl == null) {
                // seems to be the first secondary item -> setup the secondary containers
                String secondaryContainerId = Ids.build(primaryId, "secondary");
                primaryItem.element().classList.add(secondaryNavItemPf);
                primaryItem.element().dataset.set(UIConstants.TARGET, "#" + secondaryContainerId);

                HTMLElement span;
                HTMLElement div = div().css(navPfSecondaryNav, navPfSecondaryNavHal)
                        .id(secondaryContainerId)
                        .add(div().css(navItemPfHeader)
                                .add(a().css(secondaryCollapseTogglePf)
                                        .data(UIConstants.TOGGLE, "collapse-secondary-nav")) //NON-NLS
                                .add(span = span().textContent(primaryItem.text).get()))
                        .add(secondaryUl = ul().css(listGroup).get())
                        .get();

                if (text.length() > SECONDARY_VISIBLE_TEXT_LENGTH) {
                    span.title = text;
                }
                primaryItem.element().appendChild(div);
            }

            HTMLElement li = li().id(id).css(listGroupItem)
                    .add(a().css(clickable).on(click, event -> show(id))
                            .add(span().css(listGroupItemValue).textContent(text)))
                    .get();

            primaryItem.addChild(id);
            Item secondaryItem = new Item(id, primaryId, text, li);
            secondaryUl.appendChild(secondaryItem.element());
            items.put(id, secondaryItem);
            panes.put(id, pane);

        } else {
            logger.error("Unable to find primary navigation item for id '{}'", primaryId);
        }
        return this;
    }


    // ------------------------------------------------------ misc

    private void showInitial() {
        if (!items.isEmpty()) {
            String id;
            Map.Entry<String, Item> entry = items.entrySet().iterator().next();
            if (entry.getValue().hasChildren()) {
                id = entry.getValue().firstChild();
            } else {
                id = entry.getValue().id;
            }
            show(id);
        }
    }

    public void show(String id) {
        Item show = items.get(id);
        if (show != null) {
            if (show.parentId != null) {
                show(show.parentId);
            }
            for (Pane pane : panes.values()) {
                if (pane != null) {
                    Elements.setVisible(pane.element(), pane.id.equals(id));
                }
            }
            show.element().click();
            if (callbacks.containsKey(id)) {
                callbacks.get(id).execute();
            }

            // highlight active item(s)
            for (Item item : items.values()) {
                item.element().classList.remove(active);
            }
            show.element().classList.add(active);
            if (show.parentId != null) {
                Item showParent = items.get(show.parentId);
                if (showParent != null) {
                    showParent.element().classList.add(active);
                }
            }

        } else {
            logger.error("Unable to show item for id '{}': No such item!", id);
        }
    }

    /**
     * Controls the visibility of the specified item.
     */
    public void setVisible(String id, boolean visible) {
        Item item = items.get(id);
        Pane pane = panes.get(id);
        if (item != null && pane != null) {
            Elements.setVisible(item.element(), visible);
            if (!visible && Elements.isVisible(pane.element())) {
                Elements.setVisible(pane.element(), false);
            }
        } else {
            logger.error("Unable to hide item for id '{}': No such item!", id);
        }
    }

    public void onShow(String id, Callback callback) {
        callbacks.put(id, callback);
    }

    public void updateBadge(String id, int count) {
        Item item = items.get(id);
        if (item != null) {
            Element a = item.element().firstElementChild;
            HTMLElement badgeContainer = (HTMLElement) a.querySelector("." + badgeContainerPf);
            if (badgeContainer != null) {
                a.removeChild(badgeContainer);
            }
            badgeContainer = div().css(badgeContainerPf)
                    .add(span().css(badge).textContent(String.valueOf(count)))
                    .get();
            a.appendChild(badgeContainer);
        } else {
            logger.error("Unable to find navigation item for id '{}'", id);
        }
    }

    /**
     * Returns the elements which were registered using the {@code add()} methods. Use this method to add those
     * elements to another container.
     */
    public Iterable<HTMLElement> panes() {
        return panes.values().stream().map(Pane::element).collect(toList());
    }

    private boolean hasSecondary() {
        return items.values().stream().anyMatch(item -> !item.children.isEmpty());
    }


    @JsType(isNative = true)
    static class Api {

        @JsMethod(namespace = GLOBAL, name = "$")
        public static native Api select();

        public native void setupVerticalNavigation(boolean handleItemSelections);
    }


    private static class Item implements IsElement {

        private final String id;
        private final String parentId;
        private final String text;
        private final HTMLElement element;
        private final LinkedHashSet<String> children;

        private Item(String id, String parentId, String text, HTMLElement element) {
            this.id = id;
            this.parentId = parentId;
            this.text = text;
            this.element = element;
            this.children = new LinkedHashSet<>();
        }

        @Override
        public HTMLElement element() {
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

        private Pane(String id, HTMLElement element) {
            this.id = id;
            this.element = element;
            this.element.dataset.set("vnItemFor", id);
        }

        private Pane(String id, IsElement isElement) {
            this.id = id;
            this.element = isElement.element();
            this.element.dataset.set("vnItemFor", id);
        }

        @Override
        public HTMLElement element() {
            return element;
        }
    }
}
