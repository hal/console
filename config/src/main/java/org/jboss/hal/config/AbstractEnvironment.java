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

import org.jboss.hal.config.rebind.EnvironmentGenerator;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.config.OperationMode.EMBEDDED;
import static org.jboss.hal.config.OperationMode.SELF_CONTAINED;
import static org.jboss.hal.config.OperationMode.STANDALONE;

/**
 * A base implementation for the environment.
 *
 * @see EnvironmentGenerator
 */
public abstract class AbstractEnvironment implements Environment {

    private final org.jboss.hal.config.Version halVersion;
    private final Build halBuild;
    private final List<String> locales;
    private final InstanceInfo instanceInfo;
    private final Roles roles;
    private final boolean devMode;
    private final boolean productionMode;
    private String name;
    private String organization;
    private OperationMode operationMode;
    private String domainController;
    private Version managementVersion;
    private AccessControlProvider accessControlProvider;
    private boolean sso;
    private boolean patchingEnabled;

    AbstractEnvironment(String halVersion, String halBuild, List<String> locales) {
        this.halVersion = org.jboss.hal.config.Version.parseVersion(halVersion);
        this.halBuild = Build.parse(halBuild);
        this.locales = locales;
        this.instanceInfo = new InstanceInfo();
        this.roles = new Roles();
        this.devMode = System.getProperty("superdevmode", "").equals("on");
        String pathname = window.location.getPathname();
        this.productionMode = pathname.equals("/") || pathname.endsWith("index.html");
        this.operationMode = STANDALONE;
        this.name = null;
        this.organization = null;
        this.domainController = null;
        this.managementVersion = Version.EMPTY_VERSION;
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
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getOrganization() {
        return organization;
    }

    @Override
    public void setOrganization(String organization) {
        this.organization = organization;
    }

    @Override
    public void setInstanceInfo(String productName, String productVersion, String releaseName, String releaseVersion) {
        instanceInfo.update(productName, productVersion, releaseName, releaseVersion);
    }

    @Override
    public OperationMode getOperationMode() {
        return operationMode;
    }

    @Override
    public boolean isStandalone() {
        return operationMode == SELF_CONTAINED || operationMode == STANDALONE || operationMode == EMBEDDED;
    }

    @Override
    public void setOperationMode(OperationMode operationMode) {
        this.operationMode = operationMode;
    }

    @Override
    public String getDomainController() {
        return domainController;
    }

    @Override
    public void setDomainController(String domainController) {
        this.domainController = domainController;
    }

    @Override
    public Version getManagementVersion() {
        return managementVersion;
    }

    @Override
    public void setManagementVersion(Version version) {
        managementVersion = version;
    }

    @Override
    public AccessControlProvider getAccessControlProvider() {
        return accessControlProvider;
    }

    @Override
    public void setAccessControlProvider(AccessControlProvider accessControlProvider) {
        this.accessControlProvider = accessControlProvider;
    }

    @Override
    public Roles getRoles() {
        return roles;
    }

    @Override
    public boolean isSingleSignOn() {
        return sso;
    }

    @Override
    public void setSingleSignOn(boolean sso) {
        this.sso = sso;
    }

    @Override
    public boolean isDevMode() {
        return devMode;
    }

    @Override
    public boolean isProductionMode() {
        return productionMode;
    }

    @Override
    public boolean isPatchingEnabled() {
        return patchingEnabled;
    }

    @Override
    public void setPatchingEnabled(boolean enabled) {
        this.patchingEnabled = enabled;
    }

    @Override
    public String toString() {
        return "Environment(HAL " + halVersion + ", " + instanceInfo + ", management version " + managementVersion +
                ", " + accessControlProvider.name().toLowerCase() + " provider)";
    }
}
