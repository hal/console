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
package org.jboss.hal.ballroom.js;

import java.util.ArrayList;
import java.util.List;

import elemental.js.util.JsArrayOf;

/**
 * @author Harald Pehl
 */
public final class JsHelper {

    public static <T> List<T> asList(JsArrayOf<T> array) {
        List<T> list = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            list.add(array.get(i));
        }
        return list;
    }

    public static <T> JsArrayOf<T> asJsArray(List<T> list) {
        JsArrayOf<T> array = JsArrayOf.create();
        for (T t : list) {
            array.push(t);
        }
        return array;
    }

    public static native boolean supportsAdvancedUpload() /*-{
        var div = document.createElement('div');
        return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) &&
            'FormData' in window && 'FileReader' in window;
    }-*/;

    private JsHelper() {
    }
}
