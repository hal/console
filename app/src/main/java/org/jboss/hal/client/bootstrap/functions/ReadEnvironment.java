/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.hal.client.bootstrap.functions;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Random;
import org.jboss.hal.client.bootstrap.functions.HalBootstrapper.BootstrapExceptionCallback;
import org.jboss.hal.client.bootstrap.functions.HalBootstrapper.BootstrapFailedCallback;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Composite;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.flow.Control;
import org.jboss.hal.flow.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.model.ResourceAddress.ROOT;

/**
 * @author Heiko Braun
 * @date 5/19/11
 */
public class ReadEnvironment implements Function<BootstrapContext> {

    private static Logger logger = LoggerFactory.getLogger(ReadEnvironment.class);

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final User user;

    @Inject
    public ReadEnvironment(
            Dispatcher dispatcher,
            Environment environment,
            User user) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.user = user;
    }

    @Override
    public void execute(final Control<BootstrapContext> control) {

        logger.debug("Bootstrap[ReadEnvironment]: Start");
        final List<Operation> ops = new ArrayList<>();
        ops.add(new Operation.Builder(READ_RESOURCE_OPERATION, ROOT)
                .param("attributes-only", true)
                .param(INCLUDE_RUNTIME, true)
                .build());
        ops.add(new Operation.Builder("whoami", ROOT).param("verbose", true).build());

        Operation composite = new Composite(ops);
        dispatcher.execute(
                composite,
                payload -> {
                    logger.debug("Bootstrap[ReadEnvironment]: Parse root resource");

                    // server info
                    ModelNode node = payload.get("step-1").get(RESULT);
                    environment.setInstanceInfo(node.get("product-name").asString(),
                            node.get("product-version").asString(),
                            node.get("release-codename").asString(), node.get("release-version").asString(),
                            node.get("name").asString());

                    // operation mode
                    environment.setOperationMode(node.get("launch-type").asString());

                    // management version
                    environment.setManagementVersion(node.get("management-major-version").asString(),
                            node.get("management-micro-version").asString(),
                            node.get("management-minor-version").asString());

                    // user info
                    ModelNode whoami = payload.get("step-2").get(RESULT);
                    String username = whoami.get("identity").get("username").asString();
                    user.setName(username);
                    if (whoami.hasDefined("mapped-roles")) {
                        List<ModelNode> roles = whoami.get("mapped-roles").asList();
                        for (ModelNode role : roles) {
                            final String roleName = role.asString();
                            user.addRole(roleName);
                        }
                    }

                    // Simulate network latency
                    int wait = 333 + Random.nextInt(1111);
                    Scheduler.get().scheduleFixedDelay(() -> {
                        logger.info("Bootstrap[ReadEnvironment]: Done");
                        control.proceed();
                        return false;
                    }, wait);
                    //                    control.proceed();
                },
                new BootstrapFailedCallback(control),
                new BootstrapExceptionCallback(control));
    }
}
