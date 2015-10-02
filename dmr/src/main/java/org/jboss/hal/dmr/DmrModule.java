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
package org.jboss.hal.dmr;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import org.jboss.hal.config.Environment;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.dispatch.DomainProcessStateProcessor;
import org.jboss.hal.dmr.dispatch.ProcessStateProcessor;
import org.jboss.hal.dmr.dispatch.StandaloneProcessStateProcessor;
import org.jboss.hal.spi.GinModule;

/**
 * @author Harald Pehl
 */
@GinModule
public class DmrModule extends AbstractGinModule {

    private final ProcessStateProcessor standalone;
    private final ProcessStateProcessor domain;

    public DmrModule() {
        standalone = new StandaloneProcessStateProcessor();
        domain = new DomainProcessStateProcessor();
    }

    @Override
    protected void configure() {
        bind(Dispatcher.class);
    }

    @Provides
    public ProcessStateProcessor provideProcessStateProcessor(Environment environment) {
        if (environment.isStandalone()) {
            return standalone;
        } else {
            return domain;
        }
    }
}
