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
package org.jboss.hal.core.finder;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;

import elemental2.dom.HTMLElement;

public class ColumnAction<T> {

    final String id;
    final String title;
    final HTMLElement element;
    final List<ColumnAction<T>> actions;
    final ColumnActionHandler<T> handler;
    final Constraints constraints;

    private ColumnAction(final Builder<T> builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.element = builder.element;
        this.handler = builder.handler;
        this.actions = builder.actions;
        if (builder.constraints != null) {
            this.constraints = builder.constraints;
        } else if (builder.constraint != null) {
            this.constraints = Constraints.single(builder.constraint);
        } else {
            this.constraints = Constraints.empty();
        }
    }

    public static class Builder<T> {

        private final String id;
        private String title;
        private HTMLElement element;
        private ColumnActionHandler<T> handler;
        private final List<ColumnAction<T>> actions;
        private Constraint constraint;
        private Constraints constraints;

        public Builder(final String id) {
            this.id = id;
            this.title = null;
            this.element = null;
            this.handler = null;
            this.actions = new ArrayList<>();
        }

        public Builder<T> title(final String title) {
            this.title = title;
            return this;
        }

        public Builder<T> element(final HTMLElement element) {
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
            this.constraint = constraint;
            return this;
        }

        public Builder<T> constraints(final Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public ColumnAction<T> build() {
            return new ColumnAction<>(this);
        }
    }
}
