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
    enum Tuple {

        SELECTED_PROFILE("selected.profile", PROFILE),
        SELECTED_GROUP("selected.group", SERVER_GROUP),
        SELECTED_HOST("selected.host", HOST),
        SELECTED_SERVER("selected.server", SERVER);

        private final String tuple;
        private final String resource;

        Tuple(final String tuple, final String resource) {
            this.tuple = tuple;
            this.resource = resource;
        }

        public String tuple() {
            return tuple;
        }

        public String resource() {
            return resource;
        }

        public String variable() {
            return "{" + tuple + "}";
        }

        public static Tuple from(String tuple) {
            for (Tuple t : Tuple.values()) {
                if (t.tuple().equals(tuple)) {
                    return t;
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
        public String[] resolveTuple(String tuple) {
            // tuples are resolved as "echo"
            return new String[]{tuple, tuple};
        }

        @Override
        public String selectedProfile() {
            return null;
        }

        @Override
        public String selectedServerGroup() {
            return null;
        }

        @Override
        public String selectedHost() {
            return null;
        }

        @Override
        public String selectedServer() {
            return null;
        }
    };


    /**
     * Resolves a value.
     */
    String resolve(String key);

    /**
     * Resolves a tuple.
     */
    String[] resolveTuple(String tuple);

    String selectedProfile();

    String selectedServerGroup();

    String selectedHost();

    String selectedServer();
}
