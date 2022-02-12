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
package org.jboss.hal.client.runtime.group;

import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_DEFAULT_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_PORT_OFFSET;

class ServerGroupPreview extends PreviewContent<ServerGroup> {

    ServerGroupPreview(final ServerGroup serverGroup, Places places) {
        super(serverGroup.getName(), Names.PROFILE + " " + serverGroup.getProfile());

        PlaceRequest profilePlaceRequest = places
                .finderPlace(NameTokens.CONFIGURATION, new FinderPath()
                        .append(Ids.CONFIGURATION, Ids.asId(Names.PROFILES))
                        .append(Ids.PROFILE, serverGroup.getProfile()))
                .build();
        String profileHref = places.historyToken(profilePlaceRequest);

        PlaceRequest sbgPlaceRequest = places
                .finderPlace(NameTokens.CONFIGURATION, new FinderPath()
                        .append(Ids.CONFIGURATION, Ids.asId(Names.SOCKET_BINDINGS))
                        .append(Ids.SOCKET_BINDING_GROUP, serverGroup.get(SOCKET_BINDING_GROUP).asString()))
                .build();
        String sbgHref = places.historyToken(sbgPlaceRequest);

        PreviewAttributes<ServerGroup> attributes = new PreviewAttributes<>(serverGroup)
                .append(PROFILE, profileHref)
                .append(SOCKET_BINDING_GROUP, sbgHref)
                .append(SOCKET_BINDING_PORT_OFFSET)
                .append(SOCKET_BINDING_DEFAULT_INTERFACE);
        previewBuilder().addAll(attributes);
    }
}
