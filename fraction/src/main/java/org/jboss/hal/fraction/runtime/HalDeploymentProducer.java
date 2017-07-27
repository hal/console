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
package org.jboss.hal.fraction.runtime;

import java.net.URL;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.hal.fraction.HalFraction;
import org.jboss.hal.fraction.HalProperties;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.undertow.WARArchive;

public class HalDeploymentProducer {

    @Inject
    private ArtifactLookup lookup;

    @Inject @Any
    private HalFraction fraction;

    @Inject
    @ConfigurationValue(HalProperties.CONTEXT)
    private String context;

    @Produces
    public Archive managementConsoleWar() throws Exception {
        if (this.context == null) {
            this.context = fraction.contextRoot();
        }

        Module module = Module.getBootModuleLoader().loadModule(ModuleIdentifier.create("org.jboss.hal"));
        URL resource = module.getExportedResource("hal-console-resources.jar");

        WARArchive war = ShrinkWrap.create(WARArchive.class, "hal-console.war");
        war.as(ZipImporter.class).importFrom(resource.openStream());
        war.setContextRoot(this.context);

        return war;
    }
}
