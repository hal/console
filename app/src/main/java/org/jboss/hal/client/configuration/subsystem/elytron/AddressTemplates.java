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
package org.jboss.hal.client.configuration.subsystem.elytron;

import org.jboss.hal.meta.AddressTemplate;

public interface AddressTemplates {

    // @formatter:off
    String ELYTRON_SUBSYSTEM_ADDRESS = "{selected.profile}/subsystem=elytron";
    AddressTemplate ELYTRON_SUBSYSTEM_TEMPLATE = AddressTemplate.of(ELYTRON_SUBSYSTEM_ADDRESS);

    // ------------------------------------------------------ address (a-z)

    String ADD_PREFIX_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/add-prefix-role-mapper=*";
    String ADD_SUFFIX_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/add-suffix-role-mapper=*";
    String AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/aggregate-http-server-mechanism-factory=*";
    String AGGREGATE_EVIDENCE_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-evidence-decoder=*";
    String AGGREGATE_PRINCIPAL_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-principal-decoder=*";
    String AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-principal-transformer=*";
    String AGGREGATE_PROVIDERS_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-providers=*";
    String AGGREGATE_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-realm=*";
    String AGGREGATE_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-role-mapper=*";
    String AGGREGATE_SASL_SERVER_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-sasl-server-factory=*";
    String AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-security-event-listener=*";
    String AGGREGATE_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/aggregate-principal-transformer=*";
    String AUTHENTICATION_CONFIGURATION_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/authentication-configuration=*";
    String AUTHENTICATION_CONTEXT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/authentication-context=*";

    String CACHING_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/caching-realm=*";
    String CERTIFICATE_AUTHORITY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/certificate-authority=*";
    String CERTIFICATE_AUTHORITY_ACCOUNT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/certificate-authority-account=*";
    String CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/chained-principal-transformer=*";
    String CLIENT_SSL_CONTEXT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/client-ssl-context=*";
    String CONCATENATING_PRINCIPAL_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/concatenating-principal-decoder=*";
    String CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/configurable-http-server-mechanism-factory=*";
    String CONFIGURABLE_SASL_SERVER_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/configurable-sasl-server-factory=*";
    String CONSTANT_PERMISSION_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/constant-permission-mapper=*";
    String CONSTANT_PRINCIPAL_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/constant-principal-decoder=*";
    String CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/constant-principal-transformer=*";
    String CONSTANT_REALM_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/constant-realm-mapper=*";
    String CONSTANT_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/constant-role-mapper=*";
    String CONSTANT_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/constant-principal-transformer=*";
    String CREDENTIAL_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/credential-store=*";
    String CUSTOM_CREDENTIAL_SECURITY_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-credential-security-factory=*";
    String CUSTOM_EVIDENCE_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-evidence-decoder=*";
    String CUSTOM_MODIFIABLE_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-modifiable-realm=*";
    String CUSTOM_PERMISSION_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-permission-mapper=*";
    String CUSTOM_PRINCIPAL_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-principal-decoder=*";
    String CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-principal-transformer=*";
    String CUSTOM_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-realm=*";
    String CUSTOM_REALM_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-realm-mapper=*";
    String CUSTOM_ROLE_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-role-decoder=*";
    String CUSTOM_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-role-mapper=*";
    String CUSTOM_SECURITY_EVENT_LISTENER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-security-event-listener=*";
    String CUSTOM_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/custom-principal-transformer=*";

    String DIR_CONTEXT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/dir-context=*";

    String EXPRESSION_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/expression=encryption";

    String FILE_AUDIT_LOG_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/file-audit-log=*";
    String FILESYSTEM_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/filesystem-realm=*";
    String FILTERING_KEY_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/filtering-key-store=*";

    String HTTP_AUTHENTICATION_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/http-authentication-factory=*";

    String IDENTITY_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/identity-realm=*";

    String JASPI_CONFIGURATION_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/jaspi-configuration=*";
    String JDBC_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/jdbc-realm=*";

    String KERBEROS_SECURITY_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/kerberos-security-factory=*";
    String KEY_MANAGER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/key-manager=*";
    String KEY_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/key-store=*";
    String KEY_STORE_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/key-store-realm=*";

