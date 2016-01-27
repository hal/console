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

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;

@JsType(isNative = true)
abstract class Modal {

    @JsFunction
    @FunctionalInterface
    interface ModalHandler {

        void handle();
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class ModalOptions {

        public String backdrop;
        public boolean keyboard;

        @JsOverlay
        public static ModalOptions create(final boolean closeOnEsc) {
            ModalOptions options = new ModalOptions();
            options.backdrop = "static"; //NON-NLS
            options.keyboard = closeOnEsc;
            return options;
        }
    }


    @JsMethod(namespace = GLOBAL)
    native static Modal $(@NonNls String selector);

    native void modal(ModalOptions modalOptions);

    native void modal(@NonNls String action);

    native void on(@NonNls String event, ModalHandler handler);
}
