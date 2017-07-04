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
package org.jboss.hal.client.configuration.subsystem.elytron;

import org.jboss.hal.meta.AddressTemplate;

/**
 * @author Claudio Miranda
 */
interface AddressTemplates {

    // @formatter:off

    String ELYTRON_SUBSYSTEM = "{selected.profile}/subsystem=elytron";
    AddressTemplate ELYTRON_SUBSYSTEM_ADDRESS = AddressTemplate.of(ELYTRON_SUBSYSTEM);

    // ========== security realms, realm mappers

    String PROPERTIES_REALM = ELYTRON_SUBSYSTEM + "/properties-realm=*";
    String FILESYSTEM_REALM = ELYTRON_SUBSYSTEM + "/filesystem-realm=*";
    String CACHING_REALM = ELYTRON_SUBSYSTEM + "/caching-realm=*";
    String JDBC_REALM = ELYTRON_SUBSYSTEM + "/jdbc-realm=*";
    String LDAP_REALM = ELYTRON_SUBSYSTEM + "/ldap-realm=*";
    String KEYSTORE_REALM = ELYTRON_SUBSYSTEM + "/key-store-realm=*";
    String AGGREGATE_REALM = ELYTRON_SUBSYSTEM + "/aggregate-realm=*";
    String CUSTOM_MODIFIABLE_REALM = ELYTRON_SUBSYSTEM + "/custom-modifiable-realm=*";
    String CUSTOM_REALM = ELYTRON_SUBSYSTEM + "/custom-realm=*";
    String IDENTITY_REALM = ELYTRON_SUBSYSTEM + "/identity-realm=*";
    String TOKEN_REALM = ELYTRON_SUBSYSTEM + "/token-realm=*";

    String MAPPED_REGEX_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/mapped-regex-realm-mapper=*";
    String SIMPLE_REGEX_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/simple-regex-realm-mapper=*";
    String CUSTOM_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/custom-realm-mapper=*";
    String CONSTANT_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/constant-realm-mapper=*";

    AddressTemplate PROPERTIES_REALM_ADDRESS = AddressTemplate.of(PROPERTIES_REALM);
    AddressTemplate FILESYSTEM_REALM_ADDRESS = AddressTemplate.of(FILESYSTEM_REALM);
    AddressTemplate CACHING_REALM_ADDRESS = AddressTemplate.of(CACHING_REALM);
    AddressTemplate JDBC_REALM_ADDRESS = AddressTemplate.of(JDBC_REALM);
    AddressTemplate LDAP_REALM_ADDRESS = AddressTemplate.of(LDAP_REALM);
    AddressTemplate KEYSTORE_REALM_ADDRESS = AddressTemplate.of(KEYSTORE_REALM);
    AddressTemplate AGGREGATE_REALM_ADDRESS = AddressTemplate.of(AGGREGATE_REALM);
    AddressTemplate CUSTOM_MODIFIABLE_REALM_ADDRESS = AddressTemplate.of(CUSTOM_MODIFIABLE_REALM);
    AddressTemplate CUSTOM_REALM_ADDRESS = AddressTemplate.of(CUSTOM_REALM);
    AddressTemplate IDENTITY_REALM_ADDRESS = AddressTemplate.of(IDENTITY_REALM);
    AddressTemplate TOKEN_REALM_ADDRESS = AddressTemplate.of(TOKEN_REALM);

    AddressTemplate MAPPED_REGEX_REALM_MAPPER_ADDRESS = AddressTemplate.of(MAPPED_REGEX_REALM_MAPPER);
    AddressTemplate SIMPLE_REGEX_REALM_MAPPER_ADDRESS = AddressTemplate.of(SIMPLE_REGEX_REALM_MAPPER);
    AddressTemplate CUSTOM_REALM_MAPPER_ADDRESS = AddressTemplate.of(CUSTOM_REALM_MAPPER);
    AddressTemplate CONSTANT_REALM_MAPPER_ADDRESS = AddressTemplate.of(CONSTANT_REALM_MAPPER);

    // ============== factories

