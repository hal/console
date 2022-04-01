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
package org.jboss.hal.client.runtime.subsystem.messaging;

import javax.inject.Inject;

import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.finder.StaticItem;
import org.jboss.hal.core.finder.StaticItemColumn;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;

import elemental2.promise.Promise;

import static java.util.Arrays.asList;

@AsyncColumn(Ids.MESSAGING_CATEGORY_RUNTIME)
public class MessagingCategoryColumn extends StaticItemColumn {

    @Inject
    public MessagingCategoryColumn(Finder finder,
            Resources resources) {

        super(finder, Ids.MESSAGING_CATEGORY_RUNTIME, resources.constants().category(),
                context -> Promise.resolve(asList(
                        new StaticItem.Builder(Names.SERVER)
                                .id(Ids.build(Names.SERVER, Ids.ITEM, "runtime"))
                                .nextColumn(Ids.MESSAGING_SERVER_RUNTIME)
                                .onPreview(new PreviewContent<>(Names.SERVER,
                                        resources.previews().runtimeMessagingServer()))
                                .build(),
                        new StaticItem.Builder(Names.JMS_BRIDGE)
                                .id(Ids.build(Names.JMS_BRIDGE, Ids.ITEM, "runtime"))
                                .nextColumn(Ids.JMS_BRIDGE_RUNTIME)
                                .onPreview(new PreviewContent<>(Names.JMS_BRIDGE,
                                        resources.previews().runtimeMessagingJmsBridge()))
                                .build())));
    }
}
