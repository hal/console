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

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.events.JsEvent;
import elemental.js.html.JsInt16Array;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.js.JsHelper;

import java.util.List;

import static elemental.events.KeyboardEvent.KeyCode.ENTER;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.tagManagerTag;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * @author Harald Pehl
 */
public class TagsManager {

    @JsFunction
    @FunctionalInterface
    public interface RefreshListener {

        /**
         * @param cst (c)omma (s)eparated (t)ags
         */
        void onRefresh(JsEvent event, String cst);
    }


    @JsFunction
    @FunctionalInterface
    public interface Validator {

        boolean validate(String tag);
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public JsInt16Array delimiters;
        public String tagsContainer;
        public String tagClass;
        public Validator validator;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        private static final Options DEFAULT_OPTIONS = new Options();

        static {
            DEFAULT_OPTIONS.delimiters = (JsInt16Array) Browser.getWindow().newInt16Array(1);
            DEFAULT_OPTIONS.delimiters.setAt(0, ENTER);
            DEFAULT_OPTIONS.tagClass = tagManagerTag;
        }

        public static Options get() {
            return DEFAULT_OPTIONS;
        }
    }


    @JsType(isNative = true)
    public static class Bridge {

        @JsMethod(namespace = GLOBAL, name = "$")
        public native static Bridge element(Element element);

        public native void on(String event, RefreshListener refreshListener);

        @JsMethod(name = TAGS_MANAGER)
        public native JsArrayOf<String> tagsManagerGetTags(String getTags);

        @JsMethod(name = TAGS_MANAGER)
        public native void tagsManagerRemoveTags(String removeTags);

        public native void tagsManager(String pushTag, String tag);

        public native void tagsManager(Options options);

        @JsOverlay
        public final void onRefresh(RefreshListener refreshListener) {
            on(REFRESH_EVENT, refreshListener);
        }

        @JsOverlay
        public final void addTag(String tag) {
            tagsManager(PUSH_TAG, tag);
        }

        @JsOverlay
        public final List<String> getTags() {
            return JsHelper.asList(tagsManagerGetTags(TAGS));
        }

        @JsOverlay
        public final void setTags(List<String> tags) {
            removeAll();
            for (String tag : tags) {
                addTag(tag);
            }
        }

        @JsOverlay
        public final void removeAll() {
            tagsManagerRemoveTags(EMPTY);
        }
    }


    private static final String EMPTY = "empty";
    private static final String PUSH_TAG = "pushTag";
    private static final String REFRESH_EVENT = "tm:refresh";
    private static final String TAGS = "tags";
    private static final String TAGS_MANAGER = "tagsManager";
}
