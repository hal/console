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

    public static final String ELYTRON_SUBSYSTEM = "{selected.profile}/subsystem=elytron";
    public static final AddressTemplate ELYTRON_SUBSYSTEM_ADDRESS = AddressTemplate.of(ELYTRON_SUBSYSTEM);

    // ========== security realms, realm mappers

    public static final String PROPERTIES_REALM = ELYTRON_SUBSYSTEM + "/properties-realm=*";
    public static final String FILESYSTEM_REALM = ELYTRON_SUBSYSTEM + "/filesystem-realm=*";
    public static final String CACHING_REALM = ELYTRON_SUBSYSTEM + "/caching-realm=*";
    public static final String JDBC_REALM = ELYTRON_SUBSYSTEM + "/jdbc-realm=*";
    public static final String LDAP_REALM = ELYTRON_SUBSYSTEM + "/ldap-realm=*";
    public static final String KEYSTORE_REALM = ELYTRON_SUBSYSTEM + "/key-store-realm=*";
    public static final String AGGREGATE_REALM = ELYTRON_SUBSYSTEM + "/aggregate-realm=*";
    public static final String CUSTOM_MODIFIABLE_REALM = ELYTRON_SUBSYSTEM + "/custom-modifiable-realm=*";
    public static final String CUSTOM_REALM = ELYTRON_SUBSYSTEM + "/custom-realm=*";
    public static final String IDENTITY_REALM = ELYTRON_SUBSYSTEM + "/identity-realm=*";
    public static final String TOKEN_REALM = ELYTRON_SUBSYSTEM + "/token-realm=*";

    public static final String MAPPED_REGEX_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/mapped-regex-realm-mapper=*";
    public static final String SIMPLE_REGEX_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/simple-regex-realm-mapper=*";
    public static final String CUSTOM_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/custom-realm-mapper=*";
    public static final String CONSTANT_REALM_MAPPER = ELYTRON_SUBSYSTEM + "/constant-realm-mapper=*";

    public static final AddressTemplate PROPERTIES_REALM_ADDRESS = AddressTemplate.of(PROPERTIES_REALM);
    public static final AddressTemplate FILESYSTEM_REALM_ADDRESS = AddressTemplate.of(FILESYSTEM_REALM);
    public static final AddressTemplate CACHING_REALM_ADDRESS = AddressTemplate.of(CACHING_REALM);
    public static final AddressTemplate JDBC_REALM_ADDRESS = AddressTemplate.of(JDBC_REALM);
    public static final AddressTemplate LDAP_REALM_ADDRESS = AddressTemplate.of(LDAP_REALM);
    public static final AddressTemplate KEYSTORE_REALM_ADDRESS = AddressTemplate.of(KEYSTORE_REALM);
    public static final AddressTemplate AGGREGATE_REALM_ADDRESS = AddressTemplate.of(AGGREGATE_REALM);
    public static final AddressTemplate CUSTOM_MODIFIABLE_REALM_ADDRESS = AddressTemplate.of(CUSTOM_MODIFIABLE_REALM);
    public static final AddressTemplate CUSTOM_REALM_ADDRESS = AddressTemplate.of(CUSTOM_REALM);
    public static final AddressTemplate IDENTITY_REALM_ADDRESS = AddressTemplate.of(IDENTITY_REALM);
    public static final AddressTemplate TOKEN_REALM_ADDRESS = AddressTemplate.of(TOKEN_REALM);

    public static final AddressTemplate MAPPED_REGEX_REALM_MAPPER_ADDRESS = AddressTemplate.of(MAPPED_REGEX_REALM_MAPPER);
    public static final AddressTemplate SIMPLE_REGEX_REALM_MAPPER_ADDRESS = AddressTemplate.of(SIMPLE_REGEX_REALM_MAPPER);
    public static final AddressTemplate CUSTOM_REALM_MAPPER_ADDRESS = AddressTemplate.of(CUSTOM_REALM_MAPPER);
    public static final AddressTemplate CONSTANT_REALM_MAPPER_ADDRESS = AddressTemplate.of(CONSTANT_REALM_MAPPER);

    // ============== factories

    public static final String AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/aggregate-http-server-mechanism-factory=*";
    public static final String AGGREGATE_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/aggregate-sasl-server-factory=*";
    public static final String CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/configurable-http-server-mechanism-factory=*";
    public static final String CONFIGURABLE_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/configurable-sasl-server-factory=*";
    public static final String CUSTOM_CREDENTIAL_SECURITY_FACTORY = ELYTRON_SUBSYSTEM + "/custom-credential-security-factory=*";
    public static final String HTTP_AUTHENTICATION_FACTORY = ELYTRON_SUBSYSTEM + "/http-authentication-factory=*";
    public static final String KERBEROS_SECURITY_FACTORY = ELYTRON_SUBSYSTEM + "/kerberos-security-factory=*";
    public static final String MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/mechanism-provider-filtering-sasl-server-factory=*";
    public static final String PROVIDER_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/provider-http-server-mechanism-factory=*";
    public static final String PROVIDER_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/provider-sasl-server-factory=*";
    public static final String SASL_AUTHENTICATION_FACTORY = ELYTRON_SUBSYSTEM + "/sasl-authentication-factory=*";
    public static final String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY = ELYTRON_SUBSYSTEM + "/service-loader-http-server-mechanism-factory=*";
    public static final String SERVICE_LOADER_SASL_SERVER_FACTORY = ELYTRON_SUBSYSTEM + "/service-loader-sasl-server-factory=*";

    public static final String AGGREGATE_TRANSFORMER = ELYTRON_SUBSYSTEM + "/aggregate-principal-transformer=*";
    public static final String CHAINED_TRANSFORMER = ELYTRON_SUBSYSTEM + "/chained-principal-transformer=*";
    public static final String CONSTANT_TRANSFORMER = ELYTRON_SUBSYSTEM + "/constant-principal-transformer=*";
    public static final String CUSTOM_TRANSFORMER = ELYTRON_SUBSYSTEM + "/custom-principal-transformer=*";
    public static final String REGEX_VALIDATING_TRANSFORMER = ELYTRON_SUBSYSTEM + "/regex-validating-principal-transformer=*";
    public static final String REGEX_TRANSFORMER = ELYTRON_SUBSYSTEM + "/regex-principal-transformer=*";

    public static final AddressTemplate AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY);
    public static final AddressTemplate AGGREGATE_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(AGGREGATE_SASL_SERVER_FACTORY);
    public static final AddressTemplate CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY);
    public static final AddressTemplate CONFIGURABLE_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(CONFIGURABLE_SASL_SERVER_FACTORY);
    public static final AddressTemplate CUSTOM_CREDENTIAL_SECURITY_FACTORY_ADDRESS = AddressTemplate.of(CUSTOM_CREDENTIAL_SECURITY_FACTORY);
    public static final AddressTemplate HTTP_AUTHENTICATION_FACTORY_ADDRESS = AddressTemplate.of(HTTP_AUTHENTICATION_FACTORY);
    public static final AddressTemplate KERBEROS_SECURITY_FACTORY_ADDRESS = AddressTemplate.of(KERBEROS_SECURITY_FACTORY);
    public static final AddressTemplate MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY);
    public static final AddressTemplate PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY);
    public static final AddressTemplate PROVIDER_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(PROVIDER_SASL_SERVER_FACTORY);
    public static final AddressTemplate SASL_AUTHENTICATION_FACTORY_ADDRESS = AddressTemplate.of(SASL_AUTHENTICATION_FACTORY);
    public static final AddressTemplate SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = AddressTemplate.of(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY);
    public static final AddressTemplate SERVICE_LOADER_SASL_SERVER_FACTORY_ADDRESS = AddressTemplate.of(SERVICE_LOADER_SASL_SERVER_FACTORY);

    public static final AddressTemplate AGGREGATE_TRANSFORMER_ADDRESS = AddressTemplate.of(AGGREGATE_TRANSFORMER);
    public static final AddressTemplate CHAINED_TRANSFORMER_ADDRESS = AddressTemplate.of(CHAINED_TRANSFORMER);
    public static final AddressTemplate CONSTANT_TRANSFORMER_ADDRESS = AddressTemplate.of(CONSTANT_TRANSFORMER);
    public static final AddressTemplate CUSTOM_TRANSFORMER_ADDRESS = AddressTemplate.of(CUSTOM_TRANSFORMER);
    public static final AddressTemplate REGEX_VALIDATING_TRANSFORMER_ADDRESS = AddressTemplate.of(REGEX_VALIDATING_TRANSFORMER);
    public static final AddressTemplate REGEX_TRANSFORMER_ADDRESS = AddressTemplate.of(REGEX_TRANSFORMER);


    // ======== role mappers, permission mappers, decoders

    public static final String ADD_PREFIX_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/add-prefix-role-mapper=*";
    public static final String ADD_SUFFIX_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/add-suffix-role-mapper=*";
    public static final String AGGREGATE_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/aggregate-role-mapper=*";
    public static final String CONSTANT_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/constant-role-mapper=*";
    public static final String CUSTOM_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/custom-role-mapper=*";
    public static final String LOGICAL_ROLE_MAPPER = ELYTRON_SUBSYSTEM + "/logical-role-mapper=*";

    public static final String SIMPLE_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/simple-permission-mapper=*";
    public static final String LOGICAL_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/logical-permission-mapper=*";
    public static final String CUSTOM_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/custom-permission-mapper=*";
    public static final String CONSTANT_PERMISSION_MAPPER = ELYTRON_SUBSYSTEM + "/constant-permission-mapper=*";

    public static final String AGGREGATE_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/aggregate-principal-decoder=*";
    public static final String CONCATENATING_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/concatenating-principal-decoder=*";
    public static final String CONSTANT_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/constant-principal-decoder=*";
    public static final String CUSTOM_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/custom-principal-decoder=*";
    public static final String X500_PRINCIPAL_DECODER = ELYTRON_SUBSYSTEM + "/x500-attribute-principal-decoder=*";
    public static final String CUSTOM_ROLE_DECODER = ELYTRON_SUBSYSTEM + "/custom-role-decoder=*";
    public static final String SIMPLE_ROLE_DECODER = ELYTRON_SUBSYSTEM + "/simple-role-decoder=*";

    public static final AddressTemplate ADD_PREFIX_ROLE_MAPPER_ADDRESS = AddressTemplate.of(ADD_PREFIX_ROLE_MAPPER);
    public static final AddressTemplate ADD_SUFFIX_ROLE_MAPPER_ADDRESS = AddressTemplate.of(ADD_SUFFIX_ROLE_MAPPER);
    public static final AddressTemplate AGGREGATE_ROLE_MAPPER_ADDRESS = AddressTemplate.of(AGGREGATE_ROLE_MAPPER);
    public static final AddressTemplate CONSTANT_ROLE_MAPPER_ADDRESS = AddressTemplate.of(CONSTANT_ROLE_MAPPER);
    public static final AddressTemplate CUSTOM_ROLE_MAPPER_ADDRESS = AddressTemplate.of(CUSTOM_ROLE_MAPPER);
    public static final AddressTemplate LOGICAL_ROLE_MAPPER_ADDRESS = AddressTemplate.of(LOGICAL_ROLE_MAPPER);

    public static final AddressTemplate SIMPLE_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(SIMPLE_PERMISSION_MAPPER);
    public static final AddressTemplate LOGICAL_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(LOGICAL_PERMISSION_MAPPER);
    public static final AddressTemplate CUSTOM_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(CUSTOM_PERMISSION_MAPPER);
    public static final AddressTemplate CONSTANT_PERMISSION_MAPPER_ADDRESS = AddressTemplate.of(CONSTANT_PERMISSION_MAPPER);

    public static final AddressTemplate AGGREGATE_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(AGGREGATE_PRINCIPAL_DECODER);
    public static final AddressTemplate CONCATENATING_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(CONCATENATING_PRINCIPAL_DECODER);
    public static final AddressTemplate CONSTANT_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(CONSTANT_PRINCIPAL_DECODER);
    public static final AddressTemplate CUSTOM_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(CUSTOM_PRINCIPAL_DECODER);
    public static final AddressTemplate X500_PRINCIPAL_DECODER_ADDRESS = AddressTemplate.of(X500_PRINCIPAL_DECODER);
    public static final AddressTemplate CUSTOM_ROLE_DECODER_ADDRESS = AddressTemplate.of(CUSTOM_ROLE_DECODER);
    public static final AddressTemplate SIMPLE_ROLE_DECODER_ADDRESS = AddressTemplate.of(SIMPLE_ROLE_DECODER);

    // ======== other settings: authentication, stores, dir-context, SSL, logs

    public static final String KEY_STORE = ELYTRON_SUBSYSTEM + "/key-store=*";
    public static final String KEY_MANAGER = ELYTRON_SUBSYSTEM + "/key-manager=*";
    public static final String SERVER_SSL_CONTEXT = ELYTRON_SUBSYSTEM + "/server-ssl-context=*";
    public static final String CLIENT_SSL_CONTEXT = ELYTRON_SUBSYSTEM + "/client-ssl-context=*";
    public static final String TRUST_MANAGER = ELYTRON_SUBSYSTEM + "/trust-manager=*";
    public static final String CREDENTIAL_STORE = ELYTRON_SUBSYSTEM + "/credential-store=*";
    public static final String FILTERING_KEY_STORE = ELYTRON_SUBSYSTEM + "/filtering-key-store=*";
    public static final String LDAP_KEY_STORE = ELYTRON_SUBSYSTEM + "/ldap-key-store=*";
    public static final String PROVIDER_LOADER = ELYTRON_SUBSYSTEM + "/provider-loader=*";
    public static final String AGGREGATE_PROVIDERS = ELYTRON_SUBSYSTEM + "/aggregate-providers=*";
    public static final String SECURITY_DOMAIN = ELYTRON_SUBSYSTEM + "/security-domain=*";
    public static final String DIR_CONTEXT = ELYTRON_SUBSYSTEM + "/dir-context=*";
    public static final String AUTHENTICATION_CONTEXT = ELYTRON_SUBSYSTEM + "/authentication-context=*";
    public static final String AUTHENTICATION_CONF = ELYTRON_SUBSYSTEM + "/authentication-configuration=*";
    public static final String FILE_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/file-audit-log=*";
    public static final String SIZE_FILE_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/size-rotating-file-audit-log=*";
    public static final String PERIODIC_FILE_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/periodic-rotating-file-audit-log=*";
    public static final String SYSLOG_AUDIT_LOG = ELYTRON_SUBSYSTEM + "/syslog-audit-log=*";
    public static final String POLICY = ELYTRON_SUBSYSTEM + "/policy=*";
    public static final String AGGREGATE_SECURITY_EVENT_LISTENER = ELYTRON_SUBSYSTEM + "/aggregate-security-event-listener=*";

    public static final AddressTemplate KEY_STORE_ADDRESS = AddressTemplate.of(KEY_STORE);
    public static final AddressTemplate KEY_MANAGER_ADDRESS = AddressTemplate.of(KEY_MANAGER);
    public static final AddressTemplate SERVER_SSL_CONTEXT_ADDRESS = AddressTemplate.of(SERVER_SSL_CONTEXT);
    public static final AddressTemplate CLIENT_SSL_CONTEXT_ADDRESS = AddressTemplate.of(CLIENT_SSL_CONTEXT);
    public static final AddressTemplate TRUST_MANAGER_ADDRESS = AddressTemplate.of(TRUST_MANAGER);
    public static final AddressTemplate CREDENTIAL_STORE_ADDRESS = AddressTemplate.of(CREDENTIAL_STORE);
    public static final AddressTemplate FILTERING_KEY_STORE_ADDRESS = AddressTemplate.of(FILTERING_KEY_STORE);
    public static final AddressTemplate LDAP_KEY_STORE_ADDRESS = AddressTemplate.of(LDAP_KEY_STORE);
    public static final AddressTemplate PROVIDER_LOADER_ADDRESS = AddressTemplate.of(PROVIDER_LOADER);
    public static final AddressTemplate AGGREGATE_PROVIDERS_ADDRESS = AddressTemplate.of(AGGREGATE_PROVIDERS);

    public static final AddressTemplate SECURITY_DOMAIN_ADDRESS = AddressTemplate.of(SECURITY_DOMAIN);
    public static final AddressTemplate DIR_CONTEXT_ADDRESS = AddressTemplate.of(DIR_CONTEXT);
    public static final AddressTemplate AUTHENTICATION_CONTEXT_ADDRESS = AddressTemplate.of(AUTHENTICATION_CONTEXT);
    public static final AddressTemplate AUTHENTICATION_CONF_ADDRESS = AddressTemplate.of(AUTHENTICATION_CONF);

    public static final AddressTemplate FILE_AUDIT_LOG_ADDRESS = AddressTemplate.of(FILE_AUDIT_LOG);
    public static final AddressTemplate SIZE_FILE_AUDIT_LOG_ADDRESS = AddressTemplate.of(SIZE_FILE_AUDIT_LOG);
    public static final AddressTemplate PERIODIC_FILE_AUDIT_LOG_ADDRESS = AddressTemplate.of(PERIODIC_FILE_AUDIT_LOG);
    public static final AddressTemplate SYSLOG_AUDIT_LOG_ADDRESS = AddressTemplate.of(SYSLOG_AUDIT_LOG);
    public static final AddressTemplate POLICY_ADDRESS = AddressTemplate.of(POLICY);
    public static final AddressTemplate AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS = AddressTemplate.of(AGGREGATE_SECURITY_EVENT_LISTENER);

    // @formatter:on
}
