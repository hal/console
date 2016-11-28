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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.CSS;

/**
 * A structural element to manage a main and several nested page elements. The page elements have a {@linkplain
 * Breadcrumb breadcrumb} to navigate back to their parent element. Each call to {@link #addPage(String, String,
 * Element)} adds another nested page.
 * <p>
 * Use this element when you need an additional level of navigation which cannot be provided by a {@linkplain
 * VerticalNavigation vertical navigation}.
 * <p>
 * TODO Support nested pages
 *
 * @author Harald Pehl
 */
public class Pages implements IsElement {

    private final String title;
    private final Element main;
    private final Element root;
    private final Map<String, Breadcrumb> breadcrumbs;
    private final Map<String, Element> pages;

    public Pages(final String title, IsElement main) {
        this(title, main.asElement());
    }

    public Pages(final String title, final Element main) {
        this.title = title;
        this.main = main;
        this.root = Browser.getDocument().createDivElement();
        this.breadcrumbs = new HashMap<>();
        this.pages = new HashMap<>();

        root.appendChild(main);
        showMain();
    }

    public Pages addPage(String id, String title, IsElement page) {
        return addPage(id, title, page.asElement());
    }

    public Pages addPage(String id, String title, Element page) {
        Element pageContainer = Browser.getDocument().createDivElement();
        pageContainer.getClassList().add(CSS.page);
        Breadcrumb breadcrumb = new Breadcrumb().append(this.title, this::showMain).append(title);
        pageContainer.appendChild(breadcrumb.asElement());
        pageContainer.appendChild(page);

        root.appendChild(pageContainer);
        breadcrumbs.put(id, breadcrumb);
        pages.put(id, pageContainer);
        return this;
    }

    public void showMain() {
        Elements.setVisible(main, true);
        pages.values().forEach(page -> Elements.setVisible(page, false));
    }

    public void showPage(String id) {
        Elements.setVisible(main, false);
        pages.forEach((pageId, page) -> Elements.setVisible(page, id.equals(pageId)));
    }

    public String getCurrentPage() {
        Optional<String> first = pages.entrySet().stream()
                .filter(entry -> Elements.isVisible(entry.getValue()))
                .map(Entry::getKey)
                .findFirst();
        return first.orElse(null);
    }

    /**
     * Updates the breadcrumb on the specified page. The segments are updated from left to right.
     */
    public void updateBreadcrumb(String id, String firstSegment, String... restSegments) {
        Breadcrumb breadcrumb = breadcrumbs.get(id);
        if (breadcrumb != null) {
            breadcrumb.update(firstSegment, restSegments);
        }
    }

    @Override
    public Element asElement() {
        return root;
    }
}
