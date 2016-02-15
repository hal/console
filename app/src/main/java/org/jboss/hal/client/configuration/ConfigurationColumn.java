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
package org.jboss.hal.client.configuration;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
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
@Column(Ids.CONFIGURATION_COLUMN)
public class ConfigurationColumn extends StaticItemColumn {

    @Inject
    public ConfigurationColumn(final Finder finder,
            final Resources resources,
            final PlaceManager placeManager,
            final Environment environment) {

        super(finder, Ids.CONFIGURATION_COLUMN, Names.CONFIGURATION, (context, callback) -> {
            List<StaticItem> items = new ArrayList<>();
            if (environment.isStandalone()) {
                items.add(new StaticItem.Builder(Names.SUBSYSTEMS)
                        .nextColumn(Ids.SUBSYSTEM_COLUMN)
                        .onPreview(new PreviewContent(Names.SUBSYSTEMS, resources.previews().subsystems()))
                        .build());

            } else {
                items.add(new StaticItem.Builder(Names.PROFILES)
                        .nextColumn(Ids.PROFILE_COLUMN)
                        .onPreview(new PreviewContent(Names.PROFILES, resources.previews().profiles()))
                        .build());
            }

            items.addAll(asList(
                    new StaticItem.Builder(Names.INTERFACES)
                            .nextColumn(Ids.INTERFACE_COLUMN)
                            .onPreview(new PreviewContent(Names.INTERFACES, resources.previews().interfaces()))
                            .build(),

                    new StaticItem.Builder(Names.SOCKET_BINDINGS)
                            .nextColumn(Ids.SOCKET_BINDING_COLUMN)
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
