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

/**
 * @author Harald Pehl
 */
public class DataTableInfo extends MbuiElementInfo {

    private final String typeParameter;
    private final MetadataInfo metadata;

    public DataTableInfo(final String name, final String selector, String typeParameter, MetadataInfo metadata) {
        super(name, selector);
        this.typeParameter = typeParameter;
        this.metadata = metadata;
    }

    public String getTypeParameter() {
        return typeParameter;
    }

    public MetadataInfo getMetadata() {
        return metadata;
    }
}
