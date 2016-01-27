/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.config.rebind;

import java.util.List;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.ConfigurationProperty;
import com.google.gwt.core.ext.PropertyOracle;

/**
 * @author Harald Pehl
 */
final class GeneratorUtils {

    private GeneratorUtils() {
    }

    static String failFastGetProperty(PropertyOracle propertyOracle, String name) throws BadPropertyValueException {
        ConfigurationProperty property = propertyOracle.getConfigurationProperty(name);
        if (property != null) {
            List<String> values = property.getValues();
            if (values != null && !values.isEmpty()) {
                return values.get(0);
            } else {
                throw new BadPropertyValueException("Missing configuration property '" + name + "'!");
            }
        } else {
            throw new BadPropertyValueException("Missing configuration property '" + name + "'!");
        }
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
