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

import com.google.common.collect.Iterables;
import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.resources.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.UIConstants.*;

/**
 * A modal dialog with optional secondary and primary buttons. Only one dialog can be open at a time. The buttons can
 * be placed on the left or the right side. Each button has a callback which either returns {@code true} to indicate
 * that the dialog should be closed or {@code false} if it should stay open. You can add as many buttons as you like,
 * but only one of them should be a primary button.
 * <p>
 * There are convenience methods to add primary and secondary buttons which come with pre-defined placements. If
 * you want to define the placement by yourself use negative numbers to place the buttons on the left side and positive
 * numbers for the right side. On each side the buttons are ordered according to the placement.
 *
 * @author Harald Pehl
 */
public class Dialog implements IsElement {

    public enum Size {
        SMALL(modelSmall), MEDIUM(modalMedium), LARGE(modalLarge), MAX(modalMax);

        final String css;

        Size(final String css) {
            this.css = css;
        }
    }


    @FunctionalInterface
    public interface Callback {

        /**
         * A button callback
         *
         * @return {@code true} if the dialog should be closed and {@code false} if the dialog should stay open.
         */
        boolean execute();
    }


    private static class Button {

        final String label;
        final Callback callback;
        final boolean primary;

        private Button(final String label, final Callback callback, final boolean primary) {
            this.primary = primary;
            this.label = label;
            this.callback = callback;
        }
    }


    // ------------------------------------------------------ dialog builder


    public static final class Builder {

        // mandatory attributes
        private final String title;
        private final List<Element> elements;
        private final SortedMap<Integer, Button> buttons;

        // optional attributes
        private Size size;
        private boolean closeIcon;
        private boolean closeOnEsc;
        private boolean fadeIn;

        public Builder(final String title) {
            this.title = title;
            this.elements = new ArrayList<>();
            this.buttons = new TreeMap<>();

            this.size = Size.MEDIUM;
            this.closeIcon = true;
            this.fadeIn = false;
        }

