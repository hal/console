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
package org.jboss.hal.ballroom.tree;

import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * @author Harald Pehl
 */
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
public class Options<T> {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Themes {
        public String name;
        public boolean url;
        public boolean dots;
        public boolean icons;
        public boolean striped;
        public boolean responsive;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Core<T> {

        public boolean multiple;
        public boolean animation;
        public Themes themes;
        public DataFunction<T> data;
    }


    public JsArrayOf<String> plugins;
    public Core<T> core;
}
