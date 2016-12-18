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
package org.jboss.hal.ballroom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import org.jboss.hal.dmr.Property;

/**
 * Generates human readable labels from terms used in the management model.
 *
 * @author Harald Pehl
 */
public class LabelBuilder {

    @SuppressWarnings("HardCodedStringLiteral")
    private final ImmutableMap<String, String> SPECIALS = ImmutableMap.<String, String>builder()
            .put("ee", "EE")
            .put("ejb3", "EJB3")
            .put("http", "HTTP")
            .put("http2", "HTTP/2")
            .put("jaxrs", "JAX-RS")
            .put("jca", "JCA")
            .put("jdr", "JDA")
            .put("jsr", "JSR")
            .put("jmx", "JMX")
            .put("jndi", "JNDI")
            .put("jpa", "JPA")
            .put("jsf", "JSF")
            .put("sar", "SAR")
            .put("sql", "SQL")
            .put("ssl", "SSL")
            .put("tcp", "TCP")
            .put("uri", "URI")
            .put("url", "URL")
            .put("wsdl", "WSDL")
            .build();

    public String label(Property property) {
        return label(property.getName());
    }

    public String label(String name) {
        String label = name;
        label = label.replace('-', ' ');
        label = replaceSpecial(label);
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
        List<String> replacedParts = new ArrayList<>();
        for (String part : Splitter.on(' ').split(label)) {
            String replaced = part;
            for (Map.Entry<String, String> entry : SPECIALS.entrySet()) {
                if (replaced.length() == entry.getKey().length()) {
                    replaced = replaced.replace(entry.getKey(), entry.getValue());
                }
            }
            replacedParts.add(replaced);
        }
        return Joiner.on(" ").join(replacedParts);
    }
}
