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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.JsCallback;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Callback;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

/**
 * A modal dialog with optional secondary and primary buttons. Only one dialog can be open at a time. The buttons can
 * be placed on the left or the right side. Each button has a callback. The callback is either a {@link Callback}
 * which always closes the dialog or a {@link ResultCallback} with a boolean return value. A value of {@code true}
 * indicates that the dialog should be closed whereas {@code false} keeps the dialog open. You can add as many buttons
 * as you like, but only one of them should be the primary button.
 * <p>
 * There are convenience methods to add primary and secondary buttons which come with pre-defined placements. If
 * you want to define the placement by yourself use negative numbers to place the buttons on the left side and positive
 * numbers for the right side. On each side the buttons are ordered according to the placement.
 */
@JsType(namespace = "hal.ui")
public class Dialog implements IsElement {

    public enum Size {
        SMALL(modelSm), MEDIUM(modalMd), LARGE(modalLg), MAX(modalMx);

        final String css;

        Size(final String css) {
            this.css = css;
        }
    }


    /**
     * A button callback which returns a boolean to indicate whether the dialog should be closed or stay open.
     */
    @JsFunction
    @FunctionalInterface
    public interface ResultCallback {

        /**
         * @return {@code true} if the dialog should be closed and {@code false} if the dialog should stay open.
         */
        boolean eval();
    }


    private static class Button {

        final String label;
        final ResultCallback resultCallback;
        final Callback simpleCallback;
        final boolean primary;

        private Button(final String label, final ResultCallback callback, final Callback simpleCallback,
                boolean primary) {
            this.label = label;
            this.resultCallback = callback;
            this.simpleCallback = simpleCallback;
            this.primary = primary;
        }
    }


    // ------------------------------------------------------ dialog builder


    @JsType(namespace = "hal.ui", name = "DialogBuilder")
    public static class Builder {

        // mandatory attributes
        private final String title;
        private final List<HTMLElement> elements;
        private final SortedMap<Integer, Button> buttons;

        // optional attributes
        private Size size;
        private boolean closeIcon;
        private boolean closeOnEsc;
        private boolean fadeIn;
        private Callback closed;

        @JsIgnore
        public Builder(final String title) {
            this.title = title;
            this.elements = new ArrayList<>();
            this.buttons = new TreeMap<>();

            this.size = Size.MEDIUM;
            this.closeIcon = true;
            this.closeOnEsc = true;
            this.fadeIn = false;
        }

