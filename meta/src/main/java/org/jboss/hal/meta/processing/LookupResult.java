/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.meta.processing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MissingMetadataException;

class LookupResult {

    /**
     * Bit mask for missing / present metadata. 0 means metadata missing, 1 means metadata present.
     * First bit stands for resource description second one for security context.
     */
    static final int NOTHING_PRESENT = 0b00;
    static final int RESOURCE_DESCRIPTION_PRESENT = 0b10;
    static final int SECURITY_CONTEXT_PRESENT = 0b01;
    static final int ALL_PRESENT = 0b11;

    private final Map<AddressTemplate, Integer> templates;

    LookupResult(Set<AddressTemplate> templates, boolean recursive) {
        this.templates = new HashMap<>();
        for (AddressTemplate template : templates) {
            this.templates.put(template, NOTHING_PRESENT);
        }
    }

    Set<AddressTemplate> templates() {
        return templates.keySet();
    }

    void markMetadataPresent(AddressTemplate template, int flag) {
        int combined = failFastGet(template) | flag;
        templates.put(template, combined);
    }

    int missingMetadata(AddressTemplate template) {
        return failFastGet(template);
    }

    boolean allPresent() {
        for (Integer flags : templates.values()) {
            if (flags != ALL_PRESENT) {
                return false;
            }
        }
        return true;
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
        builder.append("LookupResult(");
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
                default:
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
