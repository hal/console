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
package org.jboss.hal.meta.security;

import java.util.function.Consumer;
import java.util.function.Predicate;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.NodeList;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.resources.UIConstants;

import static org.jboss.hal.resources.CSS.hidden;
import static org.jboss.hal.resources.CSS.rbacHidden;
import static org.jboss.hal.resources.UIConstants.data;

/**
 * Helper class to process elements with constraints in their {@code data-constraint} attributes. Toggles the
 * element's visibility depending on the {@link AuthorisationDecision} result.
 *
 * @author Harald Pehl
 */
public class ElementGuard {

    /**
     * Predicate which returns only visible elements (elements which don't have the CSS class {@link
     * org.jboss.hal.resources.CSS#hidden}).
     * <p>
     * Use this filter to find elements which can be processed by other security related functions such as
     * {@link Toggle}.
     *
     * @author Harald Pehl
     */
    public static class Visible implements Predicate<Element> {

        @Override
        public boolean test(final Element element) {
            return element != null && !element.getClassList().contains(hidden);
        }
    }


    /**
     * Toggle the CSS class {@link org.jboss.hal.resources.CSS#rbacHidden} based on the element's constraints.
     *
     * @author Harald Pehl
     */
    public static class Toggle implements Consumer<Element> {

        private final AuthorisationDecision authorisationDecision;

        public Toggle(final AuthorisationDecision authorisationDecision) {
            this.authorisationDecision = authorisationDecision;
        }

        @Override
        public void accept(final Element element) {
            String data = String.valueOf(element.getDataset().at(UIConstants.CONSTRAINT));
            if (data != null) {
                Constraints constraints = Constraints.parse(data);
                Elements.toggle(element, rbacHidden, !authorisationDecision.isAllowed(constraints));
            }
        }
    }


    /**
     * Adds the {@link org.jboss.hal.resources.CSS#rbacHidden} CSS class if {@code condition == true}, removes it
     * otherwise.
     */
    public static void toggle(Element element, boolean condition) {
        if (new Visible().test(element)) {
            Elements.toggle(element, rbacHidden, condition);
        }
    }

    public static void processElements(AuthorisationDecision authorisationDecision, String selector) {
        processElements(authorisationDecision, Browser.getDocument().querySelectorAll(selector));
    }

    public static void processElements(AuthorisationDecision authorisationDecision, Element element) {
        processElements(authorisationDecision, element.querySelectorAll("[" + data(UIConstants.CONSTRAINT + "]")));
    }

    public static void processElements(AuthorisationDecision authorisationDecision, NodeList elements) {
        Elements.stream(elements)
                .filter(new Visible()) // prevent that hidden elements become visible by Toggle()
                .forEach(new Toggle(authorisationDecision));
    }
}
