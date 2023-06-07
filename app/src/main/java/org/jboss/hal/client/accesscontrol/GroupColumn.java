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
package org.jboss.hal.client.accesscontrol;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.config.User;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.core.accesscontrol.Principal;
import org.jboss.hal.core.finder.ColumnActionFactory;
import org.jboss.hal.core.finder.Finder;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Requires;

import com.google.web.bindery.event.shared.EventBus;

import static org.jboss.hal.client.accesscontrol.AddressTemplates.EXCLUDE_ADDRESS;
import static org.jboss.hal.client.accesscontrol.AddressTemplates.INCLUDE_ADDRESS;

@AsyncColumn(Ids.GROUP)
@Requires({ INCLUDE_ADDRESS, EXCLUDE_ADDRESS })
public class GroupColumn extends PrincipalColumn {

    @Inject
    public GroupColumn(Finder finder,
            ColumnActionFactory columnActionFactory,
            Dispatcher dispatcher,
            EventBus eventBus,
            @Footer Provider<Progress> progress,
            User currentUser,
            AccessControl accessControl,
            AccessControlTokens tokens,
            AccessControlResources accessControlResources,
            Resources resources) {
        super(finder, Ids.GROUP, resources.constants().group(), Principal.Type.GROUP,
                columnActionFactory, dispatcher, eventBus, progress, currentUser, accessControl, tokens,
                accessControlResources, resources);
    }
}
