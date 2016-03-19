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
package org.jboss.hal.client.bootstrap.endpoint;

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.hal.resources.CSS.*;

class Feedback implements IsElement {

    private static final String ICON = "icon";
    private static final String MESSAGE = "message";

    private final Element root;
    private final Element icon;
    private final Element message;

    Feedback() {
        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().css(alert)
                .span().rememberAs(ICON).end()
                .span().rememberAs(MESSAGE).end()
            .end();
        // @formatter:on
        root = builder.build();
        icon = builder.referenceFor(ICON);
        message = builder.referenceFor(MESSAGE);
    }

    @Override
    public Element asElement() {
        return root;
    }

    void reset() {
        Elements.setVisible(root, false);
    }

    void ok(SafeHtml message) {
        root.getClassList().add(alertSuccess);
        root.getClassList().remove(alertDanger);
        icon.setClassName(pfIcon("ok"));
        this.message.setInnerHTML(message.asString());
        Elements.setVisible(root, true);
    }

    void error(SafeHtml message) {
        root.getClassList().add(alertDanger);
        root.getClassList().remove(alertSuccess);
        icon.setClassName(pfIcon(errorCircleO));
        this.message.setInnerHTML(message.asString());
        Elements.setVisible(root, true);
    }
}
