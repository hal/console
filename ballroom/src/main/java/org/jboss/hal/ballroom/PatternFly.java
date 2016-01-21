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
package org.jboss.hal.ballroom;

import com.google.gwt.core.client.Scheduler;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.form.Selectpicker;
import org.jboss.hal.ballroom.tageditor.TagEditor;
import org.jboss.hal.ballroom.typeahead.Dataset;
import org.jboss.hal.ballroom.typeahead.Options;
import org.jboss.hal.resources.CSS;
import org.jetbrains.annotations.NonNls;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.properties;

@JsType(isNative = true)
public class PatternFly {

    /**
     * Same as {@code initComponents(false)}
     */
    @JsOverlay
    public static void initComponents() {
        initComponents(false);
    }

    /**
     * Initializes JavaScript based PatternFly components.
     *
     * @param scheduled whether to run the initialization using {@code Scheduler.get().scheduleDeferred()}
     */
    @JsOverlay
    public static void initComponents(boolean scheduled) {
        if (scheduled) {
            Scheduler.get().scheduleDeferred(PatternFly::init);
        } else {
            init();
        }
    }

    @JsOverlay
    private static void init() {
        $("[data-toggle=tooltip]").tooltip();
        $("." + CSS.bootstrapSwitch).bootstrapSwitch();
        $("." + properties).tagEditor(TagEditor.Defaults.get());
        $("." + CSS.selectpicker).selectpicker(Selectpicker.Defaults.get());
    }

    @JsMethod(namespace = GLOBAL)
    public native static PatternFly $(@NonNls String selector);

    public native void bootstrapSwitch();

    public native void selectpicker(Selectpicker.Options options);

    public native void tagEditor(TagEditor.Options options);

    public native void tooltip();

    public native void typeahead(Options options, Dataset dataset);
}
