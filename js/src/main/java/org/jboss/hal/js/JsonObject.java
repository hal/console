/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.js;

import com.google.gwt.core.client.JavaScriptObject;

/** Represents a Json object. */
public class JsonObject extends JsonValue {

    public static JsonObject create() {
        return JavaScriptObject.createObject().cast();
    }

    protected JsonObject() {
    }

    public final native JsonValue get(String key) /*-{
        var value = this[key];
        // box for DevMode, not ProdMode
        return @com.google.gwt.core.client.GWT::isScript()() || value == null ? value : Object(value);
    }-*/;

    public final JsonArray getArray(String key) {
        return (JsonArray) get(key);
    }

    public final boolean getBoolean(String key) {
        return ((JsonBoolean) get(key)).getBoolean();
    }

    public final double getNumber(String key) {
        return ((JsonNumber) get(key)).getNumber();
    }

    public final JsonObject getObject(String key) {
        return (JsonObject) get(key);
    }

    public final String getString(String key) {
        return ((JsonString) get(key)).getString();
    }

    public final native boolean hasKey(String key) /*-{
        return key in this;
    }-*/;

    public final native String[] keys() /*-{
        var keys = [];
        for (var key in this) {
            if (Object.prototype.hasOwnProperty.call(this, key) && key != '$H') {
                keys.push(key);
            }
        }
        return keys;
    }-*/;

    public final native void put(String key, JsonValue value) /*-{
        this[key] = value;
    }-*/;

    public final void put(String key, String value) {
        put(key, JsonString.create(value));
    }

    public final void put(String key, double value) {
        put(key, JsonNumber.create(value));
    }

    public final void put(String key, boolean value) {
        put(key, JsonBoolean.create(value));
    }

    public final native void remove(String key) /*-{
        delete this[key];
    }-*/;
}