    String LDAP_KEY_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/ldap-key-store=*";
    String LDAP_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/ldap-realm=*";
    String LOGICAL_PERMISSION_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/logical-permission-mapper=*";
    String LOGICAL_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/logical-role-mapper=*";

    String MAPPED_REGEX_REALM_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/mapped-regex-realm-mapper=*";
    String MAPPED_ROLE_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/mapped-role-mapper=*";
    String MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/mechanism-provider-filtering-sasl-server-factory=*";

    String PERIODIC_FILE_AUDIT_LOG_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/periodic-rotating-file-audit-log=*";
    String PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/periodic-rotating-file-audit-log=*";
    String PERMISSION_SET_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/permission-set=*";
    String POLICY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/policy=*";
    String PROPERTIES_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/properties-realm=*";
    String PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/provider-http-server-mechanism-factory=*";
    String PROVIDER_LOADER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/provider-loader=*";
    String PROVIDER_SASL_SERVER_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/provider-sasl-server-factory=*";

    String REGEX_PRINCIPAL_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/regex-principal-transformer=*";
    String REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/regex-validating-principal-transformer=*";

    String SASL_AUTHENTICATION_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/sasl-authentication-factory=*";
    String SECRET_KEY_CREDENTIAL_STORE_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/secret-key-credential-store=*";
    String SECURITY_DOMAIN_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/security-domain=*";
    String SERVER_SSL_CONTEXT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/server-ssl-context=*";
    String SERVER_SSL_SNI_CONTEXT_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/server-ssl-sni-context=*";
    String SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/service-loader-http-server-mechanism-factory=*";
    String SERVICE_LOADER_SASL_SERVER_FACTORY_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/service-loader-sasl-server-factory=*";
    String SIMPLE_PERMISSION_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/simple-permission-mapper=*";
    String SIMPLE_REGEX_REALM_MAPPER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/simple-regex-realm-mapper=*";
    String SIMPLE_ROLE_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/simple-role-decoder=*";
    String SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/size-rotating-file-audit-log=*";
    String SYSLOG_AUDIT_LOG_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/syslog-audit-log=*";

    String TOKEN_REALM_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/token-realm=*";
    String TRUST_MANAGER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/trust-manager=*";

    String X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/x500-attribute-principal-decoder=*";
    String X500_SUBJECT_EVIDENCE_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS + "/x500-subject-evidence-decoder=*";
    String X509_SUBJECT_ALT_NAME_EVIDENCE_DECODER_ADDRESS = ELYTRON_SUBSYSTEM_ADDRESS
            + "/x509-subject-alt-name-evidence-decoder=*";

    // ------------------------------------------------------ templates (a-z)

    AddressTemplate ADD_PREFIX_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(ADD_PREFIX_ROLE_MAPPER_ADDRESS);
    AddressTemplate ADD_SUFFIX_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(ADD_SUFFIX_ROLE_MAPPER_ADDRESS);
    AddressTemplate AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_TEMPLATE = AddressTemplate
            .of(AGGREGATE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS);
    AddressTemplate AGGREGATE_EVIDENCE_DECODER_TEMPLATE = AddressTemplate.of(AGGREGATE_EVIDENCE_DECODER_ADDRESS);
    AddressTemplate AGGREGATE_PRINCIPAL_DECODER_TEMPLATE = AddressTemplate.of(AGGREGATE_PRINCIPAL_DECODER_ADDRESS);
    AddressTemplate AGGREGATE_PRINCIPAL_TRANSFORMER_TEMPLATE = AddressTemplate.of(AGGREGATE_PRINCIPAL_TRANSFORMER_ADDRESS);
    AddressTemplate AGGREGATE_PROVIDERS_TEMPLATE = AddressTemplate.of(AGGREGATE_PROVIDERS_ADDRESS);
    AddressTemplate AGGREGATE_REALM_TEMPLATE = AddressTemplate.of(AGGREGATE_REALM_ADDRESS);
    AddressTemplate AGGREGATE_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(AGGREGATE_ROLE_MAPPER_ADDRESS);
    AddressTemplate AGGREGATE_SASL_SERVER_FACTORY_TEMPLATE = AddressTemplate.of(AGGREGATE_SASL_SERVER_FACTORY_ADDRESS);
    AddressTemplate AGGREGATE_SECURITY_EVENT_LISTENER_TEMPLATE = AddressTemplate.of(AGGREGATE_SECURITY_EVENT_LISTENER_ADDRESS);
    AddressTemplate AUTHENTICATION_CONFIGURATION_TEMPLATE = AddressTemplate.of(AUTHENTICATION_CONFIGURATION_ADDRESS);
    AddressTemplate AUTHENTICATION_CONTEXT_TEMPLATE = AddressTemplate.of(AUTHENTICATION_CONTEXT_ADDRESS);

