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
package org.jboss.hal.client.skeleton;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.InstanceInfo;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.config.semver.Versions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CheckForUpdate {

    @FunctionalInterface
    interface VersionCallback {

        void onVersion(Version version);
    }

    private static final String UPDATE_URL = "http://access-halproject.rhcloud.com/latest";
    private static Logger logger = LoggerFactory.getLogger(CheckForUpdate.class);

    private final Environment environment;

    CheckForUpdate(Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    void execute(VersionCallback callback) {
        // only check for community updates
        if (environment.getInstanceInfo() == InstanceInfo.EAP) {
            logger.debug("Version update check skipped for EAP");
        } else {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, UPDATE_URL);
            builder.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(final Request request, final Response response) {
                    try {
                        Version version = Versions.parseVersion(response.getText());
                        callback.onVersion(version);
                    } catch (Throwable t) {
                        logger.warn("Cannot parse version from update server at {}: {}", UPDATE_URL, t.getMessage());
                    }
                }

                @Override
                public void onError(final Request request, final Throwable throwable) {
                    logger.warn("Cannot read version from update server at {}: {}", UPDATE_URL, throwable.getMessage());
                }
            });
            try {
                builder.send();
            } catch (RequestException e) {
                logger.warn("Cannot contact update server at {}: {}", UPDATE_URL, e.getMessage());
            }
        }
    }
}
