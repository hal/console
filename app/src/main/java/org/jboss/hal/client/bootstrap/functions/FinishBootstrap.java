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
package org.jboss.hal.client.bootstrap.functions;

import com.ekuefler.supereventbus.EventBus;
import com.google.gwt.core.client.GWT;
import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class FinishBootstrap implements BootstrapFunction {

    private static final Logger logger = LoggerFactory.getLogger(FinishBootstrap.class);

    private final EventBus eventBus;
    private final Resources resources;

    @Inject
    public FinishBootstrap(EventBus eventBus,
            Resources resources) {
        this.eventBus = eventBus;
        this.resources = resources;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        // reset the uncaught exception handler setup in HalPreBootstrapper
        GWT.setUncaughtExceptionHandler(e -> {
            logger.error("Uncaught exception: {}", e.getMessage());
            eventBus.post(Message.error(resources.constants().unknownError(), e.getMessage()));
        });
        control.proceed();
    }
}
