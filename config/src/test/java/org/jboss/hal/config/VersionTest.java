package org.jboss.hal.config;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

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