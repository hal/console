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
package org.jboss.hal.client.bootstrap;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.client.bootstrap.endpoint.EndpointStorage;
import org.jboss.hal.client.bootstrap.functions.LoadSettings;
import org.jboss.hal.client.bootstrap.functions.BootstrapSteps;
import org.jboss.hal.client.bootstrap.functions.FindDomainController;
import org.jboss.hal.client.bootstrap.functions.ReadAuthentication;
import org.jboss.hal.client.bootstrap.functions.ReadEnvironment;
import org.jboss.hal.client.bootstrap.functions.ReadExtensions;
import org.jboss.hal.client.bootstrap.functions.RegisterStaticCapabilities;
import org.jboss.hal.spi.GinModule;

@GinModule
public class BootstrapModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(EndpointManager.class).in(Singleton.class);
        bind(EndpointStorage.class).in(Singleton.class);

        bind(LoadSettings.class).in(Singleton.class);
        bind(ReadEnvironment.class).in(Singleton.class);
        bind(ReadExtensions.class).in(Singleton.class);
        bind(ReadAuthentication.class).in(Singleton.class);
        bind(FindDomainController.class).in(Singleton.class);
        bind(RegisterStaticCapabilities.class).in(Singleton.class);
        bind(BootstrapSteps.class).in(Singleton.class);
    }
}
