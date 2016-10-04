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
package org.jboss.hal.core.mbui.form;

import java.util.Arrays;
import java.util.LinkedHashSet;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;
import org.jetbrains.annotations.NonNls;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REQUEST_PROPERTIES;

/**
 * @author Harald Pehl
 */
public class OperationFormBuilder<T extends ModelNode> {

    private final String id;
    private final Metadata metadata;
    private final String operation;
    private final LinkedHashSet<String> includes;

    public OperationFormBuilder(@NonNls final String id, final Metadata metadata, final String operation) {
        this.id = id;
        this.metadata = metadata;
        this.operation = operation;
        this.includes = new LinkedHashSet<>();
    }

    public OperationFormBuilder<T> include(final String[] attributes) {
        includes.addAll(Arrays.asList(attributes));
        return this;
    }

    public OperationFormBuilder<T> include(final Iterable<String> attributes) {
        //noinspection ResultOfMethodCallIgnored
        Iterables.addAll(includes, attributes);
        return this;
    }

    public OperationFormBuilder<T> include(@NonNls final String first, @NonNls final String... rest) {
        includes.addAll(Lists.asList(first, rest));
        return this;
    }

    public ModelNodeForm<T> build() {
        ModelNode modelNode = ModelNodeHelper.failSafeGet(metadata.getDescription(),
                Joiner.on('/').join(OPERATIONS, operation, REQUEST_PROPERTIES));
        ModelNode repackaged = new ModelNode();
        repackaged.get(ATTRIBUTES).set(modelNode);
        ResourceDescription reloadDescription = new ResourceDescription(repackaged);
        Metadata formMetadata = new Metadata(metadata.getTemplate(), SecurityContext.RWX, reloadDescription,
                metadata.getCapabilities());
        return new ModelNodeForm.Builder<T>(id, formMetadata)
                .include(includes)
                .addOnly()
                .build();
    }
}
