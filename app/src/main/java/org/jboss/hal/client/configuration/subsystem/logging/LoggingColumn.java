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
package org.jboss.hal.client.configuration.subsystem.logging;

import javax.inject.Inject;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING_PROFILE;

/**
 * @author Harald Pehl
 */
@AsyncColumn(LOGGING)
public class LoggingColumn extends StaticItemColumn {

    @Inject
    public LoggingColumn(final Finder finder,
            final StatementContext statementContext,
            final Dispatcher dispatcher,
            final PlaceManager placeManager,
            final Places places,
            final Resources resources) {

        super(finder, LOGGING, Names.LOGGING, asList(
                new StaticItem.Builder(Names.CONFIGURATION)
                        .placeRequestAction(resources.constants().view(), placeManager,
                                places.selectedProfile(NameTokens.LOGGING_CONFIGURATION).build())
                        .onPreview(new LoggingPreview(statementContext, dispatcher, resources))
                        .build(),
                new StaticItem.Builder(Names.LOGGING_PROFILES)
                        .nextColumn(LOGGING_PROFILE)
                        .onPreview(new PreviewContent(Names.LOGGING_PROFILES, resources.previews().loggingProfiles()))
                        .build()
        ));
    }
}
