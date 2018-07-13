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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.meta.capabilitiy.Capabilities;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;

import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_PROFILE;

/** Reads the capabilities from the capability registry. Depends on {@link FindDomainController}. */
public class RegisterStaticCapabilities implements BootstrapTask {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(RegisterStaticCapabilities.class);

    private final Environment environment;
    private final Capabilities capabilities;

    @Inject
    public RegisterStaticCapabilities(Environment environment, Capabilities capabilities) {
        this.environment = environment;
        this.capabilities = capabilities;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public Completable call() {
        if (!ManagementModel.supportsCapabilitiesRegistry(environment.getManagementVersion())) {
            logger.debug("Register static capabilities");

            // Selected capabilities from https://github.com/wildfly/wildfly-capabilities
            capabilities.register("org.wildfly.data-source",
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=datasources/data-source=*"));

            capabilities.register("org.wildfly.io.buffer-pool",
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=io/buffer-pool=*"));

            capabilities.register("org.wildfly.io.worker",
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=io/worker=*"));

            // Lists local and remote outbound socket bindings of all socket binding groups
            capabilities.register("org.wildfly.network.outbound-socket-binding",
                    AddressTemplate.of("/socket-binding-group=*/local-destination-outbound-socket-binding=*"),
                    AddressTemplate.of("/socket-binding-group=*/remote-destination-outbound-socket-binding=*"));

            // Lists all socket bindings of all socket binding groups
            capabilities.register("org.wildfly.network.socket-binding",
                    AddressTemplate.of("/socket-binding-group=*/socket-binding=*"));

            // Well-known capabilities not (yet) on https://github.com/wildfly/wildfly-capabilities
            capabilities.register("org.wildfly.network.interface", AddressTemplate.of("/interface=*"));

            capabilities.register("org.wildfly.domain.profile", AddressTemplate.of("/profile=*"));

            capabilities.register("org.wildfly.domain.server-group", AddressTemplate.of("/server-group=*"));

            capabilities.register("org.wildfly.domain.socket-binding-group",
                    AddressTemplate.of("/socket-binding-group=*"));

            capabilities.register("org.wildfly.batch.job.repository",
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=batch-jberet/in-memory-job-repository=*"),
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=batch-jberet/jdbc-job-repository=*"));

            capabilities.register("org.wildfly.batch.thread.pool",
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=batch-jberet/thread-pool=*"));

            capabilities.register("org.wildfly.clustering.singleton.policy",
                    AddressTemplate.of(SELECTED_PROFILE, "subsystem=singleton/singleton-policy=*"));
        }
        return Completable.complete();
    }
}