    String AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/aggregate-http-server-mechanism-factory=*";
    String AGGREGATE_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/aggregate-sasl-server-factory=*";
    String CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/configurable-http-server-mechanism-factory=*";
    String CONFIGURABLE_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/configurable-sasl-server-factory=*";
    String CUSTOM_CREDENTIAL_SECURITY_FACTORY = ELYTRON_SUBSYSTEM + "/custom-credential-security-factory=*";
    String HTTP_AUTHENTICATION_FACTORY = ELYTRON_SUBSYSTEM + "/http-authentication-factory=*";
    String KERBEROS_SECURITY_FACTORY = ELYTRON_SUBSYSTEM + "/kerberos-security-factory=*";
    String MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/mechanism-provider-filtering-sasl-server-factory=*";
    String PROVIDER_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/provider-http-server-mechanism-factory=*";
    String PROVIDER_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/provider-sasl-server-factory=*";
    String SASL_AUTHENTICATION_FACTORY = ELYTRON_SUBSYSTEM + "/sasl-authentication-factory=*";
    String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/service-loader-http-server-mechanism-factory=*";
    String SERVICE_LOADER_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/service-loader-sasl-server-factory=*";

    String AGGREGATE_TRANSFORMER = ELYTRON_SUBSYSTEM + "/aggregate-principal-transformer=*";
    String CHAINED_TRANSFORMER = ELYTRON_SUBSYSTEM + "/chained-principal-transformer=*";
    String CONSTANT_TRANSFORMER = ELYTRON_SUBSYSTEM + "/constant-principal-transformer=*";
    String CUSTOM_TRANSFORMER = ELYTRON_SUBSYSTEM + "/custom-principal-transformer=*";
    String REGEX_VALIDATING_TRANSFORMER = ELYTRON_SUBSYSTEM + "/regex-validating-principal-transformer=*";
    String REGEX_TRANSFORMER = ELYTRON_SUBSYSTEM + "/regex-principal-transformer=*";

