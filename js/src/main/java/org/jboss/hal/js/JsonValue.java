/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.js;

import com.google.gwt.core.client.JavaScriptObject;

/** Base interface for all Json values. */
@SuppressWarnings("unused")
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
        return typeof @org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(obj);
    }-*/;

    static native boolean isArray(Object obj) /*-{
        // ensure that array detection works cross-frame
        return Object.prototype.toString.apply(@org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(obj)) === '[object Array]';
    }-*/;

    private static native boolean isNull(JsonValue jsJsonValue) /*-{
        // TODO(cromwellian): if this moves to GWT, we may have to support more leniency
        return jsJsonValue === null;
    }-*/;

    protected JsonValue() {
    }

    public final native boolean asBoolean() /*-{
        return @com.google.gwt.core.client.GWT::isScript()() || this == null ?
            !!@org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this) :
            (!!@org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this)).valueOf();
    }-*/;

    public final native double asNumber() /*-{
        if (this == null) {
            return 0;
        }
        return @com.google.gwt.core.client.GWT::isScript()() ?
            +@org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this) :
            (+@org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this)).valueOf();
    }-*/;

    // avoid casts, as compiler will throw CCE trying to cast a raw JS String to an interface
    public final native String asString() /*-{
        return this == null ? null :
            ("" + @org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this));
    }-*/;

    public final JsonType getType() {
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

    public final native boolean jsEquals(JsonValue value) /*-{
        return @org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this)
            === @org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(value);
    }-*/;

    public final native String toJson() /*-{
        // skip hashCode field
        return $wnd.JSON.stringify(this, function (keyName, value) {
            if (keyName == "$H") {
                return undefined; // skip hashCode property
            }
            return value;
        }, 0);
    }-*/;

    public final native Object toNative() /*-{
        return @org.jboss.hal.js.JsonValue::debox(Lorg/jboss/hal/js/JsonValue;)(this);
    }-*/;
}
