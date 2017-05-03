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

/**
 * @author Harald Pehl
 */
@JsType(namespace = "hal.core")
public class Extension {

    public enum Kind {HEADER, FINDER_ITEM, FOOTER}

    @JsMethod
    public static Extension header(final String id, final String title, final JsCallback entryPoint) {
        return new Extension(id, title, Kind.HEADER, entryPoint);
    }

    @JsMethod
    public static Extension footer(final String id, final String title, final JsCallback entryPoint) {
        return new Extension(id, title, Kind.FOOTER, entryPoint);
    }

    final String id;
    final String title;
    final Kind kind;
    final JsCallback entryPoint;

    private Extension(final String id, final String title, final Kind kind,
            final JsCallback entryPoint) {
        this.id = id;
        this.kind = kind;
        this.title = title;
        this.entryPoint = entryPoint;
    }
}
