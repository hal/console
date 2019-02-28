/*
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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
package org.jboss.hal.core.socketbinding;

import java.util.EnumSet;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.HandlerRegistration;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.form.Decoration;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.ReadOnlyAppearance;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.core.Core;
import org.jboss.hal.resources.CSS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.ballroom.form.Decoration.*;
import static org.jboss.hal.resources.CSS.clickable;

public class SocketBindingTextItem extends TextBoxItem {

    private SocketBindingReadOnlyAppearance appearance;
    static Logger _log = LoggerFactory.getLogger("org.jboss");

    public SocketBindingTextItem(String name) {
        this(name, new LabelBuilder().label(name), null);
    }

    public SocketBindingTextItem(String name, String label) {
        this(name, label, null);
    }

    public SocketBindingTextItem(String name, String label, String hint) {
        super(name, label, hint);

        // read-only appearance
        appearance = new SocketBindingReadOnlyAppearance();
        addAppearance(Form.State.READONLY, appearance);

    }

    @Override
    public void setValue(String value) {
        super.setValue(value);

        // use a context to pass the socket binding name
        if (value != null) {
            SocketBindingTextItem.ResolveSocketBindingContext ctx = new ResolveSocketBindingContext(value,
                    name1 -> {
                        _log.info("  AbstractFormItem.setValue and socket-binding: {}", name1);
                        ResolveSocketBindingEvent ev = new ResolveSocketBindingEvent(name1);
                        Core.INSTANCE.eventBus().fireEvent(ev);
                    });
            appearance.apply(SOCKET_BINDING, ctx);
        } else {
            appearance.unapply(SOCKET_BINDING);
        }
    }

    protected static class SocketBindingReadOnlyAppearance extends ReadOnlyAppearance<String> {

        private final HTMLElement socketBindingLink;
        private HandlerRegistration socketBindingHandler;

        SocketBindingReadOnlyAppearance() {
            super(EnumSet.of(DEFAULT, DEPRECATED, EXPRESSION, HINT, RESTRICTED, SENSITIVE, SOCKET_BINDING));
            socketBindingLink = span()
                    .css(CSS.fontAwesome("rss"), clickable)
                    .title(CONSTANTS.resolveSocketBinding())
                    .get();
        }

        @Override
        protected String name() {
            return "SocketBindingReadOnlyAppearance";
        }

        @Override
        public void showValue(String value) {
            super.showValue(value);
            Elements.setVisible(socketBindingLink, !Strings.isNullOrEmpty(value));
        }

        @Override
        public void clearValue() {
            super.clearValue();
            Elements.setVisible(socketBindingLink, false);
        }

        @Override
        protected <C> void safeApply(Decoration decoration, C context) {
            super.safeApply(decoration, context);
            if (SOCKET_BINDING == decoration) {
                ResolveSocketBindingContext sbc = (ResolveSocketBindingContext) context;
                socketBindingHandler = bind(socketBindingLink, click,
                        event -> sbc.callback.resolve(getValueIfMasked()));
                if (isApplied(HINT)) {
                    valueContainer.insertBefore(socketBindingLink, hintElement);
                } else {
                    valueContainer.appendChild(socketBindingLink);
                }
            }
        }

        @Override
        protected void safeUnapply(Decoration decoration) {
            super.safeUnapply(decoration);
            if (SOCKET_BINDING == decoration) {
                if (socketBindingHandler != null) {
                    socketBindingHandler.removeHandler();
                    socketBindingHandler = null;
                }
                Elements.failSafeRemove(valueContainer, socketBindingLink);
            }
        }
    }


    @FunctionalInterface
    interface ResolveSocketBindingCallback {

        void resolve(String name);
    }


    static class ResolveSocketBindingContext {

        final String name;
        final ResolveSocketBindingCallback callback;

        ResolveSocketBindingContext(String name, ResolveSocketBindingCallback callback) {
            this.name = name;
            this.callback = callback;
        }
    }


}
