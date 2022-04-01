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

import org.jboss.hal.config.Role;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.resources.Previews;
import org.jboss.hal.resources.Resources;

import com.google.gwt.resources.client.ExternalTextResource;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.span;

class AssignmentPreview extends PreviewContent<Assignment> {

    AssignmentPreview(AccessControlTokens tokens, Role role, Resources resources) {
        // @formatter:off
        super(role.getName(), role.isScoped()
                ? (role.getType() == Role.Type.HOST
                        ? resources.messages().hostScopedRole(role.getBaseRole().getName(),
                                String.join(", ", role.getScope()))
                        : resources.messages().serverGroupScopedRole(role.getBaseRole().getName(),
                                String.join(", ", role.getScope())))
                : null);
        // @formatter:on

        HTMLElement roleDescription = p().element();
        String roleName = role.isScoped() ? role.getBaseRole().getName() : role.getName();
        ExternalTextResource resource = resources.preview("rbac" + roleName);
        Previews.innerHtml(roleDescription, resource);
        previewBuilder().add(roleDescription);

        previewBuilder()
                .add(h(2).textContent(resources.constants().membership()))
                .add(p()
                        .add(span().textContent(resources.constants().membershipOfRole() + " "))
                        .add(a(tokens.role(role)).textContent(role.getName()))
                        .add(span().textContent(".")));
    }
}
