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
package org.jboss.hal.ballroom;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.form.SelectBoxBridge;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.bootstrapSwitch;
import static org.jboss.hal.resources.CSS.selectpicker;

@JsType(isNative = true)
public class PatternFly {

    /** Convenience method for {@code Colors.get()} */
    @JsOverlay
    public static Colors colors() {
        return Colors.get();
    }

    /** Same as {@code initComponents(false, null)} */
    @JsOverlay
    public static void initComponents() {
        init(null);
    }

    /** Same as {@code initComponents(false, parent)} */
    @JsOverlay
    public static void initComponents(final String parent) {
        init(parent);
    }

    @JsOverlay
    private static void init(String parent) {
        if (parent == null) {
            $("." + bootstrapSwitch).bootstrapSwitch();
            $("." + selectpicker).selectpicker(SelectBoxBridge.Defaults.get());
            Tooltip.select("[data-toggle=tooltip]").init(); //NON-NLS
        } else {
            $(parent + " ." + bootstrapSwitch).bootstrapSwitch();
            $(parent + " ." + selectpicker).selectpicker(SelectBoxBridge.Defaults.get());
            Tooltip.select(parent + " [data-toggle=tooltip]").init(); //NON-NLS
        }
    }

    @JsMethod(namespace = GLOBAL)
    public native static PatternFly $(@NonNls String selector);

    @JsMethod(namespace = GLOBAL)
    public native static void prettyPrint();

    public native void bootstrapSwitch();

    public native void selectpicker(SelectBoxBridge.Options options);
}
