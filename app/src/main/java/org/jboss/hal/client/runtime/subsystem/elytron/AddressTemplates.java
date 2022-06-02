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
package org.jboss.hal.client.runtime.subsystem.elytron;

import org.jboss.hal.meta.AddressTemplate;

interface AddressTemplates {

    // @formatter:off
    String ELYTRON_SUBSYSTEM_ADDRESS = "{selected.host}/{selected.server}/subsystem=elytron";
    AddressTemplate ELYTRON_SUBSYSTEM_TEMPLATE = AddressTemplate.of(ELYTRON_SUBSYSTEM_ADDRESS);
    AddressTemplate ELYTRON_PROFILE_TEMPLATE = AddressTemplate.of("{selected.profile}/subsystem=elytron");

    // ------------------------------------------------------ address (a-z)

    String CACHING_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/caching-realm=*";
    String CERTIFICATE_AUTHORITY_ACCOUNT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/certificate-authority-account=*";
    String CREDENTIAL_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/credential-store=*";
    String CUSTOM_MODIFIABLE_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-modifiable-realm=*";
    String FILESYSTEM_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/filesystem-realm=*";
    String FILTERING_KEY_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/filtering-key-store=*";
    String KEY_MANAGER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/key-manager=*";
    String KEY_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/key-store=*";
    String LDAP_KEY_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/ldap-key-store=*";
    String LDAP_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/ldap-realm=*";
    String PROPERTIES_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/properties-realm=*";
    String SECRET_KEY_CREDENTIAL_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/secret-key-credential-store=*";
    String SECURITY_DOMAIN_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/security-domain=*";
    String TRUST_MANAGER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/trust-manager=*";

    // ------------------------------------------------------ templates (a-z)

    AddressTemplate CACHING_REALM_TEMPLATE = AddressTemplate.of(CACHING_REALM_ADDRESS);
    AddressTemplate CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE = AddressTemplate.of(CERTIFICATE_AUTHORITY_ACCOUNT_ADDRESS);
    AddressTemplate CREDENTIAL_STORE_TEMPLATE = AddressTemplate.of(CREDENTIAL_STORE_ADDRESS);
    AddressTemplate CUSTOM_MODIFIABLE_REALM_TEMPLATE = AddressTemplate.of(CUSTOM_MODIFIABLE_REALM_ADDRESS);
    AddressTemplate FILESYSTEM_REALM_TEMPLATE = AddressTemplate.of(FILESYSTEM_REALM_ADDRESS);
    AddressTemplate FILTERING_KEY_STORE_TEMPLATE = AddressTemplate.of(FILTERING_KEY_STORE_ADDRESS);
    AddressTemplate KEY_MANAGER_TEMPLATE = AddressTemplate.of(KEY_MANAGER_ADDRESS);
    AddressTemplate KEY_STORE_TEMPLATE = AddressTemplate.of(KEY_STORE_ADDRESS);
    AddressTemplate LDAP_KEY_STORE_TEMPLATE = AddressTemplate.of(LDAP_KEY_STORE_ADDRESS);
    AddressTemplate LDAP_REALM_TEMPLATE = AddressTemplate.of(LDAP_REALM_ADDRESS);
    AddressTemplate PROPERTIES_REALM_TEMPLATE = AddressTemplate.of(PROPERTIES_REALM_ADDRESS);
    AddressTemplate SECRET_KEY_CREDENTIAL_STORE_TEMPLATE = AddressTemplate.of(SECRET_KEY_CREDENTIAL_STORE_ADDRESS);
    AddressTemplate SECURITY_DOMAIN_TEMPLATE = AddressTemplate.of(SECURITY_DOMAIN_ADDRESS);
    AddressTemplate TRUST_MANAGER_TEMPLATE = AddressTemplate.of(TRUST_MANAGER_ADDRESS);
    // @formatter:on
}
