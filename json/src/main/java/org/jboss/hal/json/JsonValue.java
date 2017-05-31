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
package org.jboss.hal.json;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Base interface for all Json values.
 */
public class JsonValue extends JavaScriptObject {

    static native JsonValue box(JsonValue value) /*-{
        // box for DevMode, not ProdMode
        return @com.google.gwt.core.client.GWT::isScript()() || value == null ? value : Object(value);
    }-*/;

    static native JsonValue debox(JsonValue value) /*-{
        // we don't debox (currently), because ProdMode is now unboxed, and DevMode should stay boxed
        return value;
    }-*/;

    static native String getJsType(Object obj) /*-{
        return typeof @org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(obj);
    }-*/;

    static native boolean isArray(Object obj) /*-{
        // ensure that array detection works cross-frame
        return Object.prototype.toString.apply(@org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(obj)) === '[object Array]';
    }-*/;

    private static native boolean isNull(JsonValue jsJsonValue) /*-{
        // TODO(cromwellian): if this moves to GWT, we may have to support more leniency
        return jsJsonValue === null;
    }-*/;

    protected JsonValue() {
    }

    final public native boolean asBoolean() /*-{
        return @com.google.gwt.core.client.GWT::isScript()() || this == null ?
            !!@org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this) :
            (!!@org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this)).valueOf();
    }-*/;

    final public native double asNumber() /*-{
        if (this == null) {
            return 0;
        }
        return @com.google.gwt.core.client.GWT::isScript()() ?
            +@org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this) :
            (+@org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this)).valueOf();
    }-*/;

    // avoid casts, as compiler will throw CCE trying to cast a raw JS String to an interface
    final public native String asString() /*-{
        return this == null ? null :
            ("" + @org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this));
    }-*/;

    final public JsonType getType() {
        if (isNull(this)) {
            return JsonType.NULL;
        }
        String jsType = getJsType(this);
        if ("string".equals(jsType)) {
            return JsonType.STRING;
        } else if ("number".equals(jsType)) {
            return JsonType.NUMBER;
        } else if ("boolean".equals(jsType)) {
            return JsonType.BOOLEAN;
        } else if ("object".equals(jsType)) {
            return isArray(this) ? JsonType.ARRAY : JsonType.OBJECT;
        }
        assert false : "Unknown Json Type";
        return null;
    }

    final public native boolean jsEquals(JsonValue value) /*-{
        return @org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this)
            === @org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(value);
    }-*/;

    final public native String toJson() /*-{
        // skip hashCode field
        return $wnd.JSON.stringify(this, function (keyName, value) {
            if (keyName == "$H") {
                return undefined; // skip hashCode property
            }
            return value;
        }, 0);
    }-*/;

    final public native Object toNative() /*-{
        return @org.jboss.hal.json.JsonValue::debox(Lorg/jboss/hal/json/JsonValue;)(this);
    }-*/;
}
