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
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;
import elemental.client.Browser;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderSegment;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static java.util.Collections.singletonList;
import static org.jboss.hal.core.finder.FinderContext.PATH_PARAM;
import static org.jboss.hal.core.mvp.ApplicationPresenter.EXTERNAL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;

/**
 * Factory methods for place requests.
 *
 * @author Harald Pehl
 */
public class Places {

    private final Environment environment;
    private final StatementContext statementContext;
    private final Finder finder;
    private final TokenFormatter tokenFormatter;

    @Inject
    public Places(final Environment environment,
            final StatementContext statementContext,
            final Finder finder,
            final TokenFormatter tokenFormatter) {
        this.environment = environment;
        this.statementContext = statementContext;
        this.finder = finder;
        this.tokenFormatter = tokenFormatter;
    }

    /**
     * Returns a place request builder for the specified token with parameters for the selected profile
     * (when running domain mode).
     */
    public PlaceRequest.Builder selectedProfile(final String token) throws IllegalStateException {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(token);
        if (!environment.isStandalone()) {
            builder.with(PROFILE, statementContext.selectedProfile());
        }
        return builder;
    }

    /**
     * Returns a place request builder for the specified token with parameters for the selected host and server
     * (when running domain mode).
     */
    public PlaceRequest.Builder selectedServer(final String token) throws IllegalStateException {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(token);
        if (!environment.isStandalone()) {
            if (browseByServerGroups()) {
                builder.with(SERVER_GROUP, statementContext.selectedServerGroup());
            } else {
                builder.with(HOST, statementContext.selectedHost());
            }
            builder.with(SERVER, statementContext.selectedServer());
        }
        return builder;
    }

    /**
     * Returns a new place request with the a new value for the specified parameter.
     */
    public PlaceRequest.Builder replaceParameter(PlaceRequest placeRequest, String parameter, String newValue) {
        PlaceRequest.Builder builder = new PlaceRequest.Builder().nameToken(placeRequest.getNameToken());
        for (String p : placeRequest.getParameterNames()) {
            if (parameter.equals(p)) {
                builder.with(parameter, newValue);
            } else {
                builder.with(p, placeRequest.getParameter(p, ""));
            }
        }
        return builder;
    }

    private boolean browseByServerGroups() {
        if (!finder.getContext().getPath().isEmpty()) {
            FinderSegment firstSegment = finder.getContext().getPath().iterator().next();
            return firstSegment.getItemId().equals(Ids.asId(Names.SERVER_GROUPS));
        }
        return false;
    }

    /**
     * Returns a place request builder for the specified finder path.
     */
    public PlaceRequest.Builder finderPlace(final String token, final FinderPath path) {
        return new PlaceRequest.Builder().nameToken(token).with(PATH_PARAM, path.toString());
    }

    public String historyToken(PlaceRequest placeRequest) {
        String href = Browser.getWindow().getLocation().getHref();
        href = href.substring(0, href.indexOf('#'));
        return href + "#" + tokenFormatter.toHistoryToken(singletonList(placeRequest));
    }

    public boolean isExternal(PlaceRequest placeRequest) {
        return Boolean.parseBoolean(placeRequest.getParameter(EXTERNAL, String.valueOf(false)));
    }

    public PlaceRequest external(PlaceRequest placeRequest) {
        return new PlaceRequest.Builder(placeRequest)
                .with(ApplicationPresenter.EXTERNAL, String.valueOf(true))
                .build();
    }
}
