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
package org.jboss.hal.dmr;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Represents a DMR property.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@JsType
public class Property implements Cloneable {

    private final String name;
    private final ModelNode value;

    /**
     * Creates a new property
     *
     * @param name The name of the property.
     * @param value The value of the property.
     */
    @JsConstructor
    public Property(String name, ModelNode value) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        this.name = name;
        this.value = value.clone();
    }

    /**
     * @return the name of the property
     */
    @JsProperty
    public String getName() {
        return name;
    }

    /**
     * @return the value of the property
     */
    @JsProperty
    public ModelNode getValue() {
        return value;
    }

    @JsIgnore
    public Property clone() {
        return new Property(name, value.clone());
    }
}
