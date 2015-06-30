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
package org.jboss.hal.client.bootstrap.hal;

import org.jboss.errai.ioc.client.api.InitBallot;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.dmr.model.Operation;
import org.jboss.hal.flow.Async;
import org.jboss.hal.flow.Control;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * TODO Add step to initialize Google Analytics
 * @author Harald Pehl
 */
@ApplicationScoped
public class ErraiBootstrapper {

    @Inject InitBallot<ErraiBootstrapper> ballot;
    @Inject ReadEnvironment readEnvironment;
    @Inject CheckForUpdate checkForUpdate;
    @Inject Event<BootstrapOutcome> event;

    @Inject
    public ErraiBootstrapper(Endpoints endpoints) {
        // simulate management endpoint selection
        endpoints.useBase("http://localhost:9990");
    }

    public void start() {
        Outcome<BootstrapContext> outcome = new Outcome<BootstrapContext>() {
            @Override
            public void onFailure(final BootstrapContext context) {
                ballot.voteForInit();
                event.fire(new BootstrapOutcome(context));
            }

            @Override
            public void onSuccess(final BootstrapContext context) {
                ballot.voteForInit();
                event.fire(new BootstrapOutcome(context));
            }
        };
        BootstrapContext context = new BootstrapContext();
        // order matters!
        new Async<BootstrapContext>(Progress.NOOP).waterfall(context, outcome,
                readEnvironment, checkForUpdate);
    }


    // ------------------------------------------------------ failed / exception callbacks used in bootstrap steps

    public static class BootstrapFailedCallback implements Dispatcher.FailedCallback {

        private final Control<BootstrapContext> control;

        public BootstrapFailedCallback(final Control<BootstrapContext> control) {this.control = control;}

        @Override
        public void onFailed(final Operation operation, final String failure) {
            control.getContext().failed(failure);
            control.abort();
        }
    }


    public static class BootstrapExceptionCallback implements Dispatcher.ExceptionCallback {

        private final Control<BootstrapContext> control;

        public BootstrapExceptionCallback(final Control<BootstrapContext> control) {this.control = control;}

        @Override
        public void onException(final Operation operation, final Throwable exception) {
            control.getContext().failed(exception);
            control.abort();
        }
    }
}
