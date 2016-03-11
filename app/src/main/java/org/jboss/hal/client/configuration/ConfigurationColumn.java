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
package org.jboss.hal.client.configuration;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Harald Pehl
 */
@Column(ModelDescriptionConstants.CONFIGURATION)
public class ConfigurationColumn extends StaticItemColumn {

    @Inject
    public ConfigurationColumn(final Finder finder,
            final Resources resources,
            final PlaceManager placeManager,
            final Environment environment) {

        super(finder, ModelDescriptionConstants.CONFIGURATION, Names.CONFIGURATION, (context, callback) -> {
            List<StaticItem> items = new ArrayList<>();
            if (environment.isStandalone()) {
                items.add(new StaticItem.Builder(Names.SUBSYSTEMS)
                        .nextColumn(ModelDescriptionConstants.SUBSYSTEM)
                        .onPreview(new PreviewContent(Names.SUBSYSTEMS, resources.previews().subsystems()))
                        .build());

            } else {
                items.add(new StaticItem.Builder(Names.PROFILES)
                        .nextColumn(ModelDescriptionConstants.PROFILE)
                        .onPreview(new PreviewContent(Names.PROFILES, resources.previews().profiles()))
                        .build());
            }

            items.addAll(asList(
                    new StaticItem.Builder(Names.INTERFACES)
                            .nextColumn(ModelDescriptionConstants.INTERFACE)
                            .onPreview(new PreviewContent(Names.INTERFACES, resources.previews().interfaces()))
                            .build(),

                    new StaticItem.Builder(Names.SOCKET_BINDINGS)
                            .nextColumn(ModelDescriptionConstants.SOCKET_BINDING)
                            .onPreview(new PreviewContent(Names.SOCKET_BINDINGS, resources.previews().socketBindings()))
                            .build(),

                    new StaticItem.Builder(Names.PATHS)
                            .tokenAction(resources.constants().view(), placeManager, NameTokens.PATH)
                            .onPreview(new PreviewContent(Names.PATHS, resources.previews().paths()))
                            .build(),

                    new StaticItem.Builder(Names.SYSTEM_PROPERTIES)
                            .tokenAction(resources.constants().view(), placeManager, NameTokens.SYSTEM_PROPERTIES)
                            .onPreview(new PreviewContent(Names.SYSTEM_PROPERTIES,
                                    resources.previews().systemProperties()))
                            .build()
            ));

            callback.onSuccess(items);
        });
    }
}
