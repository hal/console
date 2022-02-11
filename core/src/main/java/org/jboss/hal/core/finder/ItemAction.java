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
package org.jboss.hal.core.finder;

import java.util.HashMap;
import java.util.Map;

import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.meta.security.Constraints;

public class ItemAction<T> {

    public static final ItemAction SEPARATOR = new ItemAction.Builder().build();

    @SuppressWarnings("unchecked")
    public static <T> ItemAction<T> separator() {
        return SEPARATOR;
    }

    final String title;
    final ItemActionHandler<T> handler;
    final String href;
    final Map<String, String> attributes;
    final Constraints constraints;

    private ItemAction(Builder<T> builder) {
        this.title = builder.title;
        this.handler = builder.handler;
        this.href = builder.href;
        this.attributes = builder.attributes;
        if (builder.constraints != null) {
            this.constraints = builder.constraints;
        } else if (builder.constraint != null) {
            this.constraints = Constraints.single(builder.constraint);
        } else {
            this.constraints = Constraints.empty();
        }
    }

    public String getTitle() {
        return title;
    }

    public ItemActionHandler<T> getHandler() {
        return handler;
    }

    public static class Builder<T> {

        private String title;
        private ItemActionHandler<T> handler;
        private String href;
        private final Map<String, String> attributes;
        private Constraint constraint;
        private Constraints constraints;

        public Builder() {
            this.title = null;
            this.handler = null;
            this.href = null;
            this.attributes = new HashMap<>();
        }

        public Builder<T> title(String title) {
            this.title = title;
            return this;
        }

        public Builder<T> handler(ItemActionHandler<T> handler) {
            this.handler = handler;
            return this;
        }

        public Builder<T> href(String href, String... attributes) {
            this.href = href;
            if (attributes != null && attributes.length > 1) {
                if (attributes.length % 2 != 0) {
                    throw new IllegalArgumentException("Attributes for item action must be key/value pairs");
                }
                for (int i = 0; i < attributes.length; i += 2) {
                    this.attributes.put(attributes[i], attributes[i + 1]);
                }
            }
            return this;
        }

        public Builder<T> constraint(Constraint constraint) {
            this.constraint = constraint;
            return this;
        }

        public Builder<T> constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public ItemAction<T> build() {
            return new ItemAction<>(this);
        }
    }
}
