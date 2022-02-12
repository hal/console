/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom;

import elemental2.dom.CSSProperties.PaddingUnionType;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.resources.CSS.external;
import static org.jboss.hal.resources.CSS.navbar;

public final class Skeleton {

    public static final int MARGIN_BIG = 20; // keep this in sync with the
    public static final int MARGIN_SMALL = 10; // margins in variables.less

    public static int applicationOffset() {
        return navigationHeight() + footerHeight();
    }

    public static int applicationHeight() {
        return (int) (window.innerHeight - navigationHeight() - footerHeight());
    }

    public static void externalMode() {
        document.documentElement.classList.add(external);
        document.body.style.padding = PaddingUnionType.of(0);
    }

    private static int navigationHeight() {
        int height = 0;
        HTMLElement element = (HTMLElement) document.querySelector("body > nav." + navbar); // NON-NLS
        if (element != null) {
            height = (int) element.offsetHeight;
        }
        return height;
    }

    private static int footerHeight() {
        int height = 0;
        HTMLElement element = (HTMLElement) document.querySelector("footer > nav." + navbar); // NON-NLS
        if (element != null) {
            height = (int) element.offsetHeight;
        }
        return height;
    }

    private Skeleton() {
    }
}