    AddressTemplate AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY);
    AddressTemplate AGGREGATE_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(AGGREGATE_SASL_SERVER_FACTORY);
    AddressTemplate CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY);
    AddressTemplate CONFIGURABLE_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(CONFIGURABLE_SASL_SERVER_FACTORY);
    AddressTemplate CUSTOM_CREDENTIAL_SECURITY_FACTORY_ADDRESS = AddressTemplate.of(CUSTOM_CREDENTIAL_SECURITY_FACTORY);
    AddressTemplate HTTP_AUTHENTICATION_FACTORY_ADDRESS = AddressTemplate.of(HTTP_AUTHENTICATION_FACTORY);
    AddressTemplate KERBEROS_SECURITY_FACTORY_ADDRESS = AddressTemplate.of(KERBEROS_SECURITY_FACTORY);
    AddressTemplate MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY);
    AddressTemplate PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY);
    AddressTemplate PROVIDER_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(PROVIDER_SASL_SERVER_FACTORY);
    AddressTemplate SASL_AUTHENTICATION_FACTORY_ADDRESS = AddressTemplate.of(SASL_AUTHENTICATION_FACTORY);
    AddressTemplate SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY);
    AddressTemplate SERVICE_LOADER_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(SERVICE_LOADER_SASL_SERVER_FACTORY);

    AddressTemplate AGGREGATE_TRANSFORMER_ADDRESS = AddressTemplate.of(AGGREGATE_TRANSFORMER);
    AddressTemplate CHAINED_TRANSFORMER_ADDRESS = AddressTemplate.of(CHAINED_TRANSFORMER);
    AddressTemplate CONSTANT_TRANSFORMER_ADDRESS = AddressTemplate.of(CONSTANT_TRANSFORMER);
    AddressTemplate CUSTOM_TRANSFORMER_ADDRESS = AddressTemplate.of(CUSTOM_TRANSFORMER);
    AddressTemplate REGEX_VALIDATING_TRANSFORMER_ADDRESS = AddressTemplate.of(REGEX_VALIDATING_TRANSFORMER);
    AddressTemplate REGEX_TRANSFORMER_ADDRESS = AddressTemplate.of(REGEX_TRANSFORMER);


    // ======== role mappers, permission mappers, decoders

    String ADD_PREFIX_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/add-prefix-role-mapper=*";
    String ADD_SUFFIX_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/add-suffix-role-mapper=*";
    String AGGREGATE_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/aggregate-role-mapper=*";
    String CONSTANT_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/constant-role-mapper=*";
    String CUSTOM_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/custom-role-mapper=*";
    String LOGICAL_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/logical-role-mapper=*";

    String SIMPLE_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/simple-permission-mapper=*";
    String LOGICAL_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/logical-permission-mapper=*";
    String CUSTOM_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/custom-permission-mapper=*";
    String CONSTANT_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/constant-permission-mapper=*";

    String AGGREGATE_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/aggregate-principal-decoder=*";
    String CONCATENATING_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/concatenating-principal-decoder=*";
    String CONSTANT_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/constant-principal-decoder=*";
    String CUSTOM_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/custom-principal-decoder=*";
    String X500_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/x500-attribute-principal-decoder=*";
    String CUSTOM_ROLE_DECODER = ELYTRON_SUBSYSTEM + "/custom-role-decoder=*";
    String SIMPLE_ROLE_DECODER = ELYTRON_SUBSYSTEM + "/simple-role-decoder=*";

    AddressTemplate ADD_PREFIX_ROLE_MAPPER_ADDRESS = AddressTemplate.of(ADD_PREFIX_ROLE_MAPPER);
    AddressTemplate ADD_SUFFIX_ROLE_MAPPER_ADDRESS = AddressTemplate.of(ADD_SUFFIX_ROLE_MAPPER);
    AddressTemplate AGGREGATE_ROLE_MAPPER_ADDRESS = AddressTemplate.of(AGGREGATE_ROLE_MAPPER);
    AddressTemplate CONSTANT_ROLE_MAPPER_ADDRESS = AddressTemplate.of(CONSTANT_ROLE_MAPPER);
    AddressTemplate CUSTOM_ROLE_MAPPER_ADDRESS = AddressTemplate.of(CUSTOM_ROLE_MAPPER);
    AddressTemplate LOGICAL_ROLE_MAPPER_ADDRESS = AddressTemplate.of(LOGICAL_ROLE_MAPPER);

    AddressTemplate SIMPLE_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(SIMPLE_PERMISSION_MAPPER);
    AddressTemplate LOGICAL_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(LOGICAL_PERMISSION_MAPPER);
    AddressTemplate CUSTOM_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(CUSTOM_PERMISSION_MAPPER);
    AddressTemplate CONSTANT_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(CONSTANT_PERMISSION_MAPPER);

    AddressTemplate AGGREGATE_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(AGGREGATE_PRINCIPAL_DECODER);
    AddressTemplate CONCATENATING_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(CONCATENATING_PRINCIPAL_DECODER);
    AddressTemplate CONSTANT_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(CONSTANT_PRINCIPAL_DECODER);
    AddressTemplate CUSTOM_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(CUSTOM_PRINCIPAL_DECODER);
    AddressTemplate X500_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(X500_PRINCIPAL_DECODER);
    AddressTemplate CUSTOM_ROLE_DECODER_ADDRESS = AddressTemplate.of(CUSTOM_ROLE_DECODER);
    AddressTemplate SIMPLE_ROLE_DECODER_ADDRESS = AddressTemplate.of(SIMPLE_ROLE_DECODER);

    // ======== other settings: authentication, stores, dir-context, SSL, logs

    String KEY_STORE = ELYTRON_SUBSYSTEM + "/key-store=*";
    String KEY_MANAGER = ELYTRON_SUBSYSTEM + "/key-manager=*";
    String SERVER_SSL_CONTEXT = ELYTRON_SUBSYSTEM + "/server-ssl-context=*";
    String CLIENT_SSL_CONTEXT = ELYTRON_SUBSYSTEM + "/client-ssl-context=*";
    String TRUST_MANAGER = ELYTRON_SUBSYSTEM + "/trust-manager=*";
    String CREDENTIAL_STORE = ELYTRON_SUBSYSTEM + "/credential-store=*";
    String FILTERING_KEY_STORE = ELYTRON_SUBSYSTEM + "/filtering-key-store=*";
    String LDAP_KEY_STORE = ELYTRON_SUBSYSTEM + "/ldap-key-store=*";
    String PROVIDER_LOADER = ELYTRON_SUBSYSTEM + "/provider-loader=*";
    String AGGREGATE_PROVIDERS = ELYTRON_SUBSYSTEM + "/aggregate-providers=*";
    String SECURITY_DOMAIN = ELYTRON_SUBSYSTEM + "/security-domain=*";
    String DIR_CONTEXT = ELYTRON_SUBSYSTEM + "/dir-context=*";
    String AUTHENTICATION_CONTEXT = ELYTRON_SUBSYSTEM + "/authentication-context=*";
    String AUTHENTICATION_CONF = ELYTRON_SUBSYSTEM + "/authentication-configuration=*";
    String FILE_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/file-audit-log=*";
    String SIZE_FILE_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/size-rotating-file-audit-log=*";
    String PERIODIC_FILE_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/periodic-rotating-file-audit-log=*";
    String SYSLOG_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/syslog-audit-log=*";
    String POLICY = ELYTRON_SUBSYSTEM + "/policy=*";
    String AGGREGATE_SECURITY_EVENT_LISTENER = ELYTRON_SUBSYSTEM + "/aggregate-security-event-listener=*";

    AddressTemplate KEY_STORE_ADDRESS = AddressTemplate.of(KEY_STORE);
    AddressTemplate KEY_MANAGER_ADDRESS = AddressTemplate.of(KEY_MANAGER);
    AddressTemplate SERVER_SSL_CONTEXT_ADDRESS = AddressTemplate.of(SERVER_SSL_CONTEXT);
    AddressTemplate CLIENT_SSL_CONTEXT_ADDRESS = AddressTemplate.of(CLIENT_SSL_CONTEXT);
    AddressTemplate TRUST_MANAGER_ADDRESS = AddressTemplate.of(TRUST_MANAGER);
    AddressTemplate CREDENTIAL_STORE_ADDRESS = AddressTemplate.of(CREDENTIAL_STORE);
    AddressTemplate FILTERING_KEY_STORE_ADDRESS = AddressTemplate.of(FILTERING_KEY_STORE);
    AddressTemplate LDAP_KEY_STORE_ADDRESS = AddressTemplate.of(LDAP_KEY_STORE);
    AddressTemplate PROVIDER_LOADER_ADDRESS = AddressTemplate.of(PROVIDER_LOADER);
    AddressTemplate AGGREGATE_PROVIDERS_ADDRESS = AddressTemplate.of(AGGREGATE_PROVIDERS);

    AddressTemplate SECURITY_DOMAIN_ADDRESS = AddressTemplate.of(SECURITY_DOMAIN);
    AddressTemplate DIR_CONTEXT_ADDRESS = AddressTemplate.of(DIR_CONTEXT);
    AddressTemplate AUTHENTICATION_CONTEXT_ADDRESS = AddressTemplate.of(AUTHENTICATION_CONTEXT);
    AddressTemplate AUTHENTICATION_CONF_ADDRESS = AddressTemplate.of(AUTHENTICATION_CONF);

    AddressTemplate FILE_AUDIT_LOG_ADDRESS = AddressTemplate.of(FILE_AUDIT_LOG);
    AddressTemplate SIZE_FILE_AUDIT_LOG_ADDRESS = AddressTemplate.of(SIZE_FILE_AUDIT_LOG);
    AddressTemplate PERIODIC_FILE_AUDIT_LOG_ADDRESS = AddressTemplate.of(PERIODIC_FILE_AUDIT_LOG);
    AddressTemplate SYSLOG_AUDIT_LOG_ADDRESS = AddressTemplate.of(SYSLOG_AUDIT_LOG);
    AddressTemplate POLICY_ADDRESS = AddressTemplate.of(POLICY);
    AddressTemplate AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS = AddressTemplate.of(AGGREGATE_SECURITY_EVENT_LISTENER);

    // @formatter:on
}
