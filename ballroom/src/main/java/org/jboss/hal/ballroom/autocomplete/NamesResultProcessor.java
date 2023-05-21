/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ballroom.autocomplete;

import java.util.Comparator;
import java.util.List;

import org.jboss.hal.ballroom.form.SuggestHandler;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ModelNode;

import static java.util.stream.Collectors.toList;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Processes the result of a single READ_CHILDREN_NAMES operation.
 */
public class NamesResultProcessor extends ReadChildrenProcessor implements ResultProcessor {

    @Override
    protected List<ReadChildrenResult> processToModel(final String query, final ModelNode nodes) {
        return nodes.asList().stream()
                .map(node -> new ReadChildrenResult(node.asString()))
                .filter(result -> !isNullOrEmpty(query) &&
                        (SuggestHandler.SHOW_ALL_VALUE.equals(query) || result.name.contains(query)))
                .sorted(Comparator.comparing(result -> result.name))
                .collect(toList());
    }

    @Override
    protected List<ReadChildrenResult> processToModel(final String query, final CompositeResult compositeResult) {
        throw new UnsupportedOperationException();
    }
}
