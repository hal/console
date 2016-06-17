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
package org.jboss.hal.resources;

import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ExternalTextResource;

/**
 * @author Harald Pehl
 */
public interface Previews extends ClientBundleWithLookup {

    @Source("previews/configuration/domain.html")
    ExternalTextResource configurationDomain();

    @Source("previews/configuration/standalone.html")
    ExternalTextResource configurationStandalone();

    @Source("previews/deployments/content-repository.html")
    ExternalTextResource contentRepository();

    @Source("previews/configuration/subsystems/datasources.html")
    ExternalTextResource datasources();

    @Source("previews/configuration/subsystems/datasources-only.html")
    ExternalTextResource datasourcesOnly();

    @Source("previews/subsystems/deployment-scanner.html")
    ExternalTextResource deploymentScanner();

    @Source("previews/subsystems/ee.html")
    ExternalTextResource ee();

    @Source("previews/subsystems/io.html")
    ExternalTextResource io();

    @Source("previews/runtime/hosts.html")
    ExternalTextResource hosts();

    @Source("previews/subsystems/logging.html")
    ExternalTextResource logging();

    @Source("previews/subsystems/logging-configuration.html")
    ExternalTextResource loggingConfiguration();

    @Source("previews/subsystems/logging-profiles.html")
    ExternalTextResource loggingProfiles();

    @Source("previews/subsystems/mail.html")
    ExternalTextResource mail();

    @Source("previews/deployments/domain.html")
    ExternalTextResource deploymentsDomain();

    @Source("previews/deployments/server-groups.html")
    ExternalTextResource deploymentsServerGroups();

    @Source("previews/deployments/standalone.html")
    ExternalTextResource deploymentsStandalone();

    @Source("previews/configuration/interfaces.html")
    ExternalTextResource interfaces();

    @Source("previews/configuration/subsystems/jdbc-drivers.html")
    ExternalTextResource jdbcDrivers();

    @Source("previews/configuration/paths.html")
    ExternalTextResource paths();

    @Source("previews/configuration/profiles.html")
    ExternalTextResource profiles();

    @Source("previews/runtime/domain.html")
    ExternalTextResource runtimeDomain();

    @Source("previews/runtime/standalone.html")
    ExternalTextResource runtimeStandalone();

    @Source("previews/runtime/server-groups.html")
    ExternalTextResource runtimeServerGroups();

    @Source("previews/configuration/socket-bindings.html")
    ExternalTextResource socketBindings();

    @Source("previews/configuration/subsystems.html")
    ExternalTextResource subsystems();

    @Source("previews/configuration/system-properties.html")
    ExternalTextResource systemProperties();
}
