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
package org.jboss.hal.core.ui;

import elemental.client.Browser;
import elemental.dom.Element;

import static org.jboss.hal.resources.CSS.navbar;

/**
 * @author Harald Pehl
 */
public final class Skeleton {

    public static final int MARGIN_BIG = 20; // keep this in sync with the
    public static final int MARGIN_SMALL = 10; // margins in variables.less

    public static int navigationHeight() {
        int navigation = 0;
        Element element = Browser.getDocument().querySelector("nav." + navbar); //NON-NLS
        if (element != null) {
            navigation = element.getOffsetHeight();
        }
        return navigation;
    }

    public static int footerHeight() {
        int footer = 0;
        Element element = Browser.getDocument().querySelector("footer > nav." + navbar); //NON-NLS
        if (element != null) {
            footer = element.getOffsetHeight();
        }
        return footer;
    }

    public static int applicationHeight() {
        return Browser.getWindow().getInnerHeight() - navigationHeight() - footerHeight();
    }

    private Skeleton() {}
}
