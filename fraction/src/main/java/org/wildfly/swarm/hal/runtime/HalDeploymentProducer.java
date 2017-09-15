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
package org.wildfly.swarm.hal.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.hal.HalFraction;
import org.wildfly.swarm.spi.api.ArtifactLookup;
import org.wildfly.swarm.undertow.WARArchive;

@ApplicationScoped
public class HalDeploymentProducer {

    private static final String GAV = "org.jboss.hal:hal-console:war:*";
    private static final String DEPLOYMENT = "hal-console.war";
    private static final String INTERNAL_CONTEXT_ROOT = "/hal";

    @Inject ArtifactLookup lookup;
    @Inject @Any HalFraction fraction;

    @Produces
    public Archive halConsole() throws Exception {
        WARArchive war = ShrinkWrap.create(WARArchive.class, DEPLOYMENT)
                .setContextRoot(fraction.context());
        lookup.artifact(GAV, DEPLOYMENT)
                .getContent(path -> path.get().startsWith(INTERNAL_CONTEXT_ROOT))
                .forEach((path, content) -> {
                    if (content.getAsset() != null) {
                        String relocated = path.get().substring(INTERNAL_CONTEXT_ROOT.length());
                        war.add(content.getAsset(), relocated);
                    }
                });
        return war;
    }
}
