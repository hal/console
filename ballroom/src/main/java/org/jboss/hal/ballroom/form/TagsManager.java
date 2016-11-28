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
package org.jboss.hal.ballroom.form;

import java.util.List;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.js.events.JsEvent;
import elemental.js.html.JsInt16Array;
import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.jboss.hal.ballroom.JsHelper;

import static elemental.events.KeyboardEvent.KeyCode.ENTER;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.tagManagerTag;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * @author Harald Pehl
 */
class TagsManager {

    @JsFunction
    @FunctionalInterface
    interface RefreshListener {

        /**
         * @param cst (c)omma (s)eparated (t)ags
         */
        void onRefresh(JsEvent event, String cst);
    }


    @JsFunction
    @FunctionalInterface
    interface Validator {

        boolean validate(String tag);
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        JsInt16Array delimiters;
        String tagsContainer;
        String tagClass;
        public Validator validator;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        public static Options get() {
            Options options = new Options();
            options.delimiters = (JsInt16Array) Browser.getWindow().newInt16Array(1);
            options.delimiters.setAt(0, ENTER);
            options.tagClass = tagManagerTag;
            options.validator = null;
            return options;
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
        final void onRefresh(RefreshListener refreshListener) {
            on(REFRESH_EVENT, refreshListener);
        }

        @JsOverlay
        final void addTag(String tag) {
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
