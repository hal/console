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
package org.jboss.hal.core.extension;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.JsCallback;
import org.jboss.hal.spi.EsParam;
import org.jetbrains.annotations.NonNls;

/**
 * Represents an extension written in JavaScript.
 *
 * @author Harald Pehl
 */
@JsType(namespace = "hal.core")
public class Extension {

    public enum Point {
        HEADER("Header"), FINDER_ITEM("Finder Item"), FOOTER("Footer"), CUSTOM("Custom");

        private final String title;

        Point(@NonNls final String title) {
            this.title = title;
        }

        public String title() {
            return title;
        }
    }

    /**
     * Creates a header extension.
     *
     * @param name       A unique name of the extension.
     * @param title      The title of the menu item in the header.
     * @param entryPoint The entrypoint of the header extension.
     *
     * @return the extension which can be registered using the {@link ExtensionRegistry}.
     */
    @JsMethod
    public static Extension header(String name, String title, @EsParam("function()") JsCallback entryPoint) {
        return new Extension(name, title, Point.HEADER, entryPoint);
    }

    /**
     * Creates a footer extension.
     *
     * @param name       A unique name of the extension.
     * @param title      The title of the menu item in the footer.
     * @param entryPoint The entrypoint of the footer extension.
     *
     * @return the extension which can be registered using the {@link ExtensionRegistry}.
     */
    @JsMethod
    public static Extension footer(String name, String title, @EsParam("function()") JsCallback entryPoint) {
        return new Extension(name, title, Point.FOOTER, entryPoint);
    }

    final String name;
    final String title;
    final Point point;
    final JsCallback entryPoint;

    private Extension(final String name, final String title, final Point point, final JsCallback entryPoint) {
        this.name = name;
        this.point = point;
        this.title = title;
        this.entryPoint = entryPoint;
    }
}
