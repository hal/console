/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.client.bootstrap;

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.bootstrap.endpoint.EndpointSelection;
import org.jboss.hal.client.bootstrap.functions.BootstrapFunctions;
import org.jboss.hal.core.flow.FunctionContext;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class HalBootstrapper implements Bootstrapper {

    private final PlaceManager placeManager;
    private final EndpointSelection endpointSelection;
    private final BootstrapFunctions bootstrapFunctions;

    @Inject
    public HalBootstrapper(final PlaceManager placeManager,
            final EndpointSelection endpointSelection,
            final BootstrapFunctions bootstrapFunctions) {
        this.placeManager = placeManager;
        this.endpointSelection = endpointSelection;
        this.bootstrapFunctions = bootstrapFunctions;
    }

    @Override
    public void onBootstrap() {
        LoadingPanel.get().on();
        new Async<FunctionContext>(Progress.NOOP).waterfall(
                new FunctionContext(), new BootstrapOutcome(placeManager), bootstrapFunctions.functions());
/*
        endpointSelection.select(
                () -> {
                    LoadingPanel.get().on();
                    new Async<FunctionContext>(Progress.NOOP).waterfall(
                            new FunctionContext(), new BootstrapOutcome(placeManager), bootstrapFunctions.functions());
                });
*/
    }
}
