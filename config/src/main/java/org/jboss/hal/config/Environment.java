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

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.config.semver.Version;

/**
 * Instance holding information about the console and its environment.
 * An instance of this interface is generated using deferred binding.
 * Most of the information is updated by the bootstrap code of the console.
 */
@JsType
public interface Environment {

    /**
     * The HAL version taken form the Maven POM.
     */
    @JsProperty
    Version getHalVersion();

    @JsIgnore
    Build getHalBuild();

    /**
     * The configured locales in the GWT module.
     *
     * @return the list of supported locales
     */
    @JsIgnore
    List<String> getLocales();

    @JsProperty
    InstanceInfo getInstanceInfo();

    @JsIgnore
    void setInstanceInfo(String productName, String productVersion,
            String releaseName, String releaseVersion,
            String serverName);

    @JsProperty
    OperationMode getOperationMode();

    @JsProperty
    boolean isStandalone();

    @JsIgnore
    void setOperationMode(OperationMode operationMode);

    @JsProperty
    String getDomainController();

    @JsIgnore
    void setDomainController(String domainController);

    @JsProperty
    Version getManagementVersion();

    @JsIgnore
    void setManagementVersion(Version version);

    @JsProperty
    AccessControlProvider getAccessControlProvider();

    @JsIgnore
    void setAccessControlProvider(AccessControlProvider accessControlProvider);

    @JsProperty
    boolean isSingleSignOn();

    @JsProperty
    Roles getRoles();
}
