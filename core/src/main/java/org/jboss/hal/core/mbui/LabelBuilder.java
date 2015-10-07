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
package org.jboss.hal.core.mbui;

import com.google.common.collect.ImmutableMap;
import org.jboss.hal.dmr.Property;

import java.util.Map;

/**
 * Generates a human readable label from an attribute description of a {@link org.jboss.hal.meta.description.ResourceDescription}.
 *
 * @author Harald Pehl
 */
public class LabelBuilder {

    private final ImmutableMap<String, String> SPECIALS = ImmutableMap.of("jndi", "JNDI", "sql", "SQL", "url", "URL");

    public String label(Property attributeDescription) {
        String label = attributeDescription.getName();
        label = replaceSpecial(label);
        label = label.replace('-', ' ');
        label = capitalize(label);
        return label;
    }

    private String capitalize(final String str) {
        final char[] buffer = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            final char ch = buffer[i];
            if (Character.isWhitespace(ch)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer[i] = Character.toUpperCase(ch);
                capitalizeNext = false;
            }
        }
        return new String(buffer);
    }

    private String replaceSpecial(final String label) {
        String replaced = label;
        for (Map.Entry<String, String> entry : SPECIALS.entrySet()) {
            replaced = replaced.replace(entry.getKey(), entry.getValue());
        }
        return replaced;
    }
}
