/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.mbui.form;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static java.util.Arrays.asList;

public class OperationFormBuilder<T extends ModelNode> {

    private final String id;
    private final Metadata metadata;
    private final String operation;
    private final LinkedHashSet<String> includes;
    private final Set<String> excludes;

    public OperationFormBuilder(String id, Metadata metadata, String operation) {
        this.id = id;
        this.metadata = metadata;
        this.operation = operation;
        this.includes = new LinkedHashSet<>();
        this.excludes = new HashSet<>();
    }

    public OperationFormBuilder<T> include(String[] attributes) {
        includes.addAll(Arrays.asList(attributes));
        return this;
    }

    public OperationFormBuilder<T> include(Iterable<String> attributes) {
        // noinspection ResultOfMethodCallIgnored
        Iterables.addAll(includes, attributes);
        return this;
    }

    public OperationFormBuilder<T> include(String first, String... rest) {
        includes.addAll(Lists.asList(first, rest));
        return this;
    }

    public OperationFormBuilder<T> exclude(String[] attributes) {
        excludes.addAll(asList(attributes));
        return this;
    }

    public OperationFormBuilder<T> exclude(Iterable<String> attributes) {
        Iterables.addAll(excludes, attributes);
        return this;
    }

    public OperationFormBuilder<T> exclude(String first, String... rest) {
        excludes.addAll(Lists.asList(first, rest));
        return this;
    }

    public ModelNodeForm<T> build() {
        return new ModelNodeForm.Builder<T>(id, metadata.forOperation(operation))
                .include(includes)
                .exclude(excludes)
                .addOnly()
                .build();
    }
}
