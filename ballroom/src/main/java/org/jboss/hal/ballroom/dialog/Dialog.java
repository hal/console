/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.ballroom.dialog;

import com.google.gwt.core.client.GWT;
import elemental.client.Browser;
import elemental.dom.Element;
import elemental.html.ButtonElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.IdBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Modal.ModalOptions;
import org.jboss.hal.resources.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.dialog.Modal.$;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Names.LABEL;
import static org.jboss.hal.resources.Names.ROLE;

/**
 * @author Harald Pehl
 */
public class Dialog implements IsElement {

    // ------------------------------------------------------ inner classes


    public enum Size {
        SMALL(modelSmall), MEDIUM(modalMedium), LARGE(modalLarge), MAX(modalMax);

        final String css;

        Size(final String css) {
            this.css = css;
        }
    }


    public interface Callback {

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

        static final Constants CONSTANTS = GWT.create(Constants.class);

        // mandatory attributes
        private final String title;
        private final List<Element> elements;

        // optional attributes
        private Button primaryButton;
        private Button secondaryButton;
        private Size size;
        private boolean closeIcon;
        private boolean closeOnEsc;
        private boolean fadeIn;

        public Builder(final String title) {
            this.title = title;
            this.elements = new ArrayList<>();

            this.primaryButton = null;
            this.secondaryButton = null;
            this.size = Size.MEDIUM;
            this.closeIcon = true;
            this.fadeIn = false;
        }

        /**
         * Shortcut for a dialog with one 'Close' button.
         */
        public Builder closeOnly() {
            primaryButton = null;
            secondaryButton = new Button(CONSTANTS.close(), () -> true, false);
            closeIcon = true;
            return this;
        }

        /**
         * Shortcut for a dialog with a 'Cancel' and 'Save' button. Clicking on save will execute the specified
         * callback.
         */
        public Builder cancelSave(Callback saveCallback) {
            primaryButton = new Button(CONSTANTS.save(), saveCallback, true);
            secondaryButton = new Button(CONSTANTS.close(), () -> true, false);
            return this;
        }

        public Builder primary(Callback callback) {
            return primary(CONSTANTS.save(), callback);
        }

        public Builder primary(String label, Callback callback) {
            primaryButton = new Button(label, callback, true);
            return this;
        }

        public Builder secondary(Callback callback) {
            return secondary(CONSTANTS.cancel(), callback);
        }

        public Builder secondary(String label, Callback callback) {
            secondaryButton = new Button(label, callback, false);
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

        public Dialog build() {
            return new Dialog(this);
        }
    }


    // ------------------------------------------------------ dialog singleton

    private final static String ID = "hal-modal";
    private final static String SELECTOR_ID = "#" + ID;
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
                    .attr(ROLE, "dialog") //NON-NLS
                    .attr("tabindex", "-1")
                    .aria("labelledby", labelId)
                .div().css(modalDialog).attr("role", "document").rememberAs("dialog") //NON-NLS
                    .div().css(modalContent)
                        .div().css(modalHeader)
                            .button().css(close).aria(LABEL, "Close").rememberAs("closeIcon") //NON-NLS
                                .span().css(pfIcon("close")).aria("hidden", "true").end() //NON-NLS
                            .end()
                            .h(4).css(modalTitle).id(labelId).rememberAs("title").end()
                        .end()
                        .div().css(modalBody).rememberAs("body").end()
                        .div().css(modalFooter).rememberAs("footer").end()
                    .end()
                .end()
            .end();
        // @formatter:on

        root = rootBuilder.build();
        dialog = rootBuilder.referenceFor("dialog");
        closeIcon = rootBuilder.referenceFor("closeIcon");
        title = rootBuilder.referenceFor("title");
        body = rootBuilder.referenceFor("body");
        footer = rootBuilder.referenceFor("footer");
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
    private ButtonElement primaryButton;
    private ButtonElement secondaryButton;

    private Dialog(final Builder builder) {
        reset();
        this.closeOnEsc = builder.closeOnEsc;

        if (builder.fadeIn) {
            Dialog.root.getClassList().add(fade);
        }
        Dialog.dialog.getClassList().add(builder.size.css);
        Elements.setVisible(Dialog.closeIcon, builder.closeIcon);
        closeIcon.setOnclick(event -> {
            if (builder.secondaryButton == null) {
                close();
            } else {
                if (builder.secondaryButton.callback.execute()) {
                    close();
                }
            }
        });
        setTitle(builder.title);
        for (Element element : builder.elements) {
            Dialog.body.appendChild(element);
        }

        boolean buttons = builder.primaryButton != null || builder.secondaryButton != null;
        if (buttons) {
            if (builder.secondaryButton != null) {
                secondaryButton = new Elements.Builder()
                        .button()
                        .css(btn, btnHal, btnDefault)
                        .on(click, event -> {
                            if (builder.secondaryButton.callback.execute()) {
                                close();
                            }
                        })
                        .innerText(builder.secondaryButton.label)
                        .end().build();
                Dialog.footer.appendChild(secondaryButton);
            }
            if (builder.primaryButton != null) {
                primaryButton = new Elements.Builder()
                        .button()
                        .css(btn, btnHal, btnPrimary)
                        .on(click, event -> {
                            if (builder.primaryButton.callback.execute()) {
                                close();
                            }
                        })
                        .innerText(builder.primaryButton.label)
                        .end().build();
                Dialog.footer.appendChild(primaryButton);
            }
        }
        Elements.setVisible(Dialog.footer, buttons);
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void show() {
        if (Dialog.open) {
            throw new IllegalStateException(
                    "Another dialog is still open. Only one dialog can be open at a time. Please close the other dialog!");
        }
        $(SELECTOR_ID).modal(ModalOptions.create(closeOnEsc));
        $(SELECTOR_ID).modal("show");
        PatternFly.initComponents(false);
    }

    private void close() {
        $(SELECTOR_ID).modal("hide");
    }


    // ------------------------------------------------------ properties

    public void setTitle(String title) {
        Dialog.title.setInnerHTML(title);
    }

    public void setPrimaryButtonLabel(String label) {
        if (primaryButton != null) {
            primaryButton.setInnerHTML(label);
        }
    }

    public void setPrimaryButtonDisabled(boolean disabled) {
        if (primaryButton != null) {
            primaryButton.setDisabled(disabled);
        }
    }

    public void setSecondaryButtonLabel(String label) {
        if (secondaryButton != null) {
            secondaryButton.setInnerHTML(label);
        }
    }

    public void setSecondaryButtonDisabled(boolean disabled) {
        if (secondaryButton != null) {
            secondaryButton.setDisabled(disabled);
        }
    }
}
