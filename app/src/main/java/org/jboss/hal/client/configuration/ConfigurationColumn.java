/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.ItemActionFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import elemental2.promise.Promise;

import static java.util.Arrays.asList;

@Column(Ids.CONFIGURATION)
public class ConfigurationColumn extends StaticItemColumn {

    @Inject
    public ConfigurationColumn(Finder finder,
            ItemActionFactory itemActionFactory,
            Environment environment,
            Resources resources) {

        super(finder, Ids.CONFIGURATION, Names.CONFIGURATION, (context) -> new Promise<>((resolve, reject) -> {
            List<StaticItem> items = new ArrayList<>();
            if (environment.isStandalone()) {
                items.add(new StaticItem.Builder(Names.SUBSYSTEMS)
                        .nextColumn(Ids.CONFIGURATION_SUBSYSTEM)
                        .onPreview(
                                new PreviewContent<>(Names.SUBSYSTEMS, resources.previews().configurationSubsystems()))
                        .build());

            } else {
                items.add(new StaticItem.Builder(Names.PROFILES)
                        .nextColumn(Ids.PROFILE)
                        .onPreview(new PreviewContent<>(Names.PROFILES, resources.previews().configurationProfiles()))
                        .build());
            }

            items.addAll(asList(
                    new StaticItem.Builder(Names.INTERFACES)
                            .nextColumn(Ids.INTERFACE)
                            .onPreview(new PreviewContent<>(Names.INTERFACES,
                                    resources.previews().configurationInterfaces()))
                            .build(),

                    new StaticItem.Builder(Names.SOCKET_BINDINGS)
                            .nextColumn(Ids.SOCKET_BINDING_GROUP)
                            .onPreview(new PreviewContent<>(Names.SOCKET_BINDINGS,
                                    resources.previews().configurationSocketBindings()))
                            .build(),

                    new StaticItem.Builder(Names.PATHS)
                            .action(itemActionFactory.view(NameTokens.PATH))
                            .onPreview(new PreviewContent<>(Names.PATHS, resources.previews().configurationPaths()))
                            .build(),

                    new StaticItem.Builder(Names.SYSTEM_PROPERTIES)
                            .action(itemActionFactory.view(NameTokens.SYSTEM_PROPERTIES))
                            .onPreview(new PreviewContent<>(Names.SYSTEM_PROPERTIES,
                                    resources.previews().configurationSystemProperties()))
                            .build()));

            resolve.onInvoke(items);
        }));
    }
}
