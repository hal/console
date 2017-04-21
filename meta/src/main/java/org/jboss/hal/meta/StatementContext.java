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

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

@JsType
public interface StatementContext {

    @SuppressWarnings("HardCodedStringLiteral")
    enum Tuple {
        /**
         * Please not that this tuple might not always resolve to the domain controller. For some edge cases (e.g. when
         * the current user is assigned to a host scoped role which is scoped to a slave host), this tuple is resolved
         * to the first host which was read during bootstrap. In any case it is resolved to an existing host.
         * <p>
         * Address templates which use this tuple must be prepared that it does not always resolve to the domain
         * controller.
         */
        DOMAIN_CONTROLLER("domain.controller", HOST),
        SELECTED_PROFILE("selected.profile", PROFILE),
        SELECTED_GROUP("selected.group", SERVER_GROUP),
        SELECTED_HOST("selected.host", HOST),
        SELECTED_SERVER_CONFIG("selected.server-config", SERVER_CONFIG),
        SELECTED_SERVER("selected.server", SERVER);

        private final String name;
        private final String resource;

        Tuple(final String name, final String resource) {
            this.name = name;
            this.resource = resource;
        }

        public String resource() {
            return resource;
        }

        public String variable() {
            return "{" + name + "}";
        }

        public static Tuple from(String name) {
            for (Tuple t : Tuple.values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
            return null;
        }
    }


    StatementContext NOOP = new StatementContext() {

        @Override
        public String resolve(String placeholder) {
            return placeholder;
        }

        @Override
        public String[] resolveTuple(String placeholder) {
            return new String[]{placeholder, placeholder};
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


    /**
     * Resolves a single value.
     */
    String resolve(String placeholder);

    /**
     * Resolves a tuple.
     */
    String[] resolveTuple(String placeholder);

    @JsProperty(name = "domainController")
    String domainController();

    @JsProperty(name = "selectedProfile")
    String selectedProfile();

    @JsProperty(name = "selectedServerGroup")
    String selectedServerGroup();

    @JsProperty(name = "selectedHost")
    String selectedHost();

    @JsProperty(name = "selectedServerConfig")
    String selectedServerConfig();

    @JsProperty(name = "selectedServer")
    String selectedServer();
}
