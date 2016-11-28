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
package org.jboss.hal.core.mbui.table;

import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jetbrains.annotations.NonNls;

/**
 * Special data table for named nodes, which uses the name as a unique identifier and provides a simplified {@link
 * #update(Iterable)} method.
 *
 * @author Harald Pehl
 */
public class NamedNodeTable<T extends NamedNode> extends ModelNodeTable<T> {

    public static class Builder<T extends NamedNode> extends ModelNodeTable.Builder<T> {

        public Builder(final Metadata metadata) {
            super(metadata);
        }
    }

    public NamedNodeTable(@NonNls final String id, final Options<T> options) {
        super(id, options);
    }

    /**
     * Shortcut for {@code super.update(data, NamedNode::getName)}
     */
    public void update(final Iterable<T> data) {
        super.update(data, NamedNode::getName);
    }
}
