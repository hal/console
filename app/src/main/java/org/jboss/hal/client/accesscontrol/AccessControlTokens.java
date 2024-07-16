/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.accesscontrol;

import javax.inject.Inject;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import org.jboss.hal.config.Role;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.Ids;

public class AccessControlTokens {

    private final Places places;

    @Inject
    public AccessControlTokens(final Places places) {
        this.places = places;
    }

    public String principal(Principal principal) {
        return token(principalPath(principal));
    }

    public String role(Role role) {
        return token(rolePath(role));
    }

    private FinderPath principalPath(Principal principal) {
        String browseByItemId;
        String principalColumnId;
        if (principal.getType() == Principal.Type.USER) {
            browseByItemId = Ids.ACCESS_CONTROL_BROWSE_BY_USERS;
            principalColumnId = Ids.USER;
        } else {
            browseByItemId = Ids.ACCESS_CONTROL_BROWSE_BY_GROUPS;
            principalColumnId = Ids.GROUP;
        }

        return getPath(browseByItemId, principalColumnId, principal.getId());
    }

    private FinderPath rolePath(Role role) {
        return getPath(Ids.ACCESS_CONTROL_BROWSE_BY_ROLES, Ids.ROLE, role.getId());
    }

    private String token(FinderPath path) {
        PlaceRequest placeRequest = places.finderPlace(NameTokens.ACCESS_CONTROL, path).build();
        return places.historyToken(placeRequest);
    }

    private FinderPath getPath(String browseById, String typeId, String name) {
        return new FinderPath()
                .append(Ids.ACCESS_CONTROL_BROWSE_BY, browseById)
                .append(typeId, name);
    }
}
