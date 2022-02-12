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

/** Represents a Json array. */
public class JsonArray extends JsonValue {

    public static JsonArray create() {
        return (JsonArray) JavaScriptObject.createArray();
    }

    protected JsonArray() {
    }

    public final native JsonValue get(int index) /*-{
        var value = this[index];
        // box for DevMode, not ProdMode
        return @com.google.gwt.core.client.GWT::isScript()() || value == null ? value : Object(value);
    }-*/;

    public final JsonArray getArray(int index) {
        return (JsonArray) get(index);
    }

    public final boolean getBoolean(int index) {
        return ((JsonBoolean) get(index)).getBoolean();
    }

    public final double getNumber(int index) {
        return ((JsonNumber) get(index)).getNumber();
    }

    public final JsonObject getObject(int index) {
        return (JsonObject) get(index);
    }

    public final String getString(int index) {
        return ((JsonString) get(index)).getString();
    }

    public final native int length() /*-{
        return this.length;
    }-*/;

    public final native void set(int index, JsonValue value) /*-{
        this[index] = value;
    }-*/;

    public final void set(int index, String string) {
        set(index, JsonString.create(string));
    }

    public final void set(int index, double number) {
        set(index, JsonNumber.create(number));
    }

    public final void set(int index, boolean bool) {
        set(index, JsonBoolean.create(bool));
    }
}
