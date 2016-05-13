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

    public static class Attribute {

        private final String name;
        private final List<String> suggestHandlerTemplates;

        Attribute(final String name) {
            this.name = name;
            this.suggestHandlerTemplates = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public List<String> getSuggestHandlerTemplates() {
            return suggestHandlerTemplates;
        }

        void addSuggestHandlerTemplate(String template) {
            suggestHandlerTemplates.add(template);
        }
    }


    private final String typeParameter;
    private final MetadataInfo metadata;
    private final String title;
    private final boolean autoSave;
    private final String nameResolver;
    private final List<Attribute> attributes;

    FormInfo(final String name, final String selector, final String typeParameter, final MetadataInfo metadata,
            final String title, final boolean autoSave, final String nameResolver) {
        super(name, selector);
        this.typeParameter = typeParameter;
        this.metadata = metadata;
        this.title = Handlebars.templateSafeValue(title); // title can be a simple value or an expression
        this.autoSave = autoSave;
        this.nameResolver = Handlebars.stripHandlebar(nameResolver); // name resolver has to be an expression
        this.attributes = new ArrayList<>();
    }

    public String getTypeParameter() {
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

    public String getNameResolver() {
        return nameResolver;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Attribute> getSuggestHandlerAttributes() {
        return attributes.stream().filter(attribute -> !attribute.getSuggestHandlerTemplates().isEmpty())
                .collect(toList());
    }

    void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }
}
