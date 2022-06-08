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
package org.jboss.hal.client.configuration.subsystem.security;

import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ACL_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGIN_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAPPING_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.POLICY_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER_MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRUST_MODULE;

/** Enum struct to collect all settings for a specific security module. */
@Deprecated
enum Module {
    AUTHENTICATION(Ids.SECURITY_DOMAIN_AUTHENTICATION_ADD, Names.AUTHENTICATION_MODULE, "authentication=classic",
            LOGIN_MODULE), AUTHORIZATION(Ids.SECURITY_DOMAIN_AUTHORIZATION_ADD, Names.AUTHORIZATION_MODULE,
                    "authorization=classic",
                    POLICY_MODULE), AUDIT(Ids.SECURITY_DOMAIN_AUDIT_ADD, Names.AUDIT_MODULE, "audit=classic",
                            PROVIDER_MODULE), ACL(Ids.SECURITY_DOMAIN_ACL_MODULE_ADD, Names.ACL_MODULE, "acl=classic",
                                    ACL_MODULE), MAPPING(Ids.SECURITY_DOMAIN_MAPPING_ADD, Names.MAPPING_MODULE,
                                            "mapping=classic", MAPPING_MODULE), TRUST(Ids.SECURITY_DOMAIN_TRUST_MODULE_ADD,
                                                    Names.TRUST_MODULE, "identity-trust=classic", TRUST_MODULE);

    final String id;
    final String type;
    final String singleton;
    final String resource;

    Module(final String id, final String type, final String singleton, final String resource) {
        this.id = id;
        this.type = type;
        this.singleton = singleton;
        this.resource = resource;
    }
}