        /**
         * Shortcut for a dialog with one 'Close' button.
         */
        public Builder closeOnly() {
            buttons.clear();
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.close(), () -> true, false));
            closeIcon = true;
            return this;
        }

        /**
         * Shortcut for a dialog with a 'Cancel' and 'Save' button. Clicking on save will execute the specified
         * callback.
         */
        public Builder cancelSave(Callback saveCallback) {
            buttons.clear();
            buttons.put(PRIMARY_POSITION, new Button(CONSTANTS.save(), saveCallback, true));
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.close(), () -> true, false));
            return this;
        }

        /**
         * Shortcut for a dialog with a 'No' and 'Yes' button. Clicking on yes will execute the specified
         * callback.
         */
        public Builder noYes(Callback yesCallback) {
            buttons.clear();
            buttons.put(PRIMARY_POSITION, new Button(CONSTANTS.yes(), yesCallback, true));
            buttons.put(SECONDARY_POSITION, new Button(CONSTANTS.no(), () -> true, false));
            return this;
        }

        /**
         * Adds a primary with label 'Save' and position {@value #PRIMARY_POSITION}.
         */
        public Builder primary(Callback callback) {
            return primary(CONSTANTS.save(), callback);
        }

        public Builder primary(String label, Callback callback) {
            return primary(PRIMARY_POSITION, label, callback);
        }

        public Builder primary(int position, String label, Callback callback) {
            buttons.put(position, new Button(label, callback, true));
            return this;
        }

        /**
         * Adds a secondary button with label 'Cancel' and position {@value #SECONDARY_POSITION}
         */
        public Builder secondary(Callback callback) {
            return secondary(CONSTANTS.cancel(), callback);
        }

        public Builder secondary(String label, Callback callback) {
            return secondary(SECONDARY_POSITION, label, callback);
        }

        public Builder secondary(int position, String label, Callback callback) {
            buttons.put(position, new Button(label, callback, false));
            return this;
        }

        public Builder size(Size size) {
            this.size = size;
            return this;
        }

        public Builder closeIcon(boolean closeIcon) {
            this.closeIcon = closeIcon;
            return this;
        }

        public Builder closeOnEsc(boolean closeOnEsc) {
            this.closeOnEsc = closeOnEsc;
            return this;
        }

        public Builder fadeIn(boolean fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public Builder add(Element... elements) {
            if (elements != null) {
                this.elements.addAll(Arrays.asList(elements));
            }
            return this;
        }

        public Builder add(Iterable<Element> elements) {
            if (elements != null) {
                Iterables.addAll(this.elements, elements);
            }
            return this;
        }

        public Dialog build() {
            return new Dialog(this);
        }
    }


    // ------------------------------------------------------ dialog singleton

    public static final int SECONDARY_POSITION = 100;
    public static final int PRIMARY_POSITION = 200;

    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String BODY_ELEMENT = "body";
    private static final String CLOSE_ICON_ELEMENT = "closeIcon";
    private static final String DIALOG_ELEMENT = "dialog";
    private static final String FOOTER_ELEMENT = "footer";
    private static final String ID = "hal-modal";
    private static final String LABEL = "label";
    private static final String SELECTOR_ID = "#" + ID;
    private static final String TITLE_ELEMENT = "title";

    private static final Element root;
    private static final Element dialog;
    private static final Element closeIcon;
    private static final Element title;
    private static final Element body;
    private static final Element footer;

    private static boolean open;


    static {
        String labelId = IdBuilder.build(ID, LABEL);
        // @formatter:off
        Elements.Builder rootBuilder = new Elements.Builder()
            .div().id(ID).css(modal)
                    .attr(ROLE, DIALOG_ELEMENT)
                    .attr(TABINDEX, "-1")
                    .aria("labeledby", labelId)
                .div().css(modalDialog).attr("role", "document").rememberAs(DIALOG_ELEMENT) //NON-NLS
                    .div().css(modalContent)
                        .div().css(modalHeader)
                            .button().css(close).aria(LABEL, CONSTANTS.close()).rememberAs(CLOSE_ICON_ELEMENT)
                                .span().css(pfIcon("close")).aria(HIDDEN, String.valueOf(true)).end()
                            .end()
                            .h(4).css(modalTitle).id(labelId).rememberAs(TITLE_ELEMENT).end()
                        .end()
                        .div().css(modalBody).rememberAs(BODY_ELEMENT).end()
                        .div().css(modalFooter).rememberAs(FOOTER_ELEMENT).end()
                    .end()
                .end()
            .end();
        // @formatter:on

        root = rootBuilder.build();
        dialog = rootBuilder.referenceFor(DIALOG_ELEMENT);
        closeIcon = rootBuilder.referenceFor(CLOSE_ICON_ELEMENT);
        title = rootBuilder.referenceFor(TITLE_ELEMENT);
        body = rootBuilder.referenceFor(BODY_ELEMENT);
        footer = rootBuilder.referenceFor(FOOTER_ELEMENT);
        Browser.getDocument().getBody().appendChild(root);
        initEventHandler();
    }

    private static void initEventHandler() {
        $(SELECTOR_ID).on("shown.bs.modal", () -> Dialog.open = true);
        $(SELECTOR_ID).on("hidden.bs.modal", () -> Dialog.open = false);
    }

    private static void reset() {
        root.getClassList().remove(fade);
        for (Size size : Size.values()) {
            dialog.getClassList().remove(size.css);
        }
        Elements.removeChildrenFrom(body);
        Elements.removeChildrenFrom(footer);
    }


    // ------------------------------------------------------ dialog instance

    private final boolean closeOnEsc;
    private final Map<Integer, ButtonElement> buttons;
    private final List<Attachable> attachables;

    private Dialog(final Builder builder) {
        reset();
        this.closeOnEsc = builder.closeOnEsc;
        this.buttons = new HashMap<>();
        this.attachables = new ArrayList<>();

        if (builder.fadeIn) {
            Dialog.root.getClassList().add(fade);
        }
        Dialog.dialog.getClassList().add(builder.size.css);
        Elements.setVisible(Dialog.closeIcon, builder.closeIcon);
        closeIcon.setOnclick(event -> close());
        setTitle(builder.title);
        for (Element element : builder.elements) {
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

                ButtonElement buttonElement = new Elements.Builder()
                        .button()
                        .css(css)
                        .on(click, event -> {
                            if (button.callback.execute()) {
                                close();
                            }
                        })
                        .textContent(button.label)
                        .end().build();
                Dialog.footer.appendChild(buttonElement);
                buttons.put(position, buttonElement);
            }
        }
        Elements.setVisible(Dialog.footer, !buttons.isEmpty());
    }

    @Override
    public Element asElement() {
        return root;
    }

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
        PatternFly.initComponents();
        for (Attachable attachable : attachables) {
            attachable.attach();
        }
    }

    private void close() {
        $(SELECTOR_ID).modal("hide");
    }


    // ------------------------------------------------------ properties

    public void setTitle(String title) {
        Dialog.title.setInnerHTML(title);
    }

    public ButtonElement getButton(int position) {
        return buttons.get(position);
    }
}
