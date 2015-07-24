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
import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Id;
import org.jboss.hal.resources.HalConstants;

import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.LARGE;
import static org.jboss.hal.ballroom.dialog.Dialog.Size.SMALL;

/**
 * @author Harald Pehl
 */
public class Dialog implements IsElement {

    public enum Size {SMALL, MEDIUM, LARGE}


    @FunctionalInterface
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


    public static final class Builder {

        static final HalConstants CONSTANTS = GWT.create(HalConstants.class);

        // mandatory attributes
        private final String id;
        private final String title;
        private final Element body;

        // optional attributes
        private Button primaryButton;
        private Button secondaryButton;
        private Size size;
        private boolean closeIcon;
        private boolean fadeIn;

        public Builder(final String id, final String title, final Element body) {
            this.id = id;
            this.title = title;
            this.body = body;

            this.primaryButton = null;
            this.secondaryButton = null;
            this.size = Size.MEDIUM;
            this.closeIcon = true;
            this.fadeIn = true;
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

        public Builder fadeIn(boolean fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public Dialog build() {
            return new Dialog(this);
        }
    }


    private final Element root;
    private final String id;
    private final String title;
    private final Element body;

    private Button primaryButton;
    private Button secondaryButton;
    private Size size;
    private boolean closeIcon;
    private boolean fadeIn;

    public Dialog(final Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.body = builder.body;
        this.primaryButton = builder.primaryButton;
        this.secondaryButton = builder.secondaryButton;
        this.size = builder.size;
        this.closeIcon = builder.closeIcon;
        this.fadeIn = builder.fadeIn;

        this.root = init();
    }

    private Element init() {
        String labelId = Id.generate(id, "label");

        Elements.Builder footerBuilder = new Elements.Builder();
        if (secondaryButton != null) {
            footerBuilder.button()
                    .css("btn btn-default")
                    .on(click, event -> {
                        if (secondaryButton.callback.execute()) {
                            close();
                        }
                    })
                    .innerText(secondaryButton.label)
                    .end();
        }
        if (primaryButton != null) {
            footerBuilder.button()
                    .css("btn btn-primary")
                    .on(click, event -> {
                        if (primaryButton.callback.execute()) {
                            close();
                        }
                    })
                    .innerText(primaryButton.label)
                    .end();
        }
        Element footer = footerBuilder.build();

        // @formatter:off
        Elements.Builder builder = new Elements.Builder()
            .div().id(id).css("modal")
                    .attr("role", "dialog")
                    .attr("tabindex", "-1")
                    .aria("labelledby", labelId)
                .div().css("modal-dialog").attr("role", "document").rememberAs("dialog")
                    .div().css("modal-content")

                        // header
                        .div().css("modal-header")
                            .button().css("close")
                                    .on(click, event -> {
                                        if (secondaryButton == null) {
                                            close();
                                        } else {
                                            if (secondaryButton.callback.execute()) {
                                                close();
                                            }
                                        }
                                    })
                                    .aria("label", "Close")
                                    .rememberAs("closeIcon")
                                .span().css("pficon pficon-close").aria("hidden", "true").end()
                            .end()
                            .h(4).css("modal-title").id(labelId).innerText(title).end()
                        .end()

                        // body
                        .div().css("modal-body")
                            .start(body).end()
                        .end()

                        // footer
                        .div().css("modal-footer").rememberAs("footer")
                            .start(footer)
                        .end()
                    .end()
                .end()
            .end();
        // @formatter:on

        Element dialog = builder.build();
        if (fadeIn) {
            dialog.getClassList().add("fade");
        }
        if (!closeIcon) {
            Elements.setVisible(builder.referenceFor("closeIcon"), false);
        }
        if (size == SMALL) {
            builder.referenceFor("dialog").getClassList().add("modal-lg");
        } else if (size == LARGE) {
            builder.referenceFor("dialog").getClassList().add("modal-sm");
        }
        if (primaryButton == null && secondaryButton == null) {
            Elements.setVisible(builder.referenceFor("footer"), false);
        }
        return dialog;
    }

    @Override
    public Element asElement() {
        return root;
    }

    public void show() {

    }

    private void close() {

    }
}
