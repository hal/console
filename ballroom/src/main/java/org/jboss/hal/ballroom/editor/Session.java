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
package org.jboss.hal.ballroom.editor;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class Session {

    public native void setValue(String value);

    public native void setMode(String mode);

    public native String getValue();

    public native int getLength();

    public native void on(String event, OnChange onChange);

    @JsFunction
    @FunctionalInterface
    public interface OnChange {

        void onChange(Object delta);
    }
}
