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

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.client.bootstrap.endpoint.EndpointStorage;
import org.jboss.hal.client.bootstrap.functions.BootstrapFunctions;
import org.jboss.hal.client.bootstrap.functions.FinishBootstrap;
import org.jboss.hal.client.bootstrap.functions.ReadEnvironment;
import org.jboss.hal.spi.GinModule;

/**
 * @author Harald Pehl
 */
@GinModule
public class BootstrapModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(EndpointManager.class).in(Singleton.class);
        bind(EndpointStorage.class).in(Singleton.class);

        bind(ReadEnvironment.class).in(Singleton.class);
        bind(FinishBootstrap.class).in(Singleton.class);
        bind(BootstrapFunctions.class).in(Singleton.class);
    }
}
