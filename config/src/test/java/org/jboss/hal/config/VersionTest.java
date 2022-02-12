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

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    @Test
    public void parseEap() {
        String input = "3.0.2.Final-redhat-1";
        Version version = org.jboss.hal.config.Version.parseVersion(input);
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 2);
        Assert.assertEquals(version.getQualifier(), "Final-redhat-1");
    }

    @Test
    public void parseSnapshot() {
        String input = "3.0.1-SNAPSHOT";
        Version version = org.jboss.hal.config.Version.parseVersion(input);
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 1);
        Assert.assertEquals(version.getQualifier(), "SNAPSHOT");
    }

    @Test
    public void parseOnlyNumbers() {
        String input = "3.0.1";
        Version version = org.jboss.hal.config.Version.parseVersion(input);
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 1);
        Assert.assertEquals(version.getQualifier(), "");
    }

    @Test
    public void parseFinalVersion() {
        String input = "3.0.1.Final";
        Version version = org.jboss.hal.config.Version.parseVersion(input);
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 1);
        Assert.assertEquals(version.getQualifier(), "Final");
    }

    @Test
    public void newSnapshot() {
        Version version = new Version(3, 0, 1, "SNAPSHOT");
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 1);
        Assert.assertEquals(version.getQualifier(), "SNAPSHOT");
    }

    @Test
    public void newOnlyNumbers() {
        Version version = new Version(3, 0, 1);
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 1);
        Assert.assertEquals(version.getQualifier(), "");
    }

    @Test
    public void newFinalVersion() {
        String input = "3.0.1.Final";
        Version version = new Version(input);
        Assert.assertEquals(version.getMajor(), 3);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 1);
        Assert.assertEquals(version.getQualifier(), "Final");
    }

    @Test
    public void emptyVersion() {
        Version version = Version.parseVersion("");
        Assert.assertEquals(version.getMajor(), 0);
        Assert.assertEquals(version.getMinor(), 0);
        Assert.assertEquals(version.getMicro(), 0);
        Assert.assertEquals(version.getQualifier(), "");
    }

}