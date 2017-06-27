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

import java.util.HashMap;
import java.util.Map;

import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.HandlerRegistrations;
import elemental2.dom.DragEvent;
import elemental2.dom.HTMLElement;
import jsinterop.base.JsPropertyMap;
import jsinterop.base.JsPropertyMapOfAny;
import org.jboss.gwt.elemento.core.EventCallbackFn;

import static org.jboss.gwt.elemento.core.EventType.*;
import static org.jboss.hal.resources.CSS.ondrag;

/**
 * @author Harald Pehl
 */
public final class JsHelper {

    public static Map<String, Object> asMap(JsPropertyMapOfAny jsMap) {
        Map<String, Object> map = new HashMap<>();
        jsMap.forEach(key -> map.put(key, jsMap.get(key)));
        return map;
    }

    public static JsPropertyMapOfAny asJsMap(Map<String, Object> map) {
        JsPropertyMapOfAny jsMap = JsPropertyMap.of();
        map.forEach(jsMap::set);
        return jsMap;
    }

    public static native boolean supportsAdvancedUpload() /*-{
        var div = document.createElement('div');
        return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) &&
            'FormData' in window && 'FileReader' in window;
    }-*/;

    public static HandlerRegistration addDropHandler(HTMLElement element, EventCallbackFn<DragEvent> handler) {
        EventCallbackFn<DragEvent> noop = event -> {
            event.preventDefault();
            event.stopPropagation();
        };
        EventCallbackFn<DragEvent> addDragIndicator = event -> {
            noop.onEvent(event);
            element.classList.add(ondrag);
        };
        EventCallbackFn<DragEvent> removeDragIndicator = event -> {
            noop.onEvent(event);
            element.classList.remove(ondrag);
        };

        return HandlerRegistrations.compose(
                bind(element, drag, noop),
                bind(element, dragstart, noop),
                bind(element, dragenter, addDragIndicator),
                bind(element, dragover, addDragIndicator),
                bind(element, dragleave, removeDragIndicator),
                bind(element, dragend, removeDragIndicator),
                bind(element, drop, event -> {
                    noop.onEvent(event);
                    removeDragIndicator.onEvent(event);
                    handler.onEvent(event);
                }));
    }

    private JsHelper() {
    }
}
