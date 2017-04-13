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
package org.jboss.hal.client.skeleton;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.semver.Version;
import org.jboss.hal.config.semver.Versions;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CheckForUpdate {

    @FunctionalInterface
    interface VersionCallback {

        void onVersion(Version version);
    }

    private static final String UPDATE_URL = "http://access-halproject.rhcloud.com/latest";
    @NonNls private static Logger logger = LoggerFactory.getLogger(CheckForUpdate.class);

    private final Environment environment;

    CheckForUpdate(Environment environment) {
        this.environment = environment;
    }

    void execute(VersionCallback callback) {
        // only check for community updates
        if (environment.getHalBuild() == Build.PRODUCT) {
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
