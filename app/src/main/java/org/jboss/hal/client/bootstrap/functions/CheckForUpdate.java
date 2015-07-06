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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.jboss.hal.client.bootstrap.BootstrapContext;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.InstanceInfo;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.config.semver.Versions;
import org.jboss.hal.flow.Control;
import org.jboss.hal.flow.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class CheckForUpdate implements Function<BootstrapContext> {

    public static final String UPDATE_URL = "http://access-halproject.rhcloud.com/latest";
    private static Logger logger = LoggerFactory.getLogger(CheckForUpdate.class);

    private final Environment environment;

    @Inject
    public CheckForUpdate(final Environment environment) {
        this.environment = environment;
    }

    public void execute(final Control<BootstrapContext> control) {
        // only check for community updates
        if (environment.getInstanceInfo() == InstanceInfo.EAP) {
            logger.debug("Bootstrap[CheckForUpdate]: Skip for EAP");
            control.proceed();
        } else {
            logger.debug("Bootstrap[CheckForUpdate]: Start");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, UPDATE_URL);
            builder.setTimeoutMillis(500); // we're in bootstrap and need a response fast!
            builder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(final Request request, final Response response) {
                    try {
                        Version version = Versions.parseVersion(response.getText());
                        environment.setLatestHalVersion(version);
                        logger.debug("Bootstrap[CheckForUpdate]: Version from update url {}", version);
                    } catch (Throwable t) {
                        logger.warn("Cannot parse version from update server at {}: {}", UPDATE_URL, t.getMessage());
                    } finally {
                        control.proceed();
                    }
                }

                @Override
                public void onError(final Request request, final Throwable throwable) {
                    logger.warn("Cannot read version from update server at {}: {}", UPDATE_URL, throwable.getMessage());
                    control.proceed();
                }
            });
            try {
                builder.send();
            } catch (RequestException e) {
                logger.warn("Cannot contact update server at {}: {}", UPDATE_URL, e.getMessage());
                control.proceed();
            }
        }
    }
}
