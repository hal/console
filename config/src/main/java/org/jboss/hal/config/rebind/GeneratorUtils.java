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
package org.jboss.hal.config.rebind;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.PropertyOracle;

import java.util.List;

/**
 * @author Harald Pehl
 */
final class GeneratorUtils {

    private GeneratorUtils() {
    }

    static String failSafeGetProperty(PropertyOracle propertyOracle, String name, String defaultValue) {
        String value = null;
        try {
            ConfigurationProperty property = propertyOracle.getConfigurationProperty(name);
            if (property != null) {
                List<String> values = property.getValues();
                if (values != null && !values.isEmpty()) {
                    value = values.get(0);
                }
            }
        } catch (BadPropertyValueException e) {
            // ignore and return default value
        }
        return value == null ? defaultValue : value;
    }
}
