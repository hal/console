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

/**
 * @author Harald Pehl
 */
public enum InstanceInfo {

    WILDFLY("WildFly", "n/a", "WildFly", "na/", "n/a"),
    EAP("EAP", "n/a", "n/a", "EAP", "n/a");

    private String productName;
    private String productVersion;
    private String releaseName;
    private String releaseVersion;
    private String serverName;

    InstanceInfo(final String productName, final String productVersion,
            final String releaseName, final String releaseVersion,
            final String serverName) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.releaseName = releaseName;
        this.releaseVersion = releaseVersion;
        this.serverName = serverName;
    }

    public String productName() {
        return productName;
    }

    public String productVersion() {
        return productVersion;
    }

    public String releaseName() {
        return releaseName;
    }

    public String releaseVersion() {
        return releaseVersion;
    }

    public String serverName() {
        return serverName;
    }

    public void update(final String productName, final String productVersion,
            final String releaseName, final String releaseVersion,
            final String serverName) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.releaseName = releaseName;
        this.releaseVersion = releaseVersion;
        this.serverName = serverName;
    }
}
