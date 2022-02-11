/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.security;

import java.util.Collections;

import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_TYPE;

class SecurityDomainPreview extends PreviewContent<SecurityDomain> {

    SecurityDomainPreview(final SecurityDomain securityDomain) {
        super(securityDomain.getName());

        PreviewAttributes<SecurityDomain> attributes = new PreviewAttributes<>(securityDomain,
                Collections.singletonList(CACHE_TYPE));
        previewBuilder().addAll(attributes);
    }
}