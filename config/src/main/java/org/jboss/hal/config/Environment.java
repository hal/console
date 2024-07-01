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
package org.jboss.hal.config;

import java.util.List;

/** Instance holding information about the console and its environment. */
public interface Environment {

    /** @return the name. */
    String getName();

    void setName(String name);

    /** @return the organization. */
    String getOrganization();

    void setOrganization(String organization);

    /** @return the HAL version. */
    Version getHalVersion();

    Build getHalBuild();

    List<String> getLocales();

    /** @return information about the server instance. */
    InstanceInfo getInstanceInfo();

    void setInstanceInfo(String productName, String productVersion, String releaseName, String releaseVersion);

    OperationMode getOperationMode();

    /** @return true for standalone mode, false otherwise. */
    boolean isStandalone();

    void setOperationMode(OperationMode operationMode);

    /**
     * @return whether the naming in domain mode uses primary/secondary or master/slave.
     */
    boolean isPrimarySecondary();

    void setPrimarySecondary(boolean primarySecondary);

    /** @return the name of the domain controller (DC). */
    String getDomainController();

    void setDomainController(String domainController);

    /** @return the management model version. */
    Version getManagementVersion();

    void setManagementVersion(Version version);

    AccessControlProvider getAccessControlProvider();

    void setAccessControlProvider(AccessControlProvider accessControlProvider);

    void setSingleSignOn(boolean sso);

    /** @return true if SSO is used, false otherwise. */
    boolean isSingleSignOn();

    /** @return the standard and scoped roles. */
    Roles getRoles();

    /** @return whether HAL is run in development mode */
    boolean isDevMode();

    /** @return whether HAL is run in production mode */
    boolean isProductionMode();

    /**
     * @return the stability level of the server.
     */
    StabilityLevel getStabilityLevel();

    void setStabilityLevel(StabilityLevel stabilityLevel);

    /**
     * @return an array of all the stability levels supported by this server.
     */
    StabilityLevel[] getStabilityLevels();

    void setStabilityLevels(StabilityLevel[] stabilityLevels);

    /**
     * @return {@code true} if the {@linkplain #getStabilityLevel() stability level} is lower than
     *         {@link Build#defaultStability}, {@code false} otherwise.
     */
    boolean highlightStabilityLevel();

    /**
     * @return {@code true} if the specified stability level is lower than {@link Build#defaultStability}, {@code false}
     *         otherwise.
     */
    boolean highlightStabilityLevel(StabilityLevel stabilityLevel);
}
