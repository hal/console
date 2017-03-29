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
package org.jboss.hal.config;

import java.util.List;

import org.jboss.hal.config.rebind.EnvironmentGenerator;
import org.jboss.hal.config.semver.Version;

import static org.jboss.hal.config.OperationMode.SELF_CONTAINED;
import static org.jboss.hal.config.OperationMode.STANDALONE;

/**
 * A base implementation for the environment.
 *
 * @author Harald Pehl
 * @see EnvironmentGenerator
 */
@SuppressWarnings("unused")
public abstract class AbstractEnvironment implements Environment {

    private final Version halVersion;
    private final Build halBuild;
    private final List<String> locales;
    private final InstanceInfo instanceInfo;
    private final Roles roles;
    private OperationMode operationMode;
    private String domainController;
    private Version managementVersion;
    private AccessControlProvider accessControlProvider;

    protected AbstractEnvironment(final String halVersion, final String halBuild, final List<String> locales) {
        this.halVersion = Version.valueOf(halVersion);
        this.halBuild = Build.parse(halBuild);
        this.locales = locales;
        this.instanceInfo = new InstanceInfo();
        this.roles = new Roles();
        this.operationMode = STANDALONE;
        this.domainController = null;
        this.managementVersion = Version.forIntegers(0, 0, 0);
        this.accessControlProvider = AccessControlProvider.SIMPLE;
    }

    @Override
    public Version getHalVersion() {
        return halVersion;
    }

    @Override
    public Build getHalBuild() {
        return halBuild;
    }

    @Override
    public List<String> getLocales() {
        return locales;
    }

    @Override
    public InstanceInfo getInstanceInfo() {
        return instanceInfo;
    }

    @Override
    public void setInstanceInfo(final String productName, final String productVersion,
            final String releaseName, final String releaseVersion,
            final String serverName) {
        instanceInfo.update(productName, productVersion, releaseName, releaseVersion, serverName);
    }

    @Override
    public OperationMode getOperationMode() {
        return operationMode;
    }

    @Override
    public boolean isStandalone() {
        return operationMode == SELF_CONTAINED || operationMode == STANDALONE;
    }

    @Override
    public void setOperationMode(final OperationMode operationMode) {
        this.operationMode = operationMode;
    }

    @Override
    public String getDomainController() {
        return domainController;
    }

    @Override
    public void setDomainController(final String domainController) {
        this.domainController = domainController;
    }

    @Override
    public Version getManagementVersion() {
        return managementVersion;
    }

    @Override
    public void setManagementVersion(final Version version) {
        managementVersion = version;
    }

    @Override
    public AccessControlProvider getAccessControlProvider() {
        return accessControlProvider;
    }

    @Override
    public void setAccessControlProvider(final AccessControlProvider accessControlProvider) {
        this.accessControlProvider = accessControlProvider;
    }

    @Override
    public Roles getRoles() {
        return roles;
    }

    @Override
    public boolean isSingleSignOn() {
        return false; // not yet implemented
    }

    @Override
    public String toString() {
        return "Environment(HAL " + halVersion + ", " + instanceInfo + ", management version " + managementVersion +
                ", " + accessControlProvider.name().toLowerCase() + " provider)";
    }
}
