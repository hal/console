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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.dialog.Dialog.SimpleCallback;

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
    public static void showConfirmation(String title, SafeHtml question, SimpleCallback confirm) {
        showConfirmation(title, question, null, confirm);
    }

    /**
     * Creates and shows a confirmation dialog using the question and the provided element. When confirmed the specified
     * callback is executed.
     */
    public static void showConfirmation(String title, SafeHtml question, Element element, SimpleCallback confirm) {
        buildConfirmation(title, question, element, confirm).show();
    }

    /**
     * Creates and returns a confirmation dialog using the question and the provided element. When confirmed the
     * specified callback is executed.
     * <p>
     * Please note that the dialog is <strong>not</strong> shown by this method. You need to call {@link Dialog#show()}
     * on the returned dialog.
     */
    public static Dialog buildConfirmation(String title, SafeHtml question, Element element, SimpleCallback confirm) {
        Element content;
        if (element != null) {
            content = new Elements.Builder().div().p().innerHtml(question).end().add(element).end().build();
        } else {
            content = new Elements.Builder().p().innerHtml(question).end().build();
        }

        return new Dialog.Builder(title)
                .closeIcon(true)
                .closeOnEsc(true)
                .yesNo(confirm)
                .size(SMALL)
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
        Element element = new Elements.Builder()
                .div().css(centerBlock)
                .p().style("text-align: center").innerHtml(message).end()
                .end()
                .build();

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
        Element element = new Elements.Builder()
                .div().css(centerBlock)
                .p().style("text-align: center").innerHtml(message).end()
                .div().css(spinner, spinnerLg).end()
                .end()
                .build();

        return new BlockingDialog(new Dialog.Builder(title)
                .size(SMALL)
                .add(element));
    }
}
