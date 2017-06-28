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
package org.jboss.hal.client.deployment;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.JsHelper;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static elemental2.dom.DomGlobal.document;

/**
 * @author Harald Pehl
 */
class ServerGroupPreview extends PreviewContent<ServerGroup> {

    ServerGroupPreview(final ServerGroup serverGroup, Resources resources) {
        super(serverGroup.getName(), Names.PROFILE + " " + serverGroup.getProfile(),
                resources.previews().deploymentsServerGroup());
        Elements.setVisible((HTMLElement) document.getElementById(Ids.DRAG_AND_DROP_DEPLOYMENT),
                JsHelper.supportsAdvancedUpload());
    }
}