    AddressTemplate CACHING_REALM_TEMPLATE = AddressTemplate.of(CACHING_REALM_ADDRESS);
    AddressTemplate CHAINED_PRINCIPAL_TRANSFORMER_TEMPLATE = AddressTemplate.of(CHAINED_PRINCIPAL_TRANSFORMER_ADDRESS);
    AddressTemplate CERTIFICATE_AUTHORITY_TEMPLATE = AddressTemplate.of(CERTIFICATE_AUTHORITY_ADDRESS);
    AddressTemplate CERTIFICATE_AUTHORITY_ACCOUNT_TEMPLATE = AddressTemplate.of(CERTIFICATE_AUTHORITY_ACCOUNT_ADDRESS);
    AddressTemplate CLIENT_SSL_CONTEXT_TEMPLATE = AddressTemplate.of(CLIENT_SSL_CONTEXT_ADDRESS);
    AddressTemplate CONCATENATING_PRINCIPAL_DECODER_TEMPLATE = AddressTemplate.of(CONCATENATING_PRINCIPAL_DECODER_ADDRESS);
    AddressTemplate CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_TEMPLATE = AddressTemplate
            .of(CONFIGURABLE_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS);
    AddressTemplate CONFIGURABLE_SASL_SERVER_FACTORY_TEMPLATE = AddressTemplate.of(CONFIGURABLE_SASL_SERVER_FACTORY_ADDRESS);
    AddressTemplate CONSTANT_PERMISSION_MAPPER_TEMPLATE = AddressTemplate.of(CONSTANT_PERMISSION_MAPPER_ADDRESS);
    AddressTemplate CONSTANT_PRINCIPAL_DECODER_TEMPLATE = AddressTemplate.of(CONSTANT_PRINCIPAL_DECODER_ADDRESS);
    AddressTemplate CONSTANT_PRINCIPAL_TRANSFORMER_TEMPLATE = AddressTemplate.of(CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS);
    AddressTemplate CONSTANT_REALM_MAPPER_TEMPLATE = AddressTemplate.of(CONSTANT_REALM_MAPPER_ADDRESS);
    AddressTemplate CONSTANT_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(CONSTANT_ROLE_MAPPER_ADDRESS);
    AddressTemplate CREDENTIAL_STORE_TEMPLATE = AddressTemplate.of(CREDENTIAL_STORE_ADDRESS);
    AddressTemplate CUSTOM_CREDENTIAL_SECURITY_FACTORY_TEMPLATE = AddressTemplate
            .of(CUSTOM_CREDENTIAL_SECURITY_FACTORY_ADDRESS);
    AddressTemplate CUSTOM_EVIDENCE_DECODER_TEMPLATE = AddressTemplate.of(CUSTOM_EVIDENCE_DECODER_ADDRESS);
    AddressTemplate CUSTOM_MODIFIABLE_REALM_TEMPLATE = AddressTemplate.of(CUSTOM_MODIFIABLE_REALM_ADDRESS);
    AddressTemplate CUSTOM_PERMISSION_MAPPER_TEMPLATE = AddressTemplate.of(CUSTOM_PERMISSION_MAPPER_ADDRESS);
    AddressTemplate CUSTOM_PRINCIPAL_DECODER_TEMPLATE = AddressTemplate.of(CUSTOM_PRINCIPAL_DECODER_ADDRESS);
    AddressTemplate CUSTOM_PRINCIPAL_TRANSFORMER_TEMPLATE = AddressTemplate.of(CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS);
    AddressTemplate CUSTOM_REALM_MAPPER_TEMPLATE = AddressTemplate.of(CUSTOM_REALM_MAPPER_ADDRESS);
    AddressTemplate CUSTOM_REALM_TEMPLATE = AddressTemplate.of(CUSTOM_REALM_ADDRESS);
    AddressTemplate CUSTOM_ROLE_DECODER_TEMPLATE = AddressTemplate.of(CUSTOM_ROLE_DECODER_ADDRESS);
    AddressTemplate CUSTOM_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(CUSTOM_ROLE_MAPPER_ADDRESS);
    AddressTemplate CUSTOM_SECURITY_EVENT_LISTENER_TEMPLATE = AddressTemplate.of(CUSTOM_SECURITY_EVENT_LISTENER_ADDRESS);

