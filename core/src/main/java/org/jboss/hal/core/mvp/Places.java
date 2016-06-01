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
package org.jboss.hal.core.mvp;

import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.StatementContext;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;

/**
 * Helper class for place management.
 *
 * @author Harald Pehl
 */
public class Places {

    private final Environment environment;
    private final StatementContext statementContext;

    @Inject
    public Places(final Environment environment, final StatementContext statementContext) {
        this.environment = environment;
        this.statementContext = statementContext;
    }

    /**
     * Returns a place request builder which adds a parameter for the selected profile (when running domain mode).
     *
     * @throws IllegalStateException if there's no selected profile and operation mode is domain.
     */
    public PlaceRequest.Builder selectedProfile(final String token) throws IllegalStateException {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(token);
        if (!environment.isStandalone()) {
            if (statementContext.selectedProfile() == null) {
                throw new IllegalStateException("No selected profile");
            }
            builder.with(PROFILE, statementContext.selectedProfile());
        }
        return builder;
    }
}
