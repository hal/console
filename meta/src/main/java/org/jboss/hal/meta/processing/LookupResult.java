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
package org.jboss.hal.meta.processing;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MissingMetadataException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Harald Pehl
 */
class LookupResult {

    /**
     * Bit mask for missing / present metadata. 0 means metadata missing, 1 means metadata present.
     * First bit stands for resource description second one for security context.
     */
    final static int NOTHING_PRESENT = 0b00;
    final static int RESOURCE_DESCRIPTION_PRESENT = 0b10;
    final static int SECURITY_CONTEXT_PRESENT = 0b01;
    final static int ALL_PRESENT = 0b11;

    private final String token;
    private final Map<AddressTemplate, Integer> templates;
    private final boolean recursive;

    LookupResult(final String token, final Set<AddressTemplate> templates, final boolean recursive) {
        this.token = token;
        this.templates = new HashMap<>();
        for (AddressTemplate template : templates) {
            this.templates.put(template, NOTHING_PRESENT);
        }
        this.recursive = recursive;
    }

    Set<AddressTemplate> templates() {
        return templates.keySet();
    }

    boolean recursive() {
        return recursive;
    }

    void markMetadataPresent(AddressTemplate template, int what) {
        int combined = failFastGet(template) | what;
        templates.put(template, combined);
    }

    int missingMetadata(AddressTemplate template) {
        return failFastGet(template);
    }

    private int failFastGet(AddressTemplate template) {
        if (!templates.containsKey(template)) {
            throw new MissingMetadataException("MetadataContext", template);
        }
        return templates.get(template);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LookupResult(").append(token).append(", ");
        for (Iterator<Map.Entry<AddressTemplate, Integer>> iterator = templates.entrySet().iterator();
                iterator.hasNext(); ) {
            Map.Entry<AddressTemplate, Integer> entry = iterator.next();
            builder.append(entry.getKey()).append(" -> ");
            switch (entry.getValue()) {
                case NOTHING_PRESENT:
                    builder.append("nothing present");
                    break;
                case RESOURCE_DESCRIPTION_PRESENT:
                    builder.append("resource description present");
                    break;
                case SECURITY_CONTEXT_PRESENT:
                    builder.append("security context present");
                    break;
                case ALL_PRESENT:
                    builder.append("all present");
                    break;
            }
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