    AddressTemplate DIR_CONTEXT_TEMPLATE = AddressTemplate.of(DIR_CONTEXT_ADDRESS);

    AddressTemplate EXPRESSION_TEMPLATE = AddressTemplate.of(EXPRESSION_ADDRESS);

    AddressTemplate FILE_AUDIT_LOG_TEMPLATE = AddressTemplate.of(FILE_AUDIT_LOG_ADDRESS);
    AddressTemplate FILESYSTEM_REALM_TEMPLATE = AddressTemplate.of(FILESYSTEM_REALM_ADDRESS);
    AddressTemplate FILTERING_KEY_STORE_TEMPLATE = AddressTemplate.of(FILTERING_KEY_STORE_ADDRESS);

    AddressTemplate HTTP_AUTHENTICATION_FACTORY_TEMPLATE = AddressTemplate.of(HTTP_AUTHENTICATION_FACTORY_ADDRESS);

    AddressTemplate IDENTITY_REALM_TEMPLATE = AddressTemplate.of(IDENTITY_REALM_ADDRESS);

    AddressTemplate JASPI_CONFIGURATION_TEMPLATE = AddressTemplate.of(JASPI_CONFIGURATION_ADDRESS);
    AddressTemplate JDBC_REALM_TEMPLATE = AddressTemplate.of(JDBC_REALM_ADDRESS);

    AddressTemplate KERBEROS_SECURITY_FACTORY_TEMPLATE = AddressTemplate.of(KERBEROS_SECURITY_FACTORY_ADDRESS);
    AddressTemplate KEY_MANAGER_TEMPLATE = AddressTemplate.of(KEY_MANAGER_ADDRESS);
    AddressTemplate KEY_STORE_TEMPLATE = AddressTemplate.of(KEY_STORE_ADDRESS);
    AddressTemplate KEY_STORE_REALM_TEMPLATE = AddressTemplate.of(KEY_STORE_REALM_ADDRESS);

    AddressTemplate LDAP_KEY_STORE_TEMPLATE = AddressTemplate.of(LDAP_KEY_STORE_ADDRESS);
    AddressTemplate LDAP_REALM_TEMPLATE = AddressTemplate.of(LDAP_REALM_ADDRESS);
    AddressTemplate LOGICAL_PERMISSION_MAPPER_TEMPLATE = AddressTemplate.of(LOGICAL_PERMISSION_MAPPER_ADDRESS);
    AddressTemplate LOGICAL_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(LOGICAL_ROLE_MAPPER_ADDRESS);

    AddressTemplate MAPPED_REGEX_REALM_MAPPER_TEMPLATE = AddressTemplate.of(MAPPED_REGEX_REALM_MAPPER_ADDRESS);
    AddressTemplate MAPPED_ROLE_MAPPER_TEMPLATE = AddressTemplate.of(MAPPED_ROLE_MAPPER_ADDRESS);
    AddressTemplate MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_TEMPLATE = AddressTemplate
            .of(MECHANISM_PROVIDER_FILTERING_SASL_SERVER_FACTORY_ADDRESS);

