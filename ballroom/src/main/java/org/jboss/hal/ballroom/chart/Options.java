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
package org.jboss.hal.ballroom.chart;

import elemental2.core.JsArray;
import jsinterop.annotations.JsType;
import jsinterop.base.Any;
import jsinterop.base.JsPropertyMap;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

@SuppressWarnings("SpellCheckingInspection")
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
class Options {

    Axis axis;
    Color color;
    String bindto;
    Data data;
    Donut donut;
    Legend legend;
    Size size;
    Tooltip tooltip;


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Axis {

        public boolean rotated;
        public X x;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Color {

        public JsArray<String> pattern;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Data {

        JsPropertyMap<Object> colors;
        JsArray<JsArray<Any>> columns;
        JsArray<JsArray<String>> groups;
        JsPropertyMap<Object> names;
        @NonNls public String type;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Donut {

        public Label label;
        public String title;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Label {

        public boolean show;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Legend {

        @NonNls public String position;
        public boolean show;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Size {

        public int height;
        public int width;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Tooltip {

        public TooltipContentsFn contents;
        public boolean show;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class X {

        public JsArray<String> categories;
        public String type;
    }
}
