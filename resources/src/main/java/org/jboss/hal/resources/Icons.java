/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.resources;

import elemental2.dom.HTMLElement;

import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.hal.resources.CSS.*;

/** Collection of common icons */
public interface Icons {

    // ------------------------------------------------------ icon css classes

    String CHECK = fontAwesome("check");
    String DISABLED = fontAwesome("ban");
    String DISCONNECTED = fontAwesome("chain-broken");
    String ERROR = pfIcon(errorCircleO);
    String INFO = pfIcon(info);
    String LOCK = fontAwesome("lock");
    String NOT = fontAwesome("times");
    String OK = pfIcon(ok);
    String PAUSED = pfIcon(paused);
    String PENDING = pfIcon("pending");
    String STOPPED = fontAwesome(stopCircleO);
    String WARNING = pfIcon(warningTriangleO);
    String UNKNOWN = pfIcon("help");

    static String flag(boolean value) {
        return value ? CHECK : NOT;
    }


    // ------------------------------------------------------ icon elements

    static HTMLElement disabled() {
        return span().css(DISABLED).element();
    }

    static HTMLElement disconnected() {
        return span().css(DISCONNECTED).element();
    }

    static HTMLElement error() {
        return span().css(ERROR).element();
    }

    static HTMLElement info() {
        return span().css(INFO).element();
    }

    static HTMLElement lock() {
        return span().css(LOCK).element();
    }

    static HTMLElement ok() {
        return span().css(OK).element();
    }

    static HTMLElement paused() {
        return span().css(PAUSED).element();
    }

    static HTMLElement pending() {
        return span().css(PENDING).element();
    }

    static HTMLElement stopped() {
        return span().css(STOPPED).element();
    }

    static HTMLElement warning() {
        return span().css(WARNING).element();
    }

    static HTMLElement unknown() {
        return span().css(UNKNOWN).element();
    }

    static HTMLElement custom(String css) {
        return span().css(css).element();
    }
}
