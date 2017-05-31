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

import com.google.gwt.safehtml.shared.SafeHtml;
import elemental2.dom.HTMLElement;
import org.jboss.hal.spi.Callback;

import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.SMALL;
import static org.jboss.hal.resources.CSS.centerBlock;
import static org.jboss.hal.resources.CSS.spinner;
import static org.jboss.hal.resources.CSS.spinnerLg;

/**
 * @author Harald Pehl
 */
public final class DialogFactory {

    private DialogFactory() {}

    /**
     * Creates and shows a confirmation dialog using the provided question. When confirmed the specified callback is
     * executed.
     */
    public static void showConfirmation(String title, SafeHtml question, Callback confirm) {
        showConfirmation(title, question, null, confirm);
    }

    /**
     * Creates and shows a confirmation dialog using the question and the provided element. When confirmed the specified
     * callback is executed.
     */
    public static void showConfirmation(String title, SafeHtml question, HTMLElement element, Callback confirm) {
        buildConfirmation(title, question, element, confirm).show();
    }

    /**
     * Creates and returns a confirmation dialog using the question and the provided element. When confirmed the
     * specified callback is executed.
     * <p>
     * Please note that the dialog is <strong>not</strong> shown by this method. You need to call {@link Dialog#show()}
     * on the returned dialog.
     */
    public static Dialog buildConfirmation(String title, SafeHtml question, HTMLElement element, Callback confirm) {
        return buildConfirmation(title, question, element, SMALL, confirm);
    }

    public static Dialog buildConfirmation(String title, SafeHtml question, HTMLElement element, Dialog.Size size,
            Callback confirm) {
        HTMLElement content;
        if (element != null) {
            content = div()
                    .add(p().innerHtml(question))
                    .add(element)
                    .asElement();
        } else {
            content = p().innerHtml(question).asElement();
        }

        return new Dialog.Builder(title)
                .yesNo(confirm)
                .size(size)
                .add(content)
                .build();
    }

    /**
     * Creates and returns a blocking dialog which can only be closed programmatically.
     * <p>
     * Please note that the dialog is <strong>not</strong> shown by this method. You need to call {@link Dialog#show()}
     * on the returned dialog.
     */
    public static BlockingDialog buildBlocking(String title, SafeHtml message) {
        HTMLElement element = div().css(centerBlock)
                .add(p().style("text-align: center").innerHtml(message))
                .asElement();

        return new BlockingDialog(new Dialog.Builder(title)
                .size(SMALL)
                .add(element));
    }

    /**
     * Creates and returns a blocking dialog w/ a spinner which can only be closed programmatically.
     * <p>
     * Please note that the dialog is <strong>not</strong> shown by this method. You need to call {@link Dialog#show()}
     * on the returned dialog.
     */
    public static BlockingDialog buildLongRunning(String title, SafeHtml message) {
        HTMLElement element = div().css(centerBlock)
                .add(p().style("text-align: center").innerHtml(message))
                .add(div().css(spinner, spinnerLg))
                .asElement();

        return new BlockingDialog(new Dialog.Builder(title)
                .size(SMALL)
                .add(element));
    }
}
