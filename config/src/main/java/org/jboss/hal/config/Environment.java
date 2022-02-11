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
package org.jboss.hal.config;

import java.util.List;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Instance holding information about the console and its environment. */
@JsType
public interface Environment {

    /** @return the name. */
    @JsProperty
    String getName();

    @JsIgnore
    void setName(String name);

    /** @return the organization. */
    @JsProperty
    String getOrganization();

    @JsIgnore
    void setOrganization(String organization);

    /** @return the HAL version. */
    @JsProperty
    Version getHalVersion();

    @JsIgnore
    Build getHalBuild();

    @JsIgnore
    List<String> getLocales();

    /** @return information about the server instance. */
    @JsProperty
    InstanceInfo getInstanceInfo();

    @JsIgnore
    void setInstanceInfo(String productName, String productVersion, String releaseName, String releaseVersion);

    @JsIgnore
    OperationMode getOperationMode();

    /** @return true for standalone mode, false otherwise. */
    @JsProperty
    boolean isStandalone();

    @JsIgnore
    void setOperationMode(OperationMode operationMode);

    /** @return the name of the domain controller (DC). */
    @JsProperty
    String getDomainController();

    @JsIgnore
    void setDomainController(String domainController);

    /** @return the management model version. */
    @JsProperty
    Version getManagementVersion();

    @JsIgnore
    void setManagementVersion(Version version);

    @JsIgnore
    AccessControlProvider getAccessControlProvider();

    @JsIgnore
    void setAccessControlProvider(AccessControlProvider accessControlProvider);

    @JsIgnore
    void setSingleSignOn(boolean sso);

    /** @return true if SSO is used, false otherwise. */
    @JsProperty
    boolean isSingleSignOn();

    /** @return the standard and scoped roles. */
    @JsProperty
    Roles getRoles();

    /** @return whether HAL is run in development mode */
    boolean isDevMode();

    /** @return whether HAL is run in production mode */
    boolean isProductionMode();

    void setPatchingEnabled(boolean enabled);

    boolean isPatchingEnabled();
}
