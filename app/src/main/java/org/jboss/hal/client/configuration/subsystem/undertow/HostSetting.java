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
package org.jboss.hal.client.configuration.subsystem.undertow;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

enum HostSetting {

    ACCESS_LOG(Ids.UNDERTOW_HOST_ACCESS_LOG, Names.ACCESS_LOG, ModelDescriptionConstants.ACCESS_LOG),
    HTTP_INVOKER(Ids.UNDERTOW_HOST_HTTP_INVOKER, Names.HTTP_INVOKER, ModelDescriptionConstants.HTTP_INVOKER),
    SINGLE_SIGN_ON(Ids.UNDERTOW_HOST_SINGLE_SIGN_ON, Names.SINGLE_SIGN_ON, ModelDescriptionConstants.SINGLE_SIGN_ON);

    final String baseId;
    final String type;
    final String resource;

    HostSetting(final String baseId, final String type, final String resource) {
        this.baseId = baseId;
        this.type = type;
        this.resource = resource;
    }

    String templateSuffix() {
        return "setting=" + resource; //NON-NLS
    }

    String path() {
        return "setting/" + resource; //NON-NLS
    }
}
