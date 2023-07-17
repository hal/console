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

import org.jboss.hal.ballroom.Alert;
import org.jboss.hal.core.accesscontrol.AccessControl;
import org.jboss.hal.resources.Icons;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.window;
import static org.jboss.elemento.Elements.setVisible;

class AccessControlWarnings {

    final Alert providerWarning;
    final Alert ssoWarning;

    AccessControlWarnings(AccessControl accessControl, Resources resources) {
        this.providerWarning = new Alert(Icons.WARNING, resources.messages().simpleProviderWarning(),
                resources.constants().enableRbac(),
                event -> accessControl.switchProvider());

        this.ssoWarning = new Alert(Icons.WARNING, resources.messages().ssoAccessControlWarning(),
                Names.KEYCLOAK,
                event -> window.open(accessControl.ssoUrl(), "_blank"));

        setVisible(providerWarning.element(), accessControl.simpleProvider());
        setVisible(ssoWarning.element(), accessControl.singleSignOn());
    }
}
