package org.jboss.hal.config;

import org.jboss.hal.config.semver.Version;

import java.util.List;

/**
 * Instance holding information about the console and its environment.
 * An instance of this interface is generated using deferred binding.
 * Most of the information is updated by the bootstrap code of the console.
 */
public interface Environment {

    /**
     * The HAL version taken form the Maven POM.
     */
    Version getHalVersion();

    /**
     * The configured locales in the GWT module.
     *
     * @return the list of supported locales
     */
    List<String> getLocales();

    InstanceInfo getInstanceInfo();

    void setInstanceInfo(String productName, String productVersion,
            String releaseName, String releaseVersion,
            String serverName);

    OperationMode getOperationMode();

    boolean isStandalone();

    void setOperationMode(String launchType);

    Version getManagementVersion();

    void setManagementVersion(String major, String micro, String minor);
}
