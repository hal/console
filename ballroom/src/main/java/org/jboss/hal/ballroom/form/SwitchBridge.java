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
package org.jboss.hal.ballroom.form;

import elemental.dom.Element;
import elemental.js.events.JsEvent;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;

/**
 * @author Harald Pehl
 */
public class SwitchBridge {

    @JsFunction
    @FunctionalInterface
    public interface ChangeListener {

        void onChange(JsEvent event, boolean state);
    }


    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge element(Element element);

        public native boolean bootstrapSwitch(String method);

        public native void bootstrapSwitch(String method, boolean param);

        public native void on(@NonNls String event, ChangeListener listener);

        @JsOverlay
        public final boolean getValue() {
            return bootstrapSwitch(STATE);
        }

        @JsOverlay
        public final void setValue(boolean value) {
            bootstrapSwitch(STATE, value);
        }

        @JsOverlay
        public final void setEnable(boolean enabled) {
            bootstrapSwitch(DISABLED, !enabled);
        }

        @JsOverlay
        public final boolean isEnable() {
            return !bootstrapSwitch(DISABLED);
        }

        @JsOverlay
        public final void onChange(ChangeListener listener) {
            on(CHANGE_EVENT, listener);
        }
    }


    private static final String STATE = "state";
    private static final String DISABLED = "disabled";
    private static final String CHANGE_EVENT = "switchChange.bootstrapSwitch";
}
