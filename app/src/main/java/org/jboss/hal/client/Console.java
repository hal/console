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
package org.jboss.hal.client;

import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.hal.client.bootstrap.hal.BootstrapContext;
import org.jboss.hal.client.bootstrap.hal.BootstrapFailed;
import org.jboss.hal.client.bootstrap.hal.BootstrapOutcome;
import org.jboss.hal.client.bootstrap.hal.ErraiBootstrapper;
import org.jboss.hal.client.bootstrap.hal.LoadingPanel;
import org.jboss.hal.spi.Entrypoint;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
@Entrypoint
public class Console {

    @Inject RootPanel rootPanel;
    @Inject MainLayout mainLayout;
    @Inject ErraiBootstrapper bootstrapper;
    @Inject TransitionTo<BootstrapFailed> bootstrapFailed;

    @PostConstruct
    public void setup() {
        LoadingPanel.get().on();
        bootstrapper.start();
    }

    public void afterBootstrap(@Observes BootstrapOutcome bootstrapOutcome) {
        LoadingPanel.get().off();

        BootstrapContext bootstrapContext = bootstrapOutcome.getBootstrapContext();
        if (bootstrapContext.hasError()) {
            bootstrapFailed.go(bootstrapContext.getErrors());
        } else {
            rootPanel.add(mainLayout.getRoot());
        }
    }
}
