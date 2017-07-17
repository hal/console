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

import elemental2.core.Array;
import jsinterop.annotations.JsType;
import jsinterop.base.Any;
import jsinterop.base.JsPropertyMapOfAny;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.UIConstants.OBJECT;

@SuppressWarnings("SpellCheckingInspection")
@JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
class Options {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Size {

        public int width;
        public int height;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Data {

        Array<Array<Any>> columns;
        JsPropertyMapOfAny names;
        @NonNls public String type;
        JsPropertyMapOfAny colors;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Legend {

        public boolean show;
        @NonNls public String position;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Tooltip {

        public boolean show;
        public TooltipContentsFn contents;
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Label {

        public boolean show;

    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    static class Donut {

        public Label label;
        public String title;
    }


    String bindto;
    Size size;
    Data data;
    Donut donut;
    Legend legend;
    Tooltip tooltip;
}
