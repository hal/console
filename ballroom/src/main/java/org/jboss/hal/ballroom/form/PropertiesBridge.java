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
package org.jboss.hal.ballroom.form;

import com.google.gwt.core.client.GWT;
import elemental.dom.Element;
import elemental.js.dom.JsElement;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.resources.Constants;

import java.util.List;

import static java.util.Collections.emptyList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.ballroom.js.JsHelper.asList;
import static org.jboss.hal.resources.Names.OBJECT;

/**
 * @author Harald Pehl
 */
public class PropertiesBridge {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public String delimiter;
        public boolean forceLowercase;
        public String placeholder;
        public boolean sortable;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        private static final Options DEFAULT_OPTIONS = new Options();
        private static final Constants CONSTANTS = GWT.create(Constants.class);

        static {
            DEFAULT_OPTIONS.delimiter = ", ";
            DEFAULT_OPTIONS.forceLowercase = false;
            DEFAULT_OPTIONS.placeholder = CONSTANTS.tagEditorPlaceholder();
            DEFAULT_OPTIONS.sortable = false;
        }

        public static Options get() {
            return DEFAULT_OPTIONS;
        }
    }


    @JsFunction
    @FunctionalInterface
    public interface ChangeListener {
        void onChange(JsElement field, JsElement editor, JsArrayOf<String> tags);
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Tags {

        public JsElement field;
        public JsElement editor;
        public JsArrayOf<String> tags;
    }


    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge element(Element element);

        public native JsArrayOf<Tags> tagEditor(String method);

        public native void tagEditor(String method, ChangeListener param1);

        public native void tagEditor(String method, String param1, boolean param2);

        @JsOverlay
        public final List<String> getTags() {
            JsArrayOf<Tags> tags = tagEditor(GET_TAGS);
            if (tags.length() > 0 && tags.get(0).tags.length() > 0) {
                return asList(tags.get(0).tags);
            }
            return emptyList();
        }

        @JsOverlay
        public final void setTags(List<String> tags) {
            removeAll();
            for (String tag : tags) {
                addTag(tag);
            }
        }

        @JsOverlay
        public final void addTag(String tag) {
            tagEditor(ADD_TAG, tag, true);
        }

        @JsOverlay
        public final void removeTag(String tag) {
            tagEditor(REMOVE_TAG, tag, true);
        }

        @JsOverlay
        public final void removeAll() {
            List<String> tags = getTags();
            for (String tag : tags) {
                removeTag(tag);
            }
        }

        @JsOverlay
        public final void onChange(ChangeListener listener) {
            tagEditor(ON_CHANGE, listener);
        }
    }


    private static final String GET_TAGS = "getTags";
    private static final String ADD_TAG = "addTag";
    private static final String REMOVE_TAG = "removeTag";
    private static final String ON_CHANGE = "onChange";
}
