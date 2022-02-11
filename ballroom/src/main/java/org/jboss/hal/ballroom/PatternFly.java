/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom;

import org.jboss.hal.ballroom.form.SelectBoxBridge;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.JQuery.$;
import static org.jboss.hal.resources.CSS.bootstrapSwitch;
import static org.jboss.hal.resources.CSS.selectpicker;

@JsType(namespace = GLOBAL, name = "patternfly", isNative = true)
public class PatternFly {

    @JsProperty(name = "pfPaletteColors") public static Colors colors;

    @JsOverlay
    public static void initComponents() {
        init(null);
    }

    @JsOverlay
    public static void initComponents(String parent) {
        init(parent);
    }

    @JsOverlay
    private static void init(String parent) {
        if (parent == null) {
            $("." + bootstrapSwitch).bootstrapSwitch();
            $("." + selectpicker).selectpicker(SelectBoxBridge.Defaults.get());
            Tooltip.select("[data-toggle=tooltip]").init(); // NON-NLS
        } else {
            $(parent + " ." + bootstrapSwitch).bootstrapSwitch();
            $(parent + " ." + selectpicker).selectpicker(SelectBoxBridge.Defaults.get());
            Tooltip.select(parent + " [data-toggle=tooltip]").init(); // NON-NLS
        }
    }

    @JsMethod(namespace = "PR")
    public static native void prettyPrint();

    private PatternFly() {
    }
}
