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

/**
 * Represents a Json number value.
 */
public class JsonNumber extends JsonValue {

    public static JsonNumber create(double number) {
        return createProd(number);
    }

    /*
     * MAGIC: primitive number cast to object interface.
     */
    private static native JsonNumber createProd(double number) /*-{
        return Object(number);
    }-*/;

    protected JsonNumber() {
    }

    public final double getNumber() {
        return valueProd();
    }

    private native double valueProd() /*-{
        return this && this.valueOf();
    }-*/;
}