    AddressTemplate PERIODIC_ROTATING_FILE_AUDIT_LOG_TEMPLATE = AddressTemplate.of(PERIODIC_ROTATING_FILE_AUDIT_LOG_ADDRESS);
    AddressTemplate PERMISSION_SET_TEMPLATE = AddressTemplate.of(PERMISSION_SET_ADDRESS);
    AddressTemplate POLICY_TEMPLATE = AddressTemplate.of(POLICY_ADDRESS);
    AddressTemplate PROPERTIES_REALM_TEMPLATE = AddressTemplate.of(PROPERTIES_REALM_ADDRESS);
    AddressTemplate PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_TEMPLATE = AddressTemplate
            .of(PROVIDER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS);
    AddressTemplate PROVIDER_LOADER_TEMPLATE = AddressTemplate.of(PROVIDER_LOADER_ADDRESS);
    AddressTemplate PROVIDER_SASL_SERVER_FACTORY_TEMPLATE = AddressTemplate.of(PROVIDER_SASL_SERVER_FACTORY_ADDRESS);

    AddressTemplate REGEX_PRINCIPAL_TRANSFORMER_TEMPLATE = AddressTemplate.of(REGEX_PRINCIPAL_TRANSFORMER_ADDRESS);
    AddressTemplate REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_TEMPLATE = AddressTemplate.of(
            REGEX_VALIDATING_PRINCIPAL_TRANSFORMER_ADDRESS);

    AddressTemplate SASL_AUTHENTICATION_FACTORY_TEMPLATE = AddressTemplate.of(SASL_AUTHENTICATION_FACTORY_ADDRESS);
    AddressTemplate SECRET_KEY_CREDENTIAL_STORE_TEMPLATE = AddressTemplate.of(SECRET_KEY_CREDENTIAL_STORE_ADDRESS);
    AddressTemplate SECURITY_DOMAIN_TEMPLATE = AddressTemplate.of(SECURITY_DOMAIN_ADDRESS);
    AddressTemplate SERVER_SSL_CONTEXT_TEMPLATE = AddressTemplate.of(SERVER_SSL_CONTEXT_ADDRESS);
    AddressTemplate SERVER_SSL_SNI_CONTEXT_TEMPLATE = AddressTemplate.of(SERVER_SSL_SNI_CONTEXT_ADDRESS);
    AddressTemplate SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_TEMPLATE = AddressTemplate
            .of(SERVICE_LOADER_HTTP_SERVER_MECHANISM_FACTORY_ADDRESS);
    AddressTemplate SERVICE_LOADER_SASL_SERVER_FACTORY_TEMPLATE = AddressTemplate
            .of(SERVICE_LOADER_SASL_SERVER_FACTORY_ADDRESS);
    AddressTemplate SIMPLE_PERMISSION_MAPPER_TEMPLATE = AddressTemplate.of(SIMPLE_PERMISSION_MAPPER_ADDRESS);
    AddressTemplate SIMPLE_REGEX_REALM_MAPPER_TEMPLATE = AddressTemplate.of(SIMPLE_REGEX_REALM_MAPPER_ADDRESS);
    AddressTemplate SIMPLE_ROLE_DECODER_TEMPLATE = AddressTemplate.of(SIMPLE_ROLE_DECODER_ADDRESS);
    AddressTemplate SIZE_ROTATING_FILE_AUDIT_LOG_TEMPLATE = AddressTemplate.of(SIZE_ROTATING_FILE_AUDIT_LOG_ADDRESS);
    AddressTemplate SYSLOG_AUDIT_LOG_TEMPLATE = AddressTemplate.of(SYSLOG_AUDIT_LOG_ADDRESS);

    AddressTemplate TOKEN_REALM_TEMPLATE = AddressTemplate.of(TOKEN_REALM_ADDRESS);
    AddressTemplate TRUST_MANAGER_TEMPLATE = AddressTemplate.of(TRUST_MANAGER_ADDRESS);

    AddressTemplate X500_ATTRIBUTE_PRINCIPAL_DECODER_TEMPLATE = AddressTemplate.of(X500_ATTRIBUTE_PRINCIPAL_DECODER_ADDRESS);
    AddressTemplate X500_SUBJECT_EVIDENCE_DECODER_TEMPLATE = AddressTemplate.of(X500_SUBJECT_EVIDENCE_DECODER_ADDRESS);
    AddressTemplate X509_SUBJECT_ALT_NAME_EVIDENCE_DECODER_TEMPLATE = AddressTemplate
            .of(X509_SUBJECT_ALT_NAME_EVIDENCE_DECODER_ADDRESS);
    // @formatter:on
}
