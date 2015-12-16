/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.config;

import com.google.common.base.Joiner;
import org.jboss.hal.config.rebind.EnvironmentGenerator;
import org.jboss.hal.config.semver.Version;

import java.util.List;

import static org.jboss.hal.config.OperationMode.DOMAIN;
import static org.jboss.hal.config.OperationMode.STANDALONE;
import static org.jboss.hal.config.InstanceInfo.WILDFLY;

/**
 * A base implementation for the environment.
 *
 * @see EnvironmentGenerator
 * @author Harald Pehl
 */
@SuppressWarnings("unused")
public abstract class AbstractEnvironment implements Environment {

    private final Version halVersion;
    private final List<String> locales;
    private final InstanceInfo instanceInfo;
    private Version latestVersion;
    private OperationMode operationMode;
    private Version managementVersion;

    protected AbstractEnvironment(final String halVersion, final List<String> locales) {
        this.halVersion = Version.valueOf(halVersion);
        this.latestVersion = null;
        this.locales = locales;
        this.instanceInfo = WILDFLY;
        this.operationMode = STANDALONE;
        this.managementVersion = Version.forIntegers(0, 0, 0);
    }

    @Override
    public Version getHalVersion() {
        return halVersion;
    }

    @Override
    public Version getLatestHalVersion() {
        return latestVersion;
    }

    @Override
    public void setLatestHalVersion(final Version latestVersion) {
        this.latestVersion = latestVersion;
    }

    @Override
    public boolean halUpdateAvailable() {
        return latestVersion != null && latestVersion.greaterThan(halVersion);
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
        return operationMode == STANDALONE;
    }

    @Override
    public void setOperationMode(final String launchType) {
        operationMode = (STANDALONE.name().equals(launchType)) ? STANDALONE : DOMAIN;
    }

    @Override
    public Version getManagementVersion() {
        return managementVersion;
    }

    @Override
    public void setManagementVersion(final String major, final String micro, final String minor) {
        managementVersion = Version.valueOf(Joiner.on('.').join(major, micro, minor));
    }

    @Override
    public String toString() {
        return "Environment{HAL " + halVersion + ", " + instanceInfo + ", management version " + managementVersion + '}';
    }
}
