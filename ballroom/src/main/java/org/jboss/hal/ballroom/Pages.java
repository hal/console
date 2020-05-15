/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.resources.Ids;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.hal.resources.CSS.page;

/**
 * A structural element to manage a main and a number of nested page elements. The nested page elements provide a
 * {@linkplain Breadcrumb breadcrumb} to navigate back and forth.
 * <p>
 * Use this element when you need additional levels of navigation which cannot be provided by a {@linkplain
 * VerticalNavigation vertical navigation}.
 */
public class Pages implements IsElement {

    private final String mainId;
    private final HTMLElement mainPage;
    private final Breadcrumb breadcrumb;
    private final Map<String, Page> pages;
    private final HTMLElement root;

    /** Create a new instance with the main page id and element. */
    public Pages(String id, String mainId, IsElement element) {
        this(id, mainId, element.element());
    }

    /** Create a new instance with the main page id and element. */
    public Pages(String id, String mainId, HTMLElement element) {
        this.mainId = mainId;
        this.mainPage = element;
        this.pages = new HashMap<>();
        this.breadcrumb = new Breadcrumb();

        if (Strings.isNullOrEmpty(mainPage.id)) {
            mainPage.id = mainId;
        }
        breadcrumb.element().classList.add(page);
        breadcrumb.element().id = Ids.build(id, Ids.BREADCRUMB);

        root = div().id(id)
                .add(mainPage)
                .add(breadcrumb).element();
        showMain();
    }

    private void showMain() {
        Elements.setVisible(mainPage, true);
        Elements.setVisible(breadcrumb.element(), false);
        for (Page page : pages.values()) {
            Elements.setVisible(page.element(), false);
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
    public void addPage(String parentId, String id, Supplier<String> parentTitle, Supplier<String> title,
            IsElement element) {
        addPage(parentId, id, parentTitle, title, element.element());
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
    public void addPage(String parentId, String id, Supplier<String> parentTitle, Supplier<String> title,
            HTMLElement element) {
        Page page = new Page(parentId, parentTitle, id, title, element);
        Elements.setVisible(page.element(), false);

        pages.put(id, page);
        root.appendChild(page.element());
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
                Elements.setVisible(breadcrumb.element(), true);
                pages.forEach((pageId, page3) -> Elements.setVisible(page3.element(), id.equals(pageId)));
            }
        }
    }

    public String getCurrentId() {
        if (Elements.isVisible(mainPage)) {
            return mainId;
        } else {
            for (Map.Entry<String, Page> entry : pages.entrySet()) {
                if (Elements.isVisible(entry.getValue().element())) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }


    private static class Page implements IsElement {

        private final String parentId;
        private final Supplier<String> parentTitle;
        private final Supplier<String> title;
        private final HTMLElement element;

        private Page(String parentId, Supplier<String> parentTitle, String id, Supplier<String> title,
                HTMLElement element) {
            this.parentId = parentId;
            this.parentTitle = parentTitle;
            this.title = title;
            this.element = element;

            if (Strings.isNullOrEmpty(element.id)) {
                element.id = id;
            }
        }

        @Override
        public HTMLElement element() {
            return element;
        }
    }
}
