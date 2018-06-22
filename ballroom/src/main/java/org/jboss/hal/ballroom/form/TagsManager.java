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

import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import static java.util.Arrays.asList;
import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.CSS.tagManagerTag;
import static org.jboss.hal.resources.UIConstants.OBJECT;

/**
 * Java wrapper for <a href="https://github.com/max-favilli/tagmanager">Tags Manager</a>.
 *
 * @see <a href="https://github.com/max-favilli/tagmanager">https://github.com/max-favilli/tagmanager</a>
 */
public class TagsManager {

    private static final String EMPTY = "empty";
    private static final String PUSH_TAG = "pushTag";
    private static final String REFRESH_EVENT = "tm:refresh";
    private static final String INVALID_EVENT = "tm:invalid";
    private static final String DUPLICATED_EVENT = "tm:duplicated";
    private static final String TAGS = "tags";
    private static final String TAGS_MANAGER = "tagsManager";


    @JsFunction
    @FunctionalInterface
    public interface Validator {

        boolean validate(String tag);
    }


    @JsFunction
    @FunctionalInterface
    interface RefreshListener {

        /**
         * @param cst (c)omma (s)eparated (t)ags
         */
        void onRefresh(Event event, String cst);
    }

    @JsFunction
    @FunctionalInterface
    interface InvalidListener {

        /**
         * @param cst (c)omma (s)eparated (t)ags
         */
        void onInvalid(Event event, String cst);
    }

    @JsFunction
    @FunctionalInterface
    interface DuplicatedListener {

        /**
         * @param cst (c)omma (s)eparated (t)ags
         */
        void onDuplicated(Event event, String cst);
    }


    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        int[] delimiters;
        String tagsContainer;
        String tagClass;
        public Validator validator;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        public static Options get() {
            Options options = new Options();
            options.delimiters = new int[]{13}; // 13=enter
            options.tagClass = tagManagerTag;
            options.validator = null;
            return options;
        }
    }


    @JsType(isNative = true)
    public static class Api {

        @JsMethod(namespace = GLOBAL, name = "$")
        public static native Api element(HTMLInputElement element);

        public native void on(String event, RefreshListener refreshListener);
        public native void on(String event, InvalidListener invalidListener);
        public native void on(String event, DuplicatedListener duplicatedListener);

        @JsMethod(name = TAGS_MANAGER)
        public native String[] tagsManagerGetTags(String getTags);

        @JsMethod(name = TAGS_MANAGER)
        public native void tagsManagerRemoveTags(String removeTags);

        public native void tagsManager(String pushTag, String tag);

        public native void tagsManager(Options options);

        @JsOverlay
        final void onRefresh(RefreshListener refreshListener) {
            on(REFRESH_EVENT, refreshListener);
        }

        @JsOverlay
        final void onInvalid(InvalidListener invalidListener) {
            on(INVALID_EVENT, invalidListener);
        }

        @JsOverlay
        final void onDuplicated(DuplicatedListener duplicatedListener) {
            on(DUPLICATED_EVENT, duplicatedListener);
        }

        @JsOverlay
        final void addTag(String tag) {
            tagsManager(PUSH_TAG, tag);
        }

        @JsOverlay
        public final List<String> getTags() {
            return asList(tagsManagerGetTags(TAGS));
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
}
