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
package org.jboss.hal.meta;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

public interface StatementContext {

    @SuppressWarnings("HardCodedStringLiteral")
    enum Key {

        ANY_PROFILE("any.profile", PROFILE),
        ANY_GROUP("any.group", SERVER_GROUP),
        SELECTED_PROFILE("selected.profile", PROFILE),
        SELECTED_GROUP("selected.group", SERVER_GROUP),
        SELECTED_HOST("selected.host", HOST),
        SELECTED_SERVER("selected.server", SERVER);

        private final String key;
        private final String resource;

        Key(final String key, final String resource) {
            this.key = key;
            this.resource = resource;
        }

        public String key() {
            return key;
        }

        public String resource() {
            return resource;
        }

        public String variable() {
            return "{" + key + "}";
        }

        public static Key fromKey(String key) {
            for (Key k : Key.values()) {
                if (k.key().equals(key)) {
                    return k;
                }
            }
            return null;
        }
    }


    StatementContext NOOP = new StatementContext() {

        @Override
        public String resolve(String key) {
            // keys are resolved "as-is"
            return key;
        }

        @Override
        public String[] resolveTuple(String key) {
            // tuples are resolved as "echo"
            return new String[]{key, key};
        }

        @Override
        public String selectedProfile() {
            return null;
        }
    };


    /**
     * Resolves a value matching the key.
     */
    String resolve(String key);

    /**
     * Resolves a tuple matching the key.
     */
    String[] resolveTuple(String key);

    String selectedProfile();
}
