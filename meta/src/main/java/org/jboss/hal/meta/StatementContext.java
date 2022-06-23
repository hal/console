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
package org.jboss.hal.meta;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_CONFIG;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * Contains global state which is updated as you navigate in the console. In standalone mode most of the methods return null.
 */
public interface StatementContext {

    @SuppressWarnings("HardCodedStringLiteral")
    enum Expression {
        /**
         * Please note that this tuple might not always resolve to the domain controller. For some edge cases (e.g. when the
         * current user is assigned to a host scoped role which is scoped to a secondary host), this tuple is resolved to the
         * first host which was read during bootstrap. In any case it is resolved to an existing host.
         * <p>
         * Address templates which use this tuple must be prepared that it does not always resolve to the domain controller.
         */
        DOMAIN_CONTROLLER("domain.controller", HOST), SELECTED_PROFILE("selected.profile", PROFILE), SELECTED_GROUP(
                "selected.group", SERVER_GROUP), SELECTED_HOST("selected.host", HOST), SELECTED_SERVER_CONFIG(
                        "selected.server-config", SERVER_CONFIG), SELECTED_SERVER("selected.server", SERVER);

        private final String name;
        private final String resource;

        Expression(String name, String resource) {
            this.name = name;
            this.resource = resource;
        }

        public String resource() {
            return resource;
        }

        /** @return the {@code name} surrounded by "{" and "}" */
        public String expression() {
            return "{" + name + "}";
        }

        public static Expression from(String name) {
            for (Expression t : Expression.values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }

    StatementContext NOOP = new StatementContext() {

        @Override
        public String resolve(String placeholder, AddressTemplate template) {
            return placeholder;
        }

        @Override
        public String[] resolveTuple(String placeholder, AddressTemplate template) {
            return new String[] { placeholder, placeholder };
        }

        @Override
        public String domainController() {
            return null;
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
        public String selectedServerConfig() {
            return null;
        }

        @Override
        public String selectedServer() {
            return null;
        }
    };

    /** Resolves a single value. */
    String resolve(String placeholder, AddressTemplate template);

    /** Resolves a tuple. */
    String[] resolveTuple(String placeholder, AddressTemplate template);

    /** @return the domain controller */
    String domainController();

    /** @return the selected profile */
    String selectedProfile();

    /** @return the selected server group */
    String selectedServerGroup();

    /** @return the selected host */
    String selectedHost();

    /** @return the selected server config */
    String selectedServerConfig();

    /** @return the selected server */
    String selectedServer();
}
