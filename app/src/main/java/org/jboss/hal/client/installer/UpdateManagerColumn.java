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
package org.jboss.hal.client.installer;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Column;

import static java.util.Arrays.asList;

@Column(Ids.UPDATE_MANAGER)
public class UpdateManagerColumn extends StaticItemColumn {

    @Inject
    public UpdateManagerColumn(Finder finder, Resources resources) {

        super(finder, Ids.UPDATE_MANAGER, Names.UPDATE_MANAGER, asList(
                new StaticItem.Builder(Names.UPDATES)
                        .nextColumn(Ids.UPDATE_MANAGER_UPDATE)
                        .onPreview(new PreviewContent<>(resources.constants().updateManagerHeading(),
                                resources.previews().updateManagerUpdates()))
                        .build(),
                new StaticItem.Builder(Names.CHANNELS)
                        .nextColumn(Ids.UPDATE_MANAGER_CHANNEL)
                        .onPreview(new PreviewContent<>(resources.constants().channelDetails(),
                                resources.previews().updateManagerChannels()))
                        .build(),
                new StaticItem.Builder(Names.CERTIFICATES)
                        .nextColumn(Ids.UPDATE_MANAGER_CERTIFICATE)
                        .onPreview(new PreviewContent<>(resources.constants().updateCertificates(),
                                resources.previews().updateManagerCertificates()))
                        .build()));
    }
}
