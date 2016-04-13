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

    @Source("previews/configuration-domain.html")
    ExternalTextResource configurationDomain();

    @Source("previews/configuration-standalone.html")
    ExternalTextResource configurationStandalone();

    @Source("previews/subsystems/datasources.html")
    ExternalTextResource datasources();

    @Source("previews/deployments.html")
    ExternalTextResource deployments();

    @Source("previews/interfaces.html")
    ExternalTextResource interfaces();

    @Source("previews/paths.html")
    ExternalTextResource paths();

    @Source("previews/profiles.html")
    ExternalTextResource profiles();

    @Source("previews/runtime-domain.html")
    ExternalTextResource runtimeDomain();

    @Source("previews/runtime-standalone.html")
    ExternalTextResource runtimeStandalone();

    @Source("previews/socket-bindings.html")
    ExternalTextResource socketBindings();

    @Source("previews/subsystems.html")
    ExternalTextResource subsystems();

    @Source("previews/system-properties.html")
    ExternalTextResource systemProperties();
}
