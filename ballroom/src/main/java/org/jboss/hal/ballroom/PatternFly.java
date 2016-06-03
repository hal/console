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

import com.google.gwt.core.client.Scheduler;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.form.SelectBoxBridge;
import org.jboss.hal.resources.CSS;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;

@JsType(isNative = true)
public class PatternFly {

    /**
     * Same as {@code initComponents(false, null)}
     */
    @JsOverlay
    public static void initComponents() {
        initComponents(false, null);
    }

    /**
     * Same as {@code initComponents(false, parent)}
     */
    @JsOverlay
    public static void initComponents(final String parent) {
        initComponents(false, parent);
    }

    /**
     * Initializes JavaScript based PatternFly components.
     *
     * @param scheduled whether to run the initialization using {@code Scheduler.get().scheduleDeferred()}
     */
    @JsOverlay
    public static void initComponents(boolean scheduled) {
        initComponents(scheduled, null);
    }

    /**
     * Initializes JavaScript based PatternFly components below a given parent selector.
     *
     * @param parent a parent selector
     * @param scheduled whether to run the initialization using {@code Scheduler.get().scheduleDeferred()}
     */
    @JsOverlay
    public static void initComponents(boolean scheduled, final String parent) {
        if (scheduled) {
            Scheduler.get().scheduleDeferred(() -> init(parent));
        } else {
            init(parent);
        }
    }

    @JsOverlay
    private static void init(String parent) {
        if (parent == null) {
            $("." + CSS.bootstrapSwitch).bootstrapSwitch();
            $("." + CSS.selectpicker).selectpicker(SelectBoxBridge.Defaults.get());
            Tooltip.select("[data-toggle=tooltip]").init(); //NON-NLS
        } else {
            $(parent + " ." + CSS.bootstrapSwitch).bootstrapSwitch();
            $(parent + " ." + CSS.selectpicker).selectpicker(SelectBoxBridge.Defaults.get());
            Tooltip.select(parent + " [data-toggle=tooltip]").init(); //NON-NLS
        }
    }

    @JsMethod(namespace = GLOBAL)
    public native static PatternFly $(@NonNls String selector);

    public native void bootstrapSwitch();

    public native void selectpicker(SelectBoxBridge.Options options);
}
