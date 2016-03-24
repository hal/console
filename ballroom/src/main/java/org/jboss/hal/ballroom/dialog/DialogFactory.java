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
package org.jboss.hal.ballroom.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import elemental.client.Browser;
import elemental.html.ParagraphElement;
import org.jboss.hal.resources.Constants;

import static org.jboss.hal.ballroom.dialog.Dialog.Size.SMALL;

/**
 * @author Harald Pehl
 */
public final class DialogFactory {

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private DialogFactory() {}

    public static Dialog confirmation(String title, SafeHtml question, Dialog.Callback confirm) {
        ParagraphElement p = Browser.getDocument().createParagraphElement();
        p.setInnerHTML(question.asString());

        return new Dialog.Builder(title)
                .closeIcon(true)
                .closeOnEsc(true)
                .yesNo(confirm)
                .size(SMALL)
                .primary(CONSTANTS.yes(), confirm)
                .add(p)
                .build();
    }
}
