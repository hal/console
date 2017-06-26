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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.CSS.page;

/**
 * A structural element to manage a main and any number of nested page elements. The nested page elements provide a
 * {@linkplain Breadcrumb breadcrumb} to navigate back and forth.
 * <p>
 * Use this element when you need additional levels of navigation which cannot be provided by a {@linkplain
 * VerticalNavigation vertical navigation}.
 *
 * @author Harald Pehl
 */
public class Pages implements IsElement {

    private static class Page implements IsElement {

        private final String parentId;
        private final Supplier<String> parentTitle;
        private final Supplier<String> title;
        private final HTMLElement element;

        private Page(final String parentId, final Supplier<String> parentTitle, final Supplier<String> title,
                final HTMLElement element) {
            this.parentId = parentId;
            this.parentTitle = parentTitle;
            this.title = title;
            this.element = element;
        }

        @Override
        public HTMLElement asElement() {
            return element;
        }
    }


    private final String mainId;
    private final HTMLElement mainPage;
    private final Breadcrumb breadcrumb;
    private final Map<String, Page> pages;
    private final HTMLElement root;

    /**
     * Create a new instance with the main page id and element.
     */
    public Pages(String id, final IsElement element) {
        this(id, element.asElement());
    }

    /**
     * Create a new instance with the main page id and element.
     */
    public Pages(String id, final HTMLElement element) {
        mainId = id;
        mainPage = element;

        breadcrumb = new Breadcrumb();
        breadcrumb.asElement().classList.add(page);
        pages = new HashMap<>();
        root = div()
                .add(mainPage)
                .add(breadcrumb)
                .asElement();
        showMain();
    }

    private void showMain() {
        Elements.setVisible(mainPage, true);
        Elements.setVisible(breadcrumb.asElement(), false);
        for (Page page : pages.values()) {
            Elements.setVisible(page.asElement(), false);
        }
    }

    /**
     * Adds a nested page.
     *
     * @param parentId    the parent id
     * @param id          the id of the page being added
     * @param parentTitle the title of the parent or main page
     * @param title       the title of the page being added
     * @param element     the page element
     */
    public void addPage(final String parentId, final String id,
            final Supplier<String> parentTitle, final Supplier<String> title,
            final IsElement element) {
        addPage(parentId, id, parentTitle, title, element.asElement());
    }

    /**
     * Adds a nested page.
     *
     * @param parentId    the parent id
     * @param id          the id of the page being added
     * @param parentTitle the title of the parent or main page
     * @param title       the title of the page being added
     * @param element     the page element
     */
    public void addPage(final String parentId, final String id,
            final Supplier<String> parentTitle, final Supplier<String> title,
            final HTMLElement element) {
        Page page = new Page(parentId, parentTitle, title, element);
        Elements.setVisible(page.asElement(), false);

        pages.put(id, page);
        root.appendChild(page.asElement());
    }

    /**
     * Shows the specified main / nested page and updates the breadcrumb.
     *
     * @param id the page id
     */
    public void showPage(String id) {
        if (mainId.equals(id)) {
            showMain();

        } else {
            if (pages.containsKey(id)) {
                breadcrumb.clear();
                List<Page> bottomUp = new ArrayList<>();
                Page page1 = pages.get(id);
                do {
                    bottomUp.add(page1);
                    page1 = pages.get(page1.parentId);
                } while (page1 != null);

                List<Page> topDown = Lists.reverse(bottomUp);
                for (Iterator<Page> iterator = topDown.iterator(); iterator.hasNext(); ) {
                    Page page2 = iterator.next();
                    breadcrumb.append(page2.parentTitle.get(), () -> {
                        if (mainId.equals(page2.parentId)) {
                            showMain();
                        } else {
                            showPage(page2.parentId);
                        }
                    });
                    if (!iterator.hasNext()) {
                        breadcrumb.append(page2.title.get());
                    }
                }

                Elements.setVisible(mainPage, false);
                Elements.setVisible(breadcrumb.asElement(), true);
                pages.forEach((pageId, page3) -> Elements.setVisible(page3.asElement(), id.equals(pageId)));
            }
        }
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }
}
