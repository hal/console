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

import javax.inject.Inject;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.AccessControlProvider;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.User;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.dmr.model.ResourceAddress;

import static org.jboss.hal.config.AccessControlProvider.SIMPLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROVIDER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;

/**
 * Reads the authentication provider (simple or RBAC). This is implemented in an extra bootstrap function, because the
 * operation might fail in some corner cases (e.g. when the current user is a host scoped role scoped to a slave host).
 * In this case the provider is set to {@link org.jboss.hal.config.AccessControlProvider#SIMPLE}.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ReadAuthenticationProvider implements BootstrapFunction {

    private final Dispatcher dispatcher;
    private final Environment environment;
    private final User user;

    @Inject
    public ReadAuthenticationProvider(
            Dispatcher dispatcher,
            Environment environment,
            User user) {
        this.dispatcher = dispatcher;
        this.environment = environment;
        this.user = user;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        logStart();

        ResourceAddress address = new ResourceAddress().add("core-service", "management")
                .add("access", "authorization");
        Operation operation = new Operation.Builder(READ_ATTRIBUTE_OPERATION, address).param(NAME, PROVIDER).build();
        dispatcher.executeInFunction(control, operation,
                result -> {
                    AccessControlProvider accessControlProvider =
                            asEnumValue(result, AccessControlProvider::valueOf, SIMPLE);
                    environment.setAccessControlProvider(accessControlProvider);

                    logDone();
                    control.proceed();
                },
                (op, failure) -> {
                    logger.error(
                            "{}: Unable to read authentication provider (insufficient rights?). Use {} as default.",
                            name(), AccessControlProvider.SIMPLE.name());

                    logDone();
                    control.proceed();
                });
    }

    @Override
    public String name() {
        return "Bootstrap[ReadAuthenticationProvider]";
    }
}