        /**
         * Shortcut for a dialog with one 'Close' button.
         */
        public Builder closeOnly() {
            buttons.clear();
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.close(), null, null, false));
            closeIcon = true;
            return this;
        }

        /**
         * Shortcut for a dialog with a 'Save' and 'Cancel' button. Clicking on save will execute the specified
         * callback.
         */
        public Builder saveCancel(ResultCallback saveCallback) {
            buttons.clear();
            buttons.put(PRIMARY_POSITION, new Button(CONSTANTS.save(), saveCallback, null, true));
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.close(), null, null, false));
            return this;
        }

        /**
         * Shortcut for a dialog with a 'Yes' and 'No' button. Clicking on yes will execute the specified
         * callback.
         */
        Builder yesNo(Callback yesCallback) {
            buttons.clear();
            buttons.put(PRIMARY_POSITION, new Button(CONSTANTS.yes(), null, yesCallback, true));
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.no(), null, null, false));
            return this;
        }

        /**
         * Shortcut for a dialog with a 'Ok' and 'Cancel' button. Clicking on yes will execute the specified
         * callback.
         */
        Builder okCancel(Callback okCallback) {
            buttons.clear();
            buttons.put(PRIMARY_POSITION, new Button(CONSTANTS.ok(), null, okCallback, true));
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.cancel(), null, null, false));
            return this;
        }

        /**
         * Adds a primary with label 'Save' and position {@value #PRIMARY_POSITION}.
         */
        @JsIgnore
        public Builder primary(ResultCallback callback) {
            return primary(CONSTANTS.save(), callback);
        }

        public Builder primary(String label, ResultCallback callback) {
            return primary(PRIMARY_POSITION, label, callback);
        }

        @JsIgnore
        public Builder primary(int position, String label, ResultCallback callback) {
            buttons.put(position, new Button(label, callback, null, true));
            return this;
        }

        public Builder cancel() {
            return secondary(CONSTANTS.cancel(), null);
        }

        /**
         * Adds a secondary button with label 'Cancel' and position {@value #SECONDARY_POSITION}
         */
        @JsIgnore
        public Builder secondary(ResultCallback callback) {
            return secondary(CONSTANTS.cancel(), callback);
        }

        public Builder secondary(String label, ResultCallback callback) {
            return secondary(SECONDARY_POSITION, label, callback);
        }

        @JsIgnore
        public Builder secondary(int position, String label, ResultCallback callback) {
            buttons.put(position, new Button(label, callback, null, false));
            return this;
        }

        @JsIgnore
        public Builder size(Size size) {
            this.size = size;
            return this;
        }

        @JsIgnore
        @SuppressWarnings("SameParameterValue")
        public Builder closeIcon(boolean closeIcon) {
            this.closeIcon = closeIcon;
            return this;
        }

        @JsIgnore
        @SuppressWarnings("SameParameterValue")
        public Builder closeOnEsc(boolean closeOnEsc) {
            this.closeOnEsc = closeOnEsc;
            return this;
        }

        @JsIgnore
        public Builder closed(Callback closed) {
            this.closed = closed;
            return this;
        }

        @JsIgnore
        public Builder fadeIn(boolean fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        @JsIgnore
        public Builder add(HTMLElement... elements) {
            if (elements != null) {
                this.elements.addAll(Arrays.asList(elements));
            }
            return this;
        }

        @JsIgnore
        public Builder add(Iterable<HTMLElement> elements) {
            if (elements != null) {
                //noinspection ResultOfMethodCallIgnored
                Iterables.addAll(this.elements, elements);
            }
            return this;
        }

        public Dialog build() {
            return new Dialog(this);
        }


        // ------------------------------------------------------ JS methods

        @JsMethod(name = "add")
        public Builder jsAdd(HTMLElement element) {
            return add(element);
        }

        @JsMethod(name = "okCancel")
        public Builder jsOkCancel(JsCallback okCallback) {
            return okCancel(okCallback::execute);
        }

        @JsMethod(name = "size")
        public Builder jsSize(String size) {
            return size(Size.valueOf(size));
        }

    }


    // ------------------------------------------------------ dialog singleton

    @JsIgnore public static final int PRIMARY_POSITION = 200;
    static final int SECONDARY_POSITION = 100;
    private static final String SELECTOR_ID = "#" + Ids.HAL_MODAL;
    private static final Constants CONSTANTS = GWT.create(Constants.class);

    private static final HTMLElement root;
    private static final HTMLElement dialog;
    private static final HTMLButtonElement closeIcon;
    private static final HTMLElement title;
    private static final HTMLElement body;
    private static final HTMLElement footer;

    private static boolean open;

    static {
        root = div()
                .id(Ids.HAL_MODAL).css(modal)
                .attr(ROLE, DIALOG)
                .attr(TABINDEX, "-1")
                .aria(LABELLED_BY, Ids.HAL_MODAL_TITLE)
                .add(dialog = div().css(modalDialog).attr(ROLE, "document") //NON-NLS
                        .add(div().css(modalContent)
                                .add(div().css(modalHeader)
                                        .add(closeIcon = button().css(close).aria(LABEL, CONSTANTS.close()).asElement())
                                        .add(title = h(4).css(modalTitle).id(Ids.HAL_MODAL_TITLE).asElement()))
                                .add(body = div().css(modalBody).asElement())
                                .add(footer = div().css(modalFooter).asElement()))
                        .asElement())
                .asElement();

        document.body.appendChild(root);
        initEventHandler();
    }

    @JsIgnore
    public static boolean isOpen() {
        return open;
    }

    private static void initEventHandler() {
        $(SELECTOR_ID).on(UIConstants.SHOWN_MODAL, () -> Dialog.open = true);
        $(SELECTOR_ID).on(UIConstants.HIDDEN_MODAL, () -> Dialog.open = false);
    }

    private static void reset() {
        root.classList.remove(fade);
        for (Size size : Size.values()) {
            dialog.classList.remove(size.css);
        }
        Elements.removeChildrenFrom(body);
        Elements.removeChildrenFrom(footer);
    }


    // ------------------------------------------------------ dialog instance

    private final boolean closeOnEsc;
    private final Callback closed;
    private final Map<Integer, HTMLButtonElement> buttons;
    private final List<Attachable> attachables;

    Dialog(final Builder builder) {
        reset();
        this.closeOnEsc = builder.closeOnEsc;
        this.closed = builder.closed;
        this.buttons = new HashMap<>();
        this.attachables = new ArrayList<>();

        if (builder.fadeIn) {
            Dialog.root.classList.add(fade);
        }
        Dialog.dialog.classList.add(builder.size.css);
        Elements.setVisible(Dialog.closeIcon, builder.closeIcon);
        bind(closeIcon, click, event -> close());
        setTitle(builder.title);
        for (HTMLElement element : builder.elements) {
            Dialog.body.appendChild(element);
        }

        if (!builder.buttons.isEmpty()) {
            for (Map.Entry<Integer, Button> entry : builder.buttons.entrySet()) {
                int position = entry.getKey();
                Button button = entry.getValue();
                String css = btn + " " + btnHal + " " + (button.primary ? btnPrimary : btnDefault);
                if (position < 0) {
                    css = css + " " + pullLeft;
                }

                HTMLButtonElement buttonElement = button(button.label)
                        .css(css)
                        .on(click, event -> {
                            if (button.resultCallback != null) {
                                if (button.resultCallback.eval()) {
                                    close();
                                }
                            } else if (button.simpleCallback != null) {
                                button.simpleCallback.execute();
                                close();
                            } else {
                                close();
                            }
                        })
                        .asElement();
                Dialog.footer.appendChild(buttonElement);
                buttons.put(position, buttonElement);
            }
        }
        Elements.setVisible(Dialog.footer, !buttons.isEmpty());
    }

    @Override
    @JsIgnore
    public HTMLElement asElement() {
        return root;
    }

    @JsIgnore
    public void registerAttachable(Attachable first, Attachable... rest) {
        attachables.add(first);
        if (rest != null) {
            Collections.addAll(attachables, rest);
        }
    }

    public void show() {
        if (Dialog.open) {
            throw new IllegalStateException(
                    "Another dialog is still open. Only one dialog can be open at a time. Please close the other dialog!");
        }
        $(SELECTOR_ID).modal(ModalOptions.create(closeOnEsc));
        $(SELECTOR_ID).modal("show");
        PatternFly.initComponents(SELECTOR_ID);
        attachables.forEach(Attachable::attach);
    }

    /**
     * Please call this method only if the dialog neither have a close icon, esc handler nor a close button.
     */
    void close() {
        attachables.forEach(Attachable::detach);
        $(SELECTOR_ID).modal("hide");
        if (closed != null) {
            closed.execute();
        }
    }


    // ------------------------------------------------------ properties

    @JsIgnore
    public void setTitle(String title) {
        Dialog.title.textContent = title;
    }

    @JsIgnore
    public HTMLButtonElement getButton(int position) {
        return buttons.get(position);
    }
}
