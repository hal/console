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
package org.jboss.hal.core.finder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import elemental.dom.Element;
import org.jboss.hal.meta.security.Constraint;

/**
 * @author Harald Pehl
 */
public class ColumnAction<T> {

    public static class Builder<T> {

        private final String id;
        private String title;
        private Element element;
        private ColumnActionHandler<T> handler;
        private List<ColumnAction<T>> actions;
        private final Set<Constraint> constraints;

        public Builder(final String id) {
            this.id = id;
            this.title = null;
            this.element = null;
            this.handler = null;
            this.actions = new ArrayList<>();
            this.constraints = new HashSet<>();
        }

        public Builder<T> title(final String title) {
            this.title = title;
            return this;
        }

        public Builder<T> element(final Element element) {
            this.element = element;
            return this;
        }

        public Builder<T> handler(final ColumnActionHandler<T> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<T> action(final ColumnAction<T> action) {
            this.actions.add(action);
            return this;
        }

        public Builder<T> actions(final List<ColumnAction<T>> actions) {
            this.actions.addAll(actions);
            return this;
        }

        public Builder<T> constraint(final Constraint constraint) {
            this.constraints.add(constraint);
            return this;
        }

        public ColumnAction<T> build() {
            return new ColumnAction<>(this);
        }
    }


    final String id;
    final String title;
    final Element element;
    final List<ColumnAction<T>> actions;
    final ColumnActionHandler<T> handler;
    final Set<Constraint> constraints;

    private ColumnAction(final Builder<T> builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.element = builder.element;
        this.handler = builder.handler;
        this.actions = builder.actions;
        this.constraints = builder.constraints;
    }
}
