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
package org.jboss.hal.processor.mbui;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Harald Pehl
 */
public class FormInfo extends MbuiElementInfo {

    public static class Group {

        private final String id;
        private final String name;
        private final String title;
        private final List<Attribute> attributes;

        Group(final String id, final String name, final String title) {
            this.id = id;
            this.name = name;
            this.title = Handlebars.templateSafeValue(title);
            this.attributes = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        void addAttribute(Attribute attribute) {
            attributes.add(attribute);
        }

        public List<Attribute> getAttributes() {
            return attributes;
        }

        public boolean isHasAttributesWithProvider() {
            for (Attribute attribute : attributes) {
                if (attribute.getProvider() != null) {
                    return true;
                }
            }
            return false;
        }

        public boolean isHasUnboundAttributes() {
            for (Attribute attribute : attributes) {
                if (attribute.getFormItem() != null) {
                    return true;
                }
            }
            return false;
        }
    }


    private final TypeParameter typeParameter;
    private final MetadataInfo metadata;
    private final String title;
    private final boolean autoSave;
    private final String onSave;
    private final String nameResolver;
    private final boolean includeRuntime;
    private final boolean failSafe;
    private final List<Attribute> attributes;
    private final List<Group> groups;

    FormInfo(final String name, final String selector, final TypeParameter typeParameter,
            final MetadataInfo metadata, final String title, final boolean autoSave, final String onSave,
            final String nameResolver, final boolean includeRuntime, final boolean failSafe) {
        super(name, selector);
        this.typeParameter = typeParameter;
        this.metadata = metadata;
        this.title = Handlebars.templateSafeValue(title); // title can be a simple value or an expression
        this.autoSave = autoSave;
        this.onSave = Handlebars.stripHandlebar(onSave); // save handler has to be an expression
        this.nameResolver = Handlebars.stripHandlebar(nameResolver); // name resolver has to be an expression
        this.includeRuntime = includeRuntime;
        this.failSafe = failSafe;
        this.attributes = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    public TypeParameter getTypeParameter() {
        return typeParameter;
    }

    public MetadataInfo getMetadata() {
        return metadata;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public String getOnSave() {
        return onSave;
    }

    public String getNameResolver() {
        return nameResolver;
    }

    public boolean isIncludeRuntime() {
        return includeRuntime;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public boolean isHasAttributesWithProvider() {
        for (Attribute attribute : attributes) {
            if (attribute.getProvider() != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasUnboundAttributes() {
        for (Attribute attribute : attributes) {
            if (attribute.getFormItem() != null) {
                return true;
            }
        }
        return false;
    }

    public List<Attribute> getValidationHandlerAttributes() {
        if (groups.isEmpty()) {
            return attributes.stream()
                    .filter(attribute -> attribute.getValidationHandler() != null)
                    .collect(toList());
        } else {
            List<Attribute> attributes = new ArrayList<>();
            groups.forEach(group -> group.getAttributes().stream()
                    .filter(attribute -> attribute.getValidationHandler() != null)
                    .forEach(attributes::add));
            return attributes;
        }
    }

    public List<Attribute> getSuggestHandlerAttributes() {
        if (groups.isEmpty()) {
            return attributes.stream()
                    .filter(attribute ->
                            attribute.getSuggestHandler() != null || !attribute.getSuggestHandlerTemplates().isEmpty())
                    .collect(toList());
        } else {
            List<Attribute> attributes = new ArrayList<>();
            groups.forEach(group -> group.getAttributes().stream()
                    .filter(attribute ->
                            attribute.getSuggestHandler() != null || !attribute.getSuggestHandlerTemplates().isEmpty())
                    .forEach(attributes::add));
            return attributes;
        }
    }

    public List<Group> getGroups() {
        return groups;
    }

    void addGroup(Group group) {
        groups.add(group);
    }
}
