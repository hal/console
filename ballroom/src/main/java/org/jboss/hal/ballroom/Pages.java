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
import elemental.client.Browser;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.hal.resources.CSS.page;

/**
 * A structural element to manage a main and several nested page elements. The nested page elements provide a
 * {@linkplain Breadcrumb breadcrumb} to navigate back and forth. The breadcrumb segments can contain dynamic parts
 * (e.g. the name of a selected resource).
 * <p>
 * Use this element when you need additional levels of navigation which cannot be provided by a {@linkplain
 * VerticalNavigation vertical navigation}.
 *
 * @author Harald Pehl
 */
public class Pages implements IsElement {

    private static class Page implements IsElement {

        private final String id;
        private final String parentId;
        private final Supplier<String> title;
        private final Element element;

        private Page(final String parentId, final String id, final Supplier<String> title, final Element element) {
            this.parentId = parentId;
            this.id = id;
            this.title = title;
            this.element = element;
        }

        @Override
        public Element asElement() {
            return element;
        }
    }


    private final Element root;
    private final Breadcrumb breadcrumb;
    private final Map<String, Page> pages;

    public Pages(String id, final String title, final IsElement element) {
        this(id, () -> title, element.asElement());
    }

    public Pages(String id, final Supplier<String> title, final IsElement element) {
        this(id, title, element.asElement());
    }

    public Pages(String id, final String title, final Element element) {
        this(id, () -> title, element);
    }

    public Pages(String id, final Supplier<String> title, final Element element) {
        breadcrumb = new Breadcrumb();
        breadcrumb.asElement().getClassList().add(page);
        pages = new HashMap<>();

        Page main = new Page(null, id, title, element);
        pages.put(id, main);
        root = Browser.getDocument().createDivElement();
        root.appendChild(main.asElement());
        root.appendChild(breadcrumb.asElement());
        showPage(id);
    }


    public void addPage(final String parentId, final String id, final String title, final IsElement element) {
        addPage(parentId, id, () -> title, element.asElement());
    }

    public void addPage(final String parentId, final String id, final Supplier<String> title, final IsElement element) {
        addPage(parentId, id, title, element.asElement());
    }

    public void addPage(final String parentId, final String id, final String title, final Element element) {
        addPage(parentId, id, () -> title, element);
    }

    public void addPage(final String parentId, final String id, final Supplier<String> title, final Element element) {
        Page page = new Page(parentId, id, title, element);
        Elements.setVisible(page.asElement(), false);
        pages.put(id, page);

        root.appendChild(page.asElement());
    }

    public void showPage(String id) {
        if (pages.containsKey(id)) {
            List<Page> bottomUp = new ArrayList<>();
            for (Page page = pages.get(id); page != null; page = pages.get(page.parentId)) {
                bottomUp.add(page);
            }

            if (bottomUp.size() == 1) {
                // show main page
                Page main = bottomUp.get(0);
                Elements.setVisible(main.asElement(), true);
                Elements.setVisible(breadcrumb.asElement(), false);
                pages.forEach((pageId, page) -> Elements.setVisible(page.asElement(), id.equals(pageId)));

            } else if (bottomUp.size() > 1) {
                // show nested page
                breadcrumb.clear();
                List<Page> topDown = Lists.reverse(bottomUp);
                Page main = topDown.get(0);
                for (Iterator<Page> iterator = topDown.iterator(); iterator.hasNext(); ) {
                    Page page = iterator.next();
                    boolean lastPage = !iterator.hasNext();
                    if (lastPage) {
                        breadcrumb.append(page.title.get());
                    } else {
                        breadcrumb.append(page.title.get(), () -> showPage(page.id));
                    }
                }

                Elements.setVisible(main.asElement(), false);
                Elements.setVisible(breadcrumb.asElement(), true);
                pages.forEach((pageId, p) -> Elements.setVisible(p.asElement(), id.equals(pageId)));
            }
        }
    }

    @Override
    public Element asElement() {
        return root;
    }
}
